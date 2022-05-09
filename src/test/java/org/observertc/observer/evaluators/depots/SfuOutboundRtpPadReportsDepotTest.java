package org.observertc.observer.evaluators.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.utils.ObservedSamplesGenerator;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SfuOutboundRtpPadReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private SfuOutboundRtpPadReportsDepot depot = new SfuOutboundRtpPadReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var serviceId = observedSfuSample.getServiceId();
        var mediaUnitId = observedSfuSample.getMediaUnitId();
        var sfuSample = observedSfuSample.getSfuSample();
        var expected = sfuSample.outboundRtpPads[0];
        var clientId = UUID.randomUUID();
        var trackId = UUID.randomUUID();
        this.depot
                .setSfuOutboundRtpPad(expected)
                .setObservedSfuSample(observedSfuSample)
                .setClientId(clientId)
                .setTrackId(trackId)
                .setCallId(callId)
                .assemble();

        var actual = depot.get().get(0);

        Assertions.assertEquals(serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(sfuSample.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(sfuSample.marker, actual.marker, "marker field");
        Assertions.assertEquals(expected.internal, actual.internal, "internal field");
        Assertions.assertEquals(sfuSample.timestamp, actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.transportId.toString(), actual.transportId, "transportId field");
        Assertions.assertEquals(expected.streamId.toString(), actual.sfuStreamId, "sfuStreamId field");
        Assertions.assertEquals(expected.sinkId.toString(), actual.sfuSinkId, "sfuSinkId field");
        Assertions.assertEquals(expected.padId.toString(), actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(expected.ssrc, actual.ssrc, "ssrc field");
        Assertions.assertEquals(expected.internal, actual.internal, "internal field");
        Assertions.assertEquals(expected.roundTripTime, actual.roundTripTime, "roundTripTime field");
        Assertions.assertEquals(callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(clientId.toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(trackId.toString(), actual.trackId, "trackId field");
        Assertions.assertEquals(expected.mediaType, actual.mediaType, "mediaType field");
        Assertions.assertEquals(expected.payloadType, actual.payloadType, "payloadType field");
        Assertions.assertEquals(expected.mimeType, actual.mimeType, "mimeType field");
        Assertions.assertEquals(expected.clockRate, actual.clockRate, "clockRate field");
        Assertions.assertEquals(expected.sdpFmtpLine, actual.sdpFmtpLine, "sdpFmtpLine field");
        Assertions.assertEquals(expected.rid, actual.rid, "rid field");
        Assertions.assertEquals(expected.rtxSsrc, actual.rtxSsrc, "rtxSsrc field");
        Assertions.assertEquals(expected.targetBitrate, actual.targetBitrate, "targetBitrate field");
        Assertions.assertEquals(expected.voiceActivityFlag, actual.voiceActivityFlag, "voiceActivityFlag field");
        Assertions.assertEquals(expected.firCount, actual.firCount, "firCount field");
        Assertions.assertEquals(expected.pliCount, actual.pliCount, "pliCount field");
        Assertions.assertEquals(expected.nackCount, actual.nackCount, "nackCount field");
        Assertions.assertEquals(expected.sliCount, actual.sliCount, "sliCount field");
        Assertions.assertEquals(expected.packetsLost, actual.packetsLost, "packetsLost field");
        Assertions.assertEquals(expected.packetsSent, actual.packetsSent, "packetsSent field");
        Assertions.assertEquals(expected.packetsDiscarded, actual.packetsDiscarded, "packetsDiscarded field");
        Assertions.assertEquals(expected.packetsRetransmitted, actual.packetsRetransmitted, "packetsRetransmitted field");
        Assertions.assertEquals(expected.packetsFailedEncryption, actual.packetsFailedEncryption, "packetsFailedEncryption field");
        Assertions.assertEquals(expected.packetsDuplicated, actual.packetsDuplicated, "packetsDuplicated field");
        Assertions.assertEquals(expected.fecPacketsSent, actual.fecPacketsSent, "fecPacketsSent field");
        Assertions.assertEquals(expected.fecPacketsDiscarded, actual.fecPacketsDiscarded, "fecPacketsDiscarded field");
        Assertions.assertEquals(expected.bytesSent, actual.bytesSent, "bytesSent field");
        Assertions.assertEquals(expected.rtcpSrSent, actual.rtcpSrSent, "rtcpSrSent field");
        Assertions.assertEquals(expected.rtcpRrReceived, actual.rtcpRrReceived, "rtcpRrReceived field");
        Assertions.assertEquals(expected.rtxPacketsSent, actual.rtxPacketsSent, "rtxPacketsSent field");
        Assertions.assertEquals(expected.rtxPacketsDiscarded, actual.rtxPacketsDiscarded, "rtxPacketsDiscarded field");
        Assertions.assertEquals(expected.framesSent, actual.framesSent, "framesSent field");
        Assertions.assertEquals(expected.framesEncoded, actual.framesEncoded, "framesEncoded field");
        Assertions.assertEquals(expected.keyFramesEncoded, actual.keyFramesEncoded, "keyFramesEncoded field");
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }

    @Test
    @Order(3)
    void shouldBeCleaned() {
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var sfuSample = observedSfuSample.getSfuSample();
        var expected = sfuSample.outboundRtpPads[0];
        this.depot
                .setSfuOutboundRtpPad(expected)
                .setObservedSfuSample(observedSfuSample)
                .assemble();
        var reports = depot.get();
        var actual = reports.get(0);

        // check if the things are cleaned properly
        Assertions.assertEquals(1, reports.size());
        Assertions.assertNull( actual.clientId, "clientId field");
        Assertions.assertNull( actual.callId, "callId field");
        Assertions.assertNull( actual.trackId, "trackId field");
    }
}