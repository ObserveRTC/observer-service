package org.observertc.observer;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Singleton
public class BackgroundTasksExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundTasksExecutor.class);

    private BlockingQueue<Task<Void>> tasks = new LinkedBlockingQueue<>();
    private volatile boolean started = false;
    private final AtomicReference<Disposable> timer = new AtomicReference<>(null);
    private final io.reactivex.rxjava3.core.Scheduler scheduler = Schedulers.newThread();
    private final Map<String, ScheduledTaskSupplier> scheduledTaskSuppliers = new ConcurrentHashMap<>();
    private final long baseDelayInSec = 5;

    @Inject
    private ServerTimestamps serverTimestamps;

    @PostConstruct
    void setup() {

    }

    @PreDestroy
    void teardown() {

    }

    public long getBaseDelayInSec() {
        return this.baseDelayInSec;
    }

    public void start() {
        if (this.started) {
            logger.warn("Attempted to start twice");
            return;
        }
        this.started = true;
        this.tasks.clear();
        this.fire();
        logger.info("Background Task executor has been started");
    }

    public void stop() {
        if (!this.started) {
            return;
        }
        var timer = this.timer.getAndSet(null);
        if (timer != null && !timer.isDisposed()) {
            timer.dispose();
        }
        this.tasks.clear();
        this.started = false;
        logger.info("Background Task executor has been stopped");
    }

    private void run() {
        this.timer.set(null);

        this.getScheduledTasks().stream().forEach(this.tasks::add);

        while (!this.tasks.isEmpty()) {
            Task<Void> task;
            try {
                task = this.tasks.poll(1000, TimeUnit.MILLISECONDS);
                if (task == null) {
                    continue;
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while executing tasks");
                break;
            }
            logger.info("Executing task: {}", task.getName());
            if (!task.execute().succeeded()) {
                logger.warn("Task ({}) execution failed.", task.getName());
            }
        }
        this.fire();
    }

    public void addTask(Task<Void> task) {
        this.tasks.add(task);
    }

    private void fire() {
        if (this.timer.get() != null) {
            logger.warn("Attempted to fire twice");
            return;
        }
        var delayInMs = this.baseDelayInSec + Math.round(Math.random() * this.baseDelayInSec);
        var timer = this.scheduler.scheduleDirect(this::run, delayInMs, TimeUnit.SECONDS);
        if (!this.timer.compareAndSet(null, timer)) {
            timer.dispose();
        }
    }

    public boolean isStarted() {
        return this.started;
    }

    public void addPeriodicTask(String name, Supplier<Task<Void>> taskSupplier, long periodicDelayInMs) {
        if (periodicDelayInMs < this.baseDelayInSec * 1000) {
            logger.warn("Attempted to add a scheduled task periodic delay {} lower than the periodicity of the background scheduler {}. This should not happen as the the timer cannot be invoked more frequently", periodicDelayInMs, this.baseDelayInSec * 1000);
            periodicDelayInMs = this.baseDelayInSec * 1000;
        }
        var scheduledTaskSupplier = new ScheduledTaskSupplier(
                name,
                taskSupplier,
                this.serverTimestamps.instant().toEpochMilli(),
                periodicDelayInMs
        );
        this.scheduledTaskSuppliers.put(name, scheduledTaskSupplier);
        logger.info("Scheduled Task {} is added", scheduledTaskSupplier);
    }

    public void removePeriodicTask(String name) {
        var removed = this.scheduledTaskSuppliers.remove(name);
        if (removed != null) {
            logger.info("Scheduled Task {} is removed", removed);
        }
    }

    private List<Task<Void>> getScheduledTasks() {
        if (this.scheduledTaskSuppliers.size() < 1) {
            Collections.emptyList();
        }
        var now = this.serverTimestamps.instant().toEpochMilli();
        var expiredScheduledTasks = this.scheduledTaskSuppliers.values()
                .stream()
                .filter(scheduledTaskSupplier -> scheduledTaskSupplier.added + scheduledTaskSupplier.periodInMs <= now)
                .collect(Collectors.toList());
        if (expiredScheduledTasks.size() < 1) {
            return Collections.emptyList();
        }

        var result = new LinkedList<Task<Void>>();
        for (var scheduledTaskSupplier : expiredScheduledTasks) {
            var newScheduledTaskSupplier = new ScheduledTaskSupplier(
                    scheduledTaskSupplier.name,
                    scheduledTaskSupplier.taskSupplier,
                    now,
                    scheduledTaskSupplier.periodInMs
            );

            this.scheduledTaskSuppliers.put(scheduledTaskSupplier.name, newScheduledTaskSupplier);

            var task = scheduledTaskSupplier.taskSupplier.get();
            if (task == null) {
                logger.warn("Scheduled Task {} did not supply an actual task", scheduledTaskSupplier);
                continue;
            }
            result.add(task);
        }
        return result;
    }

    public boolean hasPeriodicTask(String periodicTaskName) {
        return this.scheduledTaskSuppliers.containsKey(periodicTaskName);
    }

    record ScheduledTaskSupplier(
            String name,
            Supplier<Task<Void>> taskSupplier,
            long added,
            long periodInMs
    ) {
        @Override
        public String toString() {
            return String.format("Scheduled Task Supplier Name: %s, periodInMs: %d",
                    name,
                    periodInMs
            );
        }
    }
}
