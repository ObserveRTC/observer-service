package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.observertc.webrtc.observer.samples.ObservedClientSampleGenerator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class ClientSurrogate implements Supplier<ObservedClientSample> {

    @Inject
    ObservedClientSampleGenerator generator;

    @PostConstruct
    void setup() {
        this.generator
                .withClientId(UUID.randomUUID());
    }


    @Override
    public ObservedClientSample get() {
        var result = this.generator.get();
        return result;
    }

    public String getServiceId() { return this.generator.getServiceId(); }
    public String getMediaUnitId() { return this.generator.getMediaUnitId(); }
    public String getUserId() { return this.generator.getUserId(); }
    public String getRoomId() { return this.generator.getRoomId(); }
    public UUID getClientId() { return this.generator.getClientId(); }


    ClientSurrogate withServiceId(String value) {
        this.generator.withServiceId(value);
        return this;
    }

    ClientSurrogate withMediaUnitId(String value) {
        this.generator.withMediaUnitId(value);
        return this;
    }

    ClientSurrogate withUserId(String value) {
        this.generator.withUserId(value);
        return this;
    }

    ClientSurrogate withRoomId(String value) {
        this.generator.withRoomId(value);
        return this;
    }

    ClientSurrogate withClientId(UUID value) {
        this.generator.withClientId(value);
        return this;
    }
}
