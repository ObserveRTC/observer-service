package org.observertc.observer.metrics;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class SourceMetrics {
    private static final String SOURCE_METRICS_PREFIX = "sources";

    private static final String OPENED_WEBSOCKET_METRIC_NAME = "opened_websockets";
    private static final String CLOSED_WEBSOCKET_METRIC_NAME = "closed_websockets";
    private static final String RECEIVED_SAMPLES_METRIC_NAME = "received_samples";
    private static final String OBSERVED_CLIENT_SAMPLES_METRIC_NAME = "observed_client_samples";
    private static final String OBSERVED_SFU_SAMPLES_METRIC_NAME = "observed_sfu_samples";
    private static final String BUFFERED_SAMPLES_METRIC_NAME = "buffered_samples";
    private static final String OVERLOADED_SAMPLES_COLLECTOR_METRIC_NAME = "overloaded_samples_collector";

    private static final String SOURCE_TAG_NAME = "source";
    private static final String REST_SOURCE_TAG_VALUE = "rest";
    private static final String WEBSOCKET_SOURCE_TAG_VALUE = "websocket";

    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.SourceMetricsConfig config;

    private String openedWebsocketMetricName;
    private String closedWebsocketMetricName;
    private String receivedSamplesMetricName;
    private String observedClientSamplesMetricName;
    private String observedSfuSamplesMetricName;
    private String overloadedSamplesCollectorMetricName;

    private final AtomicInteger bufferedSamples = new AtomicInteger(0);

    @PostConstruct
    void setup() {
        this.openedWebsocketMetricName = metrics.getMetricName(SOURCE_METRICS_PREFIX, OPENED_WEBSOCKET_METRIC_NAME);
        this.closedWebsocketMetricName = metrics.getMetricName(SOURCE_METRICS_PREFIX, CLOSED_WEBSOCKET_METRIC_NAME);
        this.receivedSamplesMetricName = metrics.getMetricName(SOURCE_METRICS_PREFIX, RECEIVED_SAMPLES_METRIC_NAME);
        this.observedClientSamplesMetricName = metrics.getMetricName(SOURCE_METRICS_PREFIX, OBSERVED_CLIENT_SAMPLES_METRIC_NAME);
        this.observedSfuSamplesMetricName = metrics.getMetricName(SOURCE_METRICS_PREFIX, OBSERVED_SFU_SAMPLES_METRIC_NAME);
        this.overloadedSamplesCollectorMetricName = metrics.getMetricName(SOURCE_METRICS_PREFIX, OVERLOADED_SAMPLES_COLLECTOR_METRIC_NAME);
        metrics.registry.gauge(this.metrics.getMetricName(SOURCE_METRICS_PREFIX, BUFFERED_SAMPLES_METRIC_NAME), this.bufferedSamples);
    }


    public SourceMetrics incrementOpenedWebsockets(String serviceId, String mediaUnitId) {
        this.metrics.registry.counter(
                this.openedWebsocketMetricName,
                metrics.getServiceIdTagName(), metrics.getTagValue(serviceId),
                metrics.getMediaUnitIdTagName(), metrics.getTagValue(mediaUnitId)
        ).increment();
        return this;
    }

    public SourceMetrics incrementClosedWebsockets(String serviceId, String mediaUnitId) {
        this.metrics.registry.counter(
                this.closedWebsocketMetricName,
                metrics.getServiceIdTagName(), metrics.getTagValue(serviceId),
                metrics.getMediaUnitIdTagName(), metrics.getTagValue(mediaUnitId)
        ).increment();
        return this;
    }

    public SourceMetrics incrementRESTReceivedSamples(String serviceId, String mediaUnitId) {
        this.metrics.registry.counter(
                this.receivedSamplesMetricName,
                metrics.getServiceIdTagName(), metrics.getTagValue(serviceId),
                metrics.getMediaUnitIdTagName(), metrics.getTagValue(mediaUnitId),
                SOURCE_TAG_NAME, REST_SOURCE_TAG_VALUE
        ).increment();
        return this;
    }

    public SourceMetrics incrementWebsocketReceivedSamples(String serviceId, String mediaUnitId) {
        this.metrics.registry.counter(
                this.receivedSamplesMetricName,
                metrics.getServiceIdTagName(), metrics.getTagValue(serviceId),
                metrics.getMediaUnitIdTagName(), metrics.getTagValue(mediaUnitId),
                SOURCE_TAG_NAME, WEBSOCKET_SOURCE_TAG_VALUE
        ).increment();
        return this;
    }

    public SourceMetrics incrementObservedClientSamplesSamples(int value) {
        this.metrics.registry.counter(
                this.observedClientSamplesMetricName
        ).increment(value);
        return this;
    }

    public SourceMetrics incrementOverloadedSamplesCollector() {
        this.metrics.registry.counter(
                this.overloadedSamplesCollectorMetricName
        ).increment();
        return this;
    }

    public SourceMetrics incrementObservedSfuSamplesSamples(int value) {
        this.metrics.registry.counter(
                this.observedSfuSamplesMetricName
        ).increment(value);
        return this;
    }

    public SourceMetrics setBufferedSamples(int value) {
        this.bufferedSamples.set(value);
        return this;
    }
}
