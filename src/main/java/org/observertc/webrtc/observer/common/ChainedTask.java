package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.functions.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Chained Task is a sequence of executable {@link TaskStage}s, each task stage
 * is connected to the next one if the prev stage has output.
 * @param <T>
 */
public class ChainedTask<T> extends TaskAbstract<T> {
    public static<R> Builder<R> builder() {
        return new Builder<>(new ChainedTask<>());
    }

    protected ChainedTask() {

    }

    private AtomicReference<T> resultHolder = new AtomicReference<>();
    private LinkedList<TaskStage> stages = new LinkedList<>();
    private Map<Integer, BreakCondition> terminals = new HashMap<>();
    private int executedNumberOfStages = 0;
    private AtomicReference lastInput;

    public TaskAbstract<T> execute(Object input) {
        this.lastInput = new AtomicReference(input);
        return this.execute();
    }

    @Override
    protected T perform() throws Throwable {
        AtomicReference nextInput = this.lastInput;
        ListIterator<TaskStage> it = this.stages.listIterator(this.executedNumberOfStages);
        for(; it.hasNext(); ++this.executedNumberOfStages) {
            TaskStage stage = it.next();
            nextInput = stage.execute(nextInput);
            if (!stage.isExecuted()) {
                throw new IllegalStateException("Execution of stage "+stage.toString()+" is interrupted due to not executed stage");
            }
            if (this.doTerminate(stage, nextInput)) {
                return this.resultHolder.get();
            }
            this.lastInput = nextInput;
        }
        return this.resultHolder.get();
    }

    private boolean doTerminate(TaskStage stage, AtomicReference nextInput) throws Throwable {
        BreakCondition terminal = this.terminals.get(this.executedNumberOfStages);
        if (Objects.isNull(terminal)) {
            return false;
        }
        boolean terminate = false;
        if (Objects.nonNull(nextInput)) {
            if (Objects.nonNull(terminal.biFuncTerminal)) {
                terminate = terminal.biFuncTerminal.apply(nextInput.get(), this.resultHolder);
            } else {
                getLogger().warn("Cannot evaluate breakCondition after Stage {}, becasue the stage produced output, but the breakpoint does not have the proper function to evaluate", stage);
            }
        } else {
            if (Objects.nonNull(terminal.funcTerminal)) {
                terminate = terminal.funcTerminal.apply(this.resultHolder);
            } else {
                getLogger().warn("Cannot evaluate breakCondition after Stage {}, becasue the stage not procceded output, but the breakpoint does not have the proper function to evaluate", stage);
            }
        }
        return terminate;
    }

    @Override
    public void rollback(Throwable t) {
        ListIterator<TaskStage> it = this.stages.listIterator(this.executedNumberOfStages);
        for(; it.hasPrevious(); --this.executedNumberOfStages) {
            TaskStage stage = it.previous();
            stage.rollback(t);
        }
    }

    ChainedTask<T> addBreakCondition(BiFunction<Object, AtomicReference<T>, Boolean> biFuncTerminal, Function<AtomicReference<T>, Boolean> funcTerminal) {
        int index = this.stages.size() - 1;
        if (index < 0) {
            throw new IllegalStateException("Cannot add terminal condition without an added stage");
        }
        BreakCondition breakCondition = new BreakCondition(biFuncTerminal, funcTerminal);
        this.terminals.put(index, breakCondition);
        return this;
    }

    class BreakCondition {
        final BiFunction< Object, AtomicReference<T>, Boolean> biFuncTerminal;
        final Function<AtomicReference<T>, Boolean> funcTerminal;

        BreakCondition(BiFunction< Object, AtomicReference<T>, Boolean> biFuncTerminal, Function<AtomicReference<T>, Boolean> funcTerminal) {
            this.biFuncTerminal = biFuncTerminal;
            this.funcTerminal = funcTerminal;
        }
    }


    /**
     * Build a Chained Task and add Stages to that sequentially
     *
     * @param <R>
     */
    public static class Builder<R> {
        ChainedTask<R> result;
        boolean terminated;

        public ChainedTask<R> build() {
            return this.result;
        }

        public Builder(ChainedTask<R> result) {
            this.result = result;
            this.terminated = false;
        }

        /**
         * This is equal to call {@link this#addStage(TaskStage)}, and then call {@link this#addTerminalPassingStage(String)}
         *
         * @param stage
         * @return
         */
        public Builder<R> addTerminalStage(TaskStage stage) {
            this.result.stages.add(stage);
            return this.addTerminalPassingStage("Terminal Stage");
        }

