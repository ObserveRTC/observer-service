package org.observertc.webrtc.observer.evaluators.witholders;

import io.reactivex.rxjava3.functions.Supplier;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ValueHolder<T> implements Function<T, Boolean>, Supplier<T>{

    public static<R> ValueHolder<R> makeEventValueHolder() {
        return new ValueHolder<>(
                (last, actual) -> !last.equals(actual),
                (last, actual) -> actual
        );
    }

    public static<R> ValueHolder<R> makeAccumulativeValueHolder(Comparator<R> cmp) {
        return new ValueHolder<>(
                (last, actual) -> 0 < cmp.compare(last, actual),
                (last, actual) -> actual
        );
    }

    final BiFunction<T, T, Boolean> trigger;
    final BiFunction<T, T, T> reducer;
    private T last;
    private T provided;

    ValueHolder(BiFunction<T, T, Boolean> trigger, BiFunction<T, T, T> reducer) {
        this.trigger = trigger;
        this.reducer = reducer;
    }

    @Override
    public Boolean apply(T value) {
        if (Objects.isNull(value)) {
            if (Objects.nonNull(this.last)) {
                this.provided = this.last;
                this.last = null;
                return true;
            }
            this.provided = null;
            return false;
        }
        if (Objects.isNull(this.last)) {
            this.provided = this.last = value;
            return false;
        }

        this.provided = this.last;
        boolean result = this.trigger.apply(this.last, value);
        this.last = this.reducer.apply(this.last, value);
        return result;
    }

    @Override
    public T get() throws Throwable {
        return this.provided;
    }
}
