package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.functions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ChainedTask<T> extends TaskAbstract<T> {
    public static<R> Builder<R> builder() {
        return new Builder<>(new ChainedTask<>());
    }

    protected ChainedTask() {

    }

    private AtomicReference<T> resultHolder = new AtomicReference<>();
    private LinkedList<TaskStage> stages = new LinkedList<>();
    private Map<Integer, BiFunction<AtomicReference, AtomicReference<T>, Boolean> > terminals = new HashMap<>();
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
                throw new IllegalStateException("Execution of stage is interrupted du to not executed stage");
            }
            BiFunction<AtomicReference, AtomicReference<T>, Boolean> terminal = this.terminals.get(this.executedNumberOfStages);
            if (Objects.nonNull(terminal)) {
                if (terminal.apply(nextInput, this.resultHolder)) {
                    return this.resultHolder.get();
                }
            }
            this.lastInput = nextInput;
        }
        return this.resultHolder.get();
    }

    @Override
    public void rollback(Throwable t) {
        ListIterator<TaskStage> it = this.stages.listIterator(this.executedNumberOfStages);
        for(; it.hasPrevious(); --this.executedNumberOfStages) {
            TaskStage stage = it.previous();
            stage.rollback(t);
        }
    }

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

        public Builder<R> addTerminalStage(TaskStage stage) {
            this.requireNotTerminated();
            this.result.stages.add(stage);
            TaskStage terminalStage = TaskStage.builder("Terminal Stage").withConsumer(input -> {
                result.resultHolder.set((R) input);
            }).build();
            this.result.stages.add(terminalStage);
            this.terminated = true;
            return this;
        }

        public Builder<R> addStage(TaskStage stage) {
            this.requireNotTerminated();
            this.result.stages.add(stage);
            return this;
        }

        public Builder<R> addTerminalCondition(BiFunction<AtomicReference, AtomicReference<R>, Boolean> terminalCondition) {
            int index = this.result.stages.size() - 1;
            if (index < 0) {
                throw new IllegalStateException("Cannot add terminal condition without an added stage");
            }
            this.result.terminals.put(index, terminalCondition);
            return this;
        }

        public Builder<R> addTerminalConditionAndAbsorbInput(BiFunction<AtomicReference, AtomicReference<R>, Boolean> terminalCondition) {
            int index = this.result.stages.size() - 1;
            if (index < 0) {
                throw new IllegalStateException("Cannot add terminal condition without an added stage");
            }
            this.result.terminals.put(index, terminalCondition);
            return this.addConsumerStage("Absorb Input", inputObj -> {});
        }

        public Builder<R> addTerminalSupplerStage(String stageName,
                                                  Supplier action,
                                                  BiConsumer<AtomicReference, Throwable> rollback) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withAction(() -> {
                R output = (R) action.get();
                this.result.resultHolder.set(output);
            }).withRollback(rollback).build();
            this.result.stages.add(stage);
            this.terminated = true;
            return this;
        }

        public Builder<R> addTerminalStageConverter(String stageName, Function<Object, R> converter) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withConsumer(inputObj -> {
                R output = converter.apply(inputObj);
                this.result.resultHolder.set(output);
            }).build();
            this.result.stages.add(stage);
            this.terminated = true;
            return this;
        }

        public Builder<R> addTerminalCondition(String stageName,
                                               Function action,
                                               BiConsumer<AtomicReference, Throwable> rollback) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withConsumer(input -> {
                R output = (R) action.apply(input);
                this.result.resultHolder.set(output);
            }).withRollback(rollback).build();
            this.result.stages.add(stage);
            this.terminated = true;
            return this;
        }

        public Builder<R> addSupplierStage(String stageName,
                                           Supplier action,
                                           BiConsumer<AtomicReference, Throwable> rollback) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withSupplier(action).withRollback(rollback).build();
            this.result.stages.add(stage);
            return this;
        }

        public Builder<R> addSupplierStage(String stageName, Supplier action) {
            return this.addSupplierStage(stageName, action, (inputHolder, thrown) -> {});
        }

        public Builder<R> addFunctionalStage(String stageName, Function action,
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

        public Builder<R> addFunctionalStage(String stageName, Function action) {
            return this.addFunctionalStage(stageName, action, (inputHolder, thrown) -> {});
        }

        public Builder<R> addConsumerStage(String stageName, Consumer action,
                                           BiConsumer<AtomicReference, Throwable> rollback) {
            this.requireNotTerminated();
            TaskStage stage = TaskStage.builder(stageName).withConsumer(action).withRollback(rollback).build();
            this.result.stages.add(stage);
            return this;
        }

        public Builder<R> addConsumerStage(String stageName, Consumer action) {
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

    }
}
