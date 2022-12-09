package org.observertc.observer.repositories.tasks;

import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public abstract class CommitAbstract {

    private static final Logger logger = LoggerFactory.getLogger(CommitAbstract.class);

    private ReentrantLock lock = new ReentrantLock();
    private final AtomicReference<CompletableFuture<Void>> execution = new AtomicReference<>(null);
    private final int tryLockTimeoutInMs;
    private final String context;

    public CommitAbstract(String context, int tryLockTimeoutInMs) {
        this.context = context;
        this.tryLockTimeoutInMs = tryLockTimeoutInMs;
    }

    public boolean executeSync() {
        try {
            this.execute().get(30000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("InterruptedException occurred while executing commits in sync", e);
            return false;
        } catch (ExecutionException e) {
            logger.warn("ExecutionException occurred while executing commits in sync", e);
            return false;
        } catch (TimeoutException e) {
            logger.warn("TimeoutException occurred while executing commits in sync", e);
            return false;
        } catch (Throwable t) {
            logger.warn("Throwable occurred while executing commits in sync", t);
            return false;
        }
        return true;
    }

    public CompletableFuture<Void> execute() {
        var process = new CompletableFuture<Void>();
        if (!this.execution.compareAndSet(null, process)) {
            return this.execution.get();
        }
        Schedulers.io().scheduleDirect(() -> {
            try {
                this.lock.tryLock(this.tryLockTimeoutInMs, TimeUnit.MILLISECONDS);

                try {
                    this.process();
                } catch (Throwable t) {
                    logger.warn("Error occurred while executing process. Context: {}", this.context);
                }
                // commit here

            } catch (InterruptedException e) {
                logger.warn("Exception occurred while trying to acquire lock for committing. Context: {}", this.context, e);
            } catch (Exception e) {
                logger.warn("Cannot acquire lock for committing due to exception. Context: {}", this.context, e);
            } finally {
                try {
                    this.lock.unlock();
                } catch (Exception ex) {
                    logger.warn("Unsuccessful unlock for committing. Context: {}", this.context, ex);
                } finally {
                    this.execution.set(null);
                    process.complete(null);
                }
            }
        });
        return process;
    }

    protected abstract void process() throws Throwable;

    public boolean isLocked() {
        return this.lock.isLocked();
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }
}
