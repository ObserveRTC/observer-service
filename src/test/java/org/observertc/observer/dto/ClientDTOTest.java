package org.observertc.observer.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

class ClientDTOTest {

    private final String ROOM_ID = UUID.randomUUID().toString();
    private final String SERVICE_ID = UUID.randomUUID().toString();
    private final String MEDIA_UNIT_ID = UUID.randomUUID().toString();
    private final Long TIMESTAMP = Instant.now().toEpochMilli();
    private final UUID CALL_ID = UUID.randomUUID();
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String TIMEZONE_ID = ZoneId.systemDefault().getId();
    private final String MARKER = SerDeUtils.NULL_STRING;

    @Test
    void structureShouldHasNotChangedSinceLastTestFixed() {
        var fields = ClientDTO.class.getFields();
        Assertions.assertEquals(10, fields.length);
    }

    @Test
    void shouldNotBuildWithoutCallId() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withClientId(CLIENT_ID)
                .withJoinedTimestamp(TIMESTAMP);


        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withRoomId(ROOM_ID)
                .withClientId(CLIENT_ID)
                .withJoinedTimestamp(TIMESTAMP)
                .withCallId(CALL_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutRoomId() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withServiceId(SERVICE_ID)
                .withClientId(CLIENT_ID)
                .withJoinedTimestamp(TIMESTAMP)
                .withCallId(CALL_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutClientId() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withJoinedTimestamp(TIMESTAMP)
                .withCallId(CALL_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutTimestamp() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withClientId(CLIENT_ID)
                .withCallId(CALL_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }



    @Test
    void shouldHasExpectedValues() {
        var subject = this.makeDTO();

        Assertions.assertEquals(subject.callId, CALL_ID);
        Assertions.assertEquals(subject.clientId, CLIENT_ID);
        Assertions.assertEquals(subject.joined, TIMESTAMP);
        Assertions.assertEquals(subject.roomId, ROOM_ID);
        Assertions.assertEquals(subject.serviceId, SERVICE_ID);
        Assertions.assertEquals(subject.mediaUnitId, MEDIA_UNIT_ID);
        Assertions.assertEquals(subject.timeZoneId, TIMEZONE_ID);
    }

    @Test
    void shouldBeEqual() {
        var source = this.makeDTO();
        var target = ClientDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private ClientDTO.Builder makeBuilder() {
        return ClientDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                .withJoinedTimestamp(TIMESTAMP)
                .withTimeZoneId(TIMEZONE_ID)
                .withMediaUnitId(MEDIA_UNIT_ID)
                .withMarker(MARKER)
                ;
    }

    private ClientDTO makeDTO() {
        return this.makeBuilder().build();
    }

}