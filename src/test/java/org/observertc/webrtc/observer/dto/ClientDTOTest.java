package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.utils.DTOGenerators;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.observertc.webrtc.observer.dto.CommonConstants.ROOM_ID;
import static org.observertc.webrtc.observer.dto.CommonConstants.SERVICE_ID;

@MicronautTest
class ClientDTOTest {

    @Inject
    DTOGenerators generator;


    @Test
    void shouldNotBuildWithoutCallId() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withClientId(UUID.randomUUID())
                .withServiceId(SERVICE_ID)
                .withConnectedTimestamp(Instant.now().toEpochMilli())
                .withRoomId(ROOM_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withConnectedTimestamp(Instant.now().toEpochMilli())
                .withRoomId(ROOM_ID);

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutRoomId() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withServiceId(SERVICE_ID)
                .withClientId(UUID.randomUUID())
                .withConnectedTimestamp(Instant.now().toEpochMilli())
                .withCallId(UUID.randomUUID());

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutClientId() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withConnectedTimestamp(Instant.now().toEpochMilli())
                .withCallId(UUID.randomUUID());

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldNotBuildWithoutTimestamp() {
        ClientDTO.Builder builder = ClientDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withClientId(UUID.randomUUID())
                .withCallId(UUID.randomUUID());

        Assertions.assertThrows(Exception.class, () -> builder.build());
    }

    @Test
    void shouldBuildWithServiceId() {
        var expectedServiceId = "MyService";
        ClientDTO clientDTO = this.makeBuilder().withServiceId(expectedServiceId).build();

        Assertions.assertEquals(expectedServiceId, clientDTO.serviceId);
    }

    @Test
    void shouldBuildWithRoomId() {
        var expectedRoomId = "MyRoom";
        ClientDTO clientDTO = this.makeBuilder().withRoomId(expectedRoomId).build();

        Assertions.assertEquals(expectedRoomId, clientDTO.roomId);
    }

    @Test
    void shouldBuildWithMediaUnitId() {
        var expectedMediaUintId = "myMediaUnitId";
        ClientDTO clientDTO = this.makeBuilder().withMediaUnitId(expectedMediaUintId).build();

        Assertions.assertEquals(expectedMediaUintId, clientDTO.mediaUnitId);
    }

    @Test
    void shouldBuildWithUserId() {
        var expectedUserId = "myUserId";
        ClientDTO clientDTO = this.makeBuilder().withUserId(expectedUserId).build();

        Assertions.assertEquals(expectedUserId, clientDTO.userId);
    }

    @Test
    void shouldBuildWithClientId() {
        var expectedClientId = UUID.randomUUID();
        ClientDTO clientDTO = this.makeBuilder().withClientId(expectedClientId).build();

        Assertions.assertEquals(expectedClientId, clientDTO.clientId);
    }

    @Test
    void shouldBuildWithTimeZoneId() {
        var expectedTimeZoneId = ZoneId.SHORT_IDS.values().stream().findFirst().get();
        ClientDTO clientDTO = this.makeBuilder().withTimeZoneId(expectedTimeZoneId).build();

        Assertions.assertEquals(expectedTimeZoneId, clientDTO.timeZoneId);
    }


    @Test
    void shouldBuildWithCallId() {
        var expectedCallId = UUID.randomUUID();
        ClientDTO clientDTO = this.makeBuilder().withCallId(expectedCallId).build();

        Assertions.assertEquals(expectedCallId, clientDTO.callId);
    }

    @Test
    void shouldBuildWithTimestamp() {
        var expectedTimestamp = Instant.now().toEpochMilli();
        ClientDTO clientDTO = this.makeBuilder().withConnectedTimestamp(expectedTimestamp).build();

        Assertions.assertEquals(expectedTimestamp, clientDTO.joined);
    }

    @Test
    void shouldBeEqual() {
        var source = this.makeBuilder()
                .withConnectedTimestamp(Instant.now().toEpochMilli())
                .build();
        var target = ClientDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private ClientDTO.Builder makeBuilder() {
        return ClientDTO.builder()
                .withServiceId(SERVICE_ID)
                .withRoomId(ROOM_ID)
                .withCallId(UUID.randomUUID())
                .withClientId(UUID.randomUUID())
                .withConnectedTimestamp(Instant.now().toEpochMilli())
                ;
    }

}