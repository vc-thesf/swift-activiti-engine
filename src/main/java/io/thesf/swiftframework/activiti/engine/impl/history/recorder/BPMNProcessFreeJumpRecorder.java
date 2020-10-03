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
import io.thesf.swiftframework.activiti.api.runtime.model.impl.BPMNActivityChainImpl;
import io.thesf.swiftframework.activiti.cache.data.BPMNProcessChainCacheManager;
import org.activiti.api.process.model.BPMNActivity;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One kind of BPMN process activity recorder. - {@literal Free Jump}
 *
 * @author VirtualCry
 */
public class BPMNProcessFreeJumpRecorder {

    private final BPMNProcessChainCacheManager  bpmnProcessChainCacheManager;

    public BPMNProcessFreeJumpRecorder(BPMNProcessChainCacheManager bpmnProcessChainCacheManager) {
        this.bpmnProcessChainCacheManager = bpmnProcessChainCacheManager;
    }


    /**
     * Record when {@literal free jump}.
     *
     * @param sourceActivities  sourceActivities
     * @param targetActivities  targetActivities
     */
    public void record(Collection<BPMNActivity> sourceActivities, Collection<BPMNActivity> targetActivities) {
        sourceActivities.forEach(sourceActivity -> this.record(sourceActivity, targetActivities));
    }

    /**
     * Record when {@literal free jump}.
     *
     * @param sourceActivity    sourceActivity
     * @param targetActivities  targetActivities
     */
    public void record(BPMNActivity sourceActivity, Collection<BPMNActivity> targetActivities) {

        // Get process chain with process instance id
        BPMNProcessChain processChain = this.bpmnProcessChainCacheManager.get(sourceActivity.getProcessInstanceId());

        // clear all counters that used to hold activity
        processChain.clearAllSuspended();

        // Get running candidate chains which its last activity is the same as the source activity from running chains
        List<BPMNActivityChain> candidateRunningActivityChains = processChain.getRunningActivityChains().stream()
                .filter(activityChain -> activityChain.getLastActivity().getElementId().equals(sourceActivity.getElementId()))
                .collect(Collectors.toList());

        if (candidateRunningActivityChains.isEmpty())
            return;

        // Exit the specified running candidate chains if the source activity is not suspended
        if (processChain.isSuspended(sourceActivity))
            processChain.exitAll(candidateRunningActivityChains);

        // Create new chains and add the each target activity to the end
        List<BPMNActivityChain> newActivityChains = targetActivities.stream()
                .flatMap(targetActivity -> candidateRunningActivityChains.stream()
                        .map(activityChain -> {
                            BPMNActivityChain newActivityChain = new BPMNActivityChainImpl(activityChain);
                            newActivityChain.add(targetActivity);
                            return newActivityChain;
                        })
                ).collect(Collectors.toList());

        // Run the new chains
        processChain.runAll(newActivityChains);
    }
}
