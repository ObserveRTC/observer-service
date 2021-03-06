package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class SentinelMetrics {
    private static final Logger logger = LoggerFactory.getLogger(SentinelMetrics.class);

    private static final String SENTINEL_TAG_NAME = "sentinel";
    private static final String MONITORED_SSRCS_NUM_METRIC_NAME = "observertc_monitored_ssrcs_num";
    private static final String MONITORED_PEER_CONNECTIONS_NUM_METRIC_NAME = "observertc_monitored_pcs_num";
    private static final String MONITORED_CALLS_NUM_METRIC_NAME = "observertc_monitored_calls_num";

    @Inject
    MeterRegistry meterRegistry;

    private String sentinelName = null;
    private final Map<String, AtomicLong> metrics;
    private List<Tag> tags;

    SentinelMetrics() {
        this.metrics = new HashMap<>();
    }

    SentinelMetrics initialize(String sentinelName) {
        if (Objects.nonNull(this.sentinelName)) {
            logger.warn("A sentinel metric has already been initialized with the name {}, therefore the newly provided name {} will not take any effect", this.sentinelName, sentinelName);
            return this;
        }
        this.sentinelName = sentinelName;
        var sentinelTag = Tag.of(SENTINEL_TAG_NAME, sentinelName);
        this.tags = List.of(sentinelTag);
        return this;
    }

    private AtomicLong getGauge(String metricsName) {
        if (Objects.isNull(this.sentinelName)) {
            throw new IllegalStateException("SentinelMetric cannot exists withou a name! Did you initialize this metric?");
        }
        AtomicLong result = this.metrics.get(metricsName);
        if (Objects.isNull(result)) {
            result = this.meterRegistry.gauge(metricsName, this.tags, new AtomicLong(0), AtomicLong::doubleValue);
            this.metrics.put(metricsName, result);
        }
        return result;
    }

    public SentinelMetrics setNumberOfSSRCs(int numOfSSRCs) {
        this.getGauge(MONITORED_SSRCS_NUM_METRIC_NAME).set(numOfSSRCs);
        return this;
    }

    public SentinelMetrics setNumberOfPeerConnections(int numOfPCs) {
        this.getGauge(MONITORED_PEER_CONNECTIONS_NUM_METRIC_NAME).set(numOfPCs);
        return this;
    }

    public SentinelMetrics setNumberOfCalls(int numOfCalls) {
        this.getGauge(MONITORED_CALLS_NUM_METRIC_NAME).set(numOfCalls);
        return this;
    }
}
