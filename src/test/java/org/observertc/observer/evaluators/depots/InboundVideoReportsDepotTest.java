package org.observertc.observer.evaluators.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.utils.ObservedSamplesGenerator;
import org.observertc.observer.utils.RandomGenerators;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InboundVideoReportsDepotTest {
    private final RandomGenerators randomGenerators = new RandomGenerators();
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private InboundVideoReportsDepot depot = new InboundVideoReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID().toString();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var clientSample = observedClientSample.getClientSample();
        var label = UUID.randomUUID().toString();
        var remoteClientId = UUID.randomUUID().toString();
        var remotePeerConnectionId = UUID.randomUUID().toString();
        var remoteTrackId = UUID.randomUUID().toString();
        var remoteUserId = randomGenerators.getRandomTestUserIds();
        var expected = clientSample.inboundVideoTracks[0];
        this.depot
                .setInboundVideoTrack(expected)
                .setObservedClientSample(observedClientSample)
                .setRemoteClientId(remoteClientId)
                .setRemotePeerConnectionId(remotePeerConnectionId)
                .setPeerConnectionLabel(label)
                .setRemoteTrackId(remoteTrackId)
                .setRemoteUserId(remoteUserId)
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
        Assertions.assertEquals(label, actual.label, "label field");
        Assertions.assertEquals(expected.trackId, actual.trackId, "trackId field");
        Assertions.assertEquals(expected.sfuStreamId, actual.sfuStreamId, "sfuStreamId field");
        Assertions.assertEquals(expected.sfuSinkId, actual.sfuSinkId, "sfuSinkId field");
        Assertions.assertEquals(remoteTrackId, actual.remoteTrackId, "remoteTrackId field");
        Assertions.assertEquals(remoteUserId, actual.remoteUserId, "remoteUserId field");
        Assertions.assertEquals(expected.remoteClientId, actual.remoteClientId, "remoteClientId field");
        Assertions.assertEquals(remotePeerConnectionId, actual.remotePeerConnectionId, "remotePeerConnectionId field");
        Assertions.assertEquals(clientSample.sampleSeq, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(expected.ssrc, actual.ssrc, "ssrc field");
        Assertions.assertEquals(expected.packetsReceived, actual.packetsReceived, "packetsReceived field");
        Assertions.assertEquals(expected.packetsLost, actual.packetsLost, "packetsLost field");
        Assertions.assertEquals(expected.jitter, actual.jitter, "jitter field");
        Assertions.assertEquals(expected.framesDropped, actual.framesDropped, "framesDropped field");
        Assertions.assertEquals(expected.lastPacketReceivedTimestamp, actual.lastPacketReceivedTimestamp, "lastPacketReceivedTimestamp field");
        Assertions.assertEquals(expected.headerBytesReceived, actual.headerBytesReceived, "headerBytesReceived field");
        Assertions.assertEquals(expected.packetsDiscarded, actual.packetsDiscarded, "packetsDiscarded field");
        Assertions.assertEquals(expected.fecPacketsReceived, actual.fecPacketsReceived, "fecPacketsReceived field");
        Assertions.assertEquals(expected.fecPacketsDiscarded, actual.fecPacketsDiscarded, "fecPacketsDiscarded field");
        Assertions.assertEquals(expected.bytesReceived, actual.bytesReceived, "bytesReceived field");
        Assertions.assertEquals(expected.nackCount, actual.nackCount, "nackCount field");
        Assertions.assertEquals(expected.totalProcessingDelay, actual.totalProcessingDelay, "totalProcessingDelay field");
        Assertions.assertEquals(expected.estimatedPlayoutTimestamp, actual.estimatedPlayoutTimestamp, "estimatedPlayoutTimestamp field");
        Assertions.assertEquals(expected.jitterBufferDelay, actual.jitterBufferDelay, "jitterBufferDelay field");
        Assertions.assertEquals(expected.jitterBufferTargetDelay, actual.jitterBufferTargetDelay, "jitterBufferTargetDelay field");
        Assertions.assertEquals(expected.jitterBufferEmittedCount, actual.jitterBufferEmittedCount, "jitterBufferEmittedCount field");
        Assertions.assertEquals(expected.jitterBufferMinimumDelay, actual.jitterBufferMinimumDelay, "jitterBufferMinimumDelay field");
        Assertions.assertEquals(expected.decoderImplementation, actual.decoderImplementation, "decoderImplementation field");
        Assertions.assertEquals(expected.framesDecoded, actual.framesDecoded, "framesDecoded field");
        Assertions.assertEquals(expected.keyFramesDecoded, actual.keyFramesDecoded, "keyFramesDecoded field");
        Assertions.assertEquals(expected.frameWidth, actual.frameWidth, "frameWidth field");
        Assertions.assertEquals(expected.frameHeight, actual.frameHeight, "frameHeight field");
        Assertions.assertEquals(expected.framesPerSecond, actual.framesPerSecond, "framesPerSecond field");
        Assertions.assertEquals(expected.qpSum, actual.qpSum, "qpSum field");
        Assertions.assertEquals(expected.totalDecodeTime, actual.totalDecodeTime, "totalDecodeTime field");
        Assertions.assertEquals(expected.totalInterFrameDelay, actual.totalInterFrameDelay, "totalInterFrameDelay field");
        Assertions.assertEquals(expected.totalSquaredInterFrameDelay, actual.totalSquaredInterFrameDelay, "totalSquaredInterFrameDelay field");
        Assertions.assertEquals(expected.firCount, actual.firCount, "firCount field");
        Assertions.assertEquals(expected.pliCount, actual.pliCount, "pliCount field");
        Assertions.assertEquals(expected.framesReceived, actual.framesReceived, "framesReceived field");
        Assertions.assertEquals(expected.packetsSent, actual.packetsSent, "packetsSent field");
        Assertions.assertEquals(expected.bytesSent, actual.bytesSent, "bytesSent field");
        Assertions.assertEquals(expected.remoteTimestamp, actual.remoteTimestamp, "remoteTimestamp field");
        Assertions.assertEquals(expected.reportsSent, actual.reportsSent, "reportsSent field");
        Assertions.assertEquals(expected.roundTripTime, actual.roundTripTime, "roundTripTime field");
        Assertions.assertEquals(expected.totalRoundTripTime, actual.totalRoundTripTime, "totalRoundTripTime field");
        Assertions.assertEquals(expected.roundTripTimeMeasurements, actual.roundTripTimeMeasurements, "roundTripTimeMeasurements field");

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
        var expected = observedClientSample.getClientSample().inboundVideoTracks[0];
        this.depot
                .setInboundVideoTrack(expected)
                .setObservedClientSample(observedClientSample)
                .assemble();
        var reports = depot.get();
        var actual = reports.get(0);

        // check if the things are cleaned properly
        Assertions.assertEquals(1, reports.size());
        Assertions.assertNull( actual.label, "label field");
        Assertions.assertNull( actual.remoteTrackId, "remoteTrackId field");
        Assertions.assertNull( actual.remoteUserId, "remoteUserId field");
        Assertions.assertNull( actual.remoteClientId, "remoteClientId field");
        Assertions.assertNull( actual.remotePeerConnectionId, "remotePeerConnectionId field");
    }
}