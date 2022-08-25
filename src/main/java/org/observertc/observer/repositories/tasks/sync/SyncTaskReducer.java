package org.observertc.observer.repositories.tasks.sync;

import org.observertc.observer.repositories.HamokStorages;

@FunctionalInterface
public interface SyncTaskReducer<T> {
    SyncTaskState reduce(SyncTaskState actualState, HamokStorages hamokStorages, T subject);
}
