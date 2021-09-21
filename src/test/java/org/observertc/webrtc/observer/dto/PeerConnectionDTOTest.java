package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class PeerConnectionDTOTest {

    @Inject
    PeerConnectionDTOGenerator generator;

    @Test
    void shouldBuild_1() {
        PeerConnectionDTO source = this.generator.get();
        PeerConnectionDTO target = PeerConnectionDTO.builder()
                .from(source)
                .build();

        Assertions.assertEquals(source, target);
    }

    @Test
    void shouldThrowException_1() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            PeerConnectionDTO.builder().build();
        });
    }

}