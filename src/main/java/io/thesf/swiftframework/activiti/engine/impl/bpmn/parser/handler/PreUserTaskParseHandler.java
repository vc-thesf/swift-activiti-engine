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
package io.thesf.swiftframework.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static io.thesf.swiftframework.activiti.api.runtime.model.ExecutionVariables.SYSTEM_ASSIGN;
import static io.thesf.swiftframework.activiti.api.runtime.model.ExecutionVariables.SYSTEM_JOINTLY_ASSIGN;

/**
 * Handle before parsing {@link UserTask}.
 *
 * 1. Set assignee variable name.
 *
 * @author VirtualCry
 */
public class PreUserTaskParseHandler extends AbstractBpmnParseHandler<UserTask> {

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return UserTask.class;
    }

    @Override
    protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {

        // if the approver is not set, set the specified approver variable name.
        if (StringUtils.isEmpty(userTask.getAssignee())) {
            userTask.setAssignee("${" + SYSTEM_ASSIGN + "}");
        }

        // if the node is multi instance and variable name is 'SYSTEM_JOINTLY_ASSIGN', set a unified approver variable name.
        Optional.ofNullable(userTask.getLoopCharacteristics())
                .ifPresent(multiInstanceLoopCharacteristics -> {
                    if (SYSTEM_JOINTLY_ASSIGN.equals(multiInstanceLoopCharacteristics.getInputDataItem())) {
                        multiInstanceLoopCharacteristics.setElementVariable(SYSTEM_ASSIGN);
                        userTask.setAssignee("${" + SYSTEM_ASSIGN + "}");
                    }
                });

    }
}
