package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

@Prototype
class CounterMonitor<T> implements ObservableOperator<T, T> {
    private BiConsumer<T, List<Tag>> tagsResolver = (input, tags) -> {};

    @Inject
    MeterRegistry meterRegistry;

    private String name;

    CounterMonitor<T> withTagsResolver(BiConsumer<T, List<Tag>> tagsResolver) {
        this.tagsResolver = tagsResolver;
        return this;
    }

    CounterMonitor<T> withName(String value) {
        this.name = value;
        return this;
    }

    @Override
    public @NonNull Observer<? super T> apply(@NonNull Observer<? super T> observer) throws Throwable {
        Objects.requireNonNull(this.name);
        return new Observer<T>() {
            private Disposable disposable;
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                this.disposable = d;
            }

            @Override
            public void onNext(@NonNull T item) {
                List<Tag> tags = new ArrayList<>();
                tagsResolver.accept(item, tags);
                meterRegistry.counter(name, tags).increment();
                observer.onNext(item);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                if (Objects.nonNull(this.disposable)) {
                    if (!this.disposable.isDisposed()) {
                        this.disposable.dispose();
                    }
                }
                observer.onError(e);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        };
    }
}
