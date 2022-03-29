package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ObservedSamplesGenerator;
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
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var clientSample = observedClientSample.getClientSample();
        var label = UUID.randomUUID().toString();
        var remoteClientId = UUID.randomUUID();
        var remotePeerConnectionId = UUID.randomUUID();
        var remoteTrackId = UUID.randomUUID();
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
        Assertions.assertEquals(expected.peerConnectionId.toString(), actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(label, actual.label, "label field");
        Assertions.assertEquals(expected.trackId.toString(), actual.trackId, "trackId field");
        Assertions.assertEquals(null, actual.sfuStreamId, "sfuStreamId field");
        Assertions.assertEquals(expected.sfuSinkId.toString(), actual.sfuSinkId, "sfuSinkId field");
        Assertions.assertEquals(remoteTrackId.toString(), actual.remoteTrackId, "remoteTrackId field");
        Assertions.assertEquals(remoteUserId, actual.remoteUserId, "remoteUserId field");
        Assertions.assertEquals(remoteClientId.toString(), actual.remoteClientId, "remoteClientId field");
        Assertions.assertEquals(remotePeerConnectionId.toString(), actual.remotePeerConnectionId, "remotePeerConnectionId field");
        Assertions.assertEquals(clientSample.sampleSeq, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(expected.ssrc, actual.ssrc, "ssrc field");
        Assertions.assertEquals(expected.packetsReceived, actual.packetsReceived, "packetsReceived field");
        Assertions.assertEquals(expected.packetsLost, actual.packetsLost, "packetsLost field");
        Assertions.assertEquals(expected.jitter, actual.jitter, "jitter field");
        Assertions.assertEquals(expected.packetsDiscarded, actual.packetsDiscarded, "packetsDiscarded field");
        Assertions.assertEquals(expected.packetsRepaired, actual.packetsRepaired, "packetsRepaired field");
        Assertions.assertEquals(expected.burstPacketsLost, actual.burstPacketsLost, "burstPacketsLost field");
        Assertions.assertEquals(expected.burstPacketsDiscarded, actual.burstPacketsDiscarded, "burstPacketsDiscarded field");
        Assertions.assertEquals(expected.burstLossCount, actual.burstLossCount, "burstLossCount field");
        Assertions.assertEquals(expected.burstDiscardCount, actual.burstDiscardCount, "burstDiscardCount field");
        Assertions.assertEquals(expected.burstLossRate, actual.burstLossRate, "burstLossRate field");
        Assertions.assertEquals(expected.burstDiscardRate, actual.burstDiscardRate, "burstDiscardRate field");
        Assertions.assertEquals(expected.gapLossRate, actual.gapLossRate, "gapLossRate field");
        Assertions.assertEquals(expected.gapDiscardRate, actual.gapDiscardRate, "gapDiscardRate field");
        Assertions.assertEquals(expected.framesDropped, actual.framesDropped, "framesDropped field");
        Assertions.assertEquals(expected.partialFramesLost, actual.partialFramesLost, "partialFramesLost field");
        Assertions.assertEquals(expected.fullFramesLost, actual.fullFramesLost, "fullFramesLost field");
        Assertions.assertEquals(expected.framesDecoded, actual.framesDecoded, "framesDecoded field");
        Assertions.assertEquals(expected.keyFramesDecoded, actual.keyFramesDecoded, "keyFramesDecoded field");
        Assertions.assertEquals(expected.frameWidth, actual.frameWidth, "frameWidth field");
        Assertions.assertEquals(expected.frameHeight, actual.frameHeight, "frameHeight field");
        Assertions.assertEquals(expected.frameBitDepth, actual.frameBitDepth, "frameBitDepth field");
        Assertions.assertEquals(expected.framesPerSecond, actual.framesPerSecond, "framesPerSecond field");
        Assertions.assertEquals(expected.qpSum, actual.qpSum, "qpSum field");
        Assertions.assertEquals(expected.totalDecodeTime, actual.totalDecodeTime, "totalDecodeTime field");
        Assertions.assertEquals(expected.totalInterFrameDelay, actual.totalInterFrameDelay, "totalInterFrameDelay field");
        Assertions.assertEquals(expected.totalSquaredInterFrameDelay, actual.totalSquaredInterFrameDelay, "totalSquaredInterFrameDelay field");
        Assertions.assertEquals(expected.lastPacketReceivedTimestamp, actual.lastPacketReceivedTimestamp, "lastPacketReceivedTimestamp field");
        Assertions.assertEquals(expected.averageRtcpInterval, actual.averageRtcpInterval, "averageRtcpInterval field");
        Assertions.assertEquals(expected.headerBytesReceived, actual.headerBytesReceived, "headerBytesReceived field");
        Assertions.assertEquals(expected.fecPacketsReceived, actual.fecPacketsReceived, "fecPacketsReceived field");
        Assertions.assertEquals(expected.fecPacketsDiscarded, actual.fecPacketsDiscarded, "fecPacketsDiscarded field");
        Assertions.assertEquals(expected.bytesReceived, actual.bytesReceived, "bytesReceived field");
        Assertions.assertEquals(expected.packetsFailedDecryption, actual.packetsFailedDecryption, "packetsFailedDecryption field");
        Assertions.assertEquals(expected.packetsDuplicated, actual.packetsDuplicated, "packetsDuplicated field");
        Assertions.assertEquals(expected.perDscpPacketsReceived, actual.perDscpPacketsReceived, "perDscpPacketsReceived field");
        Assertions.assertEquals(expected.firCount, actual.firCount, "firCount field");
        Assertions.assertEquals(expected.pliCount, actual.pliCount, "pliCount field");
        Assertions.assertEquals(expected.nackCount, actual.nackCount, "nackCount field");
        Assertions.assertEquals(expected.sliCount, actual.sliCount, "sliCount field");
        Assertions.assertEquals(expected.totalProcessingDelay, actual.totalProcessingDelay, "totalProcessingDelay field");
        Assertions.assertEquals(expected.estimatedPlayoutTimestamp, actual.estimatedPlayoutTimestamp, "estimatedPlayoutTimestamp field");
        Assertions.assertEquals(expected.jitterBufferDelay, actual.jitterBufferDelay, "jitterBufferDelay field");
        Assertions.assertEquals(expected.jitterBufferEmittedCount, actual.jitterBufferEmittedCount, "jitterBufferEmittedCount field");
        Assertions.assertEquals(expected.framesReceived, actual.framesReceived, "framesReceived field");
        Assertions.assertEquals(expected.decoderImplementation, actual.decoderImplementation, "decoderImplementation field");
        Assertions.assertEquals(expected.packetsSent, actual.packetsSent, "packetsSent field");
        Assertions.assertEquals(expected.bytesSent, actual.bytesSent, "bytesSent field");
        Assertions.assertEquals(expected.remoteTimestamp, actual.remoteTimestamp, "remoteTimestamp field");
        Assertions.assertEquals(expected.reportsSent, actual.reportsSent, "reportsSent field");
        Assertions.assertEquals(expected.ended, actual.ended, "ended field");
        Assertions.assertEquals(expected.payloadType, actual.payloadType, "payloadType field");
        Assertions.assertEquals(expected.mimeType, actual.mimeType, "mimeType field");
        Assertions.assertEquals(expected.clockRate, actual.clockRate, "clockRate field");
        Assertions.assertEquals(expected.sdpFmtpLine, actual.sdpFmtpLine, "sdpFmtpLine field");
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