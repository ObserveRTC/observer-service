package org.observertc.observer.evaluators.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.utils.ObservedSamplesGenerator;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PeerConnectionTransportReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private PeerConnectionTransportReportsDepot depot = new PeerConnectionTransportReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID().toString();
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
        Assertions.assertEquals(clientSample.callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(clientSample.roomId, actual.roomId, "roomId field");
        Assertions.assertEquals(clientSample.clientId.toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(clientSample.userId, actual.userId, "userId field");
        Assertions.assertEquals(expected.peerConnectionId, actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(expected.label, actual.label, "label field");
        Assertions.assertEquals(clientSample.sampleSeq, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(expected.packetsSent, actual.packetsSent, "packetsSent field");
        Assertions.assertEquals(expected.packetsReceived, actual.packetsReceived, "packetsReceived field");
        Assertions.assertEquals(expected.bytesSent, actual.bytesSent, "bytesSent field");
        Assertions.assertEquals(expected.bytesReceived, actual.bytesReceived, "bytesReceived field");
        Assertions.assertEquals(expected.iceRole, actual.iceRole, "iceRole field");
        Assertions.assertEquals(expected.iceLocalUsernameFragment, actual.iceLocalUsernameFragment, "iceLocalUsernameFragment field");
        Assertions.assertEquals(expected.dtlsState, actual.dtlsState, "dtlsState field");
        Assertions.assertEquals(expected.selectedCandidatePairId, actual.selectedCandidatePairId, "selectedCandidatePairId field");
        Assertions.assertEquals(expected.iceState, actual.iceState, "iceState field");
        Assertions.assertEquals(expected.localCertificateId, actual.localCertificateId, "localCertificateId field");
        Assertions.assertEquals(expected.remoteCertificateId, actual.remoteCertificateId, "remoteCertificateId field");
        Assertions.assertEquals(expected.tlsVersion, actual.tlsVersion, "tlsVersion field");
        Assertions.assertEquals(expected.dtlsCipher, actual.dtlsCipher, "dtlsCipher field");
        Assertions.assertEquals(expected.srtpCipher, actual.srtpCipher, "srtpCipher field");
        Assertions.assertEquals(expected.tlsGroup, actual.tlsGroup, "tlsGroup field");
        Assertions.assertEquals(expected.selectedCandidatePairChanges, actual.selectedCandidatePairChanges, "selectedCandidatePairChanges field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

}