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
package io.thesf.swiftframework.activiti.api.runtime.model;

import org.activiti.api.process.model.BPMNActivity;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Chain for {@link BPMNActivityChain}.
 *
 * @author VirtualCry
 */
public interface BPMNProcessChain {

    /**
     * Get activity chains which are running.
     *
     * @return The {@link Set<BPMNActivityChain>}.
     */
    Set<BPMNActivityChain> getRunningActivityChains();


    /**
     * Get activity chains which are not running.
     *
     * @return The {@link Set<BPMNActivityChain>}.
     */
    Set<BPMNActivityChain> getHistoricActivityChains();


    /**
     * Get suspended activity counter.
     *
     * @return The counter.
     */
    Map<String, Integer> getSuspendedActivityCounter();


    /**
     * Suspend activity.
     *
     * @param activity  activity
     */
    void suspend(BPMNActivity activity);


    /**
     * Judge if the activity is suspended.
     *
     * @param activity  activity
     * @return result
     */
    boolean isSuspended(BPMNActivity activity);


    /**
     * Get the number of suspended activity.
     *
     * @param activity  activity
     * @return count
     */
    int getSuspendedCount(BPMNActivity activity);


    /**
     * Clear the specified activities.
     *
     * @param activity  activity
     */
    void clearSuspended(BPMNActivity activity);


    /**
     * Clear all specified activities.
     */
    void clearAllSuspended();


    /**
     * Run the specified chain.
     *
     * @param activityChain activityChain
     */
    void run(BPMNActivityChain activityChain);


    /**
     * Run the specified chains.
     *
     * @param activityChains activityChains
     */
    void runAll(Collection<BPMNActivityChain> activityChains);


    /**
     * Rerun the specified chain.
     *
     * @param activityChain activityChain
     */
    void reRun(BPMNActivityChain activityChain);


    /**
     * Rerun the specified chains.
     *
     * @param activityChains activityChains
     */
    void reRunAll(Collection<BPMNActivityChain> activityChains);


    /**
     * Exit the specified chain.
     *
     * @param activityChain activityChain
     */
    void exit(BPMNActivityChain activityChain);


    /**
     * Exit the specified chains.
     *
     * @param activityChains activityChains
     */
    void exitAll(Collection<BPMNActivityChain> activityChains);


    /**
     * Delete the specified running chain.
     *
     * @param activityChain activityChain
     */
    void deleteRunning(BPMNActivityChain activityChain);


    /**
     * Delete the specified running chains.
     *
     * @param activityChains activityChains
     */
    void deleteAllRunning(Collection<BPMNActivityChain> activityChains);


    /**
     * Delete the specified historic chain.
     *
     * @param activityChain activityChain
     */
    void deleteHistory(BPMNActivityChain activityChain);


    /**
     * Delete the specified historic chains.
     *
     * @param activityChains activityChains
     */
    void deleteAllHistories(Collection<BPMNActivityChain> activityChains);
}
