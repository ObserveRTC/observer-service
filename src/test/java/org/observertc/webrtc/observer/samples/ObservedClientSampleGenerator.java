package org.observertc.webrtc.observer.samples;

import io.micronaut.context.annotation.Prototype;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class ObservedClientSampleGenerator implements Supplier<ObservedClientSample> {

    @Inject
    ClientSampleGenerator clientSampleGenerator;

    private UUID clientId = null;
    private String serviceId = null;
    private String roomId = null;
    private String mediaUnitId = null;

    public ObservedClientSampleGenerator() {

    }

    @PostConstruct
    void setup() {

    }

    @Override
    public ObservedClientSample get() {
        var clientSample = this.clientSampleGenerator.get();
        if (Objects.nonNull(this.clientId)) {
            clientSample.clientId = this.clientId.toString();
        }
        var result = ObservedClientSampleBuilder.from(clientSample)
                .withTimeZoneId("UTC")
                .withServiceId(this.serviceId)
                .withMediaUnitId(this.mediaUnitId)
                .build();
        return result;
    }

    public ObservedClientSampleGenerator withClientId(UUID clientId) {
        this.clientId = clientId;
        return this;
    }

    public ObservedClientSampleGenerator reset() {
        this.clientId = null;
        return this;
    }

    public ObservedClientSampleGenerator withServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public ObservedClientSampleGenerator withMediaUnitId(String mediaUnitId) {
        this.mediaUnitId = mediaUnitId;
        return this;
    }

    public ObservedClientSampleGenerator withRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }
}
