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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.thesf.swiftframework.activiti.engine.impl.cmd.FindUserTaskAssigneeCmd;
import io.thesf.swiftframework.activiti.engine.impl.interceptor.SpringCommandContext;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ManagementService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.context.Context;
import org.springframework.util.StringUtils;

import java.util.Collection;

import static io.thesf.swiftframework.activiti.api.runtime.model.ExecutionVariables.SYSTEM_JOINTLY_ASSIGN;

/**
 * Extension for {@link ParallelMultiInstanceBehavior}.
 *
 * 1. fix bug - The variable value type queried from the database is not {@link Collection} but {@link ArrayNode}.
 * 2. Dynamic setting of approvers.
 *
 * @see io.thesf.swiftframework.activiti.engine.impl.bpmn.parser.handler.PreUserTaskParseHandler
 * @author VirtualCry
 */
public class EnhanceParallelMultiInstanceBehavior extends ParallelMultiInstanceBehavior {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public EnhanceParallelMultiInstanceBehavior(Activity activity, AbstractBpmnActivityBehavior originalActivityBehavior) {
        super(activity, originalActivityBehavior);
    }


    @Override
    public void execute(DelegateExecution execution) {

        // Set assignees
        this.setJointlySignAssignees(execution);

        // Execute
        super.execute(execution);
    }

    protected void setJointlySignAssignees(DelegateExecution execution) {

        if (!(activity instanceof UserTask)
                &&  !StringUtils.isEmpty(execution.getVariableLocal(SYSTEM_JOINTLY_ASSIGN)))
            return;

        // Get service
        ManagementService managementService = ((SpringCommandContext) Context.getCommandContext()).getBean(ManagementService.class);

        // Set assignees
        managementService.executeCommand(new FindUserTaskAssigneeCmd(execution.getProcessInstanceId(), (UserTask) activity))
                .ifPresent(taskAssigneeDefinition ->
                        execution.setVariableLocal(SYSTEM_JOINTLY_ASSIGN, taskAssigneeDefinition.getAssignee())
                );
    }

    @Override
    protected Object resolveCollection(DelegateExecution execution) {
        Object collection = null;
        if (collectionExpression != null) {
            collection = collectionExpression.getValue(execution);

        } else if (collectionVariable != null) {
            collection = execution.getVariable(collectionVariable);
        }

        // Extend
        if (collection instanceof ArrayNode) {
            collection = objectMapper.convertValue(collection, Collection.class);
        }

        return collection;
    }
}
