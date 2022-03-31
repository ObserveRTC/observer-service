package org.observertc.observer.repositories;

import jakarta.inject.Singleton;

@Singleton
public interface RepositoryUpdatedEvent<T> {
    static<U> RepositoryUpdatedEvent<U> make(U oldValue, U newValue) {
        return new RepositoryUpdatedEvent<>() {
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
