package org.observertc.webrtc.observer.samples;

import io.micronaut.context.annotation.Prototype;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class ClientSamplesGenerator implements Supplier<ClientSamples> {

    @Inject
    ObservedClientSampleGenerator generator;

    private UUID clientId;
    private String roomId;
    private String serviceId;
    private Integer numOfClientSample = null;
    private Random random;

    public ClientSamplesGenerator() {
        this.random = new Random();
    }

    @PostConstruct
    void setup() {

    }

    @Override
    public ClientSamples get() {
        var observedSample = this.generator.get();
        var builder = ClientSamples.builderFrom(observedSample);
        int generationLimit = Objects.isNull(this.numOfClientSample) ? this.random.nextInt(10) : this.numOfClientSample;
        for (int i = 0; i < generationLimit; ++i) {
            var clientSample = observedSample.getClientSample();
            builder.withClientSample(clientSample);
            observedSample = this.generator.get();
        }
        return builder.build();
    }


    public ClientSamplesGenerator withNumberOfClientSample(int num) {
        this.numOfClientSample = num;
        return this;
    }


    public ClientSamplesGenerator resetIds() {
        return this
                .withClientId(UUID.randomUUID())
                .withServiceId(UUID.randomUUID().toString())
                .withRoomId(UUID.randomUUID().toString());
    }

    public ClientSamplesGenerator withClientId(UUID clientId) {
        this.clientId = clientId;
        this.generator.withClientId(this.clientId);
        return this;
    }

    public ClientSamplesGenerator withServiceId(String value) {
        this.serviceId = value;
        this.generator.withServiceId(this.serviceId);
        return this;
    }

    public ClientSamplesGenerator withRoomId(String value) {
        this.roomId = value;
        this.generator.withServiceId(this.roomId);
        return this;
    }

}
