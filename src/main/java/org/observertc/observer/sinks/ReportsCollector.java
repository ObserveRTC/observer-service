package org.observertc.observer.sinks;

import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.ObservableCollector;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.metrics.SinkMetrics;
import org.observertc.observer.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;

@Singleton
public class ReportsCollector {

    private static final Logger logger = LoggerFactory.getLogger(ReportsCollector.class);

    @Inject
    ObserverConfig.InternalBuffersConfig.ReportsCollectorConfig config;

    @Inject
    SinkMetrics sinkMetrics;

    private ObservableCollector<Report> reportsCollector;
    private int overloadedThreshold = -1;
    private int maxConsecutiveOverload = -1;
    private volatile int consecutiveOverload = 0;

    @PostConstruct
    void setup() {
        var maxItems = this.config.maxItems;
        var maxTimeInMs = this.config.maxTimeInMs;
        this.reportsCollector = ObservableCollector.<Report>builder()
                .withMaxItems(maxItems)
                .withMaxTimeInMs(maxTimeInMs)
                .build();
        if (0 < this.config.overloadThreshold) {
            var overloadedThreshold = this.config.overloadThreshold;
            if (overloadedThreshold < maxItems) {
                logger.warn("Config for ReportsCollector is invalid. THe number of items indicate the collector is overloaded {} cannot be smaller than the maximum allowed items {}. In this case the overloadThreshold will be the maximum number of items",
                        overloadedThreshold,
                        maxItems);
                overloadedThreshold = maxItems;
            }
            var maxConsecutiveOverloaded = this.config.maxConsecutiveOverloaded;
            if (maxConsecutiveOverloaded < 1) {
                logger.warn("Config for ReportsCollector is invalid. The maxConsecutiveOverloaded cannot be smaller than one (in config it is: {}) if overloadThreshold is set. In this case we set the maxConsecutiveOverloaded to 1",
                        maxConsecutiveOverloaded
                );
                maxConsecutiveOverloaded = 1;
            }
            this.overloadedThreshold = overloadedThreshold;
            this.maxConsecutiveOverload = maxConsecutiveOverloaded;
        }
        logger.info("ReportsCollector is Initialized. maxItems: {}, maxTimeIMs: {}, overloadedThreshold: {}, max consecutive overload: {}",
                maxItems,
                maxTimeInMs,
                this.overloadedThreshold,
                this.maxConsecutiveOverload
        );
    }

    @PreDestroy
    void teardown() {
        try {
            this.reportsCollector.flush();
        } catch (Exception e) {
            logger.warn("Error occurred", e);
        }
    }

    public void accept(Report report) {
        // no need to check if the collector is overused because there is only one item we add as an extra
        try {
            this.reportsCollector.add(report);
        } catch (Throwable e) {
            logger.warn("Error occurred", e);
        }
    }

    public void acceptAll(List<Report> reports) {
        if (Objects.isNull(reports) || reports.size() < 1) {
            return;
        }
        if (0 < this.overloadedThreshold) {
            var collectedReports = this.reportsCollector.size();
            if (this.overloadedThreshold < collectedReports + reports.size()) {

                this.sinkMetrics.incrementOverloadedReportsCollector();

                if (this.maxConsecutiveOverload <= ++this.consecutiveOverload) {
                    logger.warn("Dropping {} number of Reports due to consecutive overload. The current load on the collector is {}", reports.size(), collectedReports);
                    return;
                }
                logger.warn("Overloaded Collector is detected! The number of collected reports are {}, The number of new reports going to be added to the collector is {}, the collector is overloaded {} consecutive times, maxConsecutive overload before the reports will be dropped is {}",
                        collectedReports,
                        reports.size(),
                        this.consecutiveOverload,
                        this.maxConsecutiveOverload
                );

            } else {
                this.consecutiveOverload = 0;
            }
        }

        try {
            this.reportsCollector.addAll(reports);
        } catch (Throwable e) {
            logger.warn("Error occurred", e);
        }
    }

    public Observable<List<Report>> getObservableReports() {
        return this.reportsCollector.observableEmittedItems();
    }
}
