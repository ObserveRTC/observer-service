package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class SentinelMetrics {
    private static final Logger logger = LoggerFactory.getLogger(SentinelMetrics.class);

    private static final String SENTINEL_TAG_NAME = "sentinel";
    private static final String MONITORED_SSRCS_NUM_METRIC_NAME = "observertc_monitored_ssrcs_num";
    private static final String MONITORED_PEER_CONNECTIONS_NUM_METRIC_NAME = "observertc_monitored_pcs_num";
    private static final String MONITORED_CALLS_NUM_METRIC_NAME = "observertc_monitored_calls_num";
    private static final String MONITORED_MEDIA_UNITS_COUNTER_METRIC_NAME = "observertc_monitored_media_units";
    private static final String MONITORED_BROWSER_IDS_NUM_METRIC_NAME = "observertc_monitored_browser_ids_num";
    private static final String MONITORED_USER_NAMES_NUM_METRIC_NAME = "observertc_monitored_user_names_num";

    private static final String MONITORED_BYTES_RECEIVED_METRIC_NAME = "observertc_monitored_bytes_received";
    private static final String MONITORED_PACKETS_RECEIVED_METRIC_NAME = "observertc_monitored_packets_received";
    private static final String MONITORED_PACKETS_LOST_METRIC_NAME = "observertc_monitored_packets_lost";

    private static final String MONITORED_BYTES_SENT_METRIC_NAME = "observertc_monitored_bytes_sent";
    private static final String MONITORED_PACKETS_SENT_METRIC_NAME = "observertc_monitored_packets_sent";

    private static final String MONITORED_RTT_METRIC_NAME = "observertc_monitored_rtt";
    @Inject
    MeterRegistry meterRegistry;

    private String sentinelName = null;
    private final Map<String, AtomicLong> gauges;
    private final Map<String, DistributionSummary> summaries;
    private List<Tag> baseTags;

    SentinelMetrics() {
        this.gauges = new HashMap<>();
        this.summaries = new HashMap<>();
    }

    SentinelMetrics initialize(String sentinelName) {
        if (Objects.nonNull(this.sentinelName)) {
            logger.warn("A sentinel metric has already been initialized with the name {}, therefore the newly provided name {} will not take any effect", this.sentinelName, sentinelName);
            return this;
        }
        this.sentinelName = sentinelName;
        var sentinelTag = Tag.of(SENTINEL_TAG_NAME, sentinelName);
        this.baseTags = List.of(sentinelTag);
        return this;
    }

    private AtomicLong getGauge(String metricsName) {
        if (Objects.isNull(this.sentinelName)) {
            throw new IllegalStateException("SentinelMetric cannot exists withou a name! Did you initialize this metric?");
        }
        AtomicLong result = this.gauges.get(metricsName);
        if (Objects.isNull(result)) {
            result = this.meterRegistry.gauge(metricsName, this.baseTags, new AtomicLong(0), AtomicLong::doubleValue);
            this.gauges.put(metricsName, result);
        }
        return result;
    }

    private DistributionSummary getSummary(String metricsName) {
        if (Objects.isNull(this.sentinelName)) {
            throw new IllegalStateException("SentinelMetric cannot exists withou a name! Did you initialize this metric?");
        }
        DistributionSummary result = this.summaries.get(metricsName);
        if (Objects.isNull(result)) {
            result = this.meterRegistry.summary(metricsName, this.baseTags);
            this.summaries.put(metricsName, result);
        }
        return result;
    }

    private void incrementMediaUnit(String mediaUnit) {
        if (Objects.isNull(this.sentinelName)) {
            throw new IllegalStateException("SentinelMetric cannot exists withou a name! Did you initialize this metric?");
        }
        if (Objects.isNull(mediaUnit)) {
            mediaUnit = "Unknown";
        }
        List<Tag> tags = new ArrayList<>(this.baseTags);
        tags.add(Tag.of("mediaUnit", mediaUnit));
        this.meterRegistry.counter(MONITORED_MEDIA_UNITS_COUNTER_METRIC_NAME, tags).increment();
    }

    public SentinelMetrics setNumberOfSSRCs(int numOfSSRCs) {
        this.getGauge(MONITORED_SSRCS_NUM_METRIC_NAME).set(numOfSSRCs);
        return this;
    }

    public SentinelMetrics setNumberOfPeerConnections(int numOfPCs) {
        this.getGauge(MONITORED_PEER_CONNECTIONS_NUM_METRIC_NAME).set(numOfPCs);
        return this;
    }

    public SentinelMetrics setNumberOfUserNames(int numOfUserNames) {
        this.getGauge(MONITORED_USER_NAMES_NUM_METRIC_NAME).set(numOfUserNames);
        return this;
    }

    public SentinelMetrics setNumberOfCalls(int numOfCalls) {
        this.getGauge(MONITORED_CALLS_NUM_METRIC_NAME).set(numOfCalls);
        return this;
    }

    public SentinelMetrics setNumberOfBrowserIds(int browserIds) {
        this.getGauge(MONITORED_BROWSER_IDS_NUM_METRIC_NAME).set(browserIds);
        return this;
    }

    public SentinelMetrics setBytesReceived(long bytesReceived) {
        this.getGauge(MONITORED_BYTES_RECEIVED_METRIC_NAME).set(bytesReceived);
        return this;
    }

    public SentinelMetrics setPacketsReceived(long packetsReceived) {
        this.getGauge(MONITORED_PACKETS_RECEIVED_METRIC_NAME).set(packetsReceived);
        return this;
    }

    public SentinelMetrics setBytesSent(long bytesSent) {
        this.getGauge(MONITORED_BYTES_SENT_METRIC_NAME).set(bytesSent);
        return this;
    }

    public SentinelMetrics setPacketsSent(long packetsSent) {
        this.getGauge(MONITORED_PACKETS_SENT_METRIC_NAME).set(packetsSent);
        return this;
    }

    public SentinelMetrics setPacketsLost(long packetsLost) {
        this.getGauge(MONITORED_PACKETS_LOST_METRIC_NAME).set(packetsLost);
        return this;
    }

    public SentinelMetrics setRoundTripTimeDistributions(List<Double> rttAvgs) {
        rttAvgs.forEach(this.getSummary(MONITORED_RTT_METRIC_NAME)::record);
        return null;
    }

    public SentinelMetrics incrementMediaUnits(Set<String> mediaUnits) {
        if (Objects.isNull(mediaUnits)) {
            return this;
        }
        mediaUnits.stream().forEach(this::incrementMediaUnit);
        return this;
    }
}
