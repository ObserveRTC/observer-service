package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.functions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class TaskStage {
    private static final Logger logger = LoggerFactory.getLogger(TaskStage.class);

    public static TaskStage.Builder builder(String stageName) {
        return new Builder(stageName);
    }

    private String name;
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
            logger.warn("Stage {} did not executed action");
            return null;
        }

        return result;
    }

    public boolean isExecuted() {
        return this.executed;
    }

    public boolean isRolledbacked() {
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


    public static class Builder {
        private TaskStage result;
        public Builder(String stageName) {
            this.result = new TaskStage();
            this.result.name = stageName;
        }

        public Builder withAction(Action action) {
            this.requireCleanExecFlow();
            this.result.onExecAction = action;
            return this;
        }

        public Builder withConsumer(Consumer action) {
            this.requireCleanExecFlow();
            this.result.onExecConsumer = action;
            return this;
        }

        public Builder withSupplier(Supplier action) {
            this.requireCleanExecFlow();
            this.result.onExecSupplier = action;
            return this;
        }

        public Builder withFunction(Function action) {
            this.requireCleanExecFlow();
            this.result.onExecFunc = action;
            return this;
        }

        public Builder withRollback(BiConsumer<AtomicReference, Throwable> action) {
            if (Objects.nonNull(this.result.onRollback)) {
                throw new IllegalStateException("Cannot set a rollback function twice");
            }
            this.result.onRollback = action;
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

        public TaskStage build() {
            if (Objects.isNull(this.result.onExecAction) &&
                    Objects.isNull(this.result.onExecConsumer) &&
                    Objects.isNull(this.result.onExecFunc) &&
                    Objects.isNull(this.result.onExecSupplier)
            ) {
                throw new IllegalStateException("A Stage cannot be built without an action");
            }
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
