package org.observertc.observer.repositories.tasks;

import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class CommitCallEntities {

    private static final Logger logger = LoggerFactory.getLogger(CommitCallEntities.class);

    private ReentrantLock lock = new ReentrantLock();
    private final AtomicReference<CompletableFuture<Void>> process = new AtomicReference<>(null);

    public CompletableFuture<Void> execute() {
        var process = new CompletableFuture<Void>();
        if (!this.process.compareAndSet(null, process)) {
            return this.process.get();
        }
        Schedulers.io().scheduleDirect(() -> {
            try {
                this.lock.tryLock(30000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("Exception occurred while trying to acquire lock for committing");
            } finally {
                try {
                    this.lock.unlock();
                } catch (Exception ex) {

                }
            }
        });
        return process;
    }

    public boolean isLocked() {
        return this.lock.isLocked();
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {

    }
}
