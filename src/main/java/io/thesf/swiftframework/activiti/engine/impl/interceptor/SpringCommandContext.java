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

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extension for {@link CommandContext}, which can get bean from {@link ApplicationContext}.
 *
 * @author VirtualCry
 */
public class SpringCommandContext extends CommandContext {

    private final ApplicationContext      ctx;

    public SpringCommandContext(Command<?> command,
                                ProcessEngineConfigurationImpl processEngineConfiguration,
                                ApplicationContext ctx) {
        super(command, processEngineConfiguration);
        this.ctx = ctx;
    }


    /**
     * Get applicationContext instance.
     */
    public ApplicationContext getApplicationContext(){
        return ctx;
    }

    /**
     * Get bean by bean's name.
     *
     * @param beanName beanName
     */
    public Object getBean(String beanName){
        return ctx.getBean(beanName);
    }

    /**
     * Get bean by bean's type.
     *
     * @param beanType beanType
     */
    public <T> T getBean(Class<T> beanType){
        return ctx.getBean(beanType);
    }

    /**
     * Get bean by bean's name and type.
     *
     * @param beanName beanName
     * @param beanType beanType
     */
    public <T> T getBean(String beanName, Class<T> beanType){
        return ctx.getBean(beanName, beanType);
    }

    /**
     * Get beans by bean's type.
     *
     * @param beanType beanType
     */
    public <T> List<T> getBeans(Class<T> beanType){
        return Stream.of(ctx.getBeanNamesForType(beanType))
                .map(candidateName -> ctx.getBean(candidateName, beanType))
                .collect(Collectors.toList());
    }
}
