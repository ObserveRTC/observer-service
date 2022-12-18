package org.observertc.observer.hamokendpoints;

import java.util.UUID;

public record HamokConnectionConfig(
        UUID connectionId,
        String remoteHost,
        int remotePort
) {

    @Override
    public String toString() {
        return String.format("connectionId: %s, remoteHost: %s, remotePorts: %d",
                connectionId,
                remoteHost,
                remotePort
        );
    }

}
