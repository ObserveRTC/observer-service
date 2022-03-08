package org.observertc.observer.common;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IndexedMap<K, V> implements Map<K, V> {
    private final Iterator<V> EMPTY_ITERATOR = new Iterator<V>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public V next() {
            return null;
        }
    };

    private final Map<K, V> entries = new HashMap<>();
    private final Map<String, Index<K, V>> indexes = new HashMap<>();

    public IndexAdder addIndex() {
        return new IndexAdder();
    }

    public boolean removeIndex(String indexKey) {
        var indexer = this.indexes.remove(indexKey);
        return Objects.nonNull(indexer);
    }

    @Override
    public int size() {
        return this.entries.size();
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.entries.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.entries.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.entries.get(key);
    }

    public Iterable<V> getByIndex(String indexKey, Object indexValue) {
        Index<K, V> index = this.indexes.get(indexKey);
        if (Objects.isNull(index)) return () -> EMPTY_ITERATOR;
        List<K> keys = index.entries.get(indexValue);
        if (Objects.isNull(keys)) return () -> EMPTY_ITERATOR;
        Iterator<K> it = keys.iterator();
        return () -> new Iterator<V>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public V next() {
                K key = it.next();
                V value = entries.get(key);
                return value;
            }
        };
    }

    @Override
    public V put(K key, V value) {
        var result= this.entries.put(key, value);
        for (var index : this.indexes.values()) {
            if (Objects.nonNull(result)) index.remove(key, value);
            index.put(key, value);
        }
        return result;
    }

    @Override
    public V remove(Object key) {
        var result= this.entries.remove(key);
        for (var index : this.indexes.values()) {
            index.remove(key, result);
        }
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.entrySet().stream().forEach(entry -> {
            var key = entry.getKey();
            var value = entry.getValue();
            for (var index : this.indexes.values()) {
                index.remove((K) key, value);
            }
        });
        this.entries.putAll(m);
    }

    @Override
    public void clear() {
        this.entries.clear();
        this.indexes.values().stream().forEach(Index::clear);
    }

    @Override
    public Set<K> keySet() {
        return this.entries.keySet();
    }

    public Set<K> keySet(String indexKey, Object indexValue) {
        Index<K, V> index = this.indexes.get(indexKey);
        if (Objects.isNull(index)) return Collections.EMPTY_SET;
        List<K> keys = index.entries.get(indexValue);
        if (Objects.isNull(index)) return Collections.EMPTY_SET;
        return new HashSet<>(keys);
    }

    @Override
    public Collection<V> values() {
        return this.entries.values();
    }

    public Collection<V> values(String indexKey, Object indexValue) {
        Index<K, V> index = this.indexes.get(indexKey);
        if (Objects.isNull(index)) return Collections.EMPTY_LIST;
        List<K> keys = index.entries.get(indexValue);
        if (Objects.isNull(index)) return Collections.EMPTY_LIST;
        return keys.stream().map(this.entries::get).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.entries.entrySet();
    }

    public static class Index<IK, IV> {
        private String name = null;
        private Function<IV, Object> mapper = null;
        private Map<Object, List<IK>> entries = new HashMap<>();
        private Index() {

        }

        public void put(IK key, IV value) {
            Object indexKey = this.mapper.apply(value);
            if (Objects.isNull(indexKey)) return;
            List<IK> keys = this.entries.get(indexKey);
            if (Objects.isNull(keys)) {
                keys = new LinkedList<>();
                this.entries.put(indexKey, keys);
            }
            keys.add(key);
        }

        public void remove(Object key, IV value) {
            Object indexKey = this.mapper.apply(value);
            if (Objects.isNull(indexKey)) return;
            List<IK> keys = this.entries.get(indexKey);
            if (Objects.isNull(keys)) return;
            var newKeys = keys.stream().filter( addedKey -> !addedKey.equals(key)).collect(Collectors.toList());
            this.entries.put(indexKey, newKeys);
        }

        public void clear() {
            this.entries.clear();
        }

    }

    public class IndexAdder {
        private Index<K, V> result = new Index<>();
        private IndexAdder() {

        }
        public IndexAdder withMapper(Function<V, Object> mapper) {
            this.result.mapper = mapper;
            return this;
        }

        public IndexAdder withName(String name) {
            this.result.name = name;
            return this;
        }

        public IndexedMap<K, V> add() {
            Objects.requireNonNull(this.result.mapper);
            Objects.requireNonNull(this.result.name);
            IndexedMap.this.indexes.put(this.result.name, this.result);
            return IndexedMap.this;
        }
    }
}
