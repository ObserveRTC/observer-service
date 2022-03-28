package org.observertc.observer.components.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.components.eventreports.attachments.MediaTrackAttachment;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@MicronautTest
class MediaTrackRemovedReportsTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    MediaTrackRemovedReports mediaTrackRemovedReports;

    @Test
    void shouldHasExpectedValuesWhenRemoved() throws Throwable {
        var expected = dtoGenerators.getMediaTrackDTO();

        var reports = this.mediaTrackRemovedReports.mapRemovedMediaTracks(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertNull(actual.mediaUnitId, "mediaUnitId field");
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

        MediaTrackAttachment attachment = MediaTrackAttachment.builder().fromBase64(actual.attachments).build();
        Assertions.assertEquals(expected.sfuStreamId, attachment.sfuStreamId);
        Assertions.assertEquals(expected.sfuSinkId, attachment.sfuSinkId);
        Assertions.assertEquals(expected.direction, attachment.streamDirection);
    }

    @Test
    void shouldHasExpectedValuesWhenExpired_1() throws Throwable {
        var mediaTrackDTO = dtoGenerators.getMediaTrackDTO();
        var outboundTrackId = UUID.randomUUID();
        this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackDTO.trackId);
        this.hazelcastMaps.getInboundTrackIdsToOutboundTrackIds().put(mediaTrackDTO.trackId, outboundTrackId);
        var lastTouch = Instant.now().minusSeconds(238).toEpochMilli();
        var expired = RepositoryExpiredEvent.make(mediaTrackDTO, lastTouch);

        var reports = this.mediaTrackRemovedReports.mapExpiredMediaTracks(List.of(expired));
        var actual = reports.get(0);

        Assertions.assertEquals(lastTouch, actual.timestamp, "timestamp field");
        Assertions.assertNull(this.hazelcastMaps.getMediaTracks().get(mediaTrackDTO.trackId));
        Assertions.assertNull(this.hazelcastMaps.getInboundTrackIdsToOutboundTrackIds().get(mediaTrackDTO.trackId));
        Assertions.assertEquals(0, this.hazelcastMaps.getPeerConnectionToInboundTrackIds().get(mediaTrackDTO.peerConnectionId).size());

    }
}