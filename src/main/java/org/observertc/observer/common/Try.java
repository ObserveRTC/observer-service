package org.observertc.observer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Try {

    private static final Logger logger = LoggerFactory.getLogger(Try.class);

    public static Try create(Runnable action) {
        return new Try().onAction(action);
    }

    public static boolean wrap(Runnable action) {
        return new Try().onAction(action).run();
    }

    public static<T> T wrap(Supplier<T> supplier, T defaultValue) {
        AtomicReference<T> result = new AtomicReference<>(defaultValue);
        var trying = new Try().onAction(() -> {
            var value = supplier.get();
            result.set(value);
        });
        if (trying.run()) {
            return result.get();
        } else {
            return defaultValue;
        }
    }


    private final int maxRetry;
    private Consumer<Throwable> exceptionListener;
    private Runnable action;

    public Try() {
        this(3);
    }

    public Try(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public Try onAction(Runnable action) {
        this.action = action;
        return this;
    }

    public Try onException(Consumer<Throwable> exceptionListener) {
        this.exceptionListener = exceptionListener;
        return this;
    }

    public boolean run() {
        Objects.requireNonNull(this.action);
        Exception thrown = null;
        for (int attempt = 0; this.maxRetry < 1 || attempt < this.maxRetry; ++attempt) {
            try {
                this.action.run();
            } catch (Exception ex) {
                logger.warn("Error occurred while execution. attempt: {}, maxAttempt: {}", attempt, this.maxRetry, ex);
                thrown = ex;
                continue;
            }
            return true;
        }
        if (this.exceptionListener != null) {
            this.exceptionListener.accept(thrown);
        }
        return false;
    }
}
