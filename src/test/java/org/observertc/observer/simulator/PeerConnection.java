package org.observertc.observer.simulator;

import java.util.UUID;

public interface PeerConnection {
    UUID getId();
    void addRtpSession(RtpSessionSurrogate session);
    void closeRtpSession(Long SSRC);
}
