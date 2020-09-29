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

import io.thesf.swiftframework.activiti.engine.impl.cmd.FindUserTaskAssigneeCmd;
import io.thesf.swiftframework.activiti.engine.impl.interceptor.SpringCommandContext;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ManagementService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.springframework.util.StringUtils;

import static io.thesf.swiftframework.activiti.api.runtime.model.ExecutionVariables.SYSTEM_ASSIGN;

/**
 * Extension for {@link UserTaskActivityBehavior}.
 *
 * 1. Dynamic setting of approvers.
 *
 * @see io.thesf.swiftframework.activiti.engine.impl.bpmn.parser.handler.PreUserTaskParseHandler
 * @author VirtualCry
 */
public class EnhanceUserTaskActivityBehavior extends UserTaskActivityBehavior {

    public EnhanceUserTaskActivityBehavior(UserTask userTask) {
        super(userTask);
    }

    @Override
    public void execute(DelegateExecution execution) {

        // Set assignee
        this.setAssignee(execution);

        // Execute
        super.execute(execution);

        // Reset transient variables.
        execution.removeTransientVariablesLocal();
    }

    protected void setAssignee(DelegateExecution execution) {

        if (!StringUtils.isEmpty(execution.getVariableLocal(SYSTEM_ASSIGN)))
            return;

        // Get service
        ManagementService managementService = ((SpringCommandContext) Context.getCommandContext()).getBean(ManagementService.class);

        // Set assignees
        managementService.executeCommand(new FindUserTaskAssigneeCmd(execution.getProcessInstanceId(), userTask))
                .ifPresent(taskAssigneeDefinition ->
                        execution.setVariableLocal(SYSTEM_ASSIGN, taskAssigneeDefinition.getAssignee())
                );
    }
}
