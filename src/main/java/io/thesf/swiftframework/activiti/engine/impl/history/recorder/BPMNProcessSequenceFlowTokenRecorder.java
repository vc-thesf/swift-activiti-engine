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

import io.thesf.swiftframework.activiti.api.process.assembler.BPMNActivityAssembler;
import io.thesf.swiftframework.activiti.api.runtime.model.BPMNActivityChain;
import io.thesf.swiftframework.activiti.api.runtime.model.BPMNProcessChain;
import io.thesf.swiftframework.activiti.api.runtime.model.impl.BPMNActivityChainImpl;
import io.thesf.swiftframework.activiti.api.runtime.model.impl.BPMNProcessChainImpl;
import io.thesf.swiftframework.activiti.cache.data.BPMNProcessChainCacheManager;
import io.thesf.swiftframework.activiti.engine.impl.cmd.CalculateOutgoingFlowsCmd;
import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.BPMNSequenceFlow;
import org.activiti.engine.ProcessEngine;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One kind of BPMN process activity recorder. - {@literal SequenceFlow Taken}
 *
 * @author VirtualCry
 */
public class BPMNProcessSequenceFlowTokenRecorder {

    private final ProcessEngine                 processEngine;
    private final BPMNProcessChainCacheManager  bpmnProcessChainCacheManager;

    public BPMNProcessSequenceFlowTokenRecorder(ProcessEngine processEngine,
                                                BPMNProcessChainCacheManager bpmnProcessChainCacheManager) {
        this.processEngine = processEngine;
        this.bpmnProcessChainCacheManager = bpmnProcessChainCacheManager;
    }


    /**
     * Record when {@literal sequence flow token}.
     *
     * @param sequenceFlow  sequenceFlow
     */
    public void record(BPMNSequenceFlow sequenceFlow) {

        BPMNActivity sourceActivity = BPMNActivityAssembler.fromSource(sequenceFlow);
        BPMNActivity targetActivity = BPMNActivityAssembler.fromTarget(sequenceFlow);

        // Get process chain with process instance id
        BPMNProcessChain processChain = this.bpmnProcessChainCacheManager
                .computeIfAbsent(sequenceFlow.getProcessInstanceId(), key -> {
                    BPMNProcessChain candidateChain = new BPMNProcessChainImpl();
                    BPMNActivityChain activityChain = new BPMNActivityChainImpl(Arrays.asList(sourceActivity, targetActivity));
                    candidateChain.run(activityChain);
                    return candidateChain;
                });

        // Calculate the number of activities from the same source.
        int outgoingCount = processEngine.getManagementService()
                .executeCommand(new CalculateOutgoingFlowsCmd(sequenceFlow.getProcessInstanceId(), sourceActivity.getElementId()))
                .size();
        // Suspend the source activity when the number of activities is greater than one
        if (outgoingCount > 1)
            processChain.suspend(sourceActivity);
        // Clear suspended activity when the outgoingCount equals the number of suspended activities
        if (outgoingCount == processChain.getSuspendedCount(sourceActivity))
            processChain.clearSuspended(sourceActivity);

        // Get running candidate chains which its last activity is the same as the source activity from running chains
        List<BPMNActivityChain> candidateActivityChains = processChain.getRunningActivityChains().stream()
                .filter(activityChain -> activityChain.getLastActivity().getElementId().equals(sourceActivity.getElementId()))
                .collect(Collectors.toList());

        if (candidateActivityChains.isEmpty())
            return;

        // Exit the specified running candidate chains if the source activity is not suspended
        if (processChain.isSuspended(sourceActivity))
            processChain.exitAll(candidateActivityChains);

        // Create new chains and add the each target activity to the end
        List<BPMNActivityChain> newActivityChains = candidateActivityChains.stream()
                .map(activityChain -> {
                    BPMNActivityChain newActivityChain = new BPMNActivityChainImpl(activityChain);
                    newActivityChain.add(targetActivity);
                    return newActivityChain;
                })
                .collect(Collectors.toList());

        // Run the new chains
        processChain.runAll(newActivityChains);
    }
}
