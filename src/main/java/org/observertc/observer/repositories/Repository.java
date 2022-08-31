package org.observertc.observer.repositories;

import java.util.Collection;
import java.util.Map;

public interface Repository<K, V> {

    V get(K key);

    Map<K, V> getAll(Collection<K> keys);
}
