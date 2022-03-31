package org.observertc.observer.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class CallDTOTest {

    private final String ROOM_ID = UUID.randomUUID().toString();
    private final String SERVICE_ID = UUID.randomUUID().toString();
    private final Long TIMESTAMP = Instant.now().toEpochMilli();
    private final UUID CALL_ID = UUID.randomUUID();
    private final String MARKER = SerDeUtils.NULL_STRING;


    @Test
    void structureShouldHasNotChangedSinceLastTestFixed() {
        var fields = CallDTO.class.getFields();
        Assertions.assertEquals(6, fields.length);
    }

    @Test
    void shouldNotBuildWithoutCallId() {
        CallDTO.Builder builder = CallDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        CallDTO.Builder builder = CallDTO.builder()
                .withCallId(UUID.randomUUID())
                .withRoomId(ROOM_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutRoomIdId() {
        CallDTO.Builder builder = CallDTO.builder()
                .withServiceId(SERVICE_ID)
                .withCallId(CALL_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutStartedTimestamp() {
        CallDTO.Builder builder = CallDTO.builder()
                .withServiceId(SERVICE_ID)
                .withCallId(CALL_ID)
                .withStartedTimestamp(null)
                ;

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldHasExpectedValues() {
        var subject = this.makeDTO();

        Assertions.assertEquals(subject.callId, CALL_ID);
        Assertions.assertEquals(subject.started, TIMESTAMP);
        Assertions.assertEquals(subject.roomId, ROOM_ID);
        Assertions.assertEquals(subject.serviceId, SERVICE_ID);
        Assertions.assertEquals(subject.marker, MARKER);
    }

    @Test
    public void shouldBeEqual_1() {
        var builder = this.makeDtoBuilder();
        var expected = builder.build();
        var actual = CallDTO.builder().copyFrom(builder).build();

        boolean equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void shouldBeEqual_2() {
        var expected = this.makeDTO();
        var actual = CallDTO.builder().from(expected).build();

        boolean equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void shouldBeNotEqual_1() {
        var source = this.makeDTO();
        var modified_1 = CallDTO.builder().from(source)
                .withCallId(UUID.randomUUID())
                .build();
        var modified_2 = CallDTO.builder().from(source)
                .withMarker(UUID.randomUUID().toString())
                .build();
        var modified_3 = CallDTO.builder().from(source)
                .withServiceId(UUID.randomUUID().toString())
                .build();
        var modified_4 = CallDTO.builder().from(source)
                .withRoomId(UUID.randomUUID().toString())
                .build();
        var modified_5 = CallDTO.builder().from(source)
                .withStartedTimestamp(Instant.now().minusSeconds(3600).toEpochMilli())
                .build();

        boolean equals_1 = source.equals(modified_1);
        boolean equals_2 = source.equals(modified_2);
        boolean equals_3 = source.equals(modified_3);
        boolean equals_4 = source.equals(modified_4);
        boolean equals_5 = source.equals(modified_5);
        Assertions.assertFalse(equals_1);
        Assertions.assertFalse(equals_2);
        Assertions.assertFalse(equals_3);
        Assertions.assertFalse(equals_4);
        Assertions.assertFalse(equals_5);
    }

    private CallDTO makeDTO() {
        return this.makeDtoBuilder().build();
    }

    private CallDTO.Builder makeDtoBuilder() {
        return CallDTO.builder()
                .withServiceId(this.SERVICE_ID)
                .withRoomId(this.ROOM_ID)
                .withCallId(this.CALL_ID)
                .withStartedTimestamp(this.TIMESTAMP)
                .withMarker(MARKER)
                ;
    }

}