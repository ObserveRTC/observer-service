package org.observertc.observer.samples;

import java.util.UUID;

public interface ObservedCall {
    UUID getCallId();

    ServiceRoomId getServiceRoomId();

}
