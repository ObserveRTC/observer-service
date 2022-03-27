package org.observertc.observer.simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NetworkLink {
    private final UUID linkId = UUID.randomUUID();
    private Map<Long, RtpSessionSurrogate> sessions = new HashMap<>();
    private Map<UUID, NetworkLinkEvents> listeners = new HashMap<>();

    public UUID getId() {
        return this.linkId;
    }

    public PeerConnection createPeerConnection(NetworkLinkEvents listener) {
        var peerConnectionId = UUID.randomUUID();
        this.listeners.put(peerConnectionId, listener);
        return new PeerConnection() {
            @Override
            public UUID getId() {
                return peerConnectionId;
            }

            @Override
            public void addRtpSession(RtpSessionSurrogate session) {
                sessions.put(session.SSRC, session);
                listeners.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(peerConnectionId))
                        .forEach(entry -> entry.getValue().onRtpSessionAdded(session));
            }

            @Override
            public void closeRtpSession(Long ssrc) {
                var session = sessions.remove(ssrc);
                if (Objects.isNull(session)) return;
                listeners.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(peerConnectionId))
                        .forEach(entry -> entry.getValue().onRtpSessionRemoved(session));
            }
        };
    }
}
