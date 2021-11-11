package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ObservableCollector<T> extends Observable<List<T>> {

    private static final Logger defaultLogger = LoggerFactory.getLogger(ObservableCollector.class);

    public static<U> Builder<U> builder() {
        return new Builder<>();
    }

    private int maxItems = 100;
    private int maxTimeInMs = 0;
    private Logger logger = defaultLogger;
    private PassiveCollector<T> collector;
    private final List<Observer<? super List<T>>> subscribers = new LinkedList<>();
    private volatile boolean closed = false;

    private ObservableCollector() {

    }

    protected void initialize() {
        this.collector = PassiveCollector.<T>builder()
                .withMaxTime(maxTimeInMs)
                .withMaxItems(maxItems)
                .build();
        this.collector.observableItems().subscribe(samples -> {
            this.subscribers.forEach(subscriber -> {
                try {
                    subscriber.onNext(samples);
                } catch (Throwable t) {
                    this.logger.warn("Error occurred subscriber handling samples", t);
                    subscriber.onError(t);
                }
            });
        });
    }

    public boolean isClosed() {
        return this.closed;
    }

    public<U extends T> void add(U observedSample) {
        try {
            this.collector.add(observedSample);
        } catch (Throwable throwable) {
            this.logger.warn("Cannot perform add operation");
        }
    }

    public<U extends T> void addBatch(List<U> observedSamples) {
        try {
            this.collector.addBatch(observedSamples);
        } catch (Throwable throwable) {
            this.logger.warn("Cannot perform add operation");
        }
    }

    public void flush() {
        try {
            this.collector.flush();
        } catch (Exception e) {
            logger.warn("Exception occurred during flush operation");
        }
    }

    public void close() {
        if (this.closed) {
            logger.warn("Attempted to close a Collector twice");
            return;
        }
        try {
            if (Objects.nonNull(this.collector)) {
                this.collector.flush();
            }
        } catch (Exception e) {
            this.logger.warn("An exception occurred while flushing collector", e);
        }
        this.subscribers.forEach(Observer::onComplete);
        this.subscribers.clear();
        this.closed = true;
    }

    @Override
    protected void subscribeActual(@NonNull Observer<? super List<T>> subscriber) {
        this.subscribers.add(subscriber);
    }

    public static class Builder<U> {
        private ObservableCollector<U> result = new ObservableCollector<>();
        private Builder() {

        }

        public Builder<U> withLogger(Logger value) {
            this.result.logger = value;
            return this;
        }

        public Builder<U> withMaxItems(int value) {
            this.result.maxItems = value;
            return this;
        }

        public Builder<U> withMaxTimeInMs(int value) {
            this.result.maxTimeInMs = value;
            return this;
        }

        public ObservableCollector<U> build() {
            this.result.initialize();
            return this.result;
        }
    }
}
