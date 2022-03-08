package org.observertc.observer.repositories.tasks.sync;

import java.util.Objects;

public enum SyncTaskState {
    CREATED,
    PENDING,
    DONE;


    public boolean equalsTo(String stateStr) {
        if (Objects.isNull(stateStr)) {
            return false;
        }
        return this.name().equalsIgnoreCase(stateStr);
    }

    public static SyncTaskState tryParse(String state) {
        try {
            return SyncTaskState.valueOf(state);
        } catch (Exception ex) {
            return null;
        }
    }
}
