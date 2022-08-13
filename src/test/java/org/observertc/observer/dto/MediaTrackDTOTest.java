package org.observertc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

@MicronautTest
class MediaTrackDTOTest {

    private final String ROOM_ID = UUID.randomUUID().toString();
    private final String SERVICE_ID = UUID.randomUUID().toString();
    private final String MEDIA_UNIT_ID = UUID.randomUUID().toString();
    private final Long TIMESTAMP = Instant.now().toEpochMilli();
    private final UUID CALL_ID = UUID.randomUUID();
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID TRACK_ID = UUID.randomUUID();
    private final UUID PEER_CONNECTION_ID = UUID.randomUUID();
    private final UUID SFU_STREAM_ID = UUID.randomUUID();
    private final UUID SFU_SINK_ID = UUID.randomUUID();
    private final String USER_ID = UUID.randomUUID().toString();
    private final Long SSRC = 1234L;
    private final StreamDirection DIRECTION = UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND;
    private final MediaKind KIND = UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? MediaKind.AUDIO : MediaKind.VIDEO;
    private final String MARKER = SerDeUtils.NULL_STRING;

    @Test
    void structureShouldHasNotChangedSinceLastTestFixed() {
        var fields = MediaTrackDTO.class.getFields();
        Assertions.assertEquals(16, fields.length);
    }

    @Test
    void shouldNotBuildWithoutCallId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withTrackId(TRACK_ID)
                .withSSRC(SSRC)
                .withDirection(DIRECTION)
                .withAddedTimestamp(TIMESTAMP)
                .withMediaKind(KIND)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        var builder = MediaTrackDTO.builder()
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withTrackId(TRACK_ID)
                .withSSRC(SSRC)
                .withDirection(DIRECTION)
                .withAddedTimestamp(TIMESTAMP)
                .withMediaKind(KIND)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutRoomId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withTrackId(TRACK_ID)
                .withSSRC(SSRC)
                .withDirection(DIRECTION)
                .withAddedTimestamp(TIMESTAMP)
                .withMediaKind(KIND)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutClientId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withTrackId(TRACK_ID)
                .withSSRC(SSRC)
                .withDirection(DIRECTION)
                .withAddedTimestamp(TIMESTAMP)
                .withMediaKind(KIND)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutTimestamp() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withTrackId(TRACK_ID)
                .withSSRC(SSRC)
                .withDirection(DIRECTION)
                .withMediaKind(KIND)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutPeerConnectionId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withTrackId(TRACK_ID)
                .withSSRC(SSRC)
                .withDirection(DIRECTION)
                .withAddedTimestamp(TIMESTAMP)
                .withMediaKind(KIND)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutTrackId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withSSRC(SSRC)
                .withDirection(DIRECTION)
                .withAddedTimestamp(TIMESTAMP)
                .withMediaKind(KIND)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutSSRC() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withTrackId(TRACK_ID)
                .withDirection(DIRECTION)
                .withAddedTimestamp(TIMESTAMP)
                .withMediaKind(KIND)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutDirection() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withTrackId(TRACK_ID)
                .withSSRC(SSRC)
                .withAddedTimestamp(TIMESTAMP)
                .withMediaKind(KIND)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutKind() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withTrackId(TRACK_ID)
                .withDirection(DIRECTION)
                .withSSRC(SSRC)
                .withAddedTimestamp(TIMESTAMP)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldHasExpectedValues() {
        var subject = this.makeDTO();

        Assertions.assertEquals(subject.callId, CALL_ID);
        Assertions.assertEquals(subject.clientId, CLIENT_ID);
        Assertions.assertEquals(subject.added, TIMESTAMP);
        Assertions.assertEquals(subject.roomId, ROOM_ID);
        Assertions.assertEquals(subject.userId, USER_ID);
        Assertions.assertEquals(subject.trackId, TRACK_ID);
        Assertions.assertEquals(subject.peerConnectionId, PEER_CONNECTION_ID);
        Assertions.assertEquals(subject.ssrc, SSRC);
        Assertions.assertEquals(subject.serviceId, SERVICE_ID);
        Assertions.assertEquals(subject.mediaUnitId, MEDIA_UNIT_ID);
        Assertions.assertEquals(subject.sfuStreamId, SFU_STREAM_ID);
        Assertions.assertEquals(subject.sfuSinkId, SFU_SINK_ID);
        Assertions.assertEquals(subject.direction, DIRECTION);
        Assertions.assertEquals(subject.marker, MARKER);
        Assertions.assertEquals(subject.kind, KIND);
    }


    @Test
    void shouldBeEqual() {
        var source = this.makeDTO();
        var target = MediaTrackDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }

    private MediaTrackDTO makeDTO() {
        return this.makeBuilder().build();
    }


    private MediaTrackDTO.Builder makeBuilder() {
        return MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withMediaUnitId(MEDIA_UNIT_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withTrackId(TRACK_ID)
                .withSSRC(SSRC)
                .withDirection(DIRECTION)
                .withAddedTimestamp(TIMESTAMP)
                .withSfuStreamId(SFU_STREAM_ID)
                .withSfuSinkId(SFU_SINK_ID)
                .withUserId(USER_ID)
                .withMarker(MARKER)
                .withMediaKind(KIND)
                ;
    }

}