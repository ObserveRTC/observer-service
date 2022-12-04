package org.observertc.observer.hamokendpoints;

import io.github.balazskreith.hamok.transports.Endpoint;

import java.util.Set;
import java.util.UUID;

public interface HamokEndpoint extends Endpoint {

    boolean isReady();

    Set<UUID> getActiveRemoteEndpointIds();

//    Observable<UUID> remoteEndpointJoined();

//    Observable<UUID> remoteEndpointDetached();
}
