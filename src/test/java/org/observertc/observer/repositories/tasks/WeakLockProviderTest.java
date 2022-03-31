package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class WeakLockProviderTest {

    @Inject
    WeakLockProvider weakLockProvider;

    @Test
    void shouldProvideAWeaklock() {
        var lockName = "lock";
        var weakLock = weakLockProvider.makeSpinLock(lockName);

        Assertions.assertNotNull(weakLock);
    }

    @Test
    void shouldDispose() throws Exception {
        var lockName = "lock";
        var weakLock = weakLockProvider.autoLock(lockName);

        Assertions.assertNotNull(weakLock);
    }
}