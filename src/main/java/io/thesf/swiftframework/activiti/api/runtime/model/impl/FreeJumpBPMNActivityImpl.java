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

import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.runtime.model.impl.BPMNActivityImpl;

/**
 * Used to identify whether the activity is created by a @{@literal Free Jump} operation.
 *
 * @author VirtualCry
 */
public class FreeJumpBPMNActivityImpl extends BPMNActivityImpl implements BPMNActivity {

    public FreeJumpBPMNActivityImpl() {
    }

    public FreeJumpBPMNActivityImpl(String elementId, String activityName, String activityType) {
        super(elementId, activityName, activityType);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
