package org.observertc.observer.common;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collect items until a time or number limitation has been reached
 *
 * NOTE: it does not have any internal timer (therefore the name passive), and
 * only invoked when a caller demands an operation.
 * @param <T>
 */
public class ObservablePassiveCollector<T> {

    private static final Logger logger = LoggerFactory.getLogger(ObservablePassiveCollector.class);
    private static final long NOT_STARTED = -1;

    public static<U> Builder<U> builder() {
        return new Builder<>();
    }

    public static<U> ObservablePassiveCollector<U> create() {
        return new Builder<U>().build();
    }

    private final UUID id = UUID.randomUUID();
    private Subject<List<T>> output = PublishSubject.create();
    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private final int maxItems;
    private final AtomicLong started;

    private ObservablePassiveCollector(int maxItems) {
        this.maxItems = maxItems;
        this.started = new AtomicLong(NOT_STARTED);
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
     * @throws Throwable
     */
    public<U extends T> ObservablePassiveCollector<T> addAll(Collection<U> items) {
        if (Objects.isNull(items) || items.size() < 1) {
            return this;
        }
        this.started.compareAndSet(NOT_STARTED, Instant.now().toEpochMilli());

        this.queue.addAll(items);

        if (0 < this.maxItems) {
            if (this.maxItems <= this.queue.size()) {
                this.emit();
            }
        }
        return this;
    }

    public ObservablePassiveCollector<T> add(T... items)  {
        if (Objects.isNull(items)) {
            return this;
        }
        this.addAll(List.of(items));
        return this;
    }

    // because I don't have a mood to copy paste all
//    public void add(T item) {
//        if (Objects.isNull(item)) return;
//        this.addAll(List.of(item));
//    }

    public long getCollectingTimeInMs() {
        var started = this.started.get();
        if (started == NOT_STARTED) return 0;
        return Instant.now().toEpochMilli() - started;
    }

    public int getCollectedNumberOfItems() {
        return this.queue.size();
    }

    public UUID getId() {
        return this.id;
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
        List<T> collectedItems;
        if (this.queue.size() < 1) {
            return;
        }
        collectedItems = new LinkedList<>();
        this.queue.drainTo(collectedItems);
        this.started.set(NOT_STARTED);
        synchronized (this) {
            this.output.onNext(collectedItems);
        }
    }


    public static class Builder<U> {
        private int maxItems = 0;

        public Builder<U> withMaxItems(int value) {
            this.maxItems = value;
            return this;
        }


        public ObservablePassiveCollector<U> build() {
            if (this.maxItems < 1) {
                logger.debug("One passive collector does not have an automatic upper limit itself. That means it collects infinite items unless a caller entity emits it manually.");
            }
            var result = new ObservablePassiveCollector<U>(this.maxItems);
            return result;
        }

    }
}
