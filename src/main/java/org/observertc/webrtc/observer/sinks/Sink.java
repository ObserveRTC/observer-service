package org.observertc.webrtc.observer.sinks;

import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Sink implements Consumer<OutboundReports> {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(Sink.class);
    protected Logger logger = DEFAULT_LOGGER;

    Sink withLogger(Logger logger) {
        this.logger.info("Default logger for {} is switched to {}", this.getClass().getSimpleName(), logger.getName());
        this.logger = logger;
        return this;
    }

    public void close() {

    }


}
