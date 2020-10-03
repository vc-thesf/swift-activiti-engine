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
package io.thesf.swiftframework.activiti.api.process.runtime.events.listener;

import io.thesf.swiftframework.activiti.engine.impl.history.BPMNProcessActivityRecordPayload;
import io.thesf.swiftframework.activiti.engine.impl.history.BPMNProcessActivityRecorder;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;

import javax.annotation.Resource;

/**
 * Listen {@link BPMNSequenceFlowTakenEvent}.
 *
 * @see io.thesf.swiftframework.activiti.engine.impl.history.recorder.BPMNProcessSequenceFlowTokenRecorder
 * @author VirtualCry
 */
@Slf4j
public class BPMNSequenceFlowTakenEventListener implements BPMNElementEventListener<BPMNSequenceFlowTakenEvent> {

    @Resource
    private BPMNProcessActivityRecorder         bpmnProcessActivityRecorder;

    @Override
    public void onEvent(BPMNSequenceFlowTakenEvent event) {
        log.info("Event: {} received. Time: {}.\n {}", event.getEventType(), event.getTimestamp(), event);

        // Record operation - `Sequence flow taken`
        BPMNProcessActivityRecordPayload recordPayload = BPMNProcessActivityRecordPayload
                .sequenceFlowTakenRecord()
                .executeAfterCommit(true)
                .bpmnSequenceFlow(event.getEntity())
                .build();
        bpmnProcessActivityRecorder.execute(recordPayload);
    }
}
