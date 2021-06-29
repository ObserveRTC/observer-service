package org.observertc.webrtc.observer.samples;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

@MicronautTest
class RoomSamplesTest {

    @Inject
    ObservedClientSampleGenerator generator;

    private ServiceRoomId serviceRoomId;

    @BeforeEach
    void setup() {
        this.serviceRoomId = ServiceRoomId.make(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        this.generator
                .withServiceId(this.serviceRoomId.serviceId)
                .withRoomId(this.serviceRoomId.roomId)
                ;
    }

    @Test
    public void iterableClientSamples() {

        var roomSamples = RoomSamples
                .builderFrom(this.serviceRoomId)
                .build();

        Assertions.assertNotNull(roomSamples.iterator());
    }

    @Test
    public void streamableClientSamples() {

        var roomSamples = RoomSamples
                .builderFrom(this.serviceRoomId)
                .build();

        Assertions.assertNotNull(roomSamples.stream());
    }

    @Test
    public void getServiceRoomId() {
        var observedClientSample = this.generator.get();

        var roomSamples = RoomSamples
                .builderFrom(this.serviceRoomId)
                .withObservedClientSample(observedClientSample)
                .build();

        Assertions.assertEquals(this.serviceRoomId, roomSamples.getServiceRoomId());
    }

    @Test
    public void getClientSamples() {
        var observedClientSample = this.generator.get();

        var roomSamples = RoomSamples
                .builderFrom(this.serviceRoomId)
                .withObservedClientSample(observedClientSample)
                .build();

        roomSamples
                .stream()
                .flatMap(ClientSamples::stream)
                .forEach(clientSample -> {
                    Assertions.assertEquals(clientSample, observedClientSample.getClientSample());
                });
    }

}