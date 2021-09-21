package org.observertc.webrtc.observer.entities;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class PeerConnectionEntityTest {

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Test
    void shouldBuild() {
        PeerConnectionEntity source = this.entitiesTestUtils.getPeerConnectionEntity();

        PeerConnectionEntity target = PeerConnectionEntity.builder().from(source).build();

        Assertions.assertEquals(source, target);
    }
}