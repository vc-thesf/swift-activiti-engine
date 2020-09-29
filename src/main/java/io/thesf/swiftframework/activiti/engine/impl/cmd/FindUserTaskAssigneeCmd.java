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

import io.thesf.swiftframework.activiti.api.runtime.model.TaskAssigneeDefinition;
import io.thesf.swiftframework.activiti.cache.data.TaskAssigneeDefinitionCacheManager;
import io.thesf.swiftframework.activiti.engine.impl.interceptor.SpringCommand;
import io.thesf.swiftframework.activiti.engine.impl.interceptor.SpringCommandContext;
import org.activiti.bpmn.model.UserTask;

import java.util.Optional;

/**
 * Command for {@literal Find assignee}.
 *
 * @author VirtualCry
 */
public class FindUserTaskAssigneeCmd extends SpringCommand<Optional<TaskAssigneeDefinition>> {

    private final String                processInstanceId;
    private final UserTask              userTask;

    public FindUserTaskAssigneeCmd(String processInstanceId, UserTask userTask) {
        this.processInstanceId = processInstanceId;
        this.userTask = userTask;
    }


    @Override
    public Optional<TaskAssigneeDefinition> execute(SpringCommandContext commandContext) {
        return commandContext.getBean(TaskAssigneeDefinitionCacheManager.class)
                .findByProcessInstanceIdAndTaskDefinitionKey(processInstanceId, userTask.getId());
    }
}
