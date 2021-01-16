package org.observertc.webrtc.observer.common;

public interface Task<T> extends AutoCloseable {
    Task execute();
    boolean succeeded();
    T getResult();
    T getResultOrDefault(T defaultValue);

    default void close() {

    }

    default String getName() {
        return this.getClass().getSimpleName();
    }
}
