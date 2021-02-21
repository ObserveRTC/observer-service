package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;

@MicronautTest
class WeakSpinLockTest {

    @Inject
    WeakLockProvider weakLockProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldLockAndUnlockMaps_1() {
        var lockName = "shouldLockAndUnlockMaps_1";
        try (var lock = weakLockProvider.autoLock(lockName)) {
            Assertions.assertNotNull(hazelcastMaps.getWeakLocks().get(lockName));
        } catch (Exception ex) {

        }

        Assertions.assertNull(hazelcastMaps.getWeakLocks().get(lockName));
    }

    @Test
    void shouldLockAndUnlockMaps_2() {
        var lockName = "shouldLockAndUnlockMaps_2";
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

//    @Test
//    void shouldWaitAndThenReplace_1() {
//        var lockName = "shouldWaitAndThenReplace_1";
//        Instant started = Instant.now();
//
//        try (var lock = weakLockProvider.autoLock(lockName)) {
//            try (var aggressiveLock = weakLockProvider.autoLock(lockName, 10)) {
//
//            }
//        } catch (Exception ex) {
//
//        }
//
//        long elapsedInS = Duration.between(started, Instant.now()).getSeconds();
//        Assertions.assertTrue(8 < elapsedInS, "elapsed time was " + elapsedInS + "s");
//        System.out.println("elapsed time is " + elapsedInS);
//    }



}