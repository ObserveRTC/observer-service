package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PassiveCollector<T> {

    private static final Logger logger = LoggerFactory.getLogger(PassiveCollector.class);
    private Long started;

    public static<U> Builder<U> builder() {
        return new Builder<>();
    }

    private Subject<List<T>> listeners = PublishSubject.create();
    private Queue<T> queue = new ConcurrentLinkedQueue<>();
    private int maxTimeInMs = 0;
    private final int maxItems;

    private PassiveCollector(int maxItems) {
        this.maxItems = maxItems;
    }

    public void addAll(T... items) {
        if (Objects.isNull(items)) {
            return;
        }
        Arrays.stream(items).forEach(item -> {
            try {
                this.add(item);
            } catch (Throwable throwable) {
                logger.warn("Adding item thrown exception", throwable);
            }
        });
    }

    public void add(T item) throws Throwable{
        if (0 < this.maxTimeInMs) {
            Long now = Instant.now().toEpochMilli();
            if (Objects.isNull(this.started) || this.queue.size() < 1) {
                this.started = now;
            } else if (this.maxTimeInMs < now - this.started) {
                this.emit();
                this.started = null;
            }
        }
        this.queue.add(item);
        if (0 < this.maxItems && this.maxItems <= this.queue.size()) {
            this.emit();
            this.started = null;
        }
    }

    public Observable<List<T>> observableItems() {
        return this.listeners;
    }

    public void flush() throws Exception {
        if (0 < this.queue.size()) {
            this.emit();
        }
    }

    private void emit() {
        synchronized (this) {
            if (this.queue.size() < 1) {
                return;
            }
            List<T> collectedItems = new LinkedList<>();
            while (!this.queue.isEmpty()) {
                var item = this.queue.poll();
                collectedItems.add(item);
            }
            this.listeners.onNext(collectedItems);
        }
    }

    public static class Builder<U> {
        private int maxTimeInMs = 0;
        private int maxItems = 0;

        public Builder<U> withMaxTime(int maxTimeInMs) {
            this.maxTimeInMs = maxTimeInMs;
            return this;
        }

        public Builder<U> withMaxItems(int value) {
            this.maxItems = value;
            return this;
        }

        public PassiveCollector<U> build() {
            if (this.maxTimeInMs < 1 && this.maxItems < 1) {
                throw new IllegalStateException("Collector must be set to hold max items or max time");
            }
            PassiveCollector<U> result = new PassiveCollector<>(this.maxItems);
            result.maxTimeInMs = maxTimeInMs;
            return result;
        }

    }
}
