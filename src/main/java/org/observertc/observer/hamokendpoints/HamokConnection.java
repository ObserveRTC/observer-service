package org.observertc.observer.hamokendpoints;

import io.reactivex.rxjava3.core.Observable;
import org.observertc.observer.common.ObservableState;

import java.util.UUID;

public interface HamokConnection {

    UUID getConnectionId();
    HamokConnectionState getState();
    Observable<ObservableState.StateChangeEvent<HamokConnectionState>> stateChange();
    void open();
    void close();

}
