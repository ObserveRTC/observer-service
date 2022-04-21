package org.observertc.observer;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.evaluators.*;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.sinks.ReportSinks;
import org.observertc.observer.sinks.ReportsCollector;
import org.observertc.observer.sources.SampleSources;
import org.observertc.observer.sources.SamplesCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
    CallEntitiesUpdater callEntitiesUpdater;

    @Inject
    ClientSamplesAnalyzer clientSamplesAnalyzer;

    @Inject
    SfuEntitiesUpdater sfuEntitiesUpdater;

    @Inject
    SfuSamplesAnalyzer sfuSamplesAnalyzer;

    @Inject
    RepositoryEventsInterpreter repositoryEventsInterpreter;

    @Inject
    ReportsCollector reportsCollector;

    @Inject
    ReportSinks reportSinks;

    @PostConstruct
    void setup() {
        this.samplesCollector
                .observableClientSamples()
                .subscribe(this.callEntitiesUpdater::accept);

        this.samplesCollector
                .observableSfuSamples()
                .subscribe(this.sfuEntitiesUpdater::accept);

        // call samples
        this.callEntitiesUpdater.observableClientSamples()
                .subscribe(this.clientSamplesAnalyzer::accept);

        this.clientSamplesAnalyzer.observableReports()
                .subscribe(this.reportsCollector::acceptAll);

        // sfu samples
        this.sfuEntitiesUpdater.observableClientSamples()
                .subscribe(this.sfuSamplesAnalyzer::accept);

        this.sfuSamplesAnalyzer.observableReports()
                .subscribe(this.reportsCollector::acceptAll);

        // repository events
        this.repositoryEventsInterpreter.observableReports()
                .subscribe(this.reportsCollector::acceptAll);

        this.reportsCollector.getObservableReports()
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
