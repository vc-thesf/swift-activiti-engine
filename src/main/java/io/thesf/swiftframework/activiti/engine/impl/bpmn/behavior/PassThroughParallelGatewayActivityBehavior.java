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
package io.thesf.swiftframework.activiti.engine.impl.bpmn.behavior;

import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;

import java.util.Collection;

/**
 * Parallel gateway without verification and passing automatically.
 *
 * @see ParallelGatewayActivityBehavior#execute(DelegateExecution)
 * @author VirtualCry
 */
@Slf4j
public class PassThroughParallelGatewayActivityBehavior extends ParallelGatewayActivityBehavior {

    @Override
    public void execute(DelegateExecution execution) {

        // First off all, deactivate the execution
        execution.inactivate();

        // Join
        FlowElement flowElement = execution.getCurrentFlowElement();
        ParallelGateway parallelGateway;
        if (flowElement instanceof ParallelGateway) {
            parallelGateway = (ParallelGateway) flowElement;
        } else {
            throw new ActivitiException("Programmatic error: parallel gateway behaviour can only be applied" + " to a ParallelGateway instance, but got an instance of " + flowElement);
        }

        lockFirstParentScope(execution);

        DelegateExecution multiInstanceExecution = null;
        if (hasMultiInstanceParent(parallelGateway)) {
            multiInstanceExecution = findMultiInstanceParentExecution(execution);
        }

        ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
        Collection<ExecutionEntity> joinedExecutions = executionEntityManager.findInactiveExecutionsByActivityIdAndProcessInstanceId(execution.getCurrentActivityId(), execution.getProcessInstanceId());
        if (multiInstanceExecution != null) {
            joinedExecutions = cleanJoinedExecutions(joinedExecutions, multiInstanceExecution);
        }

        int nbrOfExecutionsToJoin = parallelGateway.getIncomingFlows().size();
        int nbrOfExecutionsCurrentlyJoined = joinedExecutions.size();

        // Fork

        // Is needed to set the endTime for all historic activity joins
        Context.getCommandContext().getHistoryManager().recordActivityEnd((ExecutionEntity) execution, null);

        // pass through control.
//        boolean isPassThrough = nbrOfExecutionsCurrentlyJoined == nbrOfExecutionsToJoin;
        boolean isPassThrough = true;

        if (isPassThrough) {

            // Fork
            if (log.isDebugEnabled()) {
                log.debug("parallel gateway '{}' activates: {} of {} joined", execution.getCurrentActivityId(), nbrOfExecutionsCurrentlyJoined, nbrOfExecutionsToJoin);
            }

            if (parallelGateway.getIncomingFlows().size() > 1) {

                // All (now inactive) children are deleted.
                for (ExecutionEntity joinedExecution : joinedExecutions) {

                    // The current execution will be reused and not deleted
                    if (!joinedExecution.getId().equals(execution.getId())) {
                        executionEntityManager.deleteExecutionAndRelatedData(joinedExecution, null);
                    }

                }
            }

            // TODO: potential optimization here: reuse more then 1 execution, only 1 currently
            Context.getAgenda().planTakeOutgoingSequenceFlowsOperation((ExecutionEntity) execution, false); // false -> ignoring conditions on parallel gw

        } else if (log.isDebugEnabled()) {
            log.debug("parallel gateway '{}' does not activate: {} of {} joined", execution.getCurrentActivityId(), nbrOfExecutionsCurrentlyJoined, nbrOfExecutionsToJoin);
        }

    }
}
