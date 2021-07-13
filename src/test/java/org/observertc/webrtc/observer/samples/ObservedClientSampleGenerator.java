package org.observertc.webrtc.observer.samples;

import io.micronaut.context.annotation.Prototype;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.InvalidObjectException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class ObservedClientSampleGenerator implements Supplier<ObservedClientSample> {

    @Inject
    ClientSampleGenerator generator;

    private String serviceId = "serviceId";
    private String mediaUnitId = "mediaUnitId";
    private String timeZoneId = ZoneOffset.UTC.getId();

    public ObservedClientSampleGenerator() {

    }

    @PostConstruct
    void setup() {

    }

    @Override
    public ObservedClientSample get() {
        var clientSample = this.generator.get();
        
        ObservedClientSample result = null;
        try {
            result = ObservedClientSampleBuilder.from(clientSample)
                    .withServiceId(this.serviceId)
                    .withMediaUnitId(this.mediaUnitId)
                    .withTimeZoneId(this.timeZoneId)
                    .build();
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getMediaUnitId() {
        return this.mediaUnitId;
    }

    public String getUserId() {
        return this.generator.getUserId();
    }

    public String getRoomId() {
        return this.generator.getRoomId();
    }

    public UUID getClientId() {
        return this.generator.getClientId();
    }

    public String getTimeZoneId() {
        return this.timeZoneId;
    }

    public ObservedClientSampleGenerator withServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public ObservedClientSampleGenerator withMediaUnitId(String mediaUnitId) {
        this.mediaUnitId = mediaUnitId;
        return this;
    }

    public ObservedClientSampleGenerator withRoomId(String value) {
        this.generator.withRoomId(value);
        return this;
    }

    public ObservedClientSampleGenerator withUserId(String value) {
        this.generator.withUserId(value);
        return this;
    }

    public ObservedClientSampleGenerator withClientId(UUID value) {
        this.generator.withClientId(value);
        return this;
    }

    public ObservedClientSampleGenerator withTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
        return this;
    }
}
