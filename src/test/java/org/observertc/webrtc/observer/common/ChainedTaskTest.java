package org.observertc.webrtc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

class ChainedTaskTest {

    @Test
    void shouldRunConsumers() {
        AtomicInteger value = new AtomicInteger(0);
        var task = ChainedTask.builder()
                .<Integer>addConsumerStage("test", input -> value.set(input))
                .build();

        Assertions.assertTrue(task.execute(1).succeeded());
        Assertions.assertEquals(1, value.get());
    }

    @Test
    void shouldRunSuppliersAsTerminal() {
        var task = ChainedTask.builder()
                .<Integer>addTerminalSupplier("test", () -> 10)
                .build();

        Assertions.assertTrue(task.execute().succeeded());
        Assertions.assertEquals(10, task.getResult());
    }

    @Test
    void shouldRunFunctionsAsTerminal() {
        var task = ChainedTask.builder()
                .<Integer>addTerminalFunction("test", num -> num + 1)
                .build();

        Assertions.assertTrue(task.execute(10).succeeded());
        Assertions.assertEquals(11, task.getResult());
    }


    @Test
    void shouldRunConsumersAsEntryPoint() {
        AtomicInteger value = new AtomicInteger(0);
        var task = ChainedTask.builder()
                .<Integer>addConsumerStage("test", input -> value.set(input))
                .build();

        Assertions.assertThrows(Throwable.class, () -> {
            task.withRethrowingExceptions(true);
            task.execute("1");
        });
    }

    @Test
    void shouldRunPipeline_1() {
        int input = 3;
        var task = ChainedTask.builder()
                .<Integer, Integer>addFunctionalStage("stage 1", num -> num + 1)
                .<Integer, Integer>addFunctionalStage("stage 2", num -> num * 2)
                .<Integer>addTerminalFunction("stage 3", num -> num + 3)
                .build();


        Assertions.assertEquals(((input + 1) * 2) + 3, task.execute(input).getResult());
    }

    @Test
    void shouldRunPipeline_2() {
        AtomicBoolean run = new AtomicBoolean(false);
        AtomicInteger result1 = new AtomicInteger(0);
        var task = ChainedTask.builder()
                .<Integer, Integer>addFunctionalStage("stage 1", num -> num + 1)
                .<Integer>addConsumerStage("stage 2", num -> result1.set(num))
                .addActionStage("stage 3", () -> run.set(true))
                .<Integer>addTerminalSupplier("stage 4", () -> 100)
                .build();

        task.execute(10);
        Assertions.assertEquals(11, result1.get());
        Assertions.assertEquals(100, task.getResult());
        Assertions.assertTrue(run.get());
    }

    @Test
    void shouldRunPipeline_3() {
        var task = ChainedTask.builder()
                .addSupplierChainedTask("embedded chain task", ChainedTask.builder()
                        .<Integer, Integer>addFunctionalStage("embedded stage 1", num -> num + 1)
                        .addTerminalPassingStage("Embedded Completed")
                        .build())
                .<Integer, Integer>addFunctionalStage("test stage 1", num -> num + 1)
                .addTerminalPassingStage("Task Completed")
                .build();


        Assertions.assertEquals(3, task.execute(1).getResult());
    }

    @Test
    void shouldRunPipeline_4() {
        AtomicInteger resultHolder = new AtomicInteger();
        var task = ChainedTask.builder()
                .<Integer, Integer>addFunctionalStage("test stage 1", num -> num + 1)
                .addConsumerChainedTask("embedded chain task", ChainedTask.builder()
                        .<Integer, Integer>addFunctionalStage("embedded stage 1", num -> num + 1)
                        .addConsumerStage("setup", resultHolder::set)
                        .build())
                .build();

        task.execute(1);
        Assertions.assertEquals(3, resultHolder.get());
    }

    @Test
    void shouldRunPipeline_5() {
        var task = ChainedTask.builder()
                .<Integer>addSupplierStage("provide int", () -> 1)
                .<Integer, Double>addFunctionalStage("make double", num -> (double) num + 1.5)
                .<Double>addTerminalFunction("pass as result", num -> num.toString())
                .build();

        task.execute();

        Assertions.assertTrue(task.succeeded());
        Assertions.assertEquals("2.5", task.getResult());
    }



    @Test
    void shouldRollbackPipeline_1() {
        AtomicInteger state = new AtomicInteger(0);
        AtomicBoolean touched = new AtomicBoolean(false);
        var task = ChainedTask.builder()
                .addActionStage("stage 1", () -> state.incrementAndGet(), (resultHolder, thrown) -> state.decrementAndGet())
                .addActionStage("stage 2", () -> state.incrementAndGet(), (resultHolder, thrown) -> state.decrementAndGet())
                .addActionStage("stage 3", () -> state.incrementAndGet(), (resultHolder, thrown) -> state.decrementAndGet())
                .addActionStage("throwing exception", () -> {touched.set(true); throw new RuntimeException(); })
                .build();

        task.execute();

        Assertions.assertTrue(touched.get());
        Assertions.assertEquals(0, state.get());
    }

    @Test
    void shouldRollbackPipeline_2() {
        AtomicInteger state = new AtomicInteger(0);
        AtomicInteger touched = new AtomicInteger(0);
        var task = ChainedTask.builder()
                .addActionStage("stage 1", () -> state.incrementAndGet(), (resultHolder, thrown) -> state.decrementAndGet())
                .addActionStage("stage 2", () -> state.incrementAndGet(), (resultHolder, thrown) -> state.decrementAndGet())
                .addActionStage("throwing exception", () -> {touched.set(state.get()); throw new RuntimeException(); })
                .addActionStage("stage 3", () -> state.incrementAndGet(), (resultHolder, thrown) -> state.decrementAndGet())
                .build();

        task.execute();

        Assertions.assertEquals(2, touched.get());
        Assertions.assertEquals(0, state.get());
    }

    @Test
    void shouldHitBreakConditionPipeline_1() {
        Supplier<ChainedTask<Integer>> taskProvider = () -> ChainedTask.<Integer>builder()
                .<Integer, Integer>addFunctionalStage("test stage 1", num -> num + 1)
                .<Integer>addBreakCondition((num, resultHolder) -> {
                    if (10 < num) return false;
                    resultHolder.set(100);
                    return true;
                })
                .<Integer>addTerminalFunction("test stage 2", num -> 1000)
                .build();


        Assertions.assertEquals(100, taskProvider.get().execute(1).getResult());
        Assertions.assertEquals(1000, taskProvider.get().execute(10).getResult());
    }

    @Test
    void shouldHitBreakConditionPipeline_2() {
        Supplier<ChainedTask<Integer>> taskProvider = () -> ChainedTask.<Integer>builder()
                .<Integer, Integer>addFunctionalStage("test stage 1", num -> num + 1)
                .<Integer>addBreakCondition((num, resultHolder) -> {
                    if (10 < num) return false;
                    resultHolder.set(100); return true;
                })
                .<Integer, Integer>addFunctionalStage("test stage 2", num -> num + 1)
                .<Integer>addBreakCondition((num, resultHolder) -> {
                    if (100 < num) return false;
                    resultHolder.set(1000); return true;
                })
                .<Integer>addTerminalFunction("test stage 3", num -> 10000)
                .build();


        Assertions.assertEquals(100, taskProvider.get().execute(1).getResult());
        Assertions.assertEquals(1000, taskProvider.get().execute(10).getResult());
        Assertions.assertEquals(10000, taskProvider.get().execute(100).getResult());
    }


}