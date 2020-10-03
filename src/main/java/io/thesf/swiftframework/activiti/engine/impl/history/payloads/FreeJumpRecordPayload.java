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
package io.thesf.swiftframework.activiti.engine.impl.history.payloads;

import io.thesf.swiftframework.activiti.api.process.assembler.BPMNActivityAssembler;
import io.thesf.swiftframework.activiti.engine.impl.history.BPMNProcessActivityRecordPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.activiti.api.process.model.BPMNActivity;
import org.activiti.bpmn.model.FlowElement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * One kind of payload. - {@literal Free Jump}
 *
 * @author VirtualCry
 */
@AllArgsConstructor
@Getter @Setter
public class FreeJumpRecordPayload extends BPMNProcessActivityRecordPayload {

    protected boolean                   executeAfterCommit;
    protected Collection<BPMNActivity>  sourceActivities;
    protected Collection<BPMNActivity>  targetActivities;


    public static FreeJumpRecordPayloadBuilder builder() {
        return new FreeJumpRecordPayloadBuilder();
    }

    public static class FreeJumpRecordPayloadBuilder {
        private boolean                     executeAfterCommit;
        private Collection<BPMNActivity>    sourceActivities;
        private Collection<BPMNActivity>    targetActivities;

        FreeJumpRecordPayloadBuilder() {
        }

        public FreeJumpRecordPayloadBuilder executeAfterCommit(boolean executeAfterCommit) {
            this.executeAfterCommit = executeAfterCommit;
            return this;
        }

        public FreeJumpRecordPayloadBuilder sourceActivities(Collection<BPMNActivity> sourceActivities) {
            this.sourceActivities = sourceActivities;
            return this;
        }

        public FreeJumpRecordPayloadBuilder targetActivities(Collection<BPMNActivity> targetActivities) {
            this.targetActivities = targetActivities;
            return this;
        }

        public FreeJumpRecordPayloadBuilder sourceActivities(Collection<FlowElement> flowElements, String processDefinitionId, String processInstanceId) {
            this.sourceActivities = flowElements.stream()
                    .map(flowElement -> BPMNActivityAssembler.from(flowElement, processDefinitionId, processInstanceId))
                    .collect(Collectors.toList());
            return this;
        }

        public FreeJumpRecordPayloadBuilder targetActivities(Collection<FlowElement> flowElements, String processDefinitionId, String processInstanceId) {
            this.sourceActivities = flowElements.stream()
                    .map(flowElement -> BPMNActivityAssembler.from(flowElement, processDefinitionId, processInstanceId))
                    .collect(Collectors.toList());
            return this;
        }

        public FreeJumpRecordPayload build() {
            return new FreeJumpRecordPayload(this.executeAfterCommit, this.sourceActivities, this.targetActivities);
        }
    }
}
