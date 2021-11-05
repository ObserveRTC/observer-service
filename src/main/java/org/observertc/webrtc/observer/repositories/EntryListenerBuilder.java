package org.observertc.webrtc.observer.repositories;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

class EntryListenerBuilder<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(EntryListenerBuilder.class);

    public static<R, U> EntryListenerBuilder<R, U> create(String context) {
        return new EntryListenerBuilder<R, U>(context);
    }

    public static<R, U> EntryListenerBuilder<R, U> create() {
        return create("No Context is given");
    }

    public final String context;
    private List<Consumer<EntryEvent<K, V>>> entryAddedListeners = new LinkedList<>();
    private List<Consumer<EntryEvent<K, V>>> entryEvictedListeners = new LinkedList<>();
    private List<Consumer<EntryEvent<K, V>>> entryExpiredListeners = new LinkedList<>();
    private List<Consumer<EntryEvent<K, V>>> entryRemovedListeners = new LinkedList<>();
    private List<Consumer<EntryEvent<K, V>>> entryUpdatedListeners = new LinkedList<>();
    private List<Consumer<MapEvent>> mapClearedListeners = new LinkedList<>();
    private List<Consumer<MapEvent>> mapEvictedListeners = new LinkedList<>();


    private EntryListenerBuilder(String context) {
        this.context = context;
    }

    public EntryListenerBuilder<K, V> onEntryAdded(Consumer<EntryEvent<K, V>> listener) {
        this.entryAddedListeners.add(listener);
        return this;
    }

    public EntryListenerBuilder<K, V> onEntryEvicted(Consumer<EntryEvent<K, V>> listener) {
        this.entryEvictedListeners.add(listener);
        return this;
    }

    public EntryListenerBuilder<K, V> onEntryExpired(Consumer<EntryEvent<K, V>> listener) {
        this.entryExpiredListeners.add(listener);
        return this;
    }

    public EntryListenerBuilder<K, V> onEntryRemoved(Consumer<EntryEvent<K, V>> listener) {
        this.entryRemovedListeners.add(listener);
        return this;
    }

    public EntryListenerBuilder<K, V> onEntryUpdated(Consumer<EntryEvent<K, V>> listener) {
        this.entryUpdatedListeners.add(listener);
        return this;
    }

    public EntryListenerBuilder<K, V> onMapCleared(Consumer<MapEvent> listener) {
        this.mapClearedListeners.add(listener);
        return this;
    }

    public EntryListenerBuilder<K, V> onMapEvicted(Consumer<MapEvent> listener) {
        this.mapEvictedListeners.add(listener);
        return this;
    }

    public EntryListener<K, V> build() {
        return new EntryListener<K, V>() {
            @Override
            public void entryAdded(EntryEvent<K, V> event) {
                entryAddedListeners.forEach(listener -> {
                    try {
                        listener.accept(event);
                    } catch (Throwable throwable) {
                        logger.warn("Exception occurred while dispatching added event {}", event.getValue(), throwable);
                    }
                });
            }

            @Override
            public void entryEvicted(EntryEvent<K, V> event) {
                entryEvictedListeners.forEach(listener -> {
                    try {
                        listener.accept(event);
                    } catch (Throwable throwable) {
                        logger.warn("Exception occurred while dispatching evicted event {}", event.getValue(), throwable);
                    }
                });
            }

            @Override
            public void entryExpired(EntryEvent<K, V> event) {
                entryExpiredListeners.forEach(listener -> {
                    try {
                        listener.accept(event);
                    } catch (Throwable throwable) {
                        logger.warn("Exception occurred while dispatching expired event {}", event.getValue(), throwable);
                    }
                });
            }

            @Override
            public void entryRemoved(EntryEvent<K, V> event) {
                entryRemovedListeners.forEach(listener -> {
                    try {
                        listener.accept(event);
                    } catch (Throwable throwable) {
                        logger.warn("Exception occurred while dispatching removed event {}", event.getValue(), throwable);
                    }
                });
            }

            @Override
            public void entryUpdated(EntryEvent<K, V> event) {
                entryUpdatedListeners.forEach(listener -> {
                    try {
                        listener.accept(event);
                    } catch (Throwable throwable) {
                        logger.warn("Exception occurred while dispatching updated event {}", event.getValue(), throwable);
                    }
                });
            }

            @Override
            public void mapCleared(MapEvent event) {
                mapClearedListeners.forEach(listener -> {
                    try {
                        listener.accept(event);
                    } catch (Throwable throwable) {
                        logger.warn("Exception occurred while dispatching map cleared event {}", event.toString(), throwable);
                    }
                });
            }

            @Override
            public void mapEvicted(MapEvent event) {
                mapEvictedListeners.forEach(listener -> {
                    try {
                        listener.accept(event);
                    } catch (Throwable throwable) {
                        logger.warn("Exception occurred while dispatching map cleared event {}", event.toString(), throwable);
                    }
                });
            }
        };
    }
}
