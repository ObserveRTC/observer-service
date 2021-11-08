package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class ClientDTOTest {

    @Inject
    ClientDTOGenerator generator;


    @Test
    void shouldBuild_1() {
        ClientDTO source = this.generator.get();
        ClientDTO target = ClientDTO.builder()
                .from(source)
                .build();

        Assertions.assertEquals(source, target);
    }

    @Test
    void shouldThrowException_1() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            ClientDTO.builder().build();
        });
    }

}