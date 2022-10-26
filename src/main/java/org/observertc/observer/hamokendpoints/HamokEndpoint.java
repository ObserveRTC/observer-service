package org.observertc.observer.hamokendpoints;

import io.github.balazskreith.hamok.transports.Endpoint;
import io.reactivex.rxjava3.core.Observable;

import java.util.UUID;

public interface HamokEndpoint extends Endpoint {

    boolean isReady();

    int elapsedSecSinceReady();

    Observable<UUID> remoteEndpointJoined();

    Observable<UUID> remoteEndpointDetached();
}
