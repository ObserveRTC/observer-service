package org.observertc.webrtc.observer.subscriptions;

import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class RepositoryCleaner {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryCleaner.class);

    private volatile boolean run = true;


    @Scheduled(initialDelay = "5m", fixedDelay = "10m")
    public void start() {
    }
}
