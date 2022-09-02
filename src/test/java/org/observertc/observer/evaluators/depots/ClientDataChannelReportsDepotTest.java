package org.observertc.observer.evaluators.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.utils.ObservedSamplesGenerator;
import org.observertc.observer.utils.RandomGenerators;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientDataChannelReportsDepotTest {
    private final RandomGenerators randomGenerators = new RandomGenerators();
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private ClientDataChannelReportsDepot depot = new ClientDataChannelReportsDepot();


    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID().toString();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var peerConnectionLabel = UUID.randomUUID().toString();
        var clientSample = observedClientSample.getClientSample();
        var expected = clientSample.dataChannels[0];
        this.depot
                .setObservedClientSample(observedClientSample)
                .setDataChannel(expected)
                .setPeerConnectionLabel(peerConnectionLabel)
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
        Assertions.assertEquals(expected.peerConnectionId.toString(), actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(peerConnectionLabel, actual.peerConnectionLabel, "peerConnectionLabel field");
        Assertions.assertEquals(clientSample.sampleSeq, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(expected.label, actual.label, "label field");
        Assertions.assertEquals(expected.protocol, actual.protocol, "protocol field");
        Assertions.assertEquals(expected.state, actual.state, "state field");
        Assertions.assertEquals(expected.messagesSent, actual.messagesSent, "messagesSent field");
        Assertions.assertEquals(expected.bytesSent, actual.bytesSent, "bytesSent field");
        Assertions.assertEquals(expected.messagesReceived, actual.messagesReceived, "messagesReceived field");
        Assertions.assertEquals(expected.bytesReceived, actual.bytesReceived, "bytesReceived field");
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
        var clientSample = observedClientSample.getClientSample();
        var dataChannel = clientSample.dataChannels[0];
        this.depot
                .setObservedClientSample(observedClientSample)
                .setDataChannel(dataChannel)
                .assemble();
        var actual = depot.get().get(0);

        Assertions.assertNull(actual.peerConnectionLabel, "peerConnectionId field");
    }

}