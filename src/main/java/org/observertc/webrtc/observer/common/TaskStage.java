package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.functions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TaskStage {
    private static final Logger logger = LoggerFactory.getLogger(TaskStage.class);

    public static TaskStage.Builder builder(String stageName) {
        return new Builder(stageName);
    }

    private String name;
    private boolean throwExceptionIfNotExecuted = false;
    private boolean executed = false;
    private boolean rolledbacked = false;
    private Function onExecFunc;
    private Consumer onExecConsumer;
    private Supplier onExecSupplier;
    private Action onExecAction;
    private BiConsumer<AtomicReference, Throwable> onRollback = (p1, p2) -> {};
    private AtomicReference inputHolder = null;
    private int maxExecRetry = 1;
    private int maxRollbackRetry = 1;

    private TaskStage() {

    }

    public AtomicReference execute(AtomicReference inputHolder) throws Throwable{
        if (this.rolledbacked || this.executed) {
            logger.info("Stage {} is already executed ({}) or rolled back ({}) execution forward is not allowed", this.executed, this.rolledbacked);
            return null;
        }
        this.inputHolder = inputHolder;
        boolean executed = false;
        AtomicReference result = null;
        for (int tried = 0; ; ) {
            try {
                if (Objects.nonNull(this.inputHolder)) {
                    Object input = inputHolder.get();
                    if (Objects.nonNull(this.onExecFunc)) {
                        Object nextInput = this.onExecFunc.apply(input);
                        result = new AtomicReference(nextInput);
                        executed = true;
                    } else if (Objects.nonNull(this.onExecConsumer)){
                        this.onExecConsumer.accept(input);
                        result = null;
                        executed = true;
                    }
                } else {
                    if (Objects.nonNull(this.onExecSupplier)) {
                        Object nextInput = this.onExecSupplier.get();
                        result = new AtomicReference(nextInput);
                        executed = true;
                    } else if (Objects.nonNull(this.onExecAction)) {
                        this.onExecAction.run();
                        result = null;
                        executed = true;
                    }
                }
                break;
            } catch (Throwable t) {
                if (this.maxExecRetry <= ++tried) {
                    logger.warn("Stage {} is failed to execute", this.name, t);
                    throw t;
                }
                logger.info("Stage {} is attempted to execute, but got an error. tried: {}, maxRetry: {}", this.name, tried, this.maxExecRetry, t);
            }
        }
        this.executed = executed;
        if (!this.executed) {
            if (this.throwExceptionIfNotExecuted) {
                throw new IllegalStateException("Stage " + this.name + " was not executed");
            }
            logger.warn("Stage {} did not executed action", this.name);
            return null;
        }

        return result;
    }

    public boolean isExecuted() {
        return this.executed;
    }

    public boolean isRolledback() {
        return this.rolledbacked;
    }


    public void rollback(Throwable throwable)  {
        if (this.rolledbacked || !this.executed) {
            logger.info("Stage {} is either not executed ({}) or already rolled back ({})", this.executed, this.rolledbacked);
            return;
        }

        for (int tried = 0; ; ) {
            try {
                if (Objects.nonNull(this.onRollback)) {
                    this.onRollback.accept(this.inputHolder, throwable);
                }
                this.rolledbacked = true;
                break;
            } catch (Throwable t) {
                if (this.maxRollbackRetry <= ++tried) {
                    logger.warn("Stage {} is failed to roll back", this.name, t);
                    break;
                }
                logger.info("Stage {} is attempted to roll back, but got an error. tried: {}, maxRetry: {}", this.name, tried, this.maxRollbackRetry, t);
            }
        }
    }

    @Override
    public String toString() {
        java.util.function.Function<Object, String> ifNull = obj -> Objects.isNull(obj) ? "false": "true";
        return ObjectToString.toString(Map.of(
                "name", this.name,
                "func, consumer, supplier, action", String.format("%s, %s, %s, %s", ifNull.apply(this.onExecFunc), ifNull.apply(this.onExecConsumer), ifNull.apply(this.onExecSupplier), ifNull.apply(this.onExecAction)),
                "input", Objects.nonNull(this.inputHolder) ? Objects.nonNull(inputHolder.get()) ? inputHolder.get().toString() : "null Input" : "null inputHolder"
        ));
    }


    public static class Builder {
        private TaskStage result;
        private boolean rollbackSet = false;
        public Builder(String stageName) {
            this.result = new TaskStage();
            this.result.name = stageName;
        }

        public Builder withAction(Action action) {
            this.requireCleanExecFlow();
            this.result.onExecAction = action;
            return this;
        }

        public<R> Builder withConsumer(Consumer<R> action) {
            this.requireCleanExecFlow();
            this.result.onExecConsumer = action;
            return this;
        }

        public<U> Builder withSupplier(Supplier<U> action) {
            this.requireCleanExecFlow();
            this.result.onExecSupplier = action;
            return this;
        }

        public<U, V> Builder withFunction(Function<U, V> action) {
            this.requireCleanExecFlow();
            this.result.onExecFunc = action;
            return this;
        }

        public Builder withRollback(BiConsumer<AtomicReference, Throwable> action) {
            if (this.rollbackSet) {
                throw new IllegalStateException("Cannot set a rollback function twice");
            }
            this.result.onRollback = action;
            this.rollbackSet = true;
            return this;
        }

        public TaskStage.Builder withMaxExecutionRetry(int value) {
            this.result.maxExecRetry = value;
            return this;
        }

        public TaskStage.Builder withMaxRollbackRetry(int value) {
            this.result.maxRollbackRetry = value;
            return this;
        }

        public TaskStage.Builder throwExceptionIfNotExecuted(boolean value) {
            this.result.throwExceptionIfNotExecuted = value;
            return this;
        }

        public TaskStage build() {
            if (Objects.isNull(this.result.onExecAction) &&
                    Objects.isNull(this.result.onExecConsumer) &&
                    Objects.isNull(this.result.onExecFunc) &&
                    Objects.isNull(this.result.onExecSupplier)
            ) {
                throw new IllegalStateException("A Stage cannot be built without an action");
            }
            this.requireCleanExecFlow();
            return this.result;
        }



        private void requireCleanExecFlow() {
            if (Objects.nonNull(this.result.onExecFunc) && Objects.nonNull(this.result.onExecConsumer)) {
                throw new IllegalStateException("The execution of the stage is ambiguous. It cannot be a consumer and a function, becasue the execution flow will not be able to determine which one need to be executed");
            }
            if (Objects.nonNull(this.result.onExecAction) && Objects.nonNull(this.result.onExecSupplier)) {
                throw new IllegalStateException("The execution of the stage is ambiguous. It cannot be an action and a supplier, becasue the execution flow will not be able to determine which one need to be executed");
            }
        }
    }
}