        public<U, V> Builder<R> addSupplierEntry(String stageName, Supplier<V> supplier, Function<U, V> function) {
            this.requireNoStageIsAdded();
            return this.addStage(TaskStage.builder(stageName)
                    .<V>withSupplier(supplier)
                    .<U, V>withFunction(function)
                    .build()
            );
        }

        public<U> Builder<R> addConsumerEntry(String stageName, Action action, Consumer<U> consumer) {
            this.requireNoStageIsAdded();
            return this.addStage(TaskStage.builder(stageName)
                    .withAction(action)
                    .<U>withConsumer(consumer)
                    .build()
            );
        }

        public Builder<R> addStage(TaskStage stage) {
            this.requireNotTerminated();
            this.result.stages.add(stage);
            return this;
        }


        /**
         * Adds a predicate evaluated after the last added stage is executed.
         * This Break Condition assumes that the last executed stage has an output, and that is passed
         * as part of the predicate to be evaluate.
         * If the breaking condition is true than the execution flow of the chained task breaks.
         * The resultHolder of the task is passed as a second parameter of the evaluated predicate.
         * If the breaking condition is false than the output of the predecessor stage is passed to the
         * successor stage.
         * @param terminalCondition
         * @param <U>
         * @return {@link Builder} this builder object
         */
        public<U> Builder<R> addBreakCondition(BiFunction<U, AtomicReference<R>, Boolean> terminalCondition) {
            this.result.addBreakCondition((input, resultHolder) -> terminalCondition.apply((U) input, resultHolder), null);
            return this;
        }


        /**
         * Adds a predicate evaluated after the last added stage is executed.
         * This Break Condition assumes that the last executed stage has an output, and that is passed
         * as part of the predicate to be evaluate.
         * If the breaking condition is true than the execution flow of the chained task breaks.
         * The resultHolder of the task is passed as a parameter of the evaluated predicate.
         * If the breaking condition is false than the {@link ChainedTask} executes the successor stage
         * @param terminalCondition
         * @return {@link Builder} this builder object
         */
        public Builder<R> addBreakCondition(Function<AtomicReference<R>, Boolean> terminalCondition) {
            this.result.addBreakCondition(null,terminalCondition);
            return this;
        }

        /**
         * Adds a predicate evaluated after the last added stage is executed.
         * This Break Condition assumes that the last executed stage has an output, and taht is passed
         * as part of the predicate to be evaluate.
         * If the breaking condition is true than the execution flow of the chained task breaks.
         * The resultHolder of the task is passed as a second parameter of the evaluated predicate.
         *
         * This stage absorbs the output provided by the predecessor stage, so the successor stage is assumed
         * to have no need for input
         * It is handy when you have a chain which you want to break in certain condition,
         * and wants to start a new flow after the breaking point without any predecessor flow.
         * @param terminalCondition
         * @param <U>
         * @return {@link Builder} this builder object
         */
        public<U> Builder<R> addBreakConditionAndAbsorbInput(BiFunction<U, AtomicReference<R>, Boolean> terminalCondition) {
            this.result.addBreakCondition((input, resultHolder) -> terminalCondition.apply((U) input, resultHolder), null);
            return this.addConsumerStage("Absorb Input", inputObj -> {});
        }

        /**
         * A terminal stage passing consuming the output from the last stage and passing it to the task result,
         * or in case of no output from the last stage it set the result of the task to null
         * @param stageName
         * @return
         */
        public Builder<R> addTerminalPassingStage(String stageName) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withConsumer(inputObj -> {
                this.result.resultHolder.set((R)inputObj);
            }).withAction(() -> {
                this.result.resultHolder.set(null);
            }).build();
            this.result.stages.add(stage);
            this.terminated = true;
            return this;
        }

