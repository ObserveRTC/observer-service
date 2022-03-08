package org.observertc.observer.repositories;

import javax.inject.Singleton;

@Singleton
public interface RepositoryExpiredEvent<T> {
    static<U> RepositoryExpiredEvent<U> make(U value, Long estimatedLastTouch) {
        return new RepositoryExpiredEvent<U>() {
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
