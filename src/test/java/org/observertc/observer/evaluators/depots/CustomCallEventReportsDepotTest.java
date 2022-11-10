package org.observertc.observer.evaluators.depots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.ObservedSamplesGenerator;
import org.observertc.schemas.samples.Samples;

import java.time.Instant;
import java.util.UUID;

class CustomCallEventReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private CustomCallEventReportsDepot depot = new CustomCallEventReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID().toString();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var peerConnectionId = UUID.randomUUID().toString();
        var clientSample = observedClientSample.getClientSample();
        var name = UUID.randomUUID().toString();
        var timestamp = Instant.now().toEpochMilli();
        var value = UUID.randomUUID().toString();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setCustomCallEvent(Samples.ClientSample.CustomCallEvent.newBuilder()
                        .setPeerConnectionId(peerConnectionId)
                        .setName(name)
                        .setTimestamp(timestamp)
                        .setValue(value)
                        .build())
                .assemble();

        var actual = depot.get().get(0);

        Assertions.assertEquals(serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(clientSample.marker, actual.marker, "marker field");
        Assertions.assertEquals(clientSample.callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(clientSample.roomId, actual.roomId, "roomId field");
        Assertions.assertEquals(clientSample.clientId.toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(clientSample.userId, actual.userId, "userId field");
        Assertions.assertEquals(peerConnectionId.toString(), actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(clientSample.sampleSeq, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(name, actual.name, "name field");
        Assertions.assertEquals(value, actual.value, "value field");
        Assertions.assertEquals(peerConnectionId, actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(timestamp, actual.timestamp, "timestamp field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

    @Test
    @Order(3)
    void shouldBeCleaned() {
        var callId = UUID.randomUUID().toString();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var peerConnectionId = UUID.randomUUID().toString();
        var timestamp = Instant.now().toEpochMilli();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setCustomCallEvent(Samples.ClientSample.CustomCallEvent.newBuilder()
                        .setPeerConnectionId(peerConnectionId)
                        .setTimestamp(timestamp)
                        .build())
                .assemble();
        var actual = depot.get().get(0);

        Assertions.assertNull(actual.name, "name field");
        Assertions.assertNull(actual.value, "value field");
    }
}