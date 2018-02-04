/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.application.framework.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MapBackedSet<K, V> implements Set<V> {

    Map<K, V> map;
    Function<V, K> keyFunction;

    public MapBackedSet(final Map<K, V> map, final Function<V, K> keyFunction) {
        this.map = map;
        this.keyFunction = keyFunction;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Iterator<V> iterator() {
        return this.map.values().iterator();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public boolean remove(final Object obj) {
        final V value = (V) obj;
        return null != this.map.remove(this.keyFunction.apply(value));
    }

    @Override
    public boolean add(final V obj) {
        final V value = obj;
        return null == this.map.put(this.keyFunction.apply(value), value);
    }

    @Override
    public boolean contains(final Object obj) {
        final V value = (V) obj;
        return this.map.containsKey(this.keyFunction.apply(value));
    }

    @Override
    public Object[] toArray() {
        return this.map.values().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return this.map.values().toArray(a);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.map.values().containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends V> c) {
        boolean res = false;
        for (final V e : c) {
            res |= this.add(e);
        }
        return res;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return this.map.values().retainAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return this.map.values().removeAll(c);
    }
}
