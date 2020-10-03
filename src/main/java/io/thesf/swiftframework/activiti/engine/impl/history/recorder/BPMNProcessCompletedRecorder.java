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
package io.thesf.swiftframework.activiti.engine.impl.history.recorder;

import io.thesf.swiftframework.activiti.cache.data.BPMNProcessChainCacheManager;
import org.activiti.api.process.model.ProcessInstance;

/**
 * One kind of BPMN process activity recorder. - {@literal Process Completed}
 *
 * @author VirtualCry
 */
public class BPMNProcessCompletedRecorder {

    private final BPMNProcessChainCacheManager  bpmnProcessChainCacheManager;

    public BPMNProcessCompletedRecorder(BPMNProcessChainCacheManager bpmnProcessChainCacheManager) {
        this.bpmnProcessChainCacheManager = bpmnProcessChainCacheManager;
    }


    /**
     * Record when {@literal process completed}.
     *
     * @param processInstance   processInstance
     */
    public void record(ProcessInstance processInstance) {
        this.bpmnProcessChainCacheManager.remove(processInstance.getId());
    }
}
