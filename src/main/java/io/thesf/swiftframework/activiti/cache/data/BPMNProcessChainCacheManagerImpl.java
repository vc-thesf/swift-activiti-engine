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
package io.thesf.swiftframework.activiti.cache.data;

import io.thesf.swiftframework.activiti.api.runtime.model.BPMNProcessChain;
import io.thesf.swiftframework.activiti.cache.CacheManager;
import io.thesf.swiftframework.activiti.cache.DelegateCacheManager;

/**
 * Implement of {@link BPMNProcessChainCacheManager}.
 *
 * Warning:
 * 1. Local cache is used by default. If the workflow service is deployed as multiple instances,
 * use distributed cache, such as Redis, Memcache, etc.
 *
 * @author VirtualCry
 */
public class BPMNProcessChainCacheManagerImpl extends DelegateCacheManager<String, BPMNProcessChain>
        implements BPMNProcessChainCacheManager {

    public BPMNProcessChainCacheManagerImpl(CacheManager<String, BPMNProcessChain> delegate) {
        super(delegate);
    }
}
