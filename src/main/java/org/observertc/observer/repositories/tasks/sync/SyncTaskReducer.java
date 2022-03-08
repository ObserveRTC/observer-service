package org.observertc.observer.repositories.tasks.sync;

import org.observertc.observer.repositories.HazelcastMaps;

@FunctionalInterface
public interface SyncTaskReducer<T> {
    SyncTaskState reduce(SyncTaskState actualState, HazelcastMaps hazelcastMaps, T subject);
}
