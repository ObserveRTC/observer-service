package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.events.CallMetaType;
import org.observertc.observer.samples.ObservedSamplesGenerator;
import org.observertc.observer.utils.RandomGenerators;

import java.time.Instant;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CallMetaReportsDepotTest {
    private final RandomGenerators randomGenerators = new RandomGenerators();
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private CallMetaReportsDepot depot = new CallMetaReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var payload = this.randomGenerators.getRandomString();
        var metaType = CallMetaType.ICE_LOCAL_CANDIDATE;
        var peerConnectionId = UUID.randomUUID();
        var sampleTimestamp = Instant.now().toEpochMilli();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setMetaType(metaType)
                .setPayload(payload)
                .setPeerConnectionId(peerConnectionId)
                .setSampleTimestamp(sampleTimestamp)
                .assemble();
        var clientSample = observedClientSample.getClientSample();
        var actual = depot.get().get(0);

        Assertions.assertEquals(serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(clientSample.marker, actual.marker, "marker field");
        Assertions.assertEquals(clientSample.timestamp, actual.timestamp, "timestamp field");
        Assertions.assertEquals(clientSample.callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(clientSample.roomId, actual.roomId, "roomId field");
        Assertions.assertEquals(clientSample.clientId.toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(clientSample.userId, actual.userId, "userId field");
        Assertions.assertEquals(peerConnectionId.toString(), actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(sampleTimestamp, actual.sampleTimestamp, "sampleTimestamp field");
        Assertions.assertEquals(clientSample.sampleSeq, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(metaType.name(), actual.type, "type field");
        Assertions.assertEquals(payload, actual.payload, "payload field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

    @Test
    @Order(3)
    void shouldBeCleaned() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var payload = this.randomGenerators.getRandomString();
        var metaType = CallMetaType.ICE_LOCAL_CANDIDATE;
        this.depot
                .setObservedClientSample(observedClientSample)
                .setMetaType(metaType)
                .setPayload(payload)
                .assemble();
        var clientSample = observedClientSample.getClientSample();
        var actual = depot.get().get(0);

        Assertions.assertNull(actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(clientSample.timestamp, actual.sampleTimestamp, "sampleTimestamp field");
    }
}