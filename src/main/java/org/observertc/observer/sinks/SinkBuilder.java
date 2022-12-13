package org.observertc.observer.sinks;

import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.metrics.SinkMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public interface SinkBuilder extends Builder<Sink> {
    static Logger logger = LoggerFactory.getLogger(SinkBuilder.class);

    record Essentials(
            String sinkId,
            ExecutorService ioExecutors,
            SinkMetrics sinkMetrics
    ) {

    }
    default void setEssentials(Essentials essentials) {
        return;
    }
}
