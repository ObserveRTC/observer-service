package org.observertc.observer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

public class FunctionTools {
    private static final Logger logger = LoggerFactory.getLogger(FunctionTools.class);

    public static<T> Consumer<T> shallowRxConsumer(io.reactivex.rxjava3.functions.Consumer<T> subject) {
        return shallowRxConsumer(subject, logger);
    }

    public static<T> Consumer<T> shallowRxConsumer(io.reactivex.rxjava3.functions.Consumer<T> subject, Logger _logger) {
        return new Consumer<T>() {
            @Override
            public void accept(T t) {
                try {
                    subject.accept(t);
                } catch (Throwable e) {
                    _logger.warn("Error occurred while consuming {}", t, e);
                }
            }
        };
    }

    public static<T> io.reactivex.rxjava3.functions.Consumer<T> shallowConsumer(Consumer<T> subject) {
        return new io.reactivex.rxjava3.functions.Consumer<T>() {
            @Override
            public void accept(T t) throws Throwable {
                if (Objects.isNull(t)) return;
                subject.accept(t);
            }
        };
    }

    private FunctionTools() {

    }

}
