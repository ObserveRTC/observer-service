package org.observertc.observer.repositories;

import io.micronaut.context.BeanProvider;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class RepositorySweeper {

    private static final Logger logger = LoggerFactory.getLogger(RepositorySweeper.class);

    @Inject
    BeanProvider<ClientsRepository> clientsRepositoryProvider;

    @Inject
    BeanProvider<PeerConnectionsRepository> peerConnectionsRepositoryProvider;

    @Inject
    BeanProvider<InboundTracksRepository> inboundTracksRepositoryProvider;

    @Inject
    BeanProvider<OutboundTracksRepository> outboundTracksRepositoryProvider;

    @Inject
    BeanProvider<SfusRepository> sfusRepositoryProvider;

    @Inject
    BeanProvider<SfuTransportsRepository> sfuTransportsRepositoryProvider;

    @Inject
    BeanProvider<SfuInboundRtpPadsRepository> sfuInboundRtpPadsRepositoryProvider;

    @Inject
    BeanProvider<SfuOutboundRtpPadsRepository> sfuOutboundRtpPadsRepositoryProvider;

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
        this.started = false;
        logger.info("Repository sweeper service has been stopped");
    }

    private void run() {
        this.timer.set(null);
        this.checkCollidingEntries();
        this.fire();

    }

    private void checkCollidingEntries() {
        this.clientsRepositoryProvider.get().checkCollidingEntries();
        this.peerConnectionsRepositoryProvider.get().checkCollidingEntries();
        this.inboundTracksRepositoryProvider.get().checkCollidingEntries();
        this.outboundTracksRepositoryProvider.get().checkCollidingEntries();
        this.sfusRepositoryProvider.get().checkCollidingEntries();
        this.sfuTransportsRepositoryProvider.get().checkCollidingEntries();
        this.sfuInboundRtpPadsRepositoryProvider.get().checkCollidingEntries();
        this.sfuOutboundRtpPadsRepositoryProvider.get().checkCollidingEntries();
    }

    private void fire() {
        if (this.timer.get() != null) {
            logger.warn("Attempted to fire twice");
            return;
        }
        var delayInS = 300 + Math.round(Math.random() * 300);
        var timer = this.scheduler.scheduleDirect(this::run, delayInS, TimeUnit.SECONDS);
        if (!this.timer.compareAndSet(null, timer)) {
            timer.dispose();
        }
    }

    public boolean isStarted() {
        return this.started;
    }
}
