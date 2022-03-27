package org.observertc.observer.simulator;

import java.util.UUID;

public interface PeerConnection {
    UUID getId();
    void addRtpSession(RtpSessionSurrogate session);
//    RtpSessionSurrogate createVideoSession();
//    RtpSessionSurrogate createAudioSession();
    void closeRtpSession(Long SSRC);
}
