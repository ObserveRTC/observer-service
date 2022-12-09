package org.observertc.observer.repositories.tasks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.common.Sleeper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

class CommitAbstractTest {


    @Test
    public void invokedWhenCompleted() throws ExecutionException, InterruptedException, TimeoutException {
        var commit = this.create(() -> {
            new Sleeper(() -> 100).run();
        });
        commit.execute().get(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void onlyInvokedOnce() throws ExecutionException, InterruptedException, TimeoutException {
        var invoked = new AtomicInteger(0);
        var commit = this.create(() -> {
            invoked.incrementAndGet();
            new Sleeper(() -> 100).run();
        });
        commit.execute();
        commit.execute();
        commit.execute().get(1000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1, invoked.get());
    }

    @Test
    public void onlyInvokedOnce_1() throws ExecutionException, InterruptedException, TimeoutException {
        var invoked = new AtomicInteger(0);
        var commit = this.create(() -> {
            invoked.incrementAndGet();
            new Sleeper(() -> 1000).run();
        });
        // process give a lock
        commit.lock();
        // another schedules an execute
        commit.execute();
        // but the one locked it has important stuff to complete
        new Sleeper(() -> 3000).run();

        // finally it unlocks and execute
        commit.unlock();
        commit.execute().get(3000, TimeUnit.MILLISECONDS);

        // still invoked once
        Assertions.assertEquals(1, invoked.get());
    }


    private CommitAbstract create(Runnable runnable) {
        return new CommitAbstract("Test", 30000) {

            @Override
            protected void process() throws Throwable {
                runnable.run();
            }
        };
    }
}