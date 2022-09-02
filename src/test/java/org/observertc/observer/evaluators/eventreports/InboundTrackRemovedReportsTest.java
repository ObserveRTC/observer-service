package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.utils.ModelsGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@MicronautTest
class InboundTrackRemovedReportsTest {

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    ModelsGenerator modelsGenerator;

    @Inject
    InboundTrackRemovedReports inboundTrackRemovedReports;

    @Test
    void shouldHasExpectedValuesWhenRemoved() throws Throwable {
        var expected = modelsGenerator.getMediaTrackDTO();

        var reports = this.inboundTrackRemovedReports.mapRemovedMediaTracks(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.marker, actual.marker, "marker field");
        Assertions.assertNotNull(actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(expected.roomId, actual.roomId, "roomId field");

        Assertions.assertEquals(expected.clientId.toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(expected.userId, actual.userId, "userId field");
        Assertions.assertEquals(expected.peerConnectionId.toString(), actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(expected.trackId.toString(), actual.mediaTrackId, "mediaTrackId field");
        Assertions.assertEquals(expected.ssrc,  actual.SSRC, "SSRC field");
        Assertions.assertEquals(null, actual.sampleTimestamp, "sampleTimestamp field");
        Assertions.assertEquals(null, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(CallEventType.MEDIA_TRACK_REMOVED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertNotEquals(null, actual.attachments, "attachments field");
    }

    @Test
    void shouldHasExpectedValuesWhenExpired_1() throws Throwable {
        var mediaTrackDTO = modelsGenerator.getMediaTrackDTO();
        var outboundTrackId = UUID.randomUUID();
        this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackDTO.trackId);
        this.hazelcastMaps.getInboundTrackIdsToOutboundTrackIds().put(mediaTrackDTO.trackId, outboundTrackId);
        var lastTouch = Instant.now().minusSeconds(238).toEpochMilli();
        var expired = RepositoryExpiredEvent.make(mediaTrackDTO, lastTouch);

        var reports = this.inboundTrackRemovedReports.mapExpiredMediaTracks(List.of(expired));
        var actual = reports.get(0);

        Assertions.assertEquals(lastTouch, actual.timestamp, "timestamp field");
    }
}