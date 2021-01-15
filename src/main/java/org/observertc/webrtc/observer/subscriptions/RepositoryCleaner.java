package org.observertc.webrtc.observer.subscriptions;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.health.HeartbeatEvent;
import org.observertc.webrtc.observer.monitors.CallsMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

public class RepositoryCleaner implements ApplicationEventListener<HeartbeatEvent> {

    private static final Logger logger = LoggerFactory.getLogger(CallsMonitor.class);

    private Instant lastCleaned = Instant.now();

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        Instant now = Instant.now();
        if (Duration.between(this.lastCleaned, now).getSeconds() < 3600) {
            return;
        }
        try {
            this.doClean();
        } catch(Throwable t) {

        }
        this.lastCleaned = now;
    }

    private void doClean() {

    }
}
