package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ObservedSamplesGenerator;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SfuSctpStreamReportsDepotTest {
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private SfuSctpStreamReportsDepot depot = new SfuSctpStreamReportsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedSfuSample = this.generator.generateObservedSfuSample();
        var serviceId = observedSfuSample.getServiceId();
        var roomId = UUID.randomUUID().toString();
        var mediaUnitId = observedSfuSample.getMediaUnitId();
        var sfuSample = observedSfuSample.getSfuSample();
        var expected = sfuSample.sctpChannels[0];
        this.depot
                .setSctpChannel(expected)
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
        Assertions.assertEquals(roomId, actual.roomId, "callId field");
        Assertions.assertEquals(expected.transportId.toString(), actual.transportId, "transportId field");
        Assertions.assertEquals(expected.streamId.toString(), actual.streamId, "streamId field");
        Assertions.assertEquals(expected.label, actual.label, "label field");
        Assertions.assertEquals(expected.protocol, actual.protocol, "protocol field");
        Assertions.assertEquals(expected.sctpSmoothedRoundTripTime, actual.sctpSmoothedRoundTripTime, "sctpSmoothedRoundTripTime field");
        Assertions.assertEquals(expected.sctpCongestionWindow, actual.sctpCongestionWindow, "sctpCongestionWindow field");
        Assertions.assertEquals(expected.sctpReceiverWindow, actual.sctpReceiverWindow, "sctpReceiverWindow field");
        Assertions.assertEquals(expected.sctpMtu, actual.sctpMtu, "sctpMtu field");
        Assertions.assertEquals(expected.sctpUnackData, actual.sctpUnackData, "sctpUnackData field");
        Assertions.assertEquals(expected.messageReceived, actual.messageReceived, "messageReceived field");
        Assertions.assertEquals(expected.messageSent, actual.messageSent, "messageSent field");
        Assertions.assertEquals(expected.bytesReceived, actual.bytesReceived, "bytesReceived field");
        Assertions.assertEquals(expected.bytesSent, actual.bytesSent, "bytesSent field");
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
        var expected = sfuSample.sctpChannels[0];
        this.depot
                .setSctpChannel(expected)
                .setObservedSfuSample(observedSfuSample)
                .assemble();
        var reports = depot.get();
        var actual = reports.get(0);

        // check if the things are cleaned properly
        Assertions.assertNull( actual.callId, "callId field");
    }
}