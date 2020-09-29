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
package io.thesf.swiftframework.activiti.engine.impl.interceptor;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * Extension for {@link Command}, which can use {@link org.springframework.context.ApplicationContext} in executor.
 *
 * @author VirtualCry
 */
public abstract class SpringCommand<T> implements Command<T> {

    @Override
    public final T execute(CommandContext commandContext) {
        return execute((SpringCommandContext) commandContext);
    }

    public abstract T execute(SpringCommandContext commandContext);
}
