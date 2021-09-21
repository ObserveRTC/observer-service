package org.observertc.webrtc.observer.entities;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class ClientEntityTest {

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Test
    void shouldBuild() {
        ClientEntity source = this.entitiesTestUtils.getClientEntity();

        ClientEntity target = ClientEntity.builder().from(source).build();

        Assertions.assertEquals(source, target);
    }
}