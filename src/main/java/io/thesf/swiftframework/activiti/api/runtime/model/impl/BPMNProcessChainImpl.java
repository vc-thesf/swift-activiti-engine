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
package io.thesf.swiftframework.activiti.api.runtime.model.impl;

import io.thesf.swiftframework.activiti.api.runtime.model.BPMNActivityChain;
import io.thesf.swiftframework.activiti.api.runtime.model.BPMNProcessChain;
import lombok.Getter;
import org.activiti.api.process.model.BPMNActivity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implement of {@link BPMNProcessChain}.
 *
 * @author VirtualCry
 */
@Getter
public class BPMNProcessChainImpl implements BPMNProcessChain {

    private final Set<BPMNActivityChain>        runningActivityChains;
    private final Set<BPMNActivityChain>        historicActivityChains;
    private final Map<String, Integer>          suspendedActivityCounter;

    public BPMNProcessChainImpl() {
        this.runningActivityChains = new HashSet<>();
        this.historicActivityChains = new HashSet<>();
        this.suspendedActivityCounter = new HashMap<>();
    }


    @Override
    public void suspend(BPMNActivity activity) {
        this.suspendedActivityCounter.compute(activity.getElementId(), (element, count) -> count != null ? count + 1 : 1);
    }

    @Override
    public boolean isSuspended(BPMNActivity activity) {
        return this.getSuspendedCount(activity) > 0;
    }

    @Override
    public int getSuspendedCount(BPMNActivity activity) {
        return this.suspendedActivityCounter.getOrDefault(activity.getElementId(), 0);
    }

    @Override
    public void clearSuspended(BPMNActivity activity) {
        this.suspendedActivityCounter.remove(activity.getElementId());
    }

    @Override
    public void clearAllSuspended() {
        this.suspendedActivityCounter.clear();
    }

    @Override
    public void run(BPMNActivityChain activityChain) {
        this.runningActivityChains.add(activityChain);
    }

    @Override
    public void runAll(Collection<BPMNActivityChain> activityChains) {
        this.runningActivityChains.addAll(activityChains);
    }

    @Override
    public void reRun(BPMNActivityChain activityChain) {
        this.historicActivityChains.stream()
                .filter(historicActivityChain -> historicActivityChain.equals(activityChain))
                .findAny()
                .ifPresent(candidateActivityChain -> {
                    this.historicActivityChains.remove(candidateActivityChain);
                    this.runningActivityChains.add(candidateActivityChain);
                });
    }

    @Override
    public void reRunAll(Collection<BPMNActivityChain> activityChains) {
        List<BPMNActivityChain> candidateActivityChains = this.historicActivityChains.stream()
                .filter(activityChains::contains)
                .collect(Collectors.toList());
        if (!candidateActivityChains.isEmpty()) {
            this.historicActivityChains.removeAll(candidateActivityChains);
            this.runningActivityChains.addAll(candidateActivityChains);
        }
    }

    @Override
    public void exit(BPMNActivityChain activityChain) {
        this.runningActivityChains.remove(activityChain);
        this.historicActivityChains.add(activityChain);
    }

    @Override
    public void exitAll(Collection<BPMNActivityChain> activityChains) {
        this.runningActivityChains.removeAll(activityChains);
        this.historicActivityChains.addAll(activityChains);
    }

    @Override
    public void deleteRunning(BPMNActivityChain activityChain) {
        this.runningActivityChains.remove(activityChain);
    }

    @Override
    public void deleteAllRunning(Collection<BPMNActivityChain> activityChains) {
        this.runningActivityChains.removeAll(activityChains);
    }

    @Override
    public void deleteHistory(BPMNActivityChain activityChain) {
        this.historicActivityChains.remove(activityChain);
    }

    @Override
    public void deleteAllHistories(Collection<BPMNActivityChain> activityChains) {
        this.historicActivityChains.removeAll(activityChains);
    }
}
