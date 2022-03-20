package org.observertc.observer.sinks;

import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.observer.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class Sink implements Consumer<List<Report>> {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(Sink.class);
    protected Logger logger = DEFAULT_LOGGER;

    private boolean enabled = true;

    Sink setEnabled(boolean value) {
        this.enabled = value;
        return this;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    Sink withLogger(Logger logger) {
        this.logger.info("Default logger for {} is switched to {}", this.getClass().getSimpleName(), logger.getName());
        this.logger = logger;
        return this;
    }

    public void close() {
        logger.info("Closed");
    }

    public void open() {
        logger.info("Opened");
    }

}
