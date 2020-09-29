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

import java.util.List;

/**
 * Chain for {@link BPMNActivity}.
 *
 * @author VirtualCry
 */
public interface BPMNActivityChain extends List<BPMNActivity>  {

    /**
     * Get last activity.
     *
     * @return The {@link BPMNActivity}.
     */
    BPMNActivity getLastActivity();


    /**
     * Get index of the second last task.
     *
     * @return index
     */
    int getSecondLastTaskIndex();


    /**
     * Get the second last task.
     *
     * @return The {@link BPMNActivity}.
     */
    BPMNActivity getSecondLastTask();


    /**
     * Returns a chain that is a substring of this chain. The
     * sub chain begins at the specified {@code beginIndex} and
     * extends to the chain at index {@code endIndex}.
     * Thus the length of the sub chain is {@code endIndex - beginIndex + 1}.
     *
     * @param beginIndex    the beginning index, inclusive.
     * @param endIndex      the ending index, inclusive.
     * @return A new {@link BPMNActivityChain}.
     */
    BPMNActivityChain subActivityChain(int beginIndex, int endIndex);
}
