package org.observertc.webrtc.observer.repositories;

import javax.inject.Singleton;

@Singleton
public interface RepositoryUpdateEvent<T> {
    static<U> RepositoryUpdateEvent<U> make(U oldValue, U newValue) {
        return new RepositoryUpdateEvent<U>() {
            @Override
            public U getOldValue() {
                return oldValue;
            }

            @Override
            public U getNewValue() {
                return newValue;
            }
        };
    }

    T getOldValue();
    T getNewValue();
}
