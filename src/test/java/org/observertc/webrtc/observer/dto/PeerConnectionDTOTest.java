package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

@MicronautTest
class PeerConnectionDTOTest {

    @Test
    void shouldBuild_1() {
        UUID callId = UUID.randomUUID();
        UUID peerConnectionId = UUID.randomUUID();
        Long initiated = Instant.now().toEpochMilli();

        // TODO: write the test
        Assertions.assertTrue(false);
    }

    @Test
    void shouldThrowException_1() {
        UUID callUUID = UUID.randomUUID();
        UUID serviceUUID = null;
        Long initiated = Instant.now().toEpochMilli();

        // TODO: write the test
        Assertions.assertTrue(false);
    }

}