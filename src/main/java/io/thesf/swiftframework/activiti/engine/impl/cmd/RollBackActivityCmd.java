/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.thesf.swiftframework.activiti.engine.impl.cmd;

import io.thesf.swiftframework.activiti.api.runtime.model.BPMNActivityChain;
import io.thesf.swiftframework.activiti.api.runtime.model.BPMNProcessChain;
import io.thesf.swiftframework.activiti.cache.data.BPMNProcessChainCacheManager;
import io.thesf.swiftframework.activiti.engine.impl.bpmn.behavior.PassThroughParallelGatewayActivityBehavior;
import io.thesf.swiftframework.activiti.engine.impl.history.BPMNProcessActivityRecordPayload;
import io.thesf.swiftframework.activiti.engine.impl.history.BPMNProcessActivityRecorder;
import io.thesf.swiftframework.activiti.engine.impl.interceptor.SpringCommand;
import io.thesf.swiftframework.activiti.engine.impl.interceptor.SpringCommandContext;
import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.BPMNElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command for {@literal Roll back activity}.
 *
 * 1. Rollback is for {@link org.activiti.bpmn.model.Task}.
 * 2. Rollback is based on the `execution record` rather than the definition of `bpmn flowchart`
 * For example, There's a definition of continuous task nodes  `A → B → C → D`. When B is executed, jump to D freely
 * and perform rollback operation on D. The current node after rollback is `B` rather than `C`.
 * 3. Support rollback of any operation.
 *
 * Warning:
 * 1. If the target node of free jump is a gateway that is executed immediately and the next node of the current node
 * is not the target node, it will not be able to turn back to the previous task node.
 * For example, `TaskA → TaskB → GatewayG → TaskC → TaskD`. When TaskA is executed, jump to `GatewayG` freely,
 * then through the `GatewayG` to `TaskC` immediately. When performs rollback, it will turn back to the previous operation - `Free jump to GatewayG`.
 * When turn back to `GatewayG`,  `GatewayG` is passed immediately and it arrives at `TaskC`.
 * As a whole, after TaskC performs a rollback operation, it will recreate a new TaskC. Turn back failed.
 *
 * @see io.thesf.swiftframework.activiti.engine.impl.history.recorder.BPMNProcessRollBackRecorder
 * @author VirtualCry
 */
public class RollBackActivityCmd extends SpringCommand<Void> {

    private final String                taskId;
    private final Map<String, Object>   targetActivityVariables;
    private final String                rollBackReason;


    public RollBackActivityCmd(String taskId, Map<String, Object> targetActivityVariables, String rollBackReason) {
        this.taskId = taskId;
        this.targetActivityVariables = Optional.ofNullable(targetActivityVariables).orElse(Collections.emptyMap());
        this.rollBackReason = StringUtils.isEmpty(rollBackReason) ? "ROLL_BACK" : rollBackReason;
    }

    public RollBackActivityCmd(String taskId, Map<String, Object> targetActivityVariables) {
        this(taskId, targetActivityVariables, null);
    }

    public RollBackActivityCmd(String taskId) {
        this(taskId, null);
    }


    @Override
    public Void execute(SpringCommandContext commandContext) {

        // manager for `BPMNProcessChain`
        BPMNProcessChainCacheManager bpmnProcessChainCacheManager = commandContext.getBean(BPMNProcessChainCacheManager.class);
        // recorder for `BPMNProcessChain`
        BPMNProcessActivityRecorder bpmnProcessActivityRecorder = commandContext.getBean(BPMNProcessActivityRecorder.class);
        // manager for `ExecutionEntity`
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        // manager for `TaskEntity`
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        // manager for history
        HistoryManager historyManager = commandContext.getHistoryManager();

        // Get current task info
        TaskEntity task = Optional.ofNullable(taskEntityManager.findById(taskId))
                .orElseThrow(() -> new RuntimeException("Could not find task with id " + taskId));
        // Get bpmn process
        Process bpmnProcess = ProcessDefinitionUtil.getProcess(task.getProcessDefinitionId());
        // Get root execution
        ExecutionEntity rootExecution = task.getProcessInstance();
        // Get process chain.
        BPMNProcessChain processChain = bpmnProcessChainCacheManager.get(task.getProcessInstanceId());

        // Get activity chain that ready to jump
        Set<BPMNActivityChain> prepareJumpActivityChains = processChain.getRunningActivityChains().stream()
                .filter(activityChain -> activityChain.getLastActivity().getElementId().equals(task.getTaskDefinitionKey()))
                .peek(activityChain -> Optional
                        .ofNullable(activityChain.getSecondLastTask())
                        .orElseThrow(() -> new RuntimeException("Could not find last task from task " + activityChain.getLastActivity().getElementId())))
                .collect(Collectors.toSet());

        // Get target tasks
        Set<BPMNActivity> targetTasks = prepareJumpActivityChains.stream()
                .map(BPMNActivityChain::getSecondLastTask)
                .collect(Collectors.toSet());

        //Get the activity chains with the same execution path
        Set<BPMNActivityChain> commonActivityChains = prepareJumpActivityChains.stream()
                .map(activityChain -> activityChain.subActivityChain(0, activityChain.getSecondLastTaskIndex()))
                .collect(Collectors.toSet());

        // Get the activity chains that need to be rolled back
        Set<BPMNActivityChain> turnBackActivityChains = processChain.getRunningActivityChains().stream()
                .filter(activityChain -> commonActivityChains.stream()
                        .anyMatch(commonActivityChain -> commonActivityChain.equals(activityChain.subActivityChain(0, commonActivityChain.size() - 1))))
                .collect(Collectors.toSet());

        // Get the activities that need to be rolled back
        Set<BPMNActivity> turnBackActivities = turnBackActivityChains.stream()
                .map(BPMNActivityChain::getLastActivity)
                .collect(Collectors.toSet());

        // Get the activity executions that need be rolled back
        List<ExecutionEntity> activityExecutions = executionEntityManager.findExecutionsByParentExecutionAndActivityIds(
                rootExecution.getId(),
                turnBackActivities.stream().map(BPMNElement::getElementId).collect(Collectors.toSet())
        );

        // Get the historic activity chains that need to be reran
        Set<BPMNActivityChain> recoveryActivityChains = processChain.getHistoricActivityChains().stream()
                .filter(activityChain -> commonActivityChains.stream()
                        .anyMatch(commonActivityChain -> commonActivityChain.equals(activityChain)))
                .collect(Collectors.toSet());


        // 1. Record task as ended &  Delete task's executions that need to be rolled back and related data (execution, task, task variable and so on)
        activityExecutions.forEach(activityExecution -> {

            // Record task as ended
            activityExecution.getTasks().forEach(executionTask -> historyManager.recordTaskEnd(executionTask.getId(), rollBackReason));
            // Delete task's executions that need to be rolled back and related data (execution, task, task variable and so on)
            executionEntityManager.deleteChildExecutions(activityExecution, rollBackReason);
            executionEntityManager.deleteExecutionAndRelatedData(activityExecution, rollBackReason);

        });

        // 2. Create child execution for each target task
        targetTasks.stream()
                .map(targetTask ->
                        Optional.ofNullable(bpmnProcess.getFlowElement(targetTask.getElementId()))
                                .orElseThrow(() -> new RuntimeException("Could not find nodeId [" + targetTask.getElementId() + "] "
                                        + "from processDefinitionId [" + task.getProcessDefinitionId() + "]."))
                )
                .map(targetElement -> {
                    if (targetElement instanceof ParallelGateway) { // 若是并行网关，更改网关行为，允许通过网关
                        ParallelGateway passThroughParallelGateway = (ParallelGateway) targetElement.clone();
                        BeanUtils.copyProperties(targetElement, passThroughParallelGateway);
                        passThroughParallelGateway.setBehavior(new PassThroughParallelGatewayActivityBehavior());
                        return passThroughParallelGateway;
                    } else
                        return targetElement;
                })
                .forEach(targetElement -> {

                    // Create a child execution for the next task
                    ExecutionEntity nextTaskExecution = executionEntityManager.createChildExecution(rootExecution);
                    // Set target element.
                    nextTaskExecution.setCurrentFlowElement(targetElement);
                    // Set transient variable.
                    if (!targetActivityVariables.isEmpty())
                        nextTaskExecution.setTransientVariablesLocal(targetActivityVariables);

                    // Push `ContinueProcessOperation` into operation stack.
                    commandContext.getAgenda().planContinueProcessOperation(nextTaskExecution);

                });

        // 3. Record in historic chains - `Roll Back`
        BPMNProcessActivityRecordPayload recordPayload = BPMNProcessActivityRecordPayload.rollBackRecord()
                .executeAfterCommit(true)
                .processInstanceId(rootExecution.getProcessInstanceId())
                .turnBackActivityChains(turnBackActivityChains)
                .recoveryActivityChains(recoveryActivityChains)
                .build();
        bpmnProcessActivityRecorder.execute(recordPayload);

        return null;
    }
}
