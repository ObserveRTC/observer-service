package org.observertc.webrtc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.configs.stores.SentinelsStore;

import javax.inject.Inject;

@MicronautTest
class SentinelsStoreTest {

    @Inject
    SentinelsStore sentinelsStore;

    @Inject
    HazelcastMapTestUtils testUtils;

    @Test
    void shouldFind() {


    }

}