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
package io.thesf.swiftframework.activiti.cache.data;

import io.thesf.swiftframework.activiti.api.runtime.model.TaskAssigneeDefinition;
import io.thesf.swiftframework.activiti.cache.CacheManager;
import io.thesf.swiftframework.activiti.cache.DelegateCacheManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implement of {@link TaskAssigneeDefinitionCacheManager}.
 *
 * Warning:
 * 1. Local cache is used by default. If the workflow service is deployed as multiple instances,
 * use distributed cache, such as Redis, Memcache, etc.
 *
 * @author VirtualCry
 */
public class TaskAssigneeDefinitionCacheManagerImpl extends DelegateCacheManager<String, TaskAssigneeDefinition>
        implements TaskAssigneeDefinitionCacheManager {

    public TaskAssigneeDefinitionCacheManagerImpl(CacheManager<String, TaskAssigneeDefinition> delegate) {
        super(delegate);
    }

    @Override
    public List<TaskAssigneeDefinition> findByProcessInstanceId(String processInstanceId) {
        return this.values().stream()
                .filter(taskAssigneeDefinition -> taskAssigneeDefinition.getProcessInstanceId().equals(processInstanceId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TaskAssigneeDefinition> findByProcessInstanceIdAndTaskDefinitionKey(String processInstanceId, String taskDefinitionKey) {
        return this.values().stream()
                .filter(taskAssigneeDefinition -> taskAssigneeDefinition.getProcessInstanceId().equals(processInstanceId))
                .findAny();
    }
}
