package org.observertc.webrtc.observer.samples;

import java.util.UUID;

public interface ObservedSfuSample {
    String getMediaUnitId();

    String getTimeZoneId();

    Long getTimestamp();

    UUID getSfuId();

    SfuSample getSfuSample();

    String getMarker();
}
