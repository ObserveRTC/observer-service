package org.observertc.observer.common;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.configs.ObserverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Wraps a passive collector and using RxJava buffer capabilities to
 * collect batches if items and add it to the collector.
 *
 * Note that this collector does not have resilient capabilities
 * @param <T>
 */
public class DebouncedCollector<T> extends Observable<List<T>> implements Consumer<T> {
    private static final Logger defaultLogger = LoggerFactory.getLogger(DebouncedCollector.class);


    public static<U> Builder<U> builder() {
        return new Builder<>();
    }

    private Subject<T> input = PublishSubject.create();
    private Logger logger = defaultLogger;
    private PassiveCollector<T> collector;
    private List<Observer<? super List<T>>> subscribers = new LinkedList<>();

    private DebouncedCollector() {

    }

    @Override
    protected void subscribeActual(@NonNull Observer<? super List<T>> observer) {
        synchronized (this) {
            this.subscribers.add(observer);
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

    @Override
    public void accept(T item) throws Throwable {
        synchronized (this) {
            this.input.onNext(item);
        }
    }

    public void acceptAll(List<T> items) {
        try {
            this.collector.addAll(items);
        } catch (Throwable e) {
            logger.warn("Error occurred", e);
        }
    }

    public static class Builder<U> {
        private ObserverConfig.CollectorConfig debounceConfig = new ObserverConfig.CollectorConfig();
        private PassiveCollector.Builder<U> passiveCollectorBuilder = PassiveCollector.builder();
        private DebouncedCollector<U> result = new DebouncedCollector<>();
        private Builder() {

        }

        public Builder<U> withLogger(Logger value) {
            this.result.logger = value;
            return this;
        }

        public Builder<U> withMaxItems(int value) {
            this.debounceConfig.maxItems = Math.min(1000, value);
            this.passiveCollectorBuilder.withMaxItems(value);
            return this;
        }

        public Builder<U> withMaxTimeInMs(int value) {
            this.debounceConfig.maxTimeInS = Math.min(100, value / 1000);
            this.passiveCollectorBuilder.withMaxTimeInMs(value);
            return this;
        }

        public DebouncedCollector<U> build() {
            var collector = this.passiveCollectorBuilder
                    .withListener(this.result::onEmit)
                    .build();
            this.result.collector = collector;
            BufferUtils.wrapObservable(this.result.input, this.debounceConfig)
                    .subscribe(this.result.collector::addAll);
            return this.result;
        }
    }
}
