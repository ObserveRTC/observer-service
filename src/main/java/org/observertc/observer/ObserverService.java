package org.observertc.observer;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.evaluators.*;
import org.observertc.observer.metrics.ReportMetrics;
import org.observertc.observer.metrics.RepositoryMetrics;
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
    HamokService hamokService;

    @Inject
    ReportMetrics reportMetrics;

    @Inject
    SampleSources sampleSources;

    @Inject
    SamplesCollector samplesCollector;

    @Inject
    CallEntitiesUpdater callEntitiesUpdater;

    @Inject
    ClientSamplesAnalyser clientSamplesAnalyser;

    @Inject
    CallEventReportsAdder callEventReportsAdder;

    @Inject
    SfuEntitiesUpdater sfuEntitiesUpdater;

    @Inject
    SfuSamplesAnalyser sfuSamplesAnalyser;

    @Inject
    SfuEventReportsAdder sfuEventReportsAdder;

    @Inject
    ReportsCollector reportsCollector;

    @Inject
    ReportSinks reportSinks;

    @Inject
    RepositoryMetrics repositoryMetrics;

    @Inject
    BackgroundTasksExecutor backgroundTasksExecutor;

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
                .subscribe(this.clientSamplesAnalyser::accept);

        this.clientSamplesAnalyser.observableReports()
                .subscribe(this.callEventReportsAdder.reportsObserver());

        this.callEventReportsAdder.observableReports()
                .subscribe(this.reportsCollector::acceptAll);

        // sfu samples
        this.sfuEntitiesUpdater.observableClientSamples()
                .subscribe(this.sfuSamplesAnalyser::accept);

        this.sfuSamplesAnalyser.observableReports()
                .subscribe(this.sfuEventReportsAdder.reportsObserver());

        this.sfuEventReportsAdder.observableReports()
                .subscribe(this.reportsCollector::acceptAll);

        // funneled reports
        this.reportsCollector.getObservableReports()
                .subscribe(this.reportSinks);

        if (this.reportMetrics.isEnabled()) {
            this.reportsCollector.getObservableReports()
                    .subscribe(this.reportMetrics::process);
        }
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
        if (!this.backgroundTasksExecutor.isStarted()) {
            this.backgroundTasksExecutor.start();
            this.backgroundTasksExecutor.addPeriodicTask(
                    "Repository Metric Exposure",
                    () -> ChainedTask.<Void>builder()
                            .withName("Exposing Repository Metric")
                            .addActionStage("Exposing metrics", repositoryMetrics::expose)
                            .build(),
                    5 * 60 * 1000
            );
        }
        logger.info("Started");
    }

    void stop() {
        if (!this.run) {
            logger.warn("Attempted to stop a not running service");
            return;
        }
        this.run = false;
        if (this.backgroundTasksExecutor.isStarted()) {
            this.backgroundTasksExecutor.stop();
        }
        logger.info("Stopped");
    }

    public boolean isStarted() {
        return this.run;
    }

    public boolean isReady() {
        boolean hamokIsReady = this.hamokService.isReady();
        return hamokIsReady;
    }
}
