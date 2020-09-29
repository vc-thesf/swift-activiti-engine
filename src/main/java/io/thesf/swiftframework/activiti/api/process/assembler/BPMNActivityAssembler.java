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
package io.thesf.swiftframework.activiti.api.process.assembler;

import io.thesf.swiftframework.activiti.api.runtime.model.impl.FreeJumpBPMNActivityImpl;
import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.BPMNSequenceFlow;
import org.activiti.api.runtime.model.impl.BPMNActivityImpl;
import org.activiti.bpmn.model.FlowElement;

/**
 * Assembler for {@link BPMNActivity}.
 *
 * @author VirtualCry
 */
public class BPMNActivityAssembler {

    /**
     * Assemble by {@link BPMNSequenceFlow}.
     *
     * @param sequenceFlow  sequenceFlow
     * @return A new {@link BPMNActivity}.
     */
    public static BPMNActivity fromSource(BPMNSequenceFlow sequenceFlow) {
        BPMNActivityImpl activity = new BPMNActivityImpl(
                sequenceFlow.getSourceActivityElementId(),
                sequenceFlow.getSourceActivityName(),
                sequenceFlow.getSourceActivityType()
        );
        activity.setProcessInstanceId(sequenceFlow.getProcessInstanceId());
        activity.setProcessDefinitionId(sequenceFlow.getProcessDefinitionId());
        return activity;
    }

    /**
     * Assemble by {@link BPMNSequenceFlow}.
     *
     * @param sequenceFlow  sequenceFlow
     * @return A new {@link BPMNActivity}.
     */
    public static BPMNActivity fromTarget(BPMNSequenceFlow sequenceFlow) {
        BPMNActivityImpl activity = new BPMNActivityImpl(
                sequenceFlow.getTargetActivityElementId(),
                sequenceFlow.getTargetActivityName(),
                sequenceFlow.getTargetActivityType()
        );
        activity.setProcessInstanceId(sequenceFlow.getProcessInstanceId());
        activity.setProcessDefinitionId(sequenceFlow.getProcessDefinitionId());
        return activity;
    }

    /**
     * Assemble by {@link FlowElement}.
     *
     * @param flowElement           flowElement
     * @param processDefinitionId   processDefinitionId
     * @param processInstanceId     processInstanceId
     * @return A new {@link BPMNActivity}.
     */
    public static BPMNActivity from(FlowElement flowElement, String processDefinitionId, String processInstanceId) {
        BPMNActivityImpl activity = new FreeJumpBPMNActivityImpl(flowElement.getId(), flowElement.getName(), flowElement.getClass().getName());
        activity.setProcessDefinitionId(processDefinitionId);
        activity.setProcessInstanceId(processInstanceId);
        return activity;
    }
}
