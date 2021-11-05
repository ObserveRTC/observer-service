package org.observertc.webrtc.observer;

import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.observer.common.PassiveCollector;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.evaluators.ObservedClientSampleProcessingPipeline;
import org.observertc.webrtc.observer.evaluators.ObservedSfuSampleProcessingPipeline;
import org.observertc.webrtc.observer.sinks.OutboundReportsObserver;
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
    private PassiveCollector<OutboundReport> reportCollector;

    @Inject
    ObserverConfig observerConfig;

    @Inject
    ObservedSfuSampleProcessingPipeline sfuSamplesProcessor;

    @Inject
    ObservedClientSampleProcessingPipeline clientSamplesProcessor;

    @Inject
    OutboundReportsObserver outboundReportsObserver;

    @PostConstruct
    void setup() {
        var reportsBufferMaxItems = this.observerConfig.evaluators.reportsBufferMaxItems;
        var reportsBufferMaxRetainInS = this.observerConfig.evaluators.reportsBufferMaxRetainInS;
        this.reportCollector = PassiveCollector.<OutboundReport>builder()
                .withMaxTime(reportsBufferMaxRetainInS * 1000)
                .withMaxItems(reportsBufferMaxItems).build();

        this.sfuSamplesProcessor
                .getObservableOutboundReport()
                .subscribe(this.reportCollector::add);
//                .map(Utils.createPrintingMapper("sfuSamplesProcessor"))
//                .buffer(3, TimeUnit.SECONDS)
//                .map(OutboundReports::fromList)
//                .subscribe(this.outboundReportsObserver);


        this.clientSamplesProcessor
                .getObservableOutboundReport()
                .subscribe(this.reportCollector::add);
//                .map(Utils.createPrintingMapper("clientSamplesProcessor"))
//                .buffer(3, TimeUnit.SECONDS)
//                .map(OutboundReports::fromList)
//                .subscribe(this.outboundReportsObserver);

        this.reportCollector.observableItems()
                .map(OutboundReports::fromList)
                .subscribe(this.outboundReportsObserver);
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
            if (Objects.nonNull(this.reportCollector)) {
                this.reportCollector.flush();
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
