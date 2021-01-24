package org.observertc.webrtc.observer.subscriptions;

import io.micronaut.scheduling.annotation.Scheduled;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RepositoryCleaner {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryCleaner.class);

    private volatile boolean run = true;

    @Inject
    RepositoryProvider repositoryProvider;

    @Scheduled(initialDelay = "5m")
    public void start() {
        if (this.run) {
            return;
        }
        this.run = true;
    }
}
