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
package io.thesf.swiftframework.activiti.engine.impl.history;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Used to record process activities.
 *
 * @author VirtualCry
 */
public abstract class BPMNProcessActivityRecorder {

    public void execute(BPMNProcessActivityRecordPayload recordPayload) {
        Runnable executeTask = this.selectExecuteTask(recordPayload);
        if (recordPayload.isExecuteAfterCommit())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    executeTask.run();
                }
            });
        else
            executeTask.run();
    }

    protected  abstract Runnable selectExecuteTask(BPMNProcessActivityRecordPayload recordPayload);
}
