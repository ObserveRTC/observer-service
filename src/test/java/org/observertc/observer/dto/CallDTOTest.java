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
    void shouldHasExpectedValues() {
        var subject = this.makeDTO();

        Assertions.assertEquals(subject.callId, CALL_ID);
        Assertions.assertEquals(subject.started, TIMESTAMP);
        Assertions.assertEquals(subject.roomId, ROOM_ID);
        Assertions.assertEquals(subject.serviceId, SERVICE_ID);
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

    private CallDTO makeDTO() {
        return this.makeDtoBuilder().build();
    }

    private CallDTO.Builder makeDtoBuilder() {
        return CallDTO.builder()
                .withServiceId(this.SERVICE_ID)
                .withRoomId(this.ROOM_ID)
                .withCallId(this.CALL_ID)
                .withStartedTimestamp(this.TIMESTAMP)
                ;
    }

}