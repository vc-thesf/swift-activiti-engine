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

import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.impl.util.condition.ConditionUtil;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Command for {@literal Calculate outgoing flows}.
 *
 * @author VirtualCry
 */
public class CalculateOutgoingFlowsCmd implements Command<List<SequenceFlow>> {

    private final String                processInstanceId;
    private final String                elementId;

    public CalculateOutgoingFlowsCmd(String processInstanceId, String elementId) {
        this.processInstanceId = processInstanceId;
        this.elementId = elementId;
    }


    @Override
    public List<SequenceFlow> execute(CommandContext commandContext) {

        // Get manager for `ExecutionEntity`
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

        // Get root execution
        ExecutionEntity rootExecution = executionEntityManager.findByRootProcessInstanceId(processInstanceId);
        // Root execution is null → mean that process has been ended.
        if (rootExecution == null)
            return Collections.emptyList();

        // Get bpmn process
        Process bpmnProcess = ProcessDefinitionUtil.getProcess(rootExecution.getProcessDefinitionId());
        // Get flow node.
        FlowNode flowNode = (FlowNode) bpmnProcess.getFlowElement(elementId);

        if (flowNode instanceof ExclusiveGateway)
            return Collections.singletonList(this.determineOutgoingFlow((ExclusiveGateway) flowNode, rootExecution));
        else if (flowNode instanceof InclusiveGateway)
            return this.determineOutgoingFlows((InclusiveGateway) flowNode, rootExecution);
        else if (flowNode instanceof ParallelGateway)
            return this.determineOutgoingFlows((ParallelGateway) flowNode);
        else
            return flowNode.getOutgoingFlows();
    }


    /**
     * Calculate outgoing flow for {@link ExclusiveGateway}.
     *
     * @param exclusiveGateway  exclusiveGateway
     * @param execution         execution
     * @return One {@link SequenceFlow}.
     */
    protected SequenceFlow determineOutgoingFlow(ExclusiveGateway exclusiveGateway, ExecutionEntity execution) {
        // default sequence id.
        String defaultSequenceFlowId = exclusiveGateway.getDefaultFlow();

        // default sequence.
        Supplier<SequenceFlow> defaultSequenceFlow = () -> exclusiveGateway.getOutgoingFlows().stream()
                .filter(sequenceFlow -> defaultSequenceFlowId != null && defaultSequenceFlowId.equals(sequenceFlow.getId()))
                .findAny()
                .orElseThrow(() -> new ActivitiException("No outgoing sequence flow of the exclusive gateway '"
                        + exclusiveGateway.getId() + "' could be selected for continuing the process"));

        // determine sequence flow to take
        return exclusiveGateway.getOutgoingFlows().stream()
                .filter(sequenceFlow -> hasTrueCondition(sequenceFlow, defaultSequenceFlowId, execution))
                .findAny()
                .orElseGet(defaultSequenceFlow);
    }

    /**
     * Calculate outgoing flow for {@link InclusiveGateway}.
     *
     * @param inclusiveGateway  inclusiveGateway
     * @param execution         execution
     * @return The {@link List<SequenceFlow>}.
     */
    protected List<SequenceFlow> determineOutgoingFlows(InclusiveGateway inclusiveGateway, ExecutionEntity execution) {
        // default sequence id.
        String defaultSequenceFlowId = inclusiveGateway.getDefaultFlow();

        // determine sequence flow to take
        List<SequenceFlow> outgoingFlows = inclusiveGateway.getOutgoingFlows().stream()
                .filter(sequenceFlow -> hasTrueCondition(sequenceFlow, defaultSequenceFlowId, execution))
                .collect(Collectors.toList());

        if (outgoingFlows.isEmpty()) {
            // default sequence.
            List<SequenceFlow> defaultSequenceFlows = inclusiveGateway.getOutgoingFlows().stream()
                    .filter(sequenceFlow -> defaultSequenceFlowId != null && defaultSequenceFlowId.equals(sequenceFlow.getId()))
                    .collect(Collectors.toList());
            if (defaultSequenceFlows.isEmpty())
                throw new ActivitiException("No outgoing sequence flow of the exclusive gateway '"
                        + inclusiveGateway.getId() + "' could be selected for continuing the process");
            else
                return defaultSequenceFlows;
        }
        else
            return outgoingFlows;
    }

    /**
     * Calculate outgoing flow for {@link ParallelGateway}.
     *
     * @param parallelGateway   parallelGateway
     * @return The {@link List<SequenceFlow>}.
     */
    protected List<SequenceFlow> determineOutgoingFlows(ParallelGateway parallelGateway) {
        return parallelGateway.getOutgoingFlows();
    }

    /**
     * Calculate condition.
     *
     * @param sequenceFlow              sequenceFlow
     * @param defaultSequenceFlowId     defaultSequenceFlowId
     * @param execution                 execution
     * @return The {@link Boolean}.
     */
    protected boolean hasTrueCondition(SequenceFlow sequenceFlow, String defaultSequenceFlowId, ExecutionEntity execution) {
        String skipExpressionString = sequenceFlow.getSkipExpression();
        if (!SkipExpressionUtil.isSkipExpressionEnabled(execution, skipExpressionString)) {
            return ConditionUtil.hasTrueCondition(sequenceFlow, execution)
                    && (defaultSequenceFlowId == null || !defaultSequenceFlowId.equals(sequenceFlow.getId()));
        }
        else
            return SkipExpressionUtil.shouldSkipFlowElement(Context.getCommandContext(), execution, skipExpressionString);
    }
}
