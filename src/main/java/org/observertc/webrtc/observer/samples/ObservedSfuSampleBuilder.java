package org.observertc.webrtc.observer.samples;

import javax.validation.constraints.NotNull;
import java.io.InvalidObjectException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ObservedSfuSampleBuilder {

    public static ObservedSfuSampleBuilder from(SfuSample sfuSample) {
        return new ObservedSfuSampleBuilder().withSfuSample(sfuSample);
    }

    private String mediaUnitId;
    private SfuSample sfuSample;
    private UUID sfuId;
    private String timeZoneId;

    private ObservedSfuSampleBuilder() {

    }

    public ObservedSfuSampleBuilder withMediaUnitId(String value) {
        this.mediaUnitId = value;
        return this;
    }

    private ObservedSfuSampleBuilder withSfuSample(SfuSample value) {
        this.sfuSample = value;
        this.sfuId = UUID.fromString(value.sfuId);
        return this;
    }

    public ObservedSfuSampleBuilder withTimeZoneId(String value) {
        this.timeZoneId = value;
        return this;
    }

    public boolean isValid(@NotNull AtomicReference<String> message) {
        if (Objects.isNull(this.sfuSample)) {
            message.set("SfuSample is null");
            return false;
        }
        if (Objects.isNull(this.sfuSample.sfuId)) {
            message.set("sfuId in sfu sample is null");
            return false;
        }

        return true;
    }

    public ObservedSfuSample build() throws InvalidObjectException {
        AtomicReference<String> message = new AtomicReference<>(null);
        if (!this.isValid(message)) {
            throw new InvalidObjectException(message.get());
        }

        return new ObservedSfuSample() {

            @Override
            public String getMediaUnitId() {
                return mediaUnitId;
            }

            @Override
            public UUID getSfuId() {
                return sfuId;
            }

            @Override
            public SfuSample getSfuSample() {
                return sfuSample;
            }

            @Override
            public String getMarker() {
                return sfuSample.marker;
            }

            @Override
            public String getTimeZoneId() {
                return timeZoneId;
            }

            @Override
            public Long getTimestamp() {
                return sfuSample.timestamp;
            }

        };
    }
}
