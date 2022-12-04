package org.observertc.observer.hamokdiscovery;

import java.util.UUID;

public record HamokConnection(
        UUID connectionId,
        String remoteHost,
        int remotePort
) {

    @Override
    public String toString() {
        return String.format("connectionId: %s, remoteHost: %s, remotePort: %d", connectionId, remoteHost, remotePort);
    }

}
