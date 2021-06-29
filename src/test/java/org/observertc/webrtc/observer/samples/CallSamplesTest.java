package org.observertc.webrtc.observer.samples;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.UUID;

@MicronautTest
class CallSamplesTest {

    @Inject
    ClientSamplesGenerator generator;

    private UUID callId;
    private ServiceRoomId serviceRoomId;

    @BeforeEach
    void setup() {
        this.callId = UUID.randomUUID();
        this.serviceRoomId = ServiceRoomId.make(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        this.generator
                .withServiceId(this.serviceRoomId.serviceId)
                .withRoomId(this.serviceRoomId.roomId);
    }

    @Test
    public void iterableCallSample() {
        var clientSamples = this.generator.get();

        var callSamples = CallSamples
                .builderFrom(this.callId, this.serviceRoomId)
                .withClientSamples(clientSamples)
                .build();

        Assertions.assertNotNull(callSamples.iterator());
    }

    @Test
    public void streamableCallSample() {
        var clientSamples = this.generator.get();

        var callSamples = CallSamples
                .builderFrom(this.callId, this.serviceRoomId)
                .withClientSamples(clientSamples)
                .build();

        Assertions.assertNotNull(callSamples.stream());
    }

    @Test
    public void checkPeerConnectionIds() {
        var clientSamples = this.generator.get();

        var callSamples = CallSamples
                .builderFrom(this.callId, this.serviceRoomId)
                .withClientSamples(clientSamples)
                .build();
        var pcIds = callSamples.getPeerConnectionIds();

        clientSamples.stream()
                .flatMap(c -> Arrays.stream(c.pcTransports))
                .map(c -> c.peerConnectionId)
                .forEach(pcId -> Assertions.assertNotNull(pcIds.contains(pcId)));
    }

    @Test
    public void checkClientIds() {
        var clientIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        var callSamples = CallSamples
                .builderFrom(this.callId, this.serviceRoomId)
                .withClientSamples(this.generator.withClientId(clientIds.get(0)).get())
                .withClientSamples(this.generator.withClientId(clientIds.get(1)).get())
                .build();

        var callSampleClientIds = callSamples.getClientIds();

        var allClientIdHasFound = clientIds.stream().allMatch(callSampleClientIds::contains);
        Assertions.assertTrue(allClientIdHasFound);
    }

    @Test
    public void checkCallId() {
        var callSamples = CallSamples
                .builderFrom(this.callId, this.serviceRoomId)
                .withClientSamples(this.generator.get())
                .build();

        Assertions.assertEquals(callSamples.callId, this.callId);
    }

}