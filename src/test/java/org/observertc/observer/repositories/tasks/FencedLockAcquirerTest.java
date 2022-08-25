package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HamokStorages;

@MicronautTest
class FencedLockAcquirerTest {
    @Inject
    BeanProvider<FencedLockAcquirer> createCallIfNotExistsTaskProvider;

    @Inject
    HamokStorages hamokStorages;

    @Test
    void shouldLock() {
        // not used actually
    }
}