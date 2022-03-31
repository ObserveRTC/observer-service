package org.observertc.observer.repositories;

import jakarta.inject.Singleton;

@Singleton
public interface RepositoryExpiredEvent<T> {
    static<U> RepositoryExpiredEvent<U> make(U value, Long estimatedLastTouch) {
        return new RepositoryExpiredEvent<>() {
            @Override
            public U getValue() {
                return value;
            }

            @Override
            public Long estimatedLastTouch() {
                return estimatedLastTouch;
            }
        };
    }

    T getValue();
    Long estimatedLastTouch();
}
