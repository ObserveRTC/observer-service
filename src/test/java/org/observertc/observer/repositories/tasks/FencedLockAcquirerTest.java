package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class FencedLockAcquirerTest {
    @Inject
    Provider<FencedLockAcquirer> createCallIfNotExistsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldLock() {
        // not used actually
    }
}