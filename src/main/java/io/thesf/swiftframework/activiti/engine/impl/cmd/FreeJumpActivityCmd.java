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

import io.thesf.swiftframework.activiti.engine.impl.bpmn.behavior.PassThroughParallelGatewayActivityBehavior;
import io.thesf.swiftframework.activiti.engine.impl.history.BPMNProcessActivityRecordPayload;
import io.thesf.swiftframework.activiti.engine.impl.history.BPMNProcessActivityRecorder;
import io.thesf.swiftframework.activiti.engine.impl.interceptor.SpringCommand;
import io.thesf.swiftframework.activiti.engine.impl.interceptor.SpringCommandContext;
import org.activiti.bpmn.model.FlowElement;
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
 * Command for {@literal Free jump activity}.
 *
 * 1. Jump is based on process instance.
 * 2. Each jump will delete all activities and create new activities.
 * 3. Each jump allows multiple target nodes to be specified.
 * 4. The specified target node of each jump is allowed to be of various types.
 * For example, jump to two nodes at a time, one of which is {@link org.activiti.bpmn.model.UserTask},
 * and the other is {@link org.activiti.bpmn.model.Gateway}.
 *
 * Warning:
 * 1. It is not recommended to jump directly to the branch of parallel gateway.
 * If do it, make sure to jump to each branch of each parallel gateway at the same time.
 * Otherwise, it will be {@literal stuck} in one parallel gateway.
 * 2. It is not recommended to jump to the gateway {@literal passing through immediately},
 * which will cause the current task cannot be rolled back.
 *
 * @see io.thesf.swiftframework.activiti.engine.impl.history.recorder.BPMNProcessFreeJumpRecorder
 * @author VirtualCry
 */
public class FreeJumpActivityCmd extends SpringCommand<Void> {

    private final String                processInstanceId;
    private final List<String>          targetActivityIds;
    private final Map<String, Object>   targetActivityVariables;
    private final String                jumpReason;


    public FreeJumpActivityCmd(String processInstanceId, String[] targetActivityIds, Map<String, Object> targetActivityVariables, String jumpReason) {
        this.processInstanceId = processInstanceId;
        this.targetActivityIds = Arrays.stream(targetActivityIds).collect(Collectors.toList());
        this.targetActivityVariables = Optional.ofNullable(targetActivityVariables).orElse(Collections.emptyMap());
        this.jumpReason = StringUtils.isEmpty(jumpReason) ? "FREE_JUMP" : jumpReason;
    }

    public FreeJumpActivityCmd(String processInstanceId, String[] targetActivityIds, Map<String, Object> targetActivityVariables) {
        this(processInstanceId, targetActivityIds, targetActivityVariables, null);
    }

    public FreeJumpActivityCmd(String processInstanceId, String[] targetActivityIds) {
        this(processInstanceId, targetActivityIds, null);
    }


    @Override
    public Void execute(SpringCommandContext commandContext) {

        // Get manager for `BPMNProcessChain`
        BPMNProcessActivityRecorder bpmnProcessActivityRecorder = commandContext.getBean(BPMNProcessActivityRecorder.class);
        // Get manager for `ExecutionEntity`
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        // Get manager for `TaskEntity`
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        // Get manager for history
        HistoryManager historyManager = commandContext.getHistoryManager();

        // Get root execution
        ExecutionEntity rootExecution = executionEntityManager.findByRootProcessInstanceId(processInstanceId);
        // Get bpmn process
        Process bpmnProcess = ProcessDefinitionUtil.getProcess(rootExecution.getProcessDefinitionId());
        // Get todoTasks
        List<TaskEntity> todoTaskEntities = taskEntityManager.findTasksByProcessInstanceId(processInstanceId);
        // Get todoTask elements.
        List<FlowElement> sourceElements = todoTaskEntities.stream()
                .map(todoTaskEntity -> bpmnProcess.getFlowElement(todoTaskEntity.getTaskDefinitionKey()))
                .collect(Collectors.toList());
        // Get target elements.
        List<FlowElement> targetElements = targetActivityIds.stream()
                .map(targetNodeId ->
                        Optional.ofNullable(bpmnProcess.getFlowElement(targetNodeId))
                                .orElseThrow(() -> new RuntimeException("Could not find nodeId [" + targetNodeId + "] "
                                        + "from processDefinitionId [" + rootExecution.getProcessDefinitionId() + "]."))
                )
                .map(targetElement -> {
                    if (targetElement instanceof ParallelGateway) { // if it parallel gateway, change it behavior.
                        ParallelGateway passThroughParallelGateway = (ParallelGateway) targetElement.clone();
                        BeanUtils.copyProperties(targetElement, passThroughParallelGateway);
                        passThroughParallelGateway.setBehavior(new PassThroughParallelGatewayActivityBehavior());
                        return passThroughParallelGateway;
                    } else
                        return targetElement;
                })
                .collect(Collectors.toList());

        // 1. Record task as ended
        todoTaskEntities.forEach(task -> historyManager.recordTaskEnd(task.getId(), jumpReason));

        // 2. Delete all child executions and related data (execution, task, task variable and so on)
        executionEntityManager.deleteChildExecutions(rootExecution, jumpReason);

        // 3. Create child execution for each target element
        targetElements.forEach(targetElement -> {

            // Create a child execution for the next element
            ExecutionEntity nextTaskExecution = executionEntityManager.createChildExecution(rootExecution);
            // Set target element.
            nextTaskExecution.setCurrentFlowElement(targetElement);
            // Set transient variable.
            if (!targetActivityVariables.isEmpty())
                nextTaskExecution.setTransientVariablesLocal(targetActivityVariables);

            // Push `ContinueProcessOperation` into operation stack.
            commandContext.getAgenda().planContinueProcessOperation(nextTaskExecution);

        });

        // 4. Record operation - `Free Jump`
        BPMNProcessActivityRecordPayload recordPayload = BPMNProcessActivityRecordPayload
                .freeJumpRecord()
                .executeAfterCommit(true)
                .sourceActivities(sourceElements, rootExecution.getProcessDefinitionId(), rootExecution.getProcessInstanceId())
                .targetActivities(targetElements, rootExecution.getProcessDefinitionId(), rootExecution.getProcessInstanceId())
                .build();
        bpmnProcessActivityRecorder.execute(recordPayload);

        return null;
    }
}
