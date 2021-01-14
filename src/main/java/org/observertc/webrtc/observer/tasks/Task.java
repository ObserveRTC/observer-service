package org.observertc.webrtc.observer.tasks;

public interface Task<T> {
    Task execute();
    boolean succeeded();
    T getResult();
    T getResultOrDefault(T defaultValue);
}
