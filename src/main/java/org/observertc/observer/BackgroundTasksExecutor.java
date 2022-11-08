package org.observertc.observer;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Singleton;
import org.observertc.observer.common.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class BackgroundTasksExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundTasksExecutor.class);

//    @Inject
//    BeanProvider<ClientsRepository> clientsRepositoryProvider;
//
//    @Inject
//    BeanProvider<PeerConnectionsRepository> peerConnectionsRepositoryProvider;
//
//    @Inject
//    BeanProvider<InboundTracksRepository> inboundTracksRepositoryProvider;
//
//    @Inject
//    BeanProvider<OutboundTracksRepository> outboundTracksRepositoryProvider;
//
//    @Inject
//    BeanProvider<SfusRepository> sfusRepositoryProvider;
//
//    @Inject
//    BeanProvider<SfuTransportsRepository> sfuTransportsRepositoryProvider;
//
//    @Inject
//    BeanProvider<SfuInboundRtpPadsRepository> sfuInboundRtpPadsRepositoryProvider;
//
//    @Inject
//    BeanProvider<SfuOutboundRtpPadsRepository> sfuOutboundRtpPadsRepositoryProvider;

    private BlockingQueue<Task<Void>> tasks = new LinkedBlockingQueue<>();
    private volatile boolean started = false;
    private final AtomicReference<Disposable> timer = new AtomicReference<>(null);
    private final io.reactivex.rxjava3.core.Scheduler scheduler = Schedulers.newThread();

    @PostConstruct
    void setup() {

    }

    @PreDestroy
    void teardown() {

    }

    public void start() {
        if (this.started) {
            logger.warn("Attempted to start twice");
            return;
        }
        this.started = true;
        this.tasks.clear();
        this.fire();
        logger.info("Repository sweeper service has been started");
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
        logger.info("Repository sweeper service has been stopped");
    }

    private void run() {
        this.timer.set(null);
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
            if (!task.execute().succeeded()) {
                logger.warn("Task execution failed. Task name: {}", task.getName());
            }
        }
        this.fire();

    }


    public void addTask(Task<Void> task) {
        this.tasks.add(task);
    }



    private void checkCollidingEntries() {
//        this.clientsRepositoryProvider.get().checkCollidingEntries();
//        this.peerConnectionsRepositoryProvider.get().checkCollidingEntries();
//        this.inboundTracksRepositoryProvider.get().checkCollidingEntries();
//        this.outboundTracksRepositoryProvider.get().checkCollidingEntries();
//        this.sfusRepositoryProvider.get().checkCollidingEntries();
//        this.sfuTransportsRepositoryProvider.get().checkCollidingEntries();
//        this.sfuInboundRtpPadsRepositoryProvider.get().checkCollidingEntries();
//        this.sfuOutboundRtpPadsRepositoryProvider.get().checkCollidingEntries();
    }

    private void fire() {
        if (this.timer.get() != null) {
            logger.warn("Attempted to fire twice");
            return;
        }
        var delayInS = 15 + Math.round(Math.random() * 15);
        var timer = this.scheduler.scheduleDirect(this::run, delayInS, TimeUnit.SECONDS);
        if (!this.timer.compareAndSet(null, timer)) {
            timer.dispose();
        }
    }

    public boolean isStarted() {
        return this.started;
    }
}
