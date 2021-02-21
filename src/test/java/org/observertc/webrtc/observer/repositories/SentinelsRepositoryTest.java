package org.observertc.webrtc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class SentinelsRepositoryTest {

    @Inject
    SentinelsRepository sentinelsRepository;

    @Inject
    HazelcastMapTestUtils testUtils;

    @Test
    void shouldFind() {


    }

}