package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples.SfuSample;

import java.util.UUID;

public interface ObservedSfuSample {
    String getMediaUnitId();

    String getTimeZoneId();

    Long getTimestamp();

    UUID getSfuId();

    String getServiceId();

    SfuSample getSfuSample();

    String getMarker();
}
