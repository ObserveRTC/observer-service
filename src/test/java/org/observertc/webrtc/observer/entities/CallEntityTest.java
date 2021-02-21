package org.observertc.webrtc.observer.entities;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.DTOTestUtils;

import javax.inject.Inject;

@MicronautTest
class CallEntityTest {

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Inject
    DTOTestUtils dtoTestUtils;

    @Test
    void shouldBuild() {
        CallDTO callDTO = dtoTestUtils.generateCallDTO();
        CallEntity callEntity = CallEntity.builder()
                .withCallDTO(callDTO)
                .build();

        Assertions.assertEquals(callDTO, callEntity.call);
    }

}