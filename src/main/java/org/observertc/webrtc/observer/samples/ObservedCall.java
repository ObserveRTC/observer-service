package org.observertc.webrtc.observer.samples;

import java.util.UUID;

public interface ObservedCall {
    UUID getCallId();

    ServiceRoomId getServiceRoomId();

}
