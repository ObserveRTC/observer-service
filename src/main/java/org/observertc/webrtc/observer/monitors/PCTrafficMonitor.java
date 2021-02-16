package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Requires(notEnv = Environment.TEST)
public class PCTrafficMonitor extends ExposedMonitorAbstract {
    private static final Logger logger = LoggerFactory.getLogger(PCTrafficMonitor.class);

    @Inject
    MeterRegistry meterRegistry;

    public PCTrafficMonitor() {

    }

    @PostConstruct
    void setup() {
        Config config = new Config();
        config.detailedLogs = false;
        config.maxConsecutiveErrors = 3;
        config.periodTimeInS = 30;
        config.initialDelayInS = 60;
        config.enabled = false;
        config.name = "PCTrafficMonitor";
        this.configure(config);
    }

    @Override
    protected void execute() {
    }
}
