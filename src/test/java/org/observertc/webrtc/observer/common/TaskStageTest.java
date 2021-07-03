package org.observertc.webrtc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class TaskStageTest {

    @Test
    void shouldRunAction() throws Throwable {
        AtomicBoolean result = new AtomicBoolean(false);
        TaskStage stage = TaskStage
                .builder("test")
                .withAction(() -> result.set(true))
                .build();

        stage.execute(null);

        Assertions.assertTrue(result.get());
    }

    @Test
    void shouldNotRunActionWithNullInput() throws Throwable {
        AtomicBoolean result = new AtomicBoolean(false);
        TaskStage stage = TaskStage
                .builder("test")
                .withAction(() -> result.set(true))
                .build();

        stage.execute(new AtomicReference(null));

        Assertions.assertFalse(result.get());
    }

    @Test
    void shouldRunConsumer() throws Throwable {
        AtomicBoolean result = new AtomicBoolean();
        TaskStage stage = TaskStage
                .builder("test")
                .<AtomicBoolean>withConsumer(ref -> ref.set(true))
                .build();

        stage.execute(new AtomicReference(result));

        Assertions.assertTrue(result.get());
    }

    @Test
    void shouldRunConsumerWithNullInput() throws Throwable {
        AtomicBoolean result = new AtomicBoolean();
        TaskStage stage = TaskStage
                .builder("test")
                .<AtomicBoolean>withConsumer(ref -> result.set(true))
                .build();

        stage.execute(new AtomicReference(null));

        Assertions.assertTrue(result.get());
    }

    @Test
    void shouldNotRunConsumerWithNullInputHolder() throws Throwable {
        AtomicBoolean result = new AtomicBoolean();
        TaskStage stage = TaskStage
                .builder("test")
                .<AtomicBoolean>withConsumer(ref -> result.set(true))
                .build();

        stage.execute(null);

        Assertions.assertFalse(result.get());
    }

    @Test
    void shouldRunSupplier() throws Throwable {
        TaskStage stage = TaskStage
                .builder("test")
                .<Integer>withSupplier(() -> 1)
                .build();

        AtomicReference<Integer> result = stage.execute(null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.get());
    }

    @Test
    void shouldRunSupplierWithNullInputHolder() throws Throwable {
        TaskStage stage = TaskStage
                .builder("test")
                .<Integer>withSupplier(() -> 10)
                .build();

        AtomicReference<Integer> result = stage.execute(null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.get());
    }

    @Test
    void shouldNotRunSupplierWithNullInput() throws Throwable {
        TaskStage stage = TaskStage
                .builder("test")
                .<Integer>withSupplier(() -> 10)
                .build();

        AtomicReference<Integer> result = stage.execute(new AtomicReference(null));

        Assertions.assertNull(result);
    }

    @Test
    void shouldRunFunction() throws Throwable {
        TaskStage stage = TaskStage
                .builder("test")
                .<Integer, Integer>withFunction(num -> num + 1)
                .build();

        AtomicReference<Integer> result = stage.execute(new AtomicReference<Integer>(1));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.get());
    }



    @Test
    void shouldRunFunctionWithNullInput() throws Throwable {
        TaskStage stage = TaskStage
                .builder("test")
                .<Integer,Integer>withFunction(num -> 1)
                .build();

        AtomicReference<Integer> result = stage.execute(new AtomicReference(null));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.get());
    }

    @Test
    void shouldNotRunFunctionWithNullInputHolder() throws Throwable {
        TaskStage stage = TaskStage
                .builder("test")
                .<Integer,Integer>withFunction(num -> num + 1)
                .build();

        AtomicReference<Integer> result = stage.execute(null);

        Assertions.assertNull(result);
    }


    @Test
    void shouldThrowExceptionNotExecutedConsumer() throws Throwable {
        AtomicBoolean result = new AtomicBoolean();
        TaskStage stage = TaskStage
                .builder("test")
                .<AtomicBoolean>withConsumer(ref -> result.set(true))
                .throwExceptionIfNotExecuted(true)
                .build();

        Assertions.assertThrows(IllegalStateException.class, () -> stage.execute(null));
    }

    @Test
    void shouldRetry() throws Throwable {
        AtomicInteger run = new AtomicInteger(0);
        TaskStage stage = TaskStage
                .builder("test")
                .withAction(() -> {
                    if (run.getAndIncrement() < 1) {
                        throw new RuntimeException();
                    }
                })
                .withMaxExecutionRetry(3)
                .build();
        stage.execute(null);

        Assertions.assertEquals(2, run.get());
    }

    @Test
    void shouldRollBackActionWhenItsExecuted() throws Throwable {
        TaskStage stage = TaskStage
                .builder("test")
                .withAction(() -> { })
                .withRollback((inputHolder, thrown)-> {
                    Assertions.assertNull(inputHolder);
                    Assertions.assertNull(thrown);

                })
                .build();
        stage.execute(null);
        stage.rollback(null);

        Assertions.assertTrue(stage.isExecuted());
        Assertions.assertTrue(stage.isRolledback());
    }

    @Test
    void shouldRollBackSupplierWhenItsExecuted() throws Throwable {
        TaskStage stage = TaskStage
                .builder("test")
                .withSupplier(() -> {  return 1; })
                .withRollback((inputHolder, thrown)-> {
                    Assertions.assertNull(inputHolder);
                    Assertions.assertNull(thrown);

                })
                .build();
        stage.execute(null);
        stage.rollback(null);

        Assertions.assertTrue(stage.isExecuted());
        Assertions.assertTrue(stage.isRolledback());
    }

    @Test
    void shouldRollBackConsumerWhenItsExecuted() throws Throwable {
        Object obj = new Object();
        TaskStage stage = TaskStage
                .builder("test")
                .withConsumer(input -> {  })
                .withRollback((inputHolder, thrown)-> {
                    Assertions.assertNotNull(inputHolder);
                    Assertions.assertEquals(obj, inputHolder.get());
                    Assertions.assertNull(thrown);
                })
                .build();

        stage.execute(new AtomicReference(obj));
        stage.rollback(null);

        Assertions.assertTrue(stage.isExecuted());
        Assertions.assertTrue(stage.isRolledback());
    }

    @Test
    void shouldRollBackFunctionWhenItsExecuted() throws Throwable {
        Object obj = new Object();
        TaskStage stage = TaskStage
                .builder("test")
                .withFunction(input -> 1)
                .withRollback((inputHolder, thrown)-> {
                    Assertions.assertNotNull(inputHolder);
                    Assertions.assertEquals(obj, inputHolder.get());
                    Assertions.assertNull(thrown);
                })
                .build();

        stage.execute(new AtomicReference(obj));
        stage.rollback(null);

        Assertions.assertTrue(stage.isExecuted());
        Assertions.assertTrue(stage.isRolledback());
    }

    @Test
    void shouldNotRollBackActionWhenItsNotExecuted() throws Throwable {
        TaskStage stage = TaskStage
                .builder("test")
                .withAction(() -> { throw new RuntimeException(); })
                .withRollback((inputHolder, thrown)-> {
                })
                .build();

        try {
            stage.execute(null);
        } catch (Throwable t) {
            stage.rollback(t);
        }

        Assertions.assertFalse(stage.isRolledback());
    }

    @Test
    void shouldNotBuildAmbiguousFlow_1() throws Throwable {
        Assertions.assertThrows(IllegalStateException.class, ()->{
            TaskStage stage = TaskStage
                    .builder("test")
                    .<Integer>withConsumer(num -> {})
                    .<Integer, Integer>withFunction(ref -> 1)
                    .build();
        });
    }

    @Test
    void shouldNotBuildAmbiguousFlow_2() throws Throwable {
        Assertions.assertThrows(IllegalStateException.class, ()->{
            TaskStage stage = TaskStage
                    .builder("test")
                    .withAction(() -> {})
                    .<Integer>withSupplier(() -> 1)
                    .build();
        });
    }
}