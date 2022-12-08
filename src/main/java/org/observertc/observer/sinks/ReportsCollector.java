package org.observertc.observer.sinks;

import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.ObservableCollector;
import org.observertc.observer.configs.ObserverConfig;
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
    ObserverConfig observerConfig;

    private ObservableCollector<Report> reportsCollector;

    @PostConstruct
    void setup() {
        var maxItems = observerConfig.buffers.reportsCollector.maxItems;
        var maxTimeInMs = observerConfig.buffers.reportsCollector.maxTimeInMs;
        this.reportsCollector = ObservableCollector.<Report>builder()
                .withMaxItems(maxItems)
                .withMaxTimeInMs(maxTimeInMs)
                .build();

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
