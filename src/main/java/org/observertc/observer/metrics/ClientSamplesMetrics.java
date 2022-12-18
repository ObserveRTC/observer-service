package org.observertc.observer.metrics;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;

import javax.annotation.PostConstruct;

@Singleton
public class ClientSamplesMetrics {
    private static final String UNKNOWN_VALUE = "UNKNOWN";

    private static final String CLIENT_META_METRICS_PREFIX = "client_meta";
    private static final String OPERATION_SYSTEM_METRIC_NAME = "operation_system";
    private static final String BROWSER_METRIC_NAME = "browser";
    private static final String PLATFORM_METRIC_NAME = "platform";

    private static final String NAME_TAG_NAME = "name";
    private static final String VERSION_TAG_NAME = "version";
    private static final String VENDOR_TAG_NAME = "vendor";
    private static final String TYPE_TAG_NAME = "type";

    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.ClientSamplesMetricConfig config;

    private String operationSystemMetricName;
    private String browserMetricName;
    private String platformMetricName;

    @PostConstruct
    void setup() {
        this.operationSystemMetricName = metrics.getMetricName(CLIENT_META_METRICS_PREFIX, OPERATION_SYSTEM_METRIC_NAME);
        this.browserMetricName = metrics.getMetricName(CLIENT_META_METRICS_PREFIX, BROWSER_METRIC_NAME);
        this.platformMetricName = metrics.getMetricName(CLIENT_META_METRICS_PREFIX, PLATFORM_METRIC_NAME);
    }

    public ClientSamplesMetrics incrementOperationSystem(String name) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry.counter(
                this.operationSystemMetricName,
                NAME_TAG_NAME, Utils.firstNotNull(name, UNKNOWN_VALUE)
        ).increment();
        return this;
    }


    public ClientSamplesMetrics incrementBrowser(String name) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry.counter(
                this.browserMetricName,
                NAME_TAG_NAME, Utils.firstNotNull(name, UNKNOWN_VALUE)
        ).increment();
        return this;
    }

    public ClientSamplesMetrics incrementPlatform(String type) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry.counter(
                this.platformMetricName,
                TYPE_TAG_NAME, Utils.firstNotNull(type, UNKNOWN_VALUE)
        ).increment();
        return this;
    }
}
