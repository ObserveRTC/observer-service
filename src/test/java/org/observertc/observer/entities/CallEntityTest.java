package org.observertc.observer.entities;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;

@MicronautTest
class CallEntityTest {

    @Inject
    DTOGenerators generator;

    @Test
    void shouldHasExpectedValues() {
        var callDTO = this.generator.getCallDTO();
        var clientDTO = this.generator.getClientDTO();
        var clientEntity = ClientEntity.builder().withClientDTO(clientDTO).build();
        var callEntity = CallEntity.builder()
                .withCallDTO(callDTO)
                .withClientEntity(clientEntity)
                .build();

        boolean hasCallDTO = callEntity.getCallDTO().equals(callDTO);
        boolean hasClientDTO = callEntity.getClientEntity(clientEntity.getClientId()).equals(clientDTO);
        Assertions.assertTrue(hasCallDTO);
        Assertions.assertTrue(hasClientDTO);
    }

    @Test
    void shouldBeEquals() {
        var callDTO = this.generator.getCallDTO();
        var clientDTO = this.generator.getClientDTO();
        var clientEntity = ClientEntity.builder().withClientDTO(clientDTO).build();
        var actual = CallEntity.builder()
                .withCallDTO(callDTO)
                .withClientEntity(clientEntity)
                .build();
        var expected = CallEntity.builder().from(actual).build();

        boolean equals = actual.equals(expected);
        Assertions.assertTrue(equals);
    }
}