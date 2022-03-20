package org.observertc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;
import java.time.Instant;
import java.util.UUID;

import static org.observertc.observer.dto.CommonConstants.ROOM_ID;
import static org.observertc.observer.dto.CommonConstants.SERVICE_ID;

@MicronautTest
class MediaTrackDTOTest {

    @Inject
    DTOGenerators generator;


    @Test
    void shouldNotBuildWithoutCallId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withClientId(UUID.randomUUID())
                .withAddedTimestamp(Instant.now().toEpochMilli())
                .withPeerConnectionId(UUID.randomUUID())
                .withTrackId(UUID.randomUUID())
                .withSSRC(1234L)
                .withDirection(UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        var builder = MediaTrackDTO.builder()
                .withRoomId(ROOM_ID)
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withAddedTimestamp(Instant.now().toEpochMilli())
                .withPeerConnectionId(UUID.randomUUID())
                .withTrackId(UUID.randomUUID())
                .withSSRC(1234L)
                .withDirection(UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutRoomId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withAddedTimestamp(Instant.now().toEpochMilli())
                .withPeerConnectionId(UUID.randomUUID())
                .withTrackId(UUID.randomUUID())
                .withSSRC(1234L)
                .withDirection(UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutClientId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(UUID.randomUUID())
                .withAddedTimestamp(Instant.now().toEpochMilli())
                .withPeerConnectionId(UUID.randomUUID())
                .withTrackId(UUID.randomUUID())
                .withSSRC(1234L)
                .withDirection(UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutTimestamp() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withPeerConnectionId(UUID.randomUUID())
                .withTrackId(UUID.randomUUID())
                .withSSRC(1234L)
                .withDirection(UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutPeerConnectionId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withAddedTimestamp(Instant.now().toEpochMilli())
                .withTrackId(UUID.randomUUID())
                .withSSRC(1234L)
                .withDirection(UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutTrackId() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withAddedTimestamp(Instant.now().toEpochMilli())
                .withPeerConnectionId(UUID.randomUUID())
                .withSSRC(1234L)
                .withDirection(UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutSSRC() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withAddedTimestamp(Instant.now().toEpochMilli())
                .withPeerConnectionId(UUID.randomUUID())
                .withTrackId(UUID.randomUUID())
                .withDirection(UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutDirection() {
        var builder = MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withAddedTimestamp(Instant.now().toEpochMilli())
                .withPeerConnectionId(UUID.randomUUID())
                .withTrackId(UUID.randomUUID())
                .withSSRC(1234L);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldBuildWithServiceId() {
        var expectedServiceId = "MyService";
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withServiceId(expectedServiceId).build();

        Assertions.assertEquals(expectedServiceId, mediaTrackDTO.serviceId);
    }

    @Test
    void shouldBuildWithMediaUnitId() {
        var expectedMediaUintId = "myMediaUnitId";
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withMediaUnitId(expectedMediaUintId).build();

        Assertions.assertEquals(expectedMediaUintId, mediaTrackDTO.mediaUnitId);
    }

    @Test
    void shouldBuildWithRoomId() {
        var expectedRoomId = "MyRoom";
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withRoomId(expectedRoomId).build();

        Assertions.assertEquals(expectedRoomId, mediaTrackDTO.roomId);
    }

    @Test
    void shouldBuildWithCallId() {
        var expectedCallId = UUID.randomUUID();
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withCallId(expectedCallId).build();

        Assertions.assertEquals(expectedCallId, mediaTrackDTO.callId);
    }

    @Test
    void shouldBuildWithClientId() {
        var expectedClientId = UUID.randomUUID();
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withClientId(expectedClientId).build();

        Assertions.assertEquals(expectedClientId, mediaTrackDTO.clientId);
    }

    @Test
    void shouldBuildWithPeerConnectionId() {
        var expectedPeerConnectionId = UUID.randomUUID();
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withPeerConnectionId(expectedPeerConnectionId).build();

        Assertions.assertEquals(expectedPeerConnectionId, mediaTrackDTO.peerConnectionId);
    }

    @Test
    void shouldBuildWithTrackId() {
        var expectedTrackId = UUID.randomUUID();
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withTrackId(expectedTrackId).build();

        Assertions.assertEquals(expectedTrackId, mediaTrackDTO.trackId);
    }

    @Test
    void shouldBuildWithSSRC() {
        var expectedSSRC = 2347232L;
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withSSRC(expectedSSRC).build();

        Assertions.assertEquals(expectedSSRC, mediaTrackDTO.ssrc);
    }

    @Test
    void shouldBuildWithStreamDirection() {
        var expectedStreamDirection = UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND;
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withDirection(expectedStreamDirection).build();

        Assertions.assertEquals(expectedStreamDirection, mediaTrackDTO.direction);
    }

    @Test
    void shouldBuildWithTimestamp() {
        var expectedTimestamp = Instant.now().toEpochMilli();
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withAddedTimestamp(expectedTimestamp).build();

        Assertions.assertEquals(expectedTimestamp, mediaTrackDTO.added);
    }


    @Test
    void shouldBuildWithUserId() {
        var expectedUserId = "myUserId";
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withUserId(expectedUserId).build();

        Assertions.assertEquals(expectedUserId, mediaTrackDTO.userId);
    }

    @Test
    void shouldBuildWithSfuStreamId() {
        var expectedRtpStreamId = UUID.randomUUID();
        MediaTrackDTO mediaTrackDTO = this.makeBuilder().withSfuStreamId(expectedRtpStreamId).build();

        Assertions.assertEquals(expectedRtpStreamId, mediaTrackDTO.sfuStreamId);
    }


    @Test
    void shouldBeEqual() {
        var source = this.makeBuilder()
                .withAddedTimestamp(Instant.now().toEpochMilli())
                .build();
        var target = MediaTrackDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private MediaTrackDTO.Builder makeBuilder() {
        return MediaTrackDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withPeerConnectionId(UUID.randomUUID())
                .withTrackId(UUID.randomUUID())
                .withSSRC(1234L)
                .withDirection(UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND)
                .withAddedTimestamp(Instant.now().toEpochMilli())
                ;
    }

}