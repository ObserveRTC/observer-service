package org.observertc.observer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Try<T> extends TaskAbstract<T> {

    private static final Logger logger = LoggerFactory.getLogger(Try.class);

    public static Try<Void> createForAction(Runnable action) {
        return new Try<Void>(action, null);
    }

    public static<U> Try<U> createForSupplier(Supplier<U> supplier) {
        return new Try<U>(null, supplier);
    }

    public static boolean wrap(Runnable action) {
        return new Try<Void>(action, null).execute().succeeded();
    }

    public static<T> T wrap(Supplier<T> supplier, T defaultValue) {
        AtomicReference<T> result = new AtomicReference<>(defaultValue);
        var trying = new Try<T>(() -> {
            var value = supplier.get();
            result.set(value);
        }, null);
        if (trying.execute().succeeded()) {
            return result.get();
        } else {
            return defaultValue;
        }
    }


    private final Runnable action;
    private final Supplier<T> supplier;

    public Try(Runnable action, Supplier<T> supplier) {
        this.action = action;
        this.supplier = supplier;
    }

    @Override
    protected T perform() throws Throwable {
        if (action == null && supplier == null) {
            throw new IllegalStateException("Cannot perform a task without action or supplier");
        }
        if (action != null) {
            action.run();
            return null;
        } else if (supplier != null) {
            return supplier.get();
        }
        return null;
    }
}
