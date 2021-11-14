package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ObservableCollector<T> extends Observable<List<T>> implements Observer<T>{
    private static final Logger defaultLogger = LoggerFactory.getLogger(ObservableCollector.class);


    public static<U> Builder<U> builder() {
        return new Builder<>();
    }

//    private Subject<List<T>> subscribers = PublishSubject.create();
    private Logger logger = defaultLogger;
    private PassiveCollector<T> collector;

    private boolean resilientInput = false;
    private boolean resilientOutput = false;
    private boolean closed = false;

    private List<Disposable> upstreams = new LinkedList<>();
    private List<Observer<? super List<T>>> subscribers = new LinkedList<>();

    private ObservableCollector() {

    }

    @Override
    protected void subscribeActual(@NonNull Observer<? super List<T>> observer) {
        synchronized (this) {
            this.subscribers.add(observer);
        }
//        this.subscribers.subscribe(observer);
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        synchronized (this) {
            this.upstreams.add(d);
        }
    }

    @Override
    public void onNext(@NonNull T item) {
        try {
            this.collector.add(item);
        } catch (Throwable throwable) {
            if (this.resilientInput) {
                logger.warn("Error occurred while adding item", throwable);
            } else {
                this.onError(throwable);
            }
        }
    }

    public<U extends T> void add(U item) {
        if (Objects.isNull(item)) {
            return;
        }
        try {
            this.collector.add(item);
        } catch (Throwable throwable) {
            if (this.resilientInput) {
                logger.warn("Error occurred while adding item", throwable);
            } else {
                this.onError(throwable);
            }
        }
    }

    public<U extends T> void addAll(Collection<U> items) {
        if (Objects.isNull(items)) {
            return;
        }
        try {
            this.collector.addAll(items);
        } catch (Throwable throwable) {
            if (this.resilientInput) {
                logger.warn("Error occurred while adding item", throwable);
            } else {
                this.onError(throwable);
            }
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        this.logger.warn("Error occurred, flushing buffer and terminating subscribers", e);
        this.close();
    }

    @Override
    public void onComplete() {
        this.close();
    }

    private void close() {
        if (this.closed) {
            logger.warn("Attempted to close twice");
            return;
        }
        this.flush();
        synchronized (this) {
            this.upstreams.forEach(upstream -> {
                if (upstream.isDisposed()) {
                    return;
                }
                try {
                    upstream.dispose();
                } catch (Exception ex) {
                    logger.warn("Exception occurred while disposing an ubstream subscription", ex);
                }
            });

            this.subscribers.forEach(observer -> {
                try {
                    observer.onComplete();
                } catch (Exception ex) {
                    logger.warn("Exception while calling onComplete for subscriber", ex);
                    observer.onError(ex);
                }
            });
            this.subscribers.clear();
        }
        this.closed = true;
    }

    private void flush() {
        try {
            this.collector.flush();
        } catch (Exception e) {
            logger.warn("Exception occurred during flush operation");
        }
    }

    private void onEmit(List<T> items) {
        synchronized (this) {
            Iterator<Observer<? super List<T>>> it = this.subscribers.iterator();
            while(it.hasNext()) {
                var observer = it.next();
                try {
                    observer.onNext(items);
                } catch (Exception ex) {
                    if (this.resilientOutput) {
                        logger.warn("Exception is thrown while forwarding items. Subscriber miss the items", ex);
                        continue;
                    }
                    try {
                        observer.onError(ex);
                    } finally {
                        it.remove();
                        logger.warn("An exception occurred while forwarding items. The subscriber is removed", ex);
                    }
                }
            }
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public static class Builder<U> {
        private PassiveCollector.Builder<U> passiveCollectorBuilder = PassiveCollector.builder();
        private ObservableCollector<U> result = new ObservableCollector<>();
        private Builder() {

        }

        public Builder<U> withLogger(Logger value) {
            this.result.logger = value;
            return this;
        }

        public Builder<U> withMaxItems(int value) {
            this.passiveCollectorBuilder.withMaxItems(value);
            return this;
        }

        public Builder<U> withMaxTimeInMs(int value) {
            this.passiveCollectorBuilder.withMaxTimeInMs(value);
            return this;
        }

        /**
         * Makes the public `add`, and `addAll` method of the collector resilient for errors.
         * Meaning if an error occurrs during any adding there it does not cause the collector to stop and collect
         * @param value
         * @return
         */
        public Builder<U> withResilientInput(boolean value) {
            this.result.resilientInput = value;
            return this;
        }

        /**
         * Makes the public `add`, and `addAll` method of the collector resilient for errors.
         * Meaning if an error occurrs during any adding there it does not cause the collector to stop and collect
         * @param value
         * @return
         */
        public Builder<U> withResilientOutput(boolean value) {
            this.result.resilientOutput = value;
            return this;
        }

        public ObservableCollector<U> build() {
            var collector = this.passiveCollectorBuilder
                    .withListener(this.result::onEmit)
                    .build();
            this.result.collector = collector;
            return this.result;
        }
    }
}
