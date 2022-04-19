package org.observertc.observer.common;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Collect items until a time or number limitation has been reached
 *
 * NOTE: it does not have any internal timer (therefore the name passive), and
 * only invoked when a caller demands an operation.
 * @param <T>
 */
public class ObservableCollector<T> {

    private static final Logger logger = LoggerFactory.getLogger(ObservableCollector.class);

    public static<U> Builder<U> builder() {
        return new Builder<>();
    }

    private Subject<List<T>> output = PublishSubject.create();
    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private final int maxTimeInMs;
    private final int maxItems;
    private final Scheduler scheduler;
    private Disposable timer = null;

    private ObservableCollector(int maxTimeInMs, int maxItems, Scheduler scheduler) {
        this.maxTimeInMs = maxTimeInMs;
        this.maxItems = maxItems;
        this.scheduler = scheduler;
    }

    /**
     * Add a batch of items to the inner queue at once.
     *
     * NOTE: the caller must ensure the additional number of items do not cause memory overflow.
     * Once the addBatch is called all of the items added to the queue at once and only after that it is checked
     * if limitation regarding to the number of items reached. To take an example lets say the max items of your
     * collector is 10, and you add 15 items as batch. then the queue first add 15 items (even if it means 10 + 15 = 25 in the end)
     * and only after it emits it all at once.
     *
     * NOTE 2: it emits all of the collected items regardless of the overflowing number it may contain.
     * @param items
     * @return
     */
    public<U extends T> ObservableCollector<T> addAll(Collection<U> items) {
        if (Objects.isNull(items) || items.size() < 1) {
            return this;
        }
        if (0 < this.maxTimeInMs) {
            if (Objects.isNull(this.timer)) {
                synchronized (this) {
                    if (Objects.isNull(this.timer)) {
                        this.timer = this.scheduler.scheduleDirect(() -> {
                            this.timer = null;
                            this.emit();
                        }, this.maxTimeInMs, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }

        this.queue.addAll(items);

        if (0 < this.maxItems) {
            if (this.maxItems <= this.queue.size()) {
                this.emit();
            }
        }
        return this;
    }

    public ObservableCollector<T> add(T... items)  {
        if (Objects.isNull(items)) {
            return this;
        }
        this.addAll(List.of(items));
        return this;
    }

    public Observable<List<T>> observableEmittedItems() {
        return this.output;
    }

    public void flush() {
        if (0 < this.queue.size()) {
            this.emit();
        }
    }

    private void emit() {
        synchronized (this) {
            if (Objects.nonNull(this.timer)) {
                this.timer.dispose();
                this.timer = null;
            }
            List<T> collectedItems;
            if (this.queue.size() < 1) {
                return;
            }
            collectedItems = new LinkedList<>();
            this.queue.drainTo(collectedItems);
            this.output.onNext(collectedItems);
        }
    }


    public static class Builder<U> {
        private int maxTimeInMs = 0;
        private int maxItems = 0;
        private Scheduler scheduler = null;

        public Builder<U> withScheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public Builder<U> withMaxTimeInMs(int maxTimeInMs) {
            this.maxTimeInMs = maxTimeInMs;
            return this;
        }

        public Builder<U> withMaxItems(int value) {
            this.maxItems = value;
            return this;
        }


        public ObservableCollector<U> build() {
            if (this.maxItems < 1 && this.maxTimeInMs < 1) {
                throw new IllegalStateException("Collector must be set to hold max items or max time");
            }
            if (0 < this.maxTimeInMs && Objects.isNull(this.scheduler)) {
                this.scheduler = Schedulers.computation();
            }
            var result = new ObservableCollector<U>(this.maxTimeInMs, this.maxItems, this.scheduler);
            return result;
        }

    }
}
