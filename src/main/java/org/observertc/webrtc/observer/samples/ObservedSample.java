package org.observertc.webrtc.observer.samples;

import java.util.UUID;

public interface ObservedSample {

    String getServiceId();

    String getMediaUnitId();

    UUID getClientId();

    String getTimeZoneId();

    Long getTimestamp();

    String getRoomId();
}
