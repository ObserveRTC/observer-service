package org.observertc.webrtc.observer;

import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.evaluators.ObservedClientSampleProcessingPipeline;
import org.observertc.webrtc.observer.evaluators.ObservedSfuSampleProcessingPipeline;
import org.observertc.webrtc.observer.sinks.OutboundReportsCollector;
import org.observertc.webrtc.observer.sinks.OutboundReportsDispatcher;
import org.observertc.webrtc.observer.sources.ClientSamplesCollector;
import org.observertc.webrtc.observer.sources.SfuSamplesCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public class ObserverService {

    private static final Logger logger = LoggerFactory.getLogger(ObserverService.class);
    private volatile boolean run = false;

    @Inject
    ObserverConfig observerConfig;

    @Inject
    ClientSamplesCollector clientSamplesCollector;

    @Inject
    SfuSamplesCollector sfuSamplesCollector;

    @Inject
    ObservedSfuSampleProcessingPipeline sfuSamplesProcessor;

    @Inject
    ObservedClientSampleProcessingPipeline clientSamplesProcessor;

    @Inject
    OutboundReportsCollector outboundReportsCollector;

    @Inject
    OutboundReportsDispatcher outboundReportsDispatcher;

    @PostConstruct
    void setup() {
        this.clientSamplesCollector
                .observableClientSamples()
                .subscribe(this.clientSamplesProcessor);

        this.clientSamplesProcessor
                .getObservableOutboundReports()
                .subscribe(this.outboundReportsCollector::addAll);

        this.sfuSamplesCollector
                .observableSfuSamples()
                .subscribe(this.sfuSamplesProcessor);

        this.sfuSamplesProcessor
                .getObservableOutboundReports()
                .subscribe(this.outboundReportsCollector::addAll);

        this.outboundReportsCollector.observableOutboundReports()
                .subscribe(this.outboundReportsDispatcher);
    }

    @PreDestroy
    void teardown() {

    }

    void start() {
        if (this.run) {
            logger.warn("Attempted to start the service twice");
            return;
        }
        this.run = true;
        logger.info("Started");
    }

    void stop() {
        if (!this.run) {
            logger.warn("Attempted to stop a not running service");
            return;
        }
        this.run = false;
        try {
            if (Objects.nonNull(this.outboundReportsCollector)) {
                this.outboundReportsCollector.flush();
            }
        } catch (Exception e) {
            logger.error("Error occurred while flushing collector");
        }

        logger.info("Stopped");
    }

    public boolean isStarted() {
        return this.run;
    }
}
