package org.observertc.observer.metrics;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;

import javax.annotation.PostConstruct;

@Singleton
public class SinkMetrics {
    private static final String SINK_PREFIX = "sinks";

    private static final String REPORTS_METRIC_NAME = "reports";
    private static final String SINK_ID_TAG_NAME = "sink";

    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.SourceMetricsConfig config;

    private String reportsNumMetricsName;

    @PostConstruct
    void setup() {
        this.reportsNumMetricsName = metrics.getMetricName(SINK_PREFIX, REPORTS_METRIC_NAME);
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
}
