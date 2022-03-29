package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ObservedSamplesGenerator;
import org.observertc.observer.utils.RandomGenerators;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OutboundVideoReportsDepotTest {
    private final RandomGenerators randomGenerators = new RandomGenerators();
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private OutboundVideoReportsDepot depot = new OutboundVideoReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var clientSample = observedClientSample.getClientSample();
        var label = UUID.randomUUID().toString();
        var expected = clientSample.outboundVideoTracks[0];
        this.depot
                .setOutboundVideoTrack(expected)
                .setObservedClientSample(observedClientSample)
                .setPeerConnectionLabel(label)
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
        Assertions.assertEquals(expected.sfuStreamId.toString(), actual.sfuStreamId, "sfuStreamId field");
        Assertions.assertEquals(clientSample.sampleSeq, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(expected.ssrc, actual.ssrc, "ssrc field");
        Assertions.assertEquals(expected.packetsSent, actual.packetsSent, "packetsSent field");
        Assertions.assertEquals(expected.bytesSent, actual.bytesSent, "bytesSent field");
        Assertions.assertEquals(expected.rid, actual.rid, "rid field");
        Assertions.assertEquals(expected.lastPacketSentTimestamp, actual.lastPacketSentTimestamp, "lastPacketSentTimestamp field");
        Assertions.assertEquals(expected.headerBytesSent, actual.headerBytesSent, "headerBytesSent field");
        Assertions.assertEquals(expected.packetsDiscardedOnSend, actual.packetsDiscardedOnSend, "packetsDiscardedOnSend field");
        Assertions.assertEquals(expected.bytesDiscardedOnSend, actual.bytesDiscardedOnSend, "bytesDiscardedOnSend field");
        Assertions.assertEquals(expected.fecPacketsSent, actual.fecPacketsSent, "fecPacketsSent field");
        Assertions.assertEquals(expected.retransmittedPacketsSent, actual.retransmittedPacketsSent, "retransmittedPacketsSent field");
        Assertions.assertEquals(expected.retransmittedBytesSent, actual.retransmittedBytesSent, "retransmittedBytesSent field");
        Assertions.assertEquals(expected.targetBitrate, actual.targetBitrate, "targetBitrate field");
        Assertions.assertEquals(expected.totalEncodedBytesTarget, actual.totalEncodedBytesTarget, "totalEncodedBytesTarget field");
        Assertions.assertEquals(expected.frameWidth, actual.frameWidth, "frameWidth field");
        Assertions.assertEquals(expected.frameHeight, actual.frameHeight, "frameHeight field");
        Assertions.assertEquals(expected.frameBitDepth, actual.frameBitDepth, "frameBitDepth field");
        Assertions.assertEquals(expected.framesPerSecond, actual.framesPerSecond, "framesPerSecond field");
        Assertions.assertEquals(expected.framesSent, actual.framesSent, "framesSent field");
        Assertions.assertEquals(expected.hugeFramesSent, actual.hugeFramesSent, "hugeFramesSent field");
        Assertions.assertEquals(expected.framesEncoded, actual.framesEncoded, "framesEncoded field");
        Assertions.assertEquals(expected.keyFramesEncoded, actual.keyFramesEncoded, "keyFramesEncoded field");
        Assertions.assertEquals(expected.framesDiscardedOnSend, actual.framesDiscardedOnSend, "framesDiscardedOnSend field");
        Assertions.assertEquals(expected.qpSum, actual.qpSum, "qpSum field");
        Assertions.assertEquals(expected.totalEncodeTime, actual.totalEncodeTime, "totalEncodeTime field");
        Assertions.assertEquals(expected.totalPacketSendDelay, actual.totalPacketSendDelay, "totalPacketSendDelay field");
        Assertions.assertEquals(expected.averageRtcpInterval, actual.averageRtcpInterval, "averageRtcpInterval field");
        Assertions.assertEquals(expected.qualityLimitationDurationCPU, actual.qualityLimitationDurationCPU, "qualityLimitationDurationCPU field");
        Assertions.assertEquals(expected.qualityLimitationDurationNone, actual.qualityLimitationDurationNone, "qualityLimitationDurationNone field");
        Assertions.assertEquals(expected.qualityLimitationDurationBandwidth, actual.qualityLimitationDurationBandwidth, "qualityLimitationDurationBandwidth field");
        Assertions.assertEquals(expected.qualityLimitationDurationOther, actual.qualityLimitationDurationOther, "qualityLimitationDurationOther field");
        Assertions.assertEquals(expected.qualityLimitationReason, actual.qualityLimitationReason, "qualityLimitationReason field");
        Assertions.assertEquals(expected.qualityLimitationResolutionChanges, actual.qualityLimitationResolutionChanges, "qualityLimitationResolutionChanges field");
        Assertions.assertEquals(expected.perDscpPacketsSent, actual.perDscpPacketsSent, "perDscpPacketsSent field");
        Assertions.assertEquals(expected.nackCount, actual.nackCount, "nackCount field");
        Assertions.assertEquals(expected.firCount, actual.firCount, "firCount field");
        Assertions.assertEquals(expected.pliCount, actual.pliCount, "pliCount field");
        Assertions.assertEquals(expected.sliCount, actual.sliCount, "sliCount field");
        Assertions.assertEquals(expected.encoderImplementation, actual.encoderImplementation, "encoderImplementation field");
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
        Assertions.assertEquals(expected.roundTripTime, actual.roundTripTime, "roundTripTime field");
        Assertions.assertEquals(expected.totalRoundTripTime, actual.totalRoundTripTime, "totalRoundTripTime field");
        Assertions.assertEquals(expected.fractionLost, actual.fractionLost, "fractionLost field");
        Assertions.assertEquals(expected.reportsReceived, actual.reportsReceived, "reportsReceived field");
        Assertions.assertEquals(expected.roundTripTimeMeasurements, actual.roundTripTimeMeasurements, "roundTripTimeMeasurements field");
        Assertions.assertEquals(expected.relayedSource, actual.relayedSource, "relayedSource field");
        Assertions.assertEquals(expected.width, actual.encodedFrameWidth, "encodedFrameWidth field");
        Assertions.assertEquals(expected.height, actual.encodedFrameHeight, "encodedFrameHeight field");
        Assertions.assertEquals(expected.bitDepth, actual.encodedFrameBitDepth, "encodedFrameBitDepth field");
        Assertions.assertEquals(expected.framesPerSecond, actual.encodedFramesPerSecond, "encodedFramesPerSecond field");
        Assertions.assertEquals(expected.ended, actual.ended, "ended field");
        Assertions.assertEquals(expected.payloadType, actual.payloadType, "payloadType field");
        Assertions.assertEquals(expected.mimeType, actual.mimeType, "mimeType field");
        Assertions.assertEquals(expected.clockRate, actual.clockRate, "clockRate field");
        Assertions.assertEquals(expected.channels, actual.channels, "channels field");
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
        var expected = observedClientSample.getClientSample().outboundVideoTracks[0];
        this.depot
                .setOutboundVideoTrack(expected)
                .setObservedClientSample(observedClientSample)
                .assemble();
        var reports = depot.get();
        var actual = reports.get(0);

        // check if the things are cleaned properly
        Assertions.assertEquals(1, reports.size());
        Assertions.assertNull( actual.label, "label field");
    }
}