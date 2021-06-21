package org.observertc.webrtc.observer.samples;

import java.util.Objects;
import java.util.UUID;

public class ObservedClientSampleBuilder {

    public static ObservedClientSampleBuilder from(ClientSample clientSample) {
        return new ObservedClientSampleBuilder().withClientSample(clientSample);
    }

    private String serviceId;
    private String mediaUnitId;
    private ClientSample clientSample;
    private UUID clientId;
    private String timeZoneId;

    private ObservedClientSampleBuilder() {

    }

    public ObservedClientSampleBuilder withServiceId(String value) {
        this.serviceId = value;
        return this;
    }

    public ObservedClientSampleBuilder withMediaUnitId(String value) {
        this.mediaUnitId = value;
        return this;
    }

    public ObservedClientSampleBuilder withClientSample(ClientSample value) {
        this.clientSample = value;
        this.clientId = UUID.fromString(value.clientId);
        return this;
    }

    public ObservedClientSampleBuilder withTimeZoneId(String value) {
        this.timeZoneId = value;
        return this;
    }

    public ObservedClientSample build() {
        Objects.requireNonNull(this.clientSample);
        Objects.requireNonNull(this.clientSample.clientId);
        Objects.requireNonNull(this.clientId);
        Objects.requireNonNull(this.clientSample.timestamp);
        Objects.requireNonNull(this.serviceId);
        Objects.requireNonNull(this.clientSample.roomId);
        return new ObservedClientSample() {
            @Override
            public String getServiceId() {
                return serviceId;
            }

            @Override
            public String getMediaUnitId() {
                return mediaUnitId;
            }

            @Override
            public UUID getClientId() {
                return clientId;
            }

            @Override
            public ClientSample getClientSample() {
                return clientSample;
            }

            @Override
            public String getUserId() {
                return clientSample.userId;
            }

            @Override
            public String getMarker() {
                // TODO: add marker to ClientSample
                return "NOT IMPLEMENTED";
            }

            @Override
            public String getTimeZoneId() {
                return timeZoneId;
            }

            @Override
            public Long getTimestamp() {
                return clientSample.timestamp;
            }

            @Override
            public String getRoomId() {
                return clientSample.roomId;
            }
        };
    }
}
