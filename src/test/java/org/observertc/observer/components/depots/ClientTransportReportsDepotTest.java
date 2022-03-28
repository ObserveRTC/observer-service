package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ObservedSamplesGenerator;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientTransportReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private ClientTransportReportsDepot depot = new ClientTransportReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var clientSample = observedClientSample.getClientSample();
        var expected = clientSample.pcTransports[0];
        this.depot
                .setObservedClientSample(observedClientSample)
                .setPeerConnectionTransport(expected)
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
        Assertions.assertEquals(expected.packetsSent, actual.packetsSent, "packetsSent field");
        Assertions.assertEquals(expected.packetsReceived, actual.packetsReceived, "packetsReceived field");
        Assertions.assertEquals(expected.bytesSent, actual.bytesSent, "bytesSent field");
        Assertions.assertEquals(expected.bytesReceived, actual.bytesReceived, "bytesReceived field");
        Assertions.assertEquals(expected.iceRole, actual.iceRole, "iceRole field");
        Assertions.assertEquals(expected.iceLocalUsernameFragment, actual.iceLocalUsernameFragment, "iceLocalUsernameFragment field");
        Assertions.assertEquals(expected.dtlsState, actual.dtlsState, "dtlsState field");
        Assertions.assertEquals(expected.iceState, actual.iceTransportState, "iceTransportState field");
        Assertions.assertEquals(expected.tlsVersion, actual.tlsVersion, "tlsVersion field");
        Assertions.assertEquals(expected.dtlsCipher, actual.dtlsCipher, "dtlsCipher field");
        Assertions.assertEquals(expected.srtpCipher, actual.srtpCipher, "srtpCipher field");
        Assertions.assertEquals(expected.tlsGroup, actual.tlsGroup, "tlsGroup field");
        Assertions.assertEquals(expected.selectedCandidatePairChanges, actual.selectedCandidatePairChanges, "selectedCandidatePairChanges field");
        Assertions.assertEquals(expected.localAddress, actual.localAddress, "localAddress field");
        Assertions.assertEquals(expected.localPort, actual.localPort, "localPort field");
        Assertions.assertEquals(expected.localProtocol, actual.localProtocol, "localProtocol field");
        Assertions.assertEquals(expected.localCandidateType, actual.localCandidateType, "localCandidateType field");
        Assertions.assertEquals(expected.localCandidateICEServerUrl, actual.localCandidateICEServerUrl, "localCandidateICEServerUrl field");
        Assertions.assertEquals(expected.localCandidateRelayProtocol, actual.localCandidateRelayProtocol, "localCandidateRelayProtocol field");
        Assertions.assertEquals(expected.remoteAddress, actual.remoteAddress, "remoteAddress field");
        Assertions.assertEquals(expected.remotePort, actual.remotePort, "remotePort field");
        Assertions.assertEquals(expected.remoteProtocol, actual.remoteProtocol, "remoteProtocol field");
        Assertions.assertEquals(expected.remoteCandidateType, actual.remoteCandidateType, "remoteCandidateType field");
        Assertions.assertEquals(expected.remoteCandidateICEServerUrl, actual.remoteCandidateICEServerUrl, "remoteCandidateICEServerUrl field");
        Assertions.assertEquals(expected.remoteCandidateRelayProtocol, actual.remoteCandidateRelayProtocol, "remoteCandidateRelayProtocol field");
        Assertions.assertEquals(expected.candidatePairState, actual.candidatePairState, "candidatePairState field");
        Assertions.assertEquals(expected.candidatePairPacketsSent, actual.candidatePairPacketsSent, "candidatePairPacketsSent field");
        Assertions.assertEquals(expected.candidatePairPacketsReceived, actual.candidatePairPacketsReceived, "candidatePairPacketsReceived field");
        Assertions.assertEquals(expected.candidatePairBytesSent, actual.candidatePairBytesSent, "candidatePairBytesSent field");
        Assertions.assertEquals(expected.candidatePairBytesReceived, actual.candidatePairBytesReceived, "candidatePairBytesReceived field");
        Assertions.assertEquals(expected.candidatePairLastPacketSentTimestamp, actual.candidatePairLastPacketSentTimestamp, "candidatePairLastPacketSentTimestamp field");
        Assertions.assertEquals(expected.candidatePairLastPacketReceivedTimestamp, actual.candidatePairLastPacketReceivedTimestamp, "candidatePairLastPacketReceivedTimestamp field");
        Assertions.assertEquals(expected.candidatePairFirstRequestTimestamp, actual.candidatePairFirstRequestTimestamp, "candidatePairFirstRequestTimestamp field");
        Assertions.assertEquals(expected.candidatePairLastRequestTimestamp, actual.candidatePairLastRequestTimestamp, "candidatePairLastRequestTimestamp field");
        Assertions.assertEquals(expected.candidatePairLastResponseTimestamp, actual.candidatePairLastResponseTimestamp, "candidatePairLastResponseTimestamp field");
        Assertions.assertEquals(expected.candidatePairTotalRoundTripTime, actual.candidatePairTotalRoundTripTime, "candidatePairTotalRoundTripTime field");
        Assertions.assertEquals(expected.candidatePairCurrentRoundTripTime, actual.candidatePairCurrentRoundTripTime, "candidatePairCurrentRoundTripTime field");
        Assertions.assertEquals(expected.candidatePairAvailableOutgoingBitrate, actual.candidatePairAvailableOutgoingBitrate, "candidatePairAvailableOutgoingBitrate field");
        Assertions.assertEquals(expected.candidatePairAvailableIncomingBitrate, actual.candidatePairAvailableIncomingBitrate, "candidatePairAvailableIncomingBitrate field");
        Assertions.assertEquals(expected.candidatePairCircuitBreakerTriggerCount, actual.candidatePairCircuitBreakerTriggerCount, "candidatePairCircuitBreakerTriggerCount field");
        Assertions.assertEquals(expected.candidatePairRequestsReceived, actual.candidatePairRequestsReceived, "candidatePairRequestsReceived field");
        Assertions.assertEquals(expected.candidatePairRequestsSent, actual.candidatePairRequestsSent, "candidatePairRequestsSent field");
        Assertions.assertEquals(expected.candidatePairResponsesReceived, actual.candidatePairResponsesReceived, "candidatePairResponsesReceived field");
        Assertions.assertEquals(expected.candidatePairResponsesSent, actual.candidatePairResponsesSent, "candidatePairResponsesSent field");
        Assertions.assertEquals(expected.candidatePairRetransmissionReceived, actual.candidatePairRetransmissionReceived, "candidatePairRetransmissionReceived field");
        Assertions.assertEquals(expected.candidatePairRetransmissionSent, actual.candidatePairRetransmissionSent, "candidatePairRetransmissionSent field");
        Assertions.assertEquals(expected.candidatePairConsentRequestsSent, actual.candidatePairConsentRequestsSent, "candidatePairConsentRequestsSent field");
        Assertions.assertEquals(expected.candidatePairConsentExpiredTimestamp, actual.candidatePairConsentExpiredTimestamp, "candidatePairConsentExpiredTimestamp field");
        Assertions.assertEquals(expected.candidatePairBytesDiscardedOnSend, actual.candidatePairBytesDiscardedOnSend, "candidatePairBytesDiscardedOnSend field");
        Assertions.assertEquals(expected.candidatePairPacketsDiscardedOnSend, actual.candidatePairPacketsDiscardedOnSend, "candidatePairPacketsDiscardedOnSend field");
        Assertions.assertEquals(expected.candidatePairRequestBytesSent, actual.candidatePairRequestBytesSent, "candidatePairRequestBytesSent field");
        Assertions.assertEquals(expected.candidatePairConsentRequestBytesSent, actual.candidatePairConsentRequestBytesSent, "candidatePairConsentRequestBytesSent field");
        Assertions.assertEquals(expected.candidatePairResponseBytesSent, actual.candidatePairResponseBytesSent, "candidatePairResponseBytesSent field");
        Assertions.assertEquals(expected.sctpSmoothedRoundTripTime, actual.sctpSmoothedRoundTripTime, "sctpSmoothedRoundTripTime field");
        Assertions.assertEquals(expected.sctpCongestionWindow, actual.sctpCongestionWindow, "sctpCongestionWindow field");
        Assertions.assertEquals(expected.sctpReceiverWindow, actual.sctpReceiverWindow, "sctpReceiverWindow field");
        Assertions.assertEquals(expected.sctpMtu, actual.sctpMtu, "sctpMtu field");
        Assertions.assertEquals(expected.sctpUnackData, actual.sctpUnackData, "sctpUnackData field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

}