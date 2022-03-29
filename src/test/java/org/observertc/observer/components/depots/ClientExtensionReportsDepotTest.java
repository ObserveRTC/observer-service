package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ObservedSamplesGenerator;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientExtensionReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private ClientExtensionReportsDepot depot = new ClientExtensionReportsDepot();


    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var peerConnectionId = UUID.randomUUID();
        var clientSample = observedClientSample.getClientSample();
        var extensionType = UUID.randomUUID().toString();
        var payload = UUID.randomUUID().toString();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setPayload(payload)
                .setPeerConnectionId(peerConnectionId)
                .setExtensionType(extensionType)
                .assemble();

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
        Assertions.assertEquals(clientSample.sampleSeq, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(extensionType, actual.extensionType, "extensionType field");
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
        var extensionType = UUID.randomUUID().toString();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setExtensionType(extensionType)
                .assemble();
        var actual = depot.get().get(0);

        Assertions.assertNull(actual.payload, "payload field");
        Assertions.assertNull(actual.peerConnectionId, "peerConnectionId field");
    }
}