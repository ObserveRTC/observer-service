package org.observertc.observer.metrics;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.HamokService;
import org.observertc.observer.repositories.RepositoryStorageMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class HamokMetrics {
    private static final Logger logger = LoggerFactory.getLogger(ReportMetrics.class);

    private static final String HAMOK_METRICS_PREFIX = "hamok";

    private static final String MESSAGES_SENT_METRIC_NAME = "messages_sent";
    private static final String MESSAGES_RECEIVED_METRIC_NAME = "messages_received";
    private static final String BYTES_SENT_METRIC_NAME = "bytes_sent";
    private static final String BYTES_RECEIVED_METRIC_NAME = "bytes_received";
    private static final String PENDING_REQUESTS_METRIC_NAME = "pending_requests";
    private static final String PENDING_RESPONSES_METRIC_NAME = "pending_responses";
    private static final String NOT_RESPONDING_REMOTE_IDS_METRIC_NAME = "not_responding_remote_peers";


    @Inject
    Metrics metrics;

    @Inject
    HamokService hamokService;

    private String taskExecutionTimeMetricName;
    private String taskExecutionsMetricName;
    private final AtomicInteger messagesSent = new AtomicInteger(0);
    private final AtomicInteger messagesReceived = new AtomicInteger(0);
    private final AtomicLong bytesSent = new AtomicLong(0);
    private final AtomicLong bytesReceived = new AtomicLong(0);
    private final AtomicInteger pendingRequests = new AtomicInteger(0);
    private final AtomicInteger pendingResponses = new AtomicInteger(0);

    private List<RepositoryStorageMetrics> storageMetrics;

    private final Map<String, AtomicInteger> localStorageSizes = new ConcurrentHashMap<>();

    public HamokMetrics(
    ) {

    }

    @PostConstruct
    void init() {
        this.metrics.registry.gauge(metrics.getMetricName(HAMOK_METRICS_PREFIX, MESSAGES_SENT_METRIC_NAME), messagesSent);
        this.metrics.registry.gauge(metrics.getMetricName(HAMOK_METRICS_PREFIX, MESSAGES_RECEIVED_METRIC_NAME), messagesReceived);
        this.metrics.registry.gauge(metrics.getMetricName(HAMOK_METRICS_PREFIX, BYTES_SENT_METRIC_NAME), bytesSent);
        this.metrics.registry.gauge(metrics.getMetricName(HAMOK_METRICS_PREFIX, BYTES_RECEIVED_METRIC_NAME), bytesReceived);
        this.metrics.registry.gauge(metrics.getMetricName(HAMOK_METRICS_PREFIX, PENDING_REQUESTS_METRIC_NAME), pendingRequests);
        this.metrics.registry.gauge(metrics.getMetricName(HAMOK_METRICS_PREFIX, PENDING_RESPONSES_METRIC_NAME), pendingResponses);
    }

    @PreDestroy
    void teardown() {

    }

    public void incrementNotRespondingRemotePeerIds() {
        this.metrics.registry.counter(metrics.getMetricName(HAMOK_METRICS_PREFIX, NOT_RESPONDING_REMOTE_IDS_METRIC_NAME)).increment();
    }

    public void update() {
        var storageGrid = this.hamokService.getStorageGrid();
        var stats = storageGrid.stats();
        this.messagesSent.set(stats.sentMessages());
        this.messagesReceived.set(stats.receivedMessages());
        this.bytesSent.set(stats.receivedBytes());
        this.bytesReceived.set(stats.receivedBytes());
        this.pendingRequests.set(stats.pendingRequests());
        this.pendingResponses.set(stats.pendingResponses());
    }

}
