package org.observertc.observer;

import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.ClientSamplesProcessor;
import org.observertc.observer.evaluators.SfuSamplesProcessor;
import org.observertc.observer.sinks.ReportSinks;
import org.observertc.observer.sinks.ReportsCollector;
import org.observertc.observer.sources.SampleSources;
import org.observertc.observer.sources.SamplesCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ObserverService {

    private static final Logger logger = LoggerFactory.getLogger(ObserverService.class);
    private volatile boolean run = false;

    @Inject
    ObserverConfig observerConfig;

    @Inject
    SampleSources sampleSources;

    @Inject
    SamplesCollector samplesCollector;

    @Inject
    ClientSamplesProcessor clientSamplesProcessor;

    @Inject
    SfuSamplesProcessor sfuSamplesProcessor;

    @Inject
    ReportsCollector reportsCollector;

    @Inject
    ReportSinks reportSinks;

    @PostConstruct
    void setup() {
        this.samplesCollector
                .observableClientSamples()
                .subscribe(this.clientSamplesProcessor.getObservedClientSampleObserver());

        this.clientSamplesProcessor
                .getObservableReports()
                .subscribe(this.reportsCollector::acceptAll);

        this.samplesCollector
                .observableSfuSamples()
                .subscribe(this.sfuSamplesProcessor.getObservedSfuSamplesObserver());

        this.sfuSamplesProcessor
                .getObservableReports()
                .subscribe(this.reportsCollector::acceptAll);

        this.reportsCollector
                .getObservableReports()
                .subscribe(this.reportSinks);
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
        logger.info("Stopped");
    }

    public boolean isStarted() {
        return this.run;
    }
}
