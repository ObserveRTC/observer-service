package org.observertc.observer.components.depots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.observertc.observer.samples.ObservedSamplesGenerator;

import java.util.UUID;

class SfuTransportReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private SfuTransportReportsDepot depot = new SfuTransportReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var serviceId = observedSfuSample.getServiceId();
        var mediaUnitId = observedSfuSample.getMediaUnitId();
        var sfuSample = observedSfuSample.getSfuSample();
        var expected = sfuSample.transports[0];
        var roomId = UUID.randomUUID().toString();
        this.depot
                .setSfuTransport(expected)
                .setCallId(callId)
                .setRoomId(roomId)
                .setObservedSfuSample(observedSfuSample)
                .assemble();

        var actual = depot.get().get(0);

        Assertions.assertEquals(serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(sfuSample.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(sfuSample.marker, actual.marker, "marker field");
        Assertions.assertEquals(sfuSample.timestamp, actual.timestamp, "timestamp field");
        Assertions.assertEquals(callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(roomId, actual.roomId, "roomId field");
        Assertions.assertEquals(expected.transportId.toString(), actual.transportId, "transportId field");
        Assertions.assertEquals(expected.dtlsState, actual.dtlsState, "dtlsState field");
        Assertions.assertEquals(expected.iceState, actual.iceState, "iceState field");
        Assertions.assertEquals(expected.sctpState, actual.sctpState, "sctpState field");
        Assertions.assertEquals(expected.iceRole, actual.iceRole, "iceRole field");
        Assertions.assertEquals(expected.localAddress, actual.localAddress, "localAddress field");
        Assertions.assertEquals(expected.localPort, actual.localPort, "localPort field");
        Assertions.assertEquals(expected.protocol, actual.protocol, "protocol field");
        Assertions.assertEquals(expected.remoteAddress, actual.remoteAddress, "remoteAddress field");
        Assertions.assertEquals(expected.remotePort, actual.remotePort, "remotePort field");
        Assertions.assertEquals(expected.rtpBytesReceived, actual.rtpBytesReceived, "rtpBytesReceived field");
        Assertions.assertEquals(expected.rtpBytesSent, actual.rtpBytesSent, "rtpBytesSent field");
        Assertions.assertEquals(expected.rtpPacketsReceived, actual.rtpPacketsReceived, "rtpPacketsReceived field");
        Assertions.assertEquals(expected.rtpPacketsSent, actual.rtpPacketsSent, "rtpPacketsSent field");
        Assertions.assertEquals(expected.rtpPacketsLost, actual.rtpPacketsLost, "rtpPacketsLost field");
        Assertions.assertEquals(expected.rtxBytesReceived, actual.rtxBytesReceived, "rtxBytesReceived field");
        Assertions.assertEquals(expected.rtxBytesSent, actual.rtxBytesSent, "rtxBytesSent field");
        Assertions.assertEquals(expected.rtxPacketsReceived, actual.rtxPacketsReceived, "rtxPacketsReceived field");
        Assertions.assertEquals(expected.rtxPacketsSent, actual.rtxPacketsSent, "rtxPacketsSent field");
        Assertions.assertEquals(expected.rtxPacketsLost, actual.rtxPacketsLost, "rtxPacketsLost field");
        Assertions.assertEquals(expected.rtxPacketsDiscarded, actual.rtxPacketsDiscarded, "rtxPacketsDiscarded field");
        Assertions.assertEquals(expected.sctpBytesReceived, actual.sctpBytesReceived, "sctpBytesReceived field");
        Assertions.assertEquals(expected.sctpBytesSent, actual.sctpBytesSent, "sctpBytesSent field");
        Assertions.assertEquals(expected.sctpPacketsReceived, actual.sctpPacketsReceived, "sctpPacketsReceived field");
        Assertions.assertEquals(expected.sctpPacketsSent, actual.sctpPacketsSent, "sctpPacketsSent field");
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
        var expected = sfuSample.transports[0];
        this.depot
                .setSfuTransport(expected)
                .setObservedSfuSample(observedSfuSample)
                .assemble();
        var reports = depot.get();
        var actual = reports.get(0);

        // check if the things are cleaned properly
        Assertions.assertNull( actual.callId, "callId field");
        Assertions.assertNull( actual.roomId, "roomId field");
    }
}