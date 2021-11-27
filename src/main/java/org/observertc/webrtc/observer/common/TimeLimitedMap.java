package org.observertc.webrtc.observer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

/**
 * A special LinkedHasMap created for time limited storage of items
 * @param <K>
 * @param <V>
 */
public class TimeLimitedMap<K, V>  extends HashMap<K, V>{
    private static final Logger logger = LoggerFactory.getLogger(TimeLimitedMap.class);

    private final Map<K, Instant> accessedKeys;
    private Duration threshold;
    private Consumer<V> removedCb = null;
    private Consumer<V> timeoutRemovedCb = null;

    public TimeLimitedMap(Duration threshold) {
        this.accessedKeys = new LinkedHashMap<>(16, .75f, true);
        this.threshold = threshold;
    }


    @Override
    public V put(K key, V value) {
        V result = super.put(key, value);
        Instant now = Instant.now();
        this.accessedKeys.put(key, now);
        this.update(now);
        return result;
    }

    @Override
    public V remove(Object key) {
        V removed = super.remove(key);
        if (Objects.isNull(removed)) {
            return null;
        }
        if (Objects.nonNull(this.removedCb)) {
            this.removedCb.accept(removed);
        }
        return removed;
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (Objects.isNull(value)) {
            return null;
        }
        Instant now = Instant.now();

        this.accessedKeys.put((K) key, now);
        this.update(now);
        return value;
    }

    public TimeLimitedMap<K, V> onRemoved(Consumer<V> removedCb) {
        this.removedCb = removedCb;
        return this;
    }

    public TimeLimitedMap<K, V> onTimeoutRemoved(Consumer<V> removedCb) {
        this.timeoutRemovedCb = removedCb;
        return this;
    }

    private void update(Instant now) {
        Iterator<Map.Entry<K, Instant>> it = this.accessedKeys.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, Instant> entry = it.next();

            if (Duration.between(entry.getValue(), now).compareTo(this.threshold) < 0) {
                // the first item, accessed less than threshold, then we stop the check because
                // we know all consecutive items are accessed less then this.
                return;
            }
            // no hard feelings
            var removedItem = this.remove(entry.getKey());
            it.remove();
            if (Objects.nonNull(this.timeoutRemovedCb)) {
                this.timeoutRemovedCb.accept(removedItem);
            }
        }
    }
}
