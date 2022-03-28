package org.observertc.observer.components.depots;

import org.junit.jupiter.api.*;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.samples.ObservedSamplesGenerator;
import org.observertc.observer.utils.RandomGenerators;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MediaTrackDTOsDepotTest {
    private final RandomGenerators randomGenerators = new RandomGenerators();
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private MediaTrackDTOsDepot depot = new MediaTrackDTOsDepot();

    @Test
    @Order(1)
    void shouldMakeReport() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var serviceId = observedClientSample.getServiceId();
        var mediaUnitId = observedClientSample.getMediaUnitId();
        var clientSample = observedClientSample.getClientSample();
        var streamDirection = StreamDirection.INBOUND;
        var SSRC = this.randomGenerators.getRandomSSRC();
        var trackId = UUID.randomUUID();
        var peerConnectionId = UUID.randomUUID();
        var streamId = UUID.randomUUID();
        var sinkId = UUID.randomUUID();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setSSRC(SSRC)
                .setTrackId(trackId)
                .setSfuStreamId(streamId)
                .setSfuSinkId(sinkId)
                .setPeerConnectionId(peerConnectionId)
                .setStreamDirection(streamDirection)
                .assemble();
                ;

        var actual = depot.get().get(trackId);

        Assertions.assertEquals(clientSample.callId, actual.callId, "callId field");
        Assertions.assertEquals(serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(clientSample.roomId, actual.roomId, "roomId field");

        Assertions.assertEquals(clientSample.clientId, actual.clientId, "clientId field");
        Assertions.assertEquals(mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(clientSample.userId, actual.userId, "mediaUnitId field");

        Assertions.assertEquals(peerConnectionId, actual.peerConnectionId, "timestamp field");
        Assertions.assertEquals(trackId, actual.trackId, "trackId field");
        Assertions.assertEquals(streamId, actual.sfuStreamId, "sfuStreamId field");
        Assertions.assertEquals(sinkId, actual.sfuSinkId, "sfuSinkId field");
        Assertions.assertEquals(SSRC, actual.ssrc, "ssrc field");
        Assertions.assertEquals(clientSample.timestamp, actual.added, "added field");
        Assertions.assertEquals(streamDirection, actual.direction, "direction field");

        Assertions.assertEquals(actual.marker, clientSample.marker, "marker field");
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
        var streamDirection = StreamDirection.INBOUND;
        var SSRC = this.randomGenerators.getRandomSSRC();
        var trackId = UUID.randomUUID();
        var peerConnectionId = UUID.randomUUID();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setSSRC(SSRC)
                .setTrackId(trackId)
                .setPeerConnectionId(peerConnectionId)
                .setStreamDirection(streamDirection)
                .assemble();
        ;
        var actual = depot.get().get(trackId);

        Assertions.assertNull(actual.sfuStreamId, "sfuStreamId field");
        Assertions.assertNull(actual.sfuSinkId, "sfuSinkId field");
    }

    @Test
    @Order(4)
    void shouldNotCreate_1() {
        var callId = UUID.randomUUID();
        var streamDirection = StreamDirection.INBOUND;
        var SSRC = this.randomGenerators.getRandomSSRC();
        var trackId = UUID.randomUUID();
        var peerConnectionId = UUID.randomUUID();
        this.depot
//                .setObservedClientSample(observedClientSample)
                .setSSRC(SSRC)
                .setTrackId(trackId)
                .setPeerConnectionId(peerConnectionId)
                .setStreamDirection(streamDirection)
                .assemble();
        ;

        Assertions.assertEquals(0, depot.get().size());
    }

    @Test
    @Order(5)
    void shouldNotCreate_2() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var streamDirection = StreamDirection.INBOUND;
        var trackId = UUID.randomUUID();
        var peerConnectionId = UUID.randomUUID();
        this.depot
                .setObservedClientSample(observedClientSample)
//                .setSSRC(SSRC)
                .setTrackId(trackId)
                .setPeerConnectionId(peerConnectionId)
                .setStreamDirection(streamDirection)
                .assemble();
        ;

        Assertions.assertEquals(0, depot.get().size());
    }

    @Test
    @Order(6)
    void shouldNotCreate_3() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var streamDirection = StreamDirection.INBOUND;
        var SSRC = this.randomGenerators.getRandomSSRC();
        var peerConnectionId = UUID.randomUUID();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setSSRC(SSRC)
//                .setTrackId(trackId)
                .setPeerConnectionId(peerConnectionId)
                .setStreamDirection(streamDirection)
                .assemble();
        ;

        Assertions.assertEquals(0, depot.get().size());
    }

    @Test
    @Order(7)
    void shouldNotCreate_4() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var streamDirection = StreamDirection.INBOUND;
        var SSRC = this.randomGenerators.getRandomSSRC();
        var trackId = UUID.randomUUID();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setSSRC(SSRC)
                .setTrackId(trackId)
//                .setPeerConnectionId(peerConnectionId)
                .setStreamDirection(streamDirection)
                .assemble();
        ;

        Assertions.assertEquals(0, depot.get().size());
    }

    @Test
    @Order(8)
    void shouldNotCreate_5() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var SSRC = this.randomGenerators.getRandomSSRC();
        var trackId = UUID.randomUUID();
        var peerConnectionId = UUID.randomUUID();
        this.depot
                .setObservedClientSample(observedClientSample)
                .setSSRC(SSRC)
                .setTrackId(trackId)
                .setPeerConnectionId(peerConnectionId)
//                .setStreamDirection(streamDirection)
                .assemble();
        ;

        Assertions.assertEquals(0, depot.get().size());
    }
}