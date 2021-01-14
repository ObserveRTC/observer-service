package org.observertc.webrtc.observer.tasks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class TaskAbstractTest {

    @Test
    public void shouldExecute() {
        // Given
        AtomicBoolean executed = new AtomicBoolean(false);
        TaskAbstract<Void> task = new TaskAbstract<Void>() {
            @Override
            protected Void perform() throws Throwable {
                executed.set(true);
                return null;
            }
        };

        // When
        task.execute();

        // Given
        Assertions.assertTrue(task.succeeded());
        Assertions.assertTrue(executed.get());
    }

    @Test
    public void shouldRetry() {
        // Given
        AtomicInteger increased = new AtomicInteger(0);
        AtomicBoolean executed = new AtomicBoolean(false);
        TaskAbstract<Void> task = new TaskAbstract<Void>() {
            @Override
            protected Void perform() throws Throwable {
                if (increased.compareAndSet(0, 1)) {
                    throw new RuntimeException();
                }
                executed.set(true);
                return null;
            }
        };

        // When
        task.withMaxRetry(2).execute();

        // Given
        Assertions.assertTrue(task.succeeded());
        Assertions.assertTrue(executed.get());
    }

    @Test
    public void shouldRollback() {
        // Given
        AtomicBoolean rollback = new AtomicBoolean(false);
        TaskAbstract<Void> task = new TaskAbstract<Void>() {
            @Override
            protected Void perform() throws Throwable {
                throw new RuntimeException();
            }

            @Override
            protected void rollback(Throwable t) {
                rollback.set(true);
            }
        };

        // When
        task.execute();

        // Given
        Assertions.assertFalse(task.succeeded());
        Assertions.assertTrue(rollback.get());
    }

    @Test
    public void shouldGetDefaultResultAfterRollback() {
        // Given
        AtomicBoolean rollback = new AtomicBoolean(false);
        TaskAbstract<Integer> task = new TaskAbstract<Integer>() {
            @Override
            protected Integer perform() throws Throwable {
                throw new RuntimeException();
            }

            @Override
            protected void rollback(Throwable t) {
                rollback.set(true);
            }
        };

        // When
        task.execute();

        // Given
        Assertions.assertEquals(1, task.getResultOrDefault(1));
    }

    @Test
    public void shouldReturnResult() {
        // Given
        AtomicBoolean rollback = new AtomicBoolean(false);
        TaskAbstract<Integer> task = new TaskAbstract<Integer>() {
            @Override
            protected Integer perform() throws Throwable {
                return 1;
            }

            @Override
            protected void rollback(Throwable t) {
                rollback.set(true);
            }
        };

        // When
        task.execute();

        // Given
        Assertions.assertTrue(task.succeeded());
        Assertions.assertEquals(1, task.getResult());
        Assertions.assertFalse(rollback.get());
    }

    @Test
    public void shouldResultNull() {
        // Given
        AtomicBoolean rollback = new AtomicBoolean(false);
        TaskAbstract<Integer> task = new TaskAbstract<Integer>() {
            @Override
            protected Integer perform() throws Throwable {
                return null;
            }

            @Override
            protected void rollback(Throwable t) {
                rollback.set(true);
            }
        };

        // When
        task.execute();

        // Given
        Assertions.assertTrue(task.succeeded());
        Assertions.assertEquals(null, task.getResult());
        Assertions.assertFalse(rollback.get());
    }

    @Test
    public void whenNotExecutedShouldThrowException() {
        // Given
        AtomicBoolean rollback = new AtomicBoolean(false);
        TaskAbstract<Integer> task = new TaskAbstract<Integer>() {
            @Override
            protected Integer perform() throws Throwable {
                return null;
            }

            @Override
            protected void rollback(Throwable t) {
                rollback.set(true);
            }
        };

        // When we do not execute the task
        // task.execute();

        // Given
        Assertions.assertThrows(IllegalStateException.class, () -> {
            task.getResult();
        });
        Assertions.assertThrows(IllegalStateException.class, () -> {
            task.succeeded();
        });
        Assertions.assertThrows(IllegalStateException.class, () -> {
            task.getResultOrDefault(null);
        });
        Assertions.assertThrows(IllegalStateException.class, () -> {
            task.getResultOrDefaultIfNull(null);
        });
        Assertions.assertFalse(rollback.get());
    }


}