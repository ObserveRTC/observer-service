package org.observertc.observer.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SampleSources {
    private static final Logger logger = LoggerFactory.getLogger(SampleSources.class);

    @Inject
    SamplesWebsocketController websocketController;

    @Inject
    SamplesRestApiController restApiController;

    @PostConstruct
    void init() {
        logger.info("Initialized");
    }

    @PreDestroy
    void teardown() {
        logger.info("Deinitialized");
    }
}
