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
package io.thesf.swiftframework.activiti.api.runtime.model;

import java.util.Map;

/**
 * Variables for {@link org.activiti.engine.impl.persistence.entity.ExecutionEntity#setTransientVariablesLocal(Map)}.
 *
 * @author VirtualCry
 */
public class ExecutionVariables {

    public static final String SYSTEM_ASSIGN            = "SYSTEM_ASSIGN";
    public static final String SYSTEM_JOINTLY_ASSIGN    = "SYSTEM_JOINTLY_ASSIGN";
}
