package org.observertc.observer.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Least Recent Unit cache type map, where the map size is fized, and the least read items
 * are removed when size limit is reached
 *
 * @param <K>
 * @param <V>
 */
public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private int maxSize;
    private final Consumer<Map.Entry<K, V>> removedCb;
    public LRULinkedHashMap(int capacity) {
        this(capacity, v -> {});
    }

    public LRULinkedHashMap(int capacity, Consumer<Map.Entry<K, V>> removedCb) {
        super(capacity, 0.75f, true);
        this.maxSize = capacity;
        this.removedCb = removedCb;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        boolean result = this.size() > maxSize; //must override it if used in a fixed cache
        if (result) {
            this.removedCb.accept(eldest);
        }
        return result;
    }
}
