package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;

@MicronautTest
class WeakSpinLockTest {

    @Inject
    WeakLockProvider weakLockProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldLockAndUnlockMaps_1() {
        var lockName = "myLock";
        try (var lock = weakLockProvider.autoLock(lockName)) {
            Assertions.assertNotNull(hazelcastMaps.getWeakLocks().get(lockName));
        } catch (Exception ex) {

        }

        Assertions.assertNull(hazelcastMaps.getWeakLocks().get(lockName));
    }

    @Test
    void shouldLockAndUnlockMaps_2() {
        var lockName = "myLock";
        boolean exceptionBranchIsTouched = false; // Trust no one!

        try (var lock = weakLockProvider.autoLock(lockName)) {
            throw new RuntimeException("exception throw during locked operation");
        } catch (Exception ex) {
            Assertions.assertNull(hazelcastMaps.getWeakLocks().get(lockName));
            exceptionBranchIsTouched = true;
        }

        Assertions.assertNull(hazelcastMaps.getWeakLocks().get(lockName));
        Assertions.assertTrue(exceptionBranchIsTouched);
    }

    @Test
    void shouldWaitAndThenReplace_1() {
        var lockName = "myLock";
        Instant started = Instant.now();

        try (var lock = weakLockProvider.autoLock(lockName)) {
            try (var aggressiveLock = weakLockProvider.autoLock(lockName, 10)) {

            }
        } catch (Exception ex) {

        }

        Assertions.assertTrue(9 < Duration.between(started, Instant.now()).getSeconds());
    }



}