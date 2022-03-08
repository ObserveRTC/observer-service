package org.observertc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;
import java.time.Instant;
import java.util.UUID;

@MicronautTest
class CallDTOTest {

    @Inject
    DTOGenerators generator;


    @Test
    void shouldNotBuildWithoutCallId() {
        CallDTO.Builder builder = CallDTO.builder()
                .withServiceId(CommonConstants.SERVICE_ID)
                .withRoomId(CommonConstants.ROOM_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        CallDTO.Builder builder = CallDTO.builder()
                .withCallId(UUID.randomUUID())
                .withRoomId(CommonConstants.ROOM_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutRoomIdId() {
        CallDTO.Builder builder = CallDTO.builder()
                .withServiceId(CommonConstants.SERVICE_ID)
                .withCallId(UUID.randomUUID());

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldBuildWithServiceId() {
        var expectedServiceId = "MyService";
        CallDTO callDTO = this.makeBuilder().withServiceId(expectedServiceId).build();

        Assertions.assertEquals(expectedServiceId, callDTO.serviceId);
    }

    @Test
    void shouldBuildWithRoomId() {
        var expectedRoomId = "MyRoom";
        CallDTO callDTO = this.makeBuilder().withRoomId(expectedRoomId).build();

        Assertions.assertEquals(expectedRoomId, callDTO.roomId);
    }

    @Test
    void shouldBuildWithCallId() {
        var expectedCallId = UUID.randomUUID();
        CallDTO callDTO = this.makeBuilder().withCallId(expectedCallId).build();

        Assertions.assertEquals(expectedCallId, callDTO.callId);
    }

    @Test
    void shouldBuildWithTimestamp() {
        var expectedTimestamp = Instant.now().toEpochMilli();
        CallDTO callDTO = this.makeBuilder().withStartedTimestamp(expectedTimestamp).build();

        Assertions.assertEquals(expectedTimestamp, callDTO.started);
    }

    @Test
    void shouldBeEqual() {
        var source = this.makeBuilder()
                .withStartedTimestamp(Instant.now().toEpochMilli())
                .build();
        var target = CallDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private CallDTO.Builder makeBuilder() {
        return CallDTO.builder()
                .withServiceId(CommonConstants.SERVICE_ID)
                .withRoomId(CommonConstants.ROOM_ID)
                .withCallId(UUID.randomUUID());
    }

}