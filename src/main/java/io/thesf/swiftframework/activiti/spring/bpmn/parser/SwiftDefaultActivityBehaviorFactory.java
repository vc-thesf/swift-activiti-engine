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
package io.thesf.swiftframework.activiti.spring.bpmn.parser;

import io.thesf.swiftframework.activiti.engine.impl.bpmn.behavior.InnerAssignParallelMultiInstanceBehavior;
import io.thesf.swiftframework.activiti.engine.impl.bpmn.behavior.InnerAssignSequentialMultiInstanceBehavior;
import io.thesf.swiftframework.activiti.engine.impl.bpmn.behavior.InnerAssignUserTaskActivityBehavior;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;


/**
 * Extension for {@link DefaultActivityBehaviorFactory}, which can use custom activity behavior.
 *
 * @see InnerAssignUserTaskActivityBehavior
 * @see InnerAssignParallelMultiInstanceBehavior
 * @see InnerAssignSequentialMultiInstanceBehavior
 * @author VirtualCry
 */
public class SwiftDefaultActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    @Override
    public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask) {
        return new InnerAssignUserTaskActivityBehavior(userTask);
    }

    @Override
    public ParallelMultiInstanceBehavior createParallelMultiInstanceBehavior(Activity activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
        return new InnerAssignParallelMultiInstanceBehavior(activity, innerActivityBehavior);
    }

    @Override
    public SequentialMultiInstanceBehavior createSequentialMultiInstanceBehavior(Activity activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
        return new InnerAssignSequentialMultiInstanceBehavior(activity, innerActivityBehavior);
    }
}
