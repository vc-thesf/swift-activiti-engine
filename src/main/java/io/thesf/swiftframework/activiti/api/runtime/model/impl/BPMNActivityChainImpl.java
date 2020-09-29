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
import org.activiti.api.process.model.BPMNActivity;
import org.activiti.bpmn.model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Implement of {@link BPMNActivityChain}.
 *
 * @author VirtualCry
 */
public class BPMNActivityChainImpl extends ArrayList<BPMNActivity> implements BPMNActivityChain {

    public BPMNActivityChainImpl() {
    }

    public BPMNActivityChainImpl(Collection<? extends BPMNActivity> activities) {
        super(activities);
    }

    @Override
    public BPMNActivity getLastActivity() {
        return this.isEmpty() ? null : this.get(this.size() - 1);
    }

    @Override
    public int getSecondLastTaskIndex() {
        try {
            int length = this.size();
            int taskCount = 0;
            for (int i = 1; i < length; i++) {
                int index = length - i;
                BPMNActivity activity = this.get(index);
                if (activity instanceof FreeJumpBPMNActivityImpl   // 若是通过自由跳转记录的节点，视为满足条件
                        || Task.class.isAssignableFrom(Class.forName(activity.getActivityType()))) {
                    taskCount++;
                    if (taskCount == 2)
                        return index;
                }
            }
            return -1;
        } catch (Exception ex) {
            throw new RuntimeException(ex); }
    }

    @Override
    public BPMNActivity getSecondLastTask() {
        int index = this.getSecondLastTaskIndex();
        return index < 0 ? null : this.get(index);
    }

    @Override
    public BPMNActivityChain subActivityChain(int beginIndex, int endIndex) {
        BPMNActivityChain activityChain = new BPMNActivityChainImpl();
        for (int i = 0; i < this.size(); i++) {
            if (i >= beginIndex && i <= endIndex)
                activityChain.add(this.get(i));
        }
        return activityChain;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BPMNActivityChain && this.size() == ((BPMNActivityChain) o).size()) {
            for (int i = 0; i < this.size(); i++) {
                if (!this.get(i).equals(((BPMNActivityChain) o).get(i)))
                    return false;
            }
            return true;
        }
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.stream().map(BPMNActivity::toString).reduce("", (x, y) -> x + "," + y));
    }
}
