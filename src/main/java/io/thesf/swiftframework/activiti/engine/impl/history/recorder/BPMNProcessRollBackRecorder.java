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

import io.thesf.swiftframework.activiti.api.runtime.model.BPMNActivityChain;
import io.thesf.swiftframework.activiti.api.runtime.model.BPMNProcessChain;
import io.thesf.swiftframework.activiti.cache.data.BPMNProcessChainCacheManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One kind of BPMN process activity recorder. - {@literal Roll Back}
 *
 * @author VirtualCry
 */
public class BPMNProcessRollBackRecorder {

    private final BPMNProcessChainCacheManager  bpmnProcessChainCacheManager;

    public BPMNProcessRollBackRecorder(BPMNProcessChainCacheManager bpmnProcessChainCacheManager) {
        this.bpmnProcessChainCacheManager = bpmnProcessChainCacheManager;
    }


    /**
     * Record when {@literal roll back}.
     *
     * @param processInstanceId         processInstanceId
     * @param rollBackActivityChains    chains that need to be rolled back.
     * @param recoveryActivityChains    chains from history that need to be recovered.
     */
    public void record(String processInstanceId,
                       Collection<BPMNActivityChain> rollBackActivityChains,
                       Collection<BPMNActivityChain> recoveryActivityChains) {

        // Get process chain with process instance id
        BPMNProcessChain processChain = this.bpmnProcessChainCacheManager.get(processInstanceId);

        // As long as it has the same path as the recovery chains, it is considered to be obsolete.
        List<BPMNActivityChain> obsoleteHistoricActivityChains = processChain.getHistoricActivityChains().stream()
                .filter(activityChain -> recoveryActivityChains.stream().noneMatch(recoveryActivityChain -> recoveryActivityChain.equals(activityChain))
                        && recoveryActivityChains.stream().anyMatch(recoveryActivityChain -> recoveryActivityChain.equals(activityChain.subActivityChain(0, recoveryActivityChain.size() - 1))))
                .collect(Collectors.toList());

        // Delete running chains that need to be rolled back
        processChain.deleteAllRunning(rollBackActivityChains);
        // Delete historic chains that are considered to be obsolete
        processChain.deleteAllHistories(obsoleteHistoricActivityChains);
        // Rerun chains that need to be recovered.
        processChain.reRunAll(recoveryActivityChains);
    }
}
