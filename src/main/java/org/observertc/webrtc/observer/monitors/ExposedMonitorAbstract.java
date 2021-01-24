package org.observertc.webrtc.observer.monitors;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.health.HeartbeatEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

public abstract class ExposedMonitorAbstract implements ApplicationEventListener<HeartbeatEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ExposedMonitorAbstract.class);
    private static final String OBSERVERTC_PREFIX = "observertc";
    private int consecutiveErrors = 0;
    private boolean stopped = false;
    private boolean initialDelayPassed = false;
    private Instant lastExecuted = Instant.now();
    private Config config = new Config();

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        if (this.stopped) {
            return;
        }
        Instant now = Instant.now();
        if (!this.initialDelayPassed) {
            if (0 < this.config.initialDelayInS) {
                if (Duration.between(this.lastExecuted, now).getSeconds() < this.config.initialDelayInS) {
                    return;
                }
            }
            this.initialDelayPassed = true;
        }
        if (Duration.between(this.lastExecuted, now).getSeconds() < this.config.periodTimeInS) {
            return;
        }
        if (this.config.detailedLogs) {
            logger.info("{} is started", config.name);
        }

        try {
            this.execute();
            this.consecutiveErrors = 0;
        } catch (Throwable t) {
            logger.error("Unexpected error occurred during execution", t);
            ++this.consecutiveErrors;
        }

        if (0 < this.config.maxConsecutiveErrors && this.config.maxConsecutiveErrors < this.consecutiveErrors) {
            logger.warn("Unexpected error occurred for monitor {} for {} times. The module will be shut down",
                    this.getClass().getSimpleName(),
                    this.consecutiveErrors
            );
            this.stopped = true;
        }
        this.lastExecuted = now;
        if (this.config.detailedLogs) {
            logger.info("{} is ended", config.name);
        }

    }

    protected void configure(Config config) {
        this.config = config;
    }

    protected String getMetricName(String... names) {
        return String.format("%s_%s", OBSERVERTC_PREFIX, String.join("_", names));
    }

    protected abstract void execute();

    protected static class Config {
        public boolean enabled = false;
        public int periodTimeInS = 30;
        public int initialDelayInS = 30;
        public int maxConsecutiveErrors = 3;
        public boolean detailedLogs = false;
        public String name = "unknown";
    }
}
