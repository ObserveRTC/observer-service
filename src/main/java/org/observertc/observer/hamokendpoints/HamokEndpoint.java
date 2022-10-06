package org.observertc.observer.hamokendpoints;

import io.github.balazskreith.hamok.transports.Endpoint;

public interface HamokEndpoint extends Endpoint {

    boolean isReady();

    int elapsedSecSinceReady();
}
