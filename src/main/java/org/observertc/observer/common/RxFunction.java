package org.observertc.observer.common;

import io.reactivex.rxjava3.functions.Function;

import java.util.Objects;

public interface RxFunction<TIn, TOut> extends Function<TIn, TOut> {
    default <V> RxFunction<TIn, V> andThen(Function<? super TOut, ? extends V> after) {
        Objects.requireNonNull(after);
        RxFunction<TIn, V> result = (input) -> {
            var middle = this.apply(input);
            return after.apply(middle);
        };
        return result;
    }
}
