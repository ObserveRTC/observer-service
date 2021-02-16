package org.observertc.webrtc.observer.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.repositories.tasks.WeakLockProvider;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@MicronautTest
class WeakLockProviderTest {
    @Inject
    WeakLockProvider weakLockProvider;

    @Test
    public void shouldBeLocked() throws InterruptedException {
        final String lockName = "lock";
        AtomicReference<Instant> first = new AtomicReference<>();
        AtomicReference<Instant> second = new AtomicReference<>();
        new Thread(() -> {
            try (var lock = weakLockProvider.autoLock(lockName)) {
                Thread.sleep(2000);
                first.set(Instant.now());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).start();
        Thread.sleep(1000);

        try (var lock = weakLockProvider.autoLock(lockName)) {
            second.set(Instant.now());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Assertions.assertNotNull(first.get());
        Assertions.assertNotNull(second.get());
        Assertions.assertTrue(first.get().toEpochMilli() < second.get().toEpochMilli());
    }
}