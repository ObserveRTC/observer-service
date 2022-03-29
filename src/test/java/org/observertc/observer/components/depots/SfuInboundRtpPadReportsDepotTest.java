package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ObservedSamplesGenerator;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SfuInboundRtpPadReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private SfuInboundRtpPadReportsDepot depot = new SfuInboundRtpPadReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var serviceId = observedSfuSample.getServiceId();
        var mediaUnitId = observedSfuSample.getMediaUnitId();
        var sfuSample = observedSfuSample.getSfuSample();
        var expected = sfuSample.inboundRtpPads[0];
        var clientId = UUID.randomUUID();
        var trackId = UUID.randomUUID();
        this.depot
                .setSfuInboundRtpPad(expected)
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
        Assertions.assertEquals(expected.padId.toString(), actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(expected.ssrc, actual.ssrc, "ssrc field");
        Assertions.assertEquals(trackId.toString(), actual.trackId, "trackId field");
        Assertions.assertEquals(clientId.toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(callId.toString(), actual.callId, "callId field");
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
        Assertions.assertEquals(expected.packetsReceived, actual.packetsReceived, "packetsReceived field");
        Assertions.assertEquals(expected.packetsDiscarded, actual.packetsDiscarded, "packetsDiscarded field");
        Assertions.assertEquals(expected.packetsRepaired, actual.packetsRepaired, "packetsRepaired field");
        Assertions.assertEquals(expected.packetsFailedDecryption, actual.packetsFailedDecryption, "packetsFailedDecryption field");
        Assertions.assertEquals(expected.packetsDuplicated, actual.packetsDuplicated, "packetsDuplicated field");
        Assertions.assertEquals(expected.fecPacketsReceived, actual.fecPacketsReceived, "fecPacketsReceived field");
        Assertions.assertEquals(expected.fecPacketsDiscarded, actual.fecPacketsDiscarded, "fecPacketsDiscarded field");
        Assertions.assertEquals(expected.bytesReceived, actual.bytesReceived, "bytesReceived field");
        Assertions.assertEquals(expected.rtcpSrReceived, actual.rtcpSrReceived, "rtcpSrReceived field");
        Assertions.assertEquals(expected.rtcpRrSent, actual.rtcpRrSent, "rtcpRrSent field");
        Assertions.assertEquals(expected.rtxPacketsReceived, actual.rtxPacketsReceived, "rtxPacketsReceived field");
        Assertions.assertEquals(expected.rtxPacketsDiscarded, actual.rtxPacketsDiscarded, "rtxPacketsDiscarded field");
        Assertions.assertEquals(expected.framesReceived, actual.framesReceived, "framesReceived field");
        Assertions.assertEquals(expected.framesDecoded, actual.framesDecoded, "framesDecoded field");
        Assertions.assertEquals(expected.keyFramesDecoded, actual.keyFramesDecoded, "keyFramesDecoded field");
        Assertions.assertEquals(expected.fractionLost, actual.fractionLost, "fractionLost field");
        Assertions.assertEquals(expected.jitter, actual.jitter, "jitter field");
        Assertions.assertEquals(expected.roundTripTime, actual.roundTripTime, "roundTripTime field");
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
        var expected = sfuSample.inboundRtpPads[0];
        this.depot
                .setSfuInboundRtpPad(expected)
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