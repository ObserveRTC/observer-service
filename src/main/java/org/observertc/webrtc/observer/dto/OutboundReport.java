package org.observertc.webrtc.observer.dto;

public interface OutboundReport {
    enum Type {

    }

    Type getType();
    byte[] getBytes();
}
