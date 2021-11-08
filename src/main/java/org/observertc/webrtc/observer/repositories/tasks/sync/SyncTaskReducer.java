package org.observertc.webrtc.observer.repositories.tasks.sync;

import org.observertc.webrtc.observer.repositories.HazelcastMaps;

@FunctionalInterface
public interface SyncTaskReducer<T> {
    SyncTaskState reduce(SyncTaskState actualState, HazelcastMaps hazelcastMaps, T subject);
}
