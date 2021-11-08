package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class CallDTOTest {

    @Inject
    CallDTOGenerator generator;


    @Test
    void shouldBuild_1() {
        CallDTO source = this.generator.get();
        CallDTO target = CallDTO.builder()
                .from(source)
                .build();

        Assertions.assertEquals(source, target);
    }

    @Test
    void shouldThrowException_1() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            CallDTO.builder().build();
        });
    }

}