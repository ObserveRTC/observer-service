package org.observertc.observer.simulator;

public interface NetworkLinkEvents {
    void onRtpSessionAdded(RtpSessionSurrogate session);
    void onRtpSessionRemoved(RtpSessionSurrogate session);
}
