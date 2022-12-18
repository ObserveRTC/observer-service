package org.observertc.observer;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.common.TaskAbstract;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@MicronautTest
class BackgroundTasksExecutorTest {

    @Inject
    BackgroundTasksExecutor backgroundTasksExecutor;

    @BeforeEach
    void setup() {
        this.backgroundTasksExecutor.start();
    }

    @AfterEach
    void teardown() {
        this.backgroundTasksExecutor.stop();
    }

    @Test
    void executeTaskPeriodically() throws ExecutionException, InterruptedException, TimeoutException {
        var periodicTaskName = BackgroundTasksExecutorTest.class.getSimpleName();
        var promise = new CompletableFuture<Void>();
        var invoked = new AtomicInteger(0);
        this.backgroundTasksExecutor.addPeriodicTask(
                periodicTaskName,
                () -> TaskAbstract.wrapRunnable(() -> {
                    if (invoked.incrementAndGet() < 2) return;
                    promise.complete(null);
                }),
                this.backgroundTasksExecutor.getBaseDelayInSec()
        );
        Assertions.assertTrue(this.backgroundTasksExecutor.hasPeriodicTask(periodicTaskName));

        promise.get(this.backgroundTasksExecutor.getBaseDelayInSec() * 10, TimeUnit.SECONDS);

        Assertions.assertEquals(2, invoked.get());

        this.backgroundTasksExecutor.removePeriodicTask(periodicTaskName);

        Assertions.assertFalse(this.backgroundTasksExecutor.hasPeriodicTask(periodicTaskName));
    }
}