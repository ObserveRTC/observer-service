package org.observertc.observer;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.Objects;
import java.util.function.Consumer;

public class RxObserverBuilder<T> {
    private Consumer<Disposable> onSubscribeListener = null;
    private Consumer<T> onNextListener = null;
    private Consumer<Throwable> onErrorListener = null;
    private Runnable onCompleted = null;

    public RxObserverBuilder<T> onSubscribed(Consumer<Disposable> listener) {
        this.onSubscribeListener = listener;
        return this;
    }

    public RxObserverBuilder<T> onNext(Consumer<T> listener) {
        this.onNextListener = listener;
        return this;
    }

    public RxObserverBuilder<T> onError(Consumer<Throwable> listener) {
        this.onErrorListener = listener;
        return this;
    }

    public RxObserverBuilder<T> onCompleted(Runnable listener) {
        this.onCompleted = listener;
        return this;
    }

    public Observer<T> build() {
        return new Observer<T>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                if (Objects.nonNull(onSubscribeListener)) {
                    onSubscribeListener.accept(d);
                }
            }

            @Override
            public void onNext(@NonNull T t) {
                if (Objects.nonNull(onNextListener)) {
                    onNextListener.accept(t);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                if (Objects.nonNull(onErrorListener)) {
                    onErrorListener.accept(e);
                }
            }

            @Override
            public void onComplete() {
                if (Objects.nonNull(onCompleted)) {
                    onCompleted.run();
                }
            }
        };
    }
}
