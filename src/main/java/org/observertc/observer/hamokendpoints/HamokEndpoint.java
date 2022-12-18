package org.observertc.observer.hamokendpoints;

import io.github.balazskreith.hamok.transports.Endpoint;
import io.reactivex.rxjava3.core.Observable;
import org.observertc.observer.common.ObservableState;

import java.util.Set;
import java.util.UUID;

public interface HamokEndpoint extends Endpoint {

    Set<UUID> getActiveRemoteEndpointIds();
    Observable<ObservableState.StateChangeEvent<HamokEndpointState>> stateChanged();
    void addConnection(HamokConnectionConfig connectionConfig);
    void removeConnection(UUID connectionId);
    void removeConnectionByEndpointId(UUID endpointId);
}
