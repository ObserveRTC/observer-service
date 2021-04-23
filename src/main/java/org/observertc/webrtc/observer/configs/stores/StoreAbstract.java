package org.observertc.webrtc.observer.configs.stores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class StoreAbstract<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(StoreAbstract.class);

    private final AtomicReference<Map<K, V>> mapHolder;

    public StoreAbstract() {
        this.mapHolder = new AtomicReference<>(Collections.EMPTY_MAP);
    }

    public Map<K, V> findAll() {
        return this.mapHolder.get();
    }

    public Map<K, V> findByNames(Set<K> keys) {
        var map = this.mapHolder.get();
        return map.entrySet()
                .stream()
                .filter(e -> keys.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Optional<V> findByName(K name) {
        var map = this.mapHolder.get();
        var result = map.get(name);
        if (Objects.isNull(result)) {
            return Optional.empty();
        } else {
            return Optional.of(result);
        }
    }

    protected void setMap(Map<K, V> map) {
        this.mapHolder.set(map);
    }

    protected Map<K, V> getMap() {
        return this.mapHolder.get();
    }

}