        public<U> Builder<R> addTerminalFunction(String stageName, Function<U, R> function) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withConsumer(inputObj -> {
                U passed = (U) inputObj;
                R output = function.apply(passed);
                this.result.resultHolder.set(output);
            }).build();
            this.result.stages.add(stage);
            this.terminated = true;
            return this;
        }

        public Builder<R> addTerminalSupplier(String stageName, Supplier<R> function) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withAction(() -> {
                R output = function.get();
                this.result.resultHolder.set(output);
            }).build();
            this.result.stages.add(stage);
            this.terminated = true;
            return this;
        }

        /**
         * Add a ChainedTask to this chainedTask and execute it as a stage.
         * This type of adding assuming the added chained task has no output, and
         * therefore no passing forward will be done.
         * @param stageName
         * @param chainedTask
         * @param <U>
         * @return
         */
        public<U> Builder<R> addConsumerChainedTask(String stageName, ChainedTask<U> chainedTask) {
            this.requireNotTerminated();
            chainedTask.withRethrowingExceptions(true);
            return this.addStage(TaskStage.builder(stageName)
                    .<U>withConsumer(input -> chainedTask.execute(input))
                    .withAction(() -> chainedTask.execute())
                    .build()
            );
        }

        /**
         * Add a ChainedTask to this chainedTask and execute it as a stage.
         * This type of adding assuming the added chained task has output, and
         * therefore passing it forward will be done.
         * @param stageName
         * @param chainedTask
         * @param <U>
         * @return
         */
        public<U> Builder<R> addSupplierChainedTask(String stageName, ChainedTask<U> chainedTask) {
            this.requireNotTerminated();
            chainedTask.withRethrowingExceptions(true);
            return this.addStage(TaskStage.builder(stageName)
                    .<U, Object>withFunction(input -> chainedTask.execute(input).getResult())
                    .<U>withSupplier(() -> chainedTask.execute().getResult())
                    .build()
            );
        }

        public<U> Builder<R> addSupplierStage(String stageName,
                                           Supplier<U> action,
                                           BiConsumer<AtomicReference, Throwable> rollback) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withSupplier(action).withRollback(rollback).build();
            this.result.stages.add(stage);
            return this;
        }

        public<U> Builder<R> addSupplierStage(String stageName, Supplier<U> action) {
            return this.<U>addSupplierStage(stageName, action, (inputHolder, thrown) -> {});
        }

        public<U, V> Builder<R> addFunctionalStage(String stageName, Function<U, V> action,
                                             BiConsumer<AtomicReference, Throwable> rollback) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withFunction(action).withRollback(rollback).build();
            this.result.stages.add(stage);
            return this;
        }

        public<K> Builder<R> addSupplierTask(String stageName, Task<K> task) {
            return this.addSupplierStage(stageName, () -> {
                if (!task.execute().succeeded()) {
                    throw new IllegalStateException("Task was not succeeded");
                }
                return task.getResult();
            });
        }

        public<K> Builder<R> addSupplierTaskIfInput(String stageName, Predicate predicate, Task<K> task) {
            return this.addFunctionalStage(stageName, inputObj -> {
                if (!predicate.test(inputObj)) {
                    return inputObj;
                }
                if (!task.execute().succeeded()) {
                    throw new IllegalStateException("Task was not succeeded");
                }
                return task.getResult();
            });
        }

        public<U, V> Builder<R> addFunctionalStage(String stageName, Function<U, V> action) {
            return this.addFunctionalStage(stageName, action, (inputHolder, thrown) -> {});
        }

        public<U> Builder<R> addConsumerStage(String stageName, Consumer<U> action,
                                           BiConsumer<AtomicReference, Throwable> rollback) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withConsumer(action).withRollback(rollback).build();
            this.result.stages.add(stage);
            return this;
        }

        public<U> Builder<R> addConsumerStage(String stageName, Consumer<U> action) {
            return this.addConsumerStage(stageName, action, (inputHolder, thrown) -> {});
        }

        public Builder<R> addActionStage(String stageName,
                                         Action action,
                                         BiConsumer<AtomicReference, Throwable> rollback) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withAction(action).withRollback(rollback).build();
            this.result.stages.add(stage);
            return this;
        }

        public<K> Builder<R> addActionTask(String stageName, Task<K> task) {
            return this.addActionStage(stageName, () -> {
                if (!task.execute().succeeded()) {
                    throw new IllegalStateException("Task was not succeeded");
                }
            });
        }

        public Builder<R> addActionStage(String stageName,
                                         Action action) {
            return this.addActionStage(stageName, action, (r, t) -> {});
        }


        public Builder<R> withLockProvider(java.util.function.Supplier<AutoCloseable> value) {
            this.result.withLockProvider(value);
            return this;
        }

        private void requireNotTerminated() {
            if (this.terminated) {
                throw new IllegalStateException("Terminated Stage has already been set");
            }
        }

        private void requireNoStageIsAdded() {
            if (0 < this.result.stages.size()) {
                throw new IllegalStateException("The operation requires to not having any added stage before");
            }
        }
    }
}
