package org.observertc.observer.common;

public interface Task<T> extends AutoCloseable {
    Task<T> execute();
    boolean succeeded();
    T getResult();
    T getResultOrDefault(T defaultValue);

    default void close() {

    }

    default String getName() {
        return this.getClass().getSimpleName();
    }
}
