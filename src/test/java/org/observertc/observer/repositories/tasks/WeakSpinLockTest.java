package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.functions.Action;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

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
        boolean exceptionBranchIsTouched = false;

        try (var lock = weakLockProvider.autoLock(lockName)) {
            throw new RuntimeException("exception throw during locked operation");
        } catch (Exception ex) {
            exceptionBranchIsTouched = true;
        }

        Assertions.assertNull(hazelcastMaps.getWeakLocks().get(lockName));
        Assertions.assertTrue(exceptionBranchIsTouched);
    }


    @Test
    void shouldMakeAccessExclusive() throws InterruptedException, ExecutionException, TimeoutException {
        var lockName = "shouldMakeAccessExclusive";
        AtomicBoolean inCriticalSection = new AtomicBoolean(false);
        CompletableFuture<Void> done = new CompletableFuture<>();
        var alice = new Actor.Builder()
                .withName("Alice")
                .withWaitingTimeInMs(100)
                .withAction(() -> {
                    try (var lock = weakLockProvider.autoLock(lockName)) {
                        Assertions.assertFalse(inCriticalSection.get());
                        inCriticalSection.set(true);
                        Thread.sleep(2000);
                        inCriticalSection.set(false);
                    }
                }).build();

        var bob = new Actor.Builder()
                .withName("Bob")
                .withWaitingTimeInMs(500)
                .withCompletableFuture(done)
                .withAction(() -> {
                    Assertions.assertTrue(inCriticalSection.get());
                    try (var lock = weakLockProvider.autoLock(lockName)) {
                        Assertions.assertFalse(inCriticalSection.get());
                        inCriticalSection.set(true);
                        Thread.sleep(1000);
                        inCriticalSection.set(false);
                    }
                }).build();

        alice.start();
        bob.start();
        done.get(15000, TimeUnit.MILLISECONDS);
    }

    static class Actor implements Runnable {
        private Thread thread;
        private String name;
        private Action action;



        Actor() {

        }

        public void run() {
            try {
                this.action.run();
            } catch (Throwable throwable) {
                new RuntimeException(this.name + " exception " + throwable.getMessage());
            }
        }

        public Actor start () {
            if (thread == null) {
                thread = new Thread (this, name);
                thread.start();
            }
            return this;
        }

        static class Builder {
            private CompletableFuture<Void> callback;
            public int waitingTimeInMs = 0;
            public Action action;
            public Actor result = new Actor();

            public Builder withWaitingTimeInMs(int timeInMs) {
                this.waitingTimeInMs = timeInMs;
                return this;
            }

            public Builder withAction(Action value) {
                this.action = value;
                return this;
            }

            public Builder withCompletableFuture(CompletableFuture<Void> done) {
                this.callback = done;
                return this;
            }

            public Builder withName(String value) {
                this.result.name = value;
                return this;
            }

            public Actor build() {
                this.result.action = () -> {
                    if (0 < this.waitingTimeInMs) {
                        Thread.sleep(this.waitingTimeInMs);
                    }
                    action.run();
                    if (Objects.nonNull(this.callback)) {
                        this.callback.complete(null);
                    }
                };
                return this.result;
            }
        }
    }


}