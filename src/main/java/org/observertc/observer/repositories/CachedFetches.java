package org.observertc.observer.repositories;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class CachedFetches<K, V> {

    public static<U, R> Builder<U, R> builder() {
        return new Builder<>();
    }

    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private Function<K, V> fetchOne;
    private Function<Set<K>, Map<K, V>> fetchAll;

    private CachedFetches() {
    }

    public void add(K key, V value) {
        this.cache.put(key, value);
    }

    public V get(K key) {
        if (key == null) {
            return null;
        }
        var result = this.cache.get(key);
        if (result != null) {
            return result;
        }
        result = this.fetchOne.apply(key);
        if (result != null) {
            this.cache.put(key, result);
        }
        return result;
    }

    public Map<K, V> getAll(Set<K> keys) {
        if (keys == null || keys.size() < 1) {
            return Collections.emptyMap();
        }
        var result = new HashMap<K, V>();
        Set<K> remaining = null;
        for (var key : keys) {
            var value = this.cache.get(key);
            if (value != null) {
                result.put(key, value);
                continue;
            }
            if (remaining == null) {
                remaining = new HashSet<>();
            }
            remaining.add(key);
        }
        if (remaining != null) {
            var values = this.fetchAll.apply(remaining);
            if (values != null && 0 < values.size()) {
                result.putAll(values);
            }
            // the add puts into the cache, should be in the createXXXX in the repositories
//            this.cache.putAll(values);
        }
        return result;
    }

    public void clear() {
        this.cache.clear();
    }

    public static class Builder<U, R> {
        private CachedFetches result = new CachedFetches();

        public Builder<U, R> onFetchOne(Function<U, R> func) {
            this.result.fetchOne = func;
            return this;
        }

        public Builder<U, R> onFetchAll(Function<Set<U>, Map<U, R>> func) {
            this.result.fetchAll = func;
            return this;
        }

        public CachedFetches build() {
            Objects.requireNonNull(this.result.fetchOne);
            Objects.requireNonNull(this.result.fetchAll);
            return this.result;
        }
    }
}
