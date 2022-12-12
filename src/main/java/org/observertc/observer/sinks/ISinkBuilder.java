package org.observertc.observer.sinks;

import org.observertc.observer.configbuilders.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public interface ISinkBuilder extends Builder<Sink> {
    static Logger logger = LoggerFactory.getLogger(ISinkBuilder.class);

    record Essentials(
            ExecutorService ioExecutors
    ) {

    }
    default void setEssential(Essentials essentials) {
        return;
    }
}
