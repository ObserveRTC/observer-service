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
    private final String MARKER = SerDeUtils.NULL_STRING;

    @Test
    void structureShouldHasNotChangedSinceLastTestFixed() {
        var fields = ClientDTO.class.getFields();
        Assertions.assertEquals(10, fields.length);
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        PeerConnectionDTO.Builder builder = this.makeBuilder()
                .withServiceId(null)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutRoomId() {
        PeerConnectionDTO.Builder builder = this.makeBuilder()
                .withRoomId(null)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutCallId() {
        PeerConnectionDTO.Builder builder = this.makeBuilder()
                .withCallId(null)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutClientId() {
        PeerConnectionDTO.Builder builder = this.makeBuilder()
                .withClientId(null)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutPeerConnectionId() {
        PeerConnectionDTO.Builder builder = this.makeBuilder()
                .withPeerConnectionId(null)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutTimestamp() {
        PeerConnectionDTO.Builder builder = this.makeBuilder()
                .withCreatedTimestamp(null)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }



    @Test
    void shouldHasExpectedValues() {
        var subject = this.makeDTO();

        Assertions.assertEquals(subject.callId, CALL_ID);
        Assertions.assertEquals(subject.serviceId, SERVICE_ID);
        Assertions.assertEquals(subject.roomId, ROOM_ID);
        Assertions.assertEquals(subject.mediaUnitId, MEDIA_UNIT_ID);
        Assertions.assertEquals(subject.userId, USER_ID);
        Assertions.assertEquals(subject.clientId, CLIENT_ID);
        Assertions.assertEquals(subject.peerConnectionId, PEER_CONNECTION_ID);
        Assertions.assertEquals(subject.created, TIMESTAMP);
        Assertions.assertEquals(subject.marker, MARKER);
    }

    @Test
    void shouldBeEqual() {
        var source = this.makeDTO();
        var target = PeerConnectionDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private PeerConnectionDTO.Builder makeBuilder() {
        return PeerConnectionDTO.builder()
                .withCallId(CALL_ID)
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)

                .withMediaUnitId(MEDIA_UNIT_ID)
                .withUserId(USER_ID)

                .withClientId(CLIENT_ID)
                .withPeerConnectionId(PEER_CONNECTION_ID)
                .withCreatedTimestamp(TIMESTAMP)
                .withMarker(MARKER)
                ;
    }

    private PeerConnectionDTO makeDTO() {
        return this.makeBuilder().build();
    }

}