package org.observertc.webrtc.observer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Collect items until a time or number limitation has been reached
 *
 * NOTE: it does not have any internal timer (therefore the number passive), and
 * only invoked when a caller demands an operation.
 * @param <T>
 */
public class PassiveCollector<T> {

    private static final Logger logger = LoggerFactory.getLogger(PassiveCollector.class);
    private Long started;

    public static<U> Builder<U> builder() {
        return new Builder<>();
    }

    private Consumer<List<T>> listener = null;
    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private int maxTimeInMs = 0;
    private int maxItems = 0;
    private boolean mutableResults = true;

    private PassiveCollector() {

    }

    /**
     * Add a batch of items to the inner queue at once.
     *
     * NOTE: the caller must ensure the additional number of items do not cause memory overflow.
     * Once the addBatch is called all of the items added to the queue at once and only after that it is checked
     * if limitation regarding to the number of items reached. To take an example lets say the max items of your
     * collector is 10, and you add 15 items as batch. then the queue first add 15 items (even if it means 24 in the end)
     * and only after it emits it.
     *
     * NOTE 2: it emits all of the collected items regardless of the overflowing number it may contain.
     * @param items
     * @return
     * @throws Throwable
     */
    public<U extends T> PassiveCollector<T> addAll(Collection<U> items) throws Throwable {
        if (Objects.isNull(items)) {
            return this;
        }
        this.execute(() -> {
            this.queue.addAll(items);
        });
        return this;
    }

    public PassiveCollector<T> add(T... items) throws Throwable {
        if (Objects.isNull(items)) {
            return this;
        }
        for (int i = 0; i< items.length; ++i) {
            var item = items[i];
            this.execute(() -> {
                this.queue.add(item);
            });
        }
        return this;
    }

    /**
     * A method to help to make the collector active.
     * An outside timer can call this method, which eventually can push forward the items.
     */
    public void checkTime() {
        if (Objects.isNull(this.started)) {
            return;
        }
        Long now = Instant.now().toEpochMilli();
        if (this.maxTimeInMs < now - this.started) {
            this.emit();
            this.started = null;
        }
    }

    private void execute(Runnable action) throws Throwable{
        if (0 < this.maxTimeInMs) {
            Long now = Instant.now().toEpochMilli();
            if (Objects.isNull(this.started) || this.queue.size() < 1) {
                this.started = now;
            } else if (this.maxTimeInMs < now - this.started) {
                this.emit();
                this.started = null;
            }
        }
        action.run();
        if (0 < this.maxItems && this.maxItems <= this.queue.size()) {
            this.emit();
            this.started = null;
        }
    }

    public void flush() throws Exception {
        if (0 < this.queue.size()) {
            this.emit();
        }
    }

    private void emit() {
        List<T> collectedItems;
        synchronized (this) {
            if (this.queue.size() < 1) {
                return;
            }
            collectedItems = new LinkedList<>();
            this.queue.drainTo(collectedItems);
            if (!this.mutableResults) {
                collectedItems = Collections.unmodifiableList(collectedItems);
            }
        }
        this.listener.accept(collectedItems);
    }


    public static class Builder<U> {
        private PassiveCollector<U> result = new PassiveCollector<>();
        public Builder<U> withMaxTimeInMs(int maxTimeInMs) {
            this.result.maxTimeInMs = maxTimeInMs;
            return this;
        }

        public Builder<U> withMaxItems(int value) {
            this.result.maxItems = value;
            return this;
        }

        public Builder<U> withMutableResults(boolean value) {
            this.result.mutableResults = value;
            return this;
        }

        public Builder<U> withListener(Consumer<List<U>> listener) {
            this.result.listener = listener;
            return this;
        }

        public PassiveCollector<U> build() {
            if (this.result.maxItems < 1 && this.result.maxTimeInMs < 1) {
                throw new IllegalStateException("Collector must be set to hold max items or max time");
            }
            Objects.requireNonNull(this.result.listener);
            return result;
        }

    }
}
