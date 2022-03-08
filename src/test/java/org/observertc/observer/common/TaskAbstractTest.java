package org.observertc.observer.common;

import io.reactivex.rxjava3.functions.Action;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


class TaskAbstractTest {

    @Test
    void shouldBeExecuted_1() {
        var task = this.makeTask(1);

        task.execute();

        Assertions.assertTrue(task.succeeded());
        Assertions.assertEquals(1, task.getResult());
    }

    @Test
    void shouldNotBeExecutedTwice_1() {
        var task = this.makeTask(1);

        task.execute();

        Assertions.assertThrows(Exception.class, () -> task.execute());
    }

    @Test
    void shouldBeExecutedTwice_1() {
        var task = this.makeOneTimeFailingTask(1);

        task.withMaxRetry(2).execute();

        Assertions.assertTrue(task.succeeded());
        Assertions.assertEquals(1, task.getResult());
    }

    @Test
    void shouldReThrowException_1() {
        var task = this.makeOneTimeFailingTask(1);

        task.withRethrowingExceptions(true);

        Assertions.assertThrows(RuntimeException.class, task::execute);
        Assertions.assertFalse(task.succeeded());
    }

    @Test
    void shouldThrowExceptionIfNotExecuted_1() {
        var task = this.makeOneTimeFailingTask(1);

        Assertions.assertThrows(Exception.class, () -> task.getResult());
    }

    @Test
    void shouldThrowExceptionIfNotExecuted_2() {
        var task = this.makeOneTimeFailingTask(1);

        Assertions.assertThrows(Exception.class, () -> task.succeeded());
    }

    @Test
    void shouldReturnDefaultIfExecutedButFailed_1() {
        var task = this.makeOneTimeFailingTask(1);

        task.execute();

        Assertions.assertEquals(2, task.getResultOrDefault(2));
    }

    @Test
    void shouldReturnDefaultIfExecutedButNull_2() {
        var task = this.makeTask(null);

        task.execute();

        Assertions.assertEquals(2, task.getResultOrDefaultIfNull(2));
    }

    @Test
    void shouldCloseLock_1() {
        AtomicBoolean closed = new AtomicBoolean(false);
        var task = this.makeTaskAndRun(null, () -> {Assertions.assertTrue(closed.get());});

        task.withLockProvider(() -> getLocker(closed)).execute();

        Assertions.assertTrue(task.succeeded());
        Assertions.assertFalse(closed.get());
    }

    @Test
    void shouldCloseLock_2() {
        AtomicBoolean closed = new AtomicBoolean(false);
        var task = this.makeOneTimeFailingTask(1);

        task.withLockProvider(() -> getLocker(closed)).execute();

        Assertions.assertFalse(task.succeeded());
        Assertions.assertFalse(closed.get());
    }

    private AutoCloseable getLocker(AtomicBoolean closed) {
        closed.set(true);
        return () -> closed.set(false);
    }

    private TaskAbstract<Integer> makeTask(Integer result) {
        return this.makeTaskAndRun(result, ()->{});
    }

    private TaskAbstract<Integer> makeTaskAndRun(Integer result, Action action) {
        return new TaskAbstract<Integer>() {
            @Override
            protected Integer perform() throws Throwable {
                if (Objects.nonNull(action)) {
                    action.run();
                }
                return result;
            }
        };
    }

    private TaskAbstract<Integer> makeOneTimeFailingTask(Integer result) {
        return this.makeTaskAndRun(result, new Action() {
            boolean failed = false;
            @Override
            public void run() throws Throwable {
                if (this.failed) return;
                this.failed = true;
                throw new RuntimeException("Failed by definition");
            }
        });
    }
}