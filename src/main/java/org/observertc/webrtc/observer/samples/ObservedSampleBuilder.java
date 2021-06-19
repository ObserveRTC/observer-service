package org.observertc.webrtc.observer.samples;

import org.observertc.webrtc.observer.dto.CallDTO;

import java.util.UUID;

public class ObservedSampleBuilder {

    public static ObservedSampleBuilder from(ClientSample sample, String serviceId, String mediaUnitId) {
        return new ObservedSampleBuilder()
                .withClientSample(sample)
                .withServiceId(serviceId)
                .withMediaUnitId(mediaUnitId);
    }

    final ObservedSample result = new ObservedSample();

    private ObservedSampleBuilder() {

    }

    public CallDTO.Builder makeCallDTOBuilder() {
        var result = CallDTO.builder()
                .withRoomId(this.result.sample.roomId)
                .withStartedTimestamp(this.result.timestamp)
                .withServiceId(this.result.serviceId)
                ;
        return result;
    }

    public String getClientId() {
        return this.result.sample.clientId;
    }

    public String getServiceId() {
        return this.result.serviceId;
    }

    public String getMediaUnitId() {
        return this.result.mediaUnitId;
    }

    public ServiceRoomId getServiceRoomId() {
        var result = ServiceRoomId.make(this.result.serviceId, this.result.sample.roomId);
        return result;
    }

    public Long getTimestamp() { return this.result.timestamp; }

    public String getRoomId() {
        return this.result.sample.roomId;
    }

    public ObservedSampleBuilder withServiceId(String value) {
        this.result.serviceId = value;
        return this;
    }

    public ObservedSampleBuilder withCallId(UUID value) {
        this.result.callId = value;
        return this;
    }

    public ObservedSampleBuilder withMediaUnitId(String value) {
        this.result.mediaUnitId = value;
        return this;
    }

    public ObservedSampleBuilder withClientSample(ClientSample value) {
        this.result.sample = value;
        return this;
    }

    public ObservedSampleBuilder withTimestamp(long value) {
        this.result.timestamp = value;
        return this;
    }

    public ObservedSampleBuilder withTimeZoneId(String value) {
        this.result.timeZoneId = value;
        return this;
    }

    public ObservedSampleBuilder withServiceName(String value) {
        this.result.serviceName = value;
        return this;
    }

    public ObservedSample build() {
        return this.result;
    }

    public String getMarker() {
        return this.result.sample.marker;
    }

    public String getUserId() {
        return this.result.sample.userId;
    }
}
