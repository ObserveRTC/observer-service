package org.observertc.observer.evaluators.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.utils.ObservedSamplesGenerator;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IceCandidatePairReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private IceCandidatePairReportsDepot depot = new IceCandidatePairReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID().toString();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var clientSample = observedClientSample.getClientSample();
        var expected = clientSample.iceCandidatePairs[0];
        this.depot
                .setObservedClientSample(observedClientSample)
                .setIceCandidatePair(expected)
                .assemble();

        var actual = depot.get().get(0);

        Assertions.assertEquals(serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(clientSample.marker, actual.marker, "marker field");
        Assertions.assertEquals(clientSample.timestamp, actual.timestamp, "timestamp field");
        Assertions.assertEquals(clientSample.callId, actual.callId, "callId field");
        Assertions.assertEquals(clientSample.roomId, actual.roomId, "roomId field");
        Assertions.assertEquals(clientSample.clientId, actual.clientId, "clientId field");
        Assertions.assertEquals(clientSample.userId, actual.userId, "userId field");
        Assertions.assertEquals(expected.peerConnectionId, actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(expected.label, actual.label, "label field");
        Assertions.assertEquals(clientSample.sampleSeq, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(expected.transportId, actual.transportId, "transportId field");
        Assertions.assertEquals(expected.localCandidateId, actual.localCandidateId, "localCandidateId field");
        Assertions.assertEquals(expected.remoteCandidateId, actual.remoteCandidateId, "remoteCandidateId field");
        Assertions.assertEquals(expected.state, actual.state, "state field");
        Assertions.assertEquals(expected.nominated, actual.nominated, "nominated field");
        Assertions.assertEquals(expected.packetsSent, actual.packetsSent, "packetsSent field");
        Assertions.assertEquals(expected.packetsReceived, actual.packetsReceived, "packetsReceived field");
        Assertions.assertEquals(expected.bytesSent, actual.bytesSent, "bytesSent field");
        Assertions.assertEquals(expected.bytesReceived, actual.bytesReceived, "bytesReceived field");
        Assertions.assertEquals(expected.lastPacketSentTimestamp, actual.lastPacketSentTimestamp, "lastPacketSentTimestamp field");
        Assertions.assertEquals(expected.lastPacketReceivedTimestamp, actual.lastPacketReceivedTimestamp, "lastPacketReceivedTimestamp field");
        Assertions.assertEquals(expected.totalRoundTripTime, actual.totalRoundTripTime, "totalRoundTripTime field");
        Assertions.assertEquals(expected.currentRoundTripTime, actual.currentRoundTripTime, "currentRoundTripTime field");
        Assertions.assertEquals(expected.availableOutgoingBitrate, actual.availableOutgoingBitrate, "availableOutgoingBitrate field");
        Assertions.assertEquals(expected.availableIncomingBitrate, actual.availableIncomingBitrate, "availableIncomingBitrate field");
        Assertions.assertEquals(expected.requestsReceived, actual.requestsReceived, "requestsReceived field");
        Assertions.assertEquals(expected.requestsSent, actual.requestsSent, "requestsSent field");
        Assertions.assertEquals(expected.responsesReceived, actual.responsesReceived, "responsesReceived field");
        Assertions.assertEquals(expected.responsesSent, actual.responsesSent, "responsesSent field");
        Assertions.assertEquals(expected.consentRequestsSent, actual.consentRequestsSent, "consentRequestsSent field");
        Assertions.assertEquals(expected.packetsDiscardedOnSend, actual.packetsDiscardedOnSend, "packetsDiscardedOnSend field");
        Assertions.assertEquals(expected.bytesDiscardedOnSend, actual.bytesDiscardedOnSend, "bytesDiscardedOnSend field");

    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

}