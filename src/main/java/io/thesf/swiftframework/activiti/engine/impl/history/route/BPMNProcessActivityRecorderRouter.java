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
package io.thesf.swiftframework.activiti.engine.impl.history.route;

import io.thesf.swiftframework.activiti.engine.impl.history.BPMNProcessActivityRecordPayload;
import io.thesf.swiftframework.activiti.engine.impl.history.BPMNProcessActivityRecorder;
import io.thesf.swiftframework.activiti.engine.impl.history.payloads.FreeJumpRecordPayload;
import io.thesf.swiftframework.activiti.engine.impl.history.payloads.ProcessCompletedRecordPayload;
import io.thesf.swiftframework.activiti.engine.impl.history.payloads.RollBackRecordPayload;
import io.thesf.swiftframework.activiti.engine.impl.history.payloads.SequenceFlowTakenRecordPayload;
import io.thesf.swiftframework.activiti.engine.impl.history.recorder.BPMNProcessCompletedRecorder;
import io.thesf.swiftframework.activiti.engine.impl.history.recorder.BPMNProcessFreeJumpRecorder;
import io.thesf.swiftframework.activiti.engine.impl.history.recorder.BPMNProcessRollBackRecorder;
import io.thesf.swiftframework.activiti.engine.impl.history.recorder.BPMNProcessSequenceFlowTokenRecorder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Route to different execute task base on the kind of {@link BPMNProcessActivityRecordPayload}.
 *
 * @author VirtualCry
 */
public class BPMNProcessActivityRecorderRouter extends BPMNProcessActivityRecorder implements ApplicationContextAware {

    private ApplicationContext  ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Override
    protected Runnable selectExecuteTask(BPMNProcessActivityRecordPayload recordPayload) {
        if (recordPayload instanceof SequenceFlowTakenRecordPayload) {
            BPMNProcessSequenceFlowTokenRecorder recorder = this.ctx.getBean(BPMNProcessSequenceFlowTokenRecorder.class);
            SequenceFlowTakenRecordPayload payload = (SequenceFlowTakenRecordPayload) recordPayload;
            return () -> recorder.record(payload.getBpmnSequenceFlow());
        }
        else if (recordPayload instanceof FreeJumpRecordPayload) {
            BPMNProcessFreeJumpRecorder recorder = this.ctx.getBean(BPMNProcessFreeJumpRecorder.class);
            FreeJumpRecordPayload payload = (FreeJumpRecordPayload) recordPayload;
            return () -> recorder.record(payload.getSourceActivities(), payload.getTargetActivities());
        }
        else if (recordPayload instanceof RollBackRecordPayload) {
            BPMNProcessRollBackRecorder recorder = this.ctx.getBean(BPMNProcessRollBackRecorder.class);
            RollBackRecordPayload payload = (RollBackRecordPayload) recordPayload;
            return () -> recorder.record(payload.getProcessInstanceId(), payload.getTurnBackActivityChains(), payload.getRecoveryActivityChains());
        }
        else if (recordPayload instanceof ProcessCompletedRecordPayload) {
            BPMNProcessCompletedRecorder recorder = this.ctx.getBean(BPMNProcessCompletedRecorder.class);
            ProcessCompletedRecordPayload payload = (ProcessCompletedRecordPayload) recordPayload;
            return () -> recorder.record(payload.getProcessInstance());
        }
        else
            return () -> { };
    }
}
