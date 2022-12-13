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

    private static final String NAME_TAG_NAME = "name";
    private static final String VERSION_TAG_NAME = "version";
    private static final String VENDOR_TAG_NAME = "vendor";
    private static final String TYPE_TAG_NAME = "type";
    private static final String MODEL_TAG_NAME = "model";

    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.ClientSamplesMetricConfig config;

    private String operationSystemMetricName;
    private String browserSystemMetricName;

    @PostConstruct
    void setup() {
        this.operationSystemMetricName = metrics.getMetricName(CLIENT_META_METRICS_PREFIX, OPERATION_SYSTEM_METRIC_NAME);
        this.browserSystemMetricName = metrics.getMetricName(BROWSER_METRIC_NAME, OPERATION_SYSTEM_METRIC_NAME);
        this.browserSystemMetricName = metrics.getMetricName(BROWSER_METRIC_NAME, OPERATION_SYSTEM_METRIC_NAME);
    }

    public ClientSamplesMetrics incrementOperationSystem(String name, String version) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry.counter(
                this.operationSystemMetricName,
                NAME_TAG_NAME, Utils.firstNotNull(name, UNKNOWN_VALUE),
                VERSION_TAG_NAME, Utils.firstNotNull(version, UNKNOWN_VALUE)
        );
        return this;
    }


    public ClientSamplesMetrics incrementBrowser(String name, String version) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry.counter(
                this.operationSystemMetricName,
                NAME_TAG_NAME, Utils.firstNotNull(name, UNKNOWN_VALUE),
                VERSION_TAG_NAME, Utils.firstNotNull(version, UNKNOWN_VALUE)
        );
        return this;
    }

    public ClientSamplesMetrics incrementPlatform(String vendor, String type, String model) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry.counter(
                this.operationSystemMetricName,
                VENDOR_TAG_NAME, Utils.firstNotNull(vendor, UNKNOWN_VALUE),
                TYPE_TAG_NAME, Utils.firstNotNull(type, UNKNOWN_VALUE),
                MODEL_TAG_NAME, Utils.firstNotNull(model, UNKNOWN_VALUE)
        );
        return this;
    }
}
