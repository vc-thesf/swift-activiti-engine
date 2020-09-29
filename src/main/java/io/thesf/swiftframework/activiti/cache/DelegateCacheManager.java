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
package io.thesf.swiftframework.activiti.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Delegate Cache Manager.
 *
 * @author VirtualCry
 */
public abstract class DelegateCacheManager<K, V> implements CacheManager<K, V> {

    private final CacheManager<K, V>            delegate;

    public DelegateCacheManager(CacheManager<K, V> delegate) {
        this.delegate = delegate;
    }


    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.delegate.get(key);
    }

    @Override
    public V put(K key, V value) {
        return this.delegate.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.delegate.putAll(m);
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.delegate.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.delegate.entrySet();
    }
}
