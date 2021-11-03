package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Collector<T> implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Collector.class);

    public static<U> Builder<U> builder() {
        return new Builder<>();
    }

    private Subject<List<T>> listeners = PublishSubject.create();
    private Queue<T> queue = new ConcurrentLinkedQueue<>();
    private int maxTimeInMs = 0;
    private final int maxItems;
    private Timer timer = null;
    private volatile boolean collecting = false;

    private Collector(int maxItems) {
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
        this.queue.add(item);
        if (0 < this.maxItems && this.maxItems <= this.queue.size()) {
            synchronized (this) {
                if (Objects.nonNull(this.timer)) {
                    this.timer.cancel();
                    this.timer.purge();
                    this.timer = null;
                }
                this.emit();
            }
            return;
        }
        if (this.collecting) {
            return;
        }
        synchronized (this) {
            if (this.collecting || Objects.nonNull(this.timer)) {
                return;
            }
            if (this.maxTimeInMs < 1) {
                this.collecting = true;
                return;
            }
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (this) {
                        emit();
                        if (Objects.nonNull(timer)) {
                            timer = null;
                        }
                    }
                }
            }, this.maxTimeInMs);
            this.collecting = true;
        }
    }

    private void emit() {
        if (this.queue.size() < 1) {
            return;
        }
        List<T> collectedItems = new LinkedList<>();
        while (!this.queue.isEmpty()) {
            var item = this.queue.poll();
            collectedItems.add(item);
        }
        this.listeners.onNext(collectedItems);
        this.collecting = false;
    }

    public Observable<List<T>> observableItems() {
        return this.listeners;
    }

    @Override
    public void close() throws Exception {
        if (!this.collecting) {
            return;
        }
        if (Objects.nonNull(this.timer)) {
            this.timer.cancel();
            this.timer.purge();
            this.timer = null;
        }
        this.emit();
    }


    public static class Builder<U> {
        private int maxTimeInMs = 0;
        private int maxItems = 0;

        public Builder<U> withMaxTime(int value) {
            this.maxTimeInMs = value;
            return this;
        }

        public Builder<U> withMaxItems(int value) {
            this.maxItems = value;
            return this;
        }

        public Collector<U> build() {
            if (this.maxTimeInMs < 1 && this.maxItems < 1) {
                throw new IllegalStateException("Collector must be set to hold max items or max time");
            }
            Collector<U> result = new Collector<>(this.maxItems);
            result.maxTimeInMs = maxTimeInMs;
            return result;
        }

    }
}
