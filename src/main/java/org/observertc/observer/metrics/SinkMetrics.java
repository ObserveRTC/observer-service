package org.observertc.observer.metrics;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class SinkMetrics {
    private static final String SINK_PREFIX = "sinks";

    private static final String BUFFERED_REPORTS_METRIC_NAME = "buffered_reports";
    private static final String REPORTS_METRIC_NAME = "reports";
    private static final String SINK_ID_TAG_NAME = "sink";
    private static final String OVERLOADED_REPORTS_COLLECTOR_METRIC_NAME = "overloaded_reports_collector";

    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.SourceMetricsConfig config;



    private String reportsNumMetricsName;
    private String overloadedReportsCollectorMetricName;
    private AtomicInteger bufferedReports = new AtomicInteger(0);

    @PostConstruct
    void setup() {
        this.reportsNumMetricsName = metrics.getMetricName(SINK_PREFIX, REPORTS_METRIC_NAME);
        this.overloadedReportsCollectorMetricName = metrics.getMetricName(SINK_PREFIX, OVERLOADED_REPORTS_COLLECTOR_METRIC_NAME);
        metrics.registry.gauge(this.metrics.getMetricName(SINK_PREFIX, BUFFERED_REPORTS_METRIC_NAME), this.bufferedReports);
    }

    public boolean isEnabled() {
        return this.config.enabled;
    }


    public SinkMetrics incrementReportsNum(int value, String sinkId) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry.counter(
                this.reportsNumMetricsName,
                SINK_ID_TAG_NAME, sinkId
        ).increment(value);
        return this;
    }

    public SinkMetrics incrementOverloadedReportsCollector() {
        this.metrics.registry.counter(
                this.overloadedReportsCollectorMetricName
        ).increment();
        return this;
    }

    public SinkMetrics setBufferedReports(int value) {
        this.bufferedReports.set(value);
        return this;
    }
}
