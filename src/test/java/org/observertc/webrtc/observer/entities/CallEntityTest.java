package org.observertc.webrtc.observer.entities;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class CallEntityTest {

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Test
    void shouldBuild() {
        CallEntity source = this.entitiesTestUtils.getCallEntity();

        CallEntity target = CallEntity.builder().from(source).build();

        Assertions.assertEquals(source, target);
    }
}