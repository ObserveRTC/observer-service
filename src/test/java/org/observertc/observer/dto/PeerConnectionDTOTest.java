package org.observertc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

@MicronautTest
class PeerConnectionDTOTest {

    private final String ROOM_ID = UUID.randomUUID().toString();
    private final String SERVICE_ID = UUID.randomUUID().toString();
    private final String MEDIA_UNIT_ID = UUID.randomUUID().toString();
    private final Long TIMESTAMP = Instant.now().toEpochMilli();
    private final UUID CALL_ID = UUID.randomUUID();
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID PEER_CONNECTION_ID = UUID.randomUUID();
    private final String USER_ID = UUID.randomUUID().toString();
    private final Long SSRC = 1234L;
    private final StreamDirection DIRECTION = UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND;

    @Test
    void shouldNotBuildWithoutCallId() {
        var builder = PeerConnectionDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withCreatedTimestamp(TIMESTAMP)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        var builder = PeerConnectionDTO.builder()
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withCreatedTimestamp(TIMESTAMP)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutRoomId() {
        var builder = PeerConnectionDTO.builder()
                .withServiceId(SERVICE_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withCreatedTimestamp(TIMESTAMP)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutClientId() {
        var builder = PeerConnectionDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withCreatedTimestamp(TIMESTAMP)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutTimestamp() {
        var builder = PeerConnectionDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutPeerConnectionId() {
        var builder = PeerConnectionDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withCreatedTimestamp(TIMESTAMP)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldBuildWithServiceId() {
        var expectedServiceId = "MyService";
        var peerConnectionDTO = this.makeBuilder().withServiceId(expectedServiceId).build();

        Assertions.assertEquals(expectedServiceId, peerConnectionDTO.serviceId);
    }

    @Test
    void shouldBuildWithMediaUnitId() {
        var expectedMediaUintId = "myMediaUnitId";
        var peerConnectionDTO = this.makeBuilder().withMediaUnitId(expectedMediaUintId).build();

        Assertions.assertEquals(expectedMediaUintId, peerConnectionDTO.mediaUnitId);
    }

    @Test
    void shouldBuildWithRoomId() {
        var expectedRoomId = "MyRoom";
        var peerConnectionDTO = this.makeBuilder().withRoomId(expectedRoomId).build();

        Assertions.assertEquals(expectedRoomId, peerConnectionDTO.roomId);
    }

    @Test
    void shouldBuildWithCallId() {
        var expectedCallId = UUID.randomUUID();
        var peerConnectionDTO = this.makeBuilder().withCallId(expectedCallId).build();

        Assertions.assertEquals(expectedCallId, peerConnectionDTO.callId);
    }

    @Test
    void shouldBuildWithClientId() {
        var expectedClientId = UUID.randomUUID();
        var peerConnectionDTO = this.makeBuilder().withClientId(expectedClientId).build();

        Assertions.assertEquals(expectedClientId, peerConnectionDTO.clientId);
    }

    @Test
    void shouldBuildWithPeerConnectionId() {
        var expectedPeerConnectionId = UUID.randomUUID();
        var peerConnectionDTO = this.makeBuilder().withPeerConnectionId(expectedPeerConnectionId).build();

        Assertions.assertEquals(expectedPeerConnectionId, peerConnectionDTO.peerConnectionId);
    }

    @Test
    void shouldBuildWithTimestamp() {
        var expectedTimestamp = Instant.now().toEpochMilli();
        var peerConnectionDTO = this.makeBuilder().withCreatedTimestamp(expectedTimestamp).build();

        Assertions.assertEquals(expectedTimestamp, peerConnectionDTO.created);
    }


    @Test
    void shouldBuildWithUserId() {
        var expectedUserId = "myUserId";
        var peerConnectionDTO = this.makeBuilder().withUserId(expectedUserId).build();

        Assertions.assertEquals(expectedUserId, peerConnectionDTO.userId);
    }



    @Test
    void shouldBeEqual() {
        var source = this.makeBuilder()
                .withCreatedTimestamp(Instant.now().toEpochMilli())
                .build();
        var target = PeerConnectionDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private PeerConnectionDTO.Builder makeBuilder() {
        return PeerConnectionDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withCreatedTimestamp(TIMESTAMP)
                ;
    }

}