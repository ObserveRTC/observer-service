package org.observertc.observer.metrics;

import io.micrometer.core.instrument.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.TaskAbstract;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Singleton
public class RepositoryMetrics {
    private static final Logger logger = LoggerFactory.getLogger(ReportMetrics.class);

    private static final String REPOSITORY_METRICS_PREFIX = "repository";

    private static final String TASK_EXECUTION_TIME_METRIC_NAME = "task_execution_time";
    private static final String TASK_EXECUTIONS_METRIC_NAME = "task_executions";
    private static final String TASK_TAG_NAME = "task";
    private static final String ENTRIES_METRIC_NAME = "entries";
    private static final String TASK_SUCCEEDED_TAG_NAME = "succeeded";

    private static final String STORAGE_ID_TAG_NAME = "storageId";

    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.RepositoryMetricsConfig config;

    private String taskExecutionTimeMetricName;
    private String taskExecutionsMetricName;
    private String entriesMetricName;

    private List<RepositoryStorageMetrics> storageMetrics;

    public RepositoryMetrics(
                    CallsRepository callsRepository,
                    ClientsRepository clientsRepository,
                    PeerConnectionsRepository peerConnectionsRepository,
                    InboundTracksRepository inboundTracksRepository,
                    OutboundTracksRepository outboundTracksRepository,
                    SfusRepository sfusRepository,
                    SfuTransportsRepository sfuTransportsRepository,
                    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository,
                    SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository,
                    SfuSctpChannelsRepository sfuSctpStreamsRepository
    ) {
        this.storageMetrics = List.of(
                callsRepository,
                clientsRepository,
                peerConnectionsRepository,
                inboundTracksRepository,
                outboundTracksRepository,
                sfusRepository,
                sfuTransportsRepository,
                sfuInboundRtpPadsRepository,
                sfuOutboundRtpPadsRepository,
                sfuSctpStreamsRepository
        );
    }

    @PostConstruct
    void init() {
        this.taskExecutionTimeMetricName = metrics.getMetricName(REPOSITORY_METRICS_PREFIX, TASK_EXECUTION_TIME_METRIC_NAME);
        this.taskExecutionsMetricName = metrics.getMetricName(REPOSITORY_METRICS_PREFIX, TASK_EXECUTIONS_METRIC_NAME);
        this.entriesMetricName = metrics.getMetricName(REPOSITORY_METRICS_PREFIX, ENTRIES_METRIC_NAME);
    }

    public void processTaskStats(TaskAbstract.Stats stats) {
        if (!this.config.enabled || Objects.isNull(stats)) {
            return;
        }
        if (Objects.nonNull(stats.started) && Objects.nonNull(stats.ended)) {
            this.addTaskExecutionTime(stats.taskName, stats.started, stats.ended);
        }
        this.incrementTaskExecutions(stats.taskName, stats.succeeded);
    }

    private void addTaskExecutionTime(String taskName, Instant started, Instant ended) {
        this.metrics.registry
                .timer(this.taskExecutionTimeMetricName, TASK_TAG_NAME, taskName)
                .record(Duration.between(started, ended));
    }

    private void incrementTaskExecutions(String taskName, boolean succeeded) {
        this.metrics.registry
                .counter(this.taskExecutionsMetricName, TASK_TAG_NAME, taskName, TASK_SUCCEEDED_TAG_NAME, succeeded ? "true" : "false")
                .increment();
    }

    @PreDestroy
    void teardown() {

    }

    public void expose() {
        for (var storageMetrics : this.storageMetrics) {
            var tags = List.of(Tag.of(STORAGE_ID_TAG_NAME, storageMetrics.storageId()));
            this.metrics.registry.gauge(this.entriesMetricName, tags, storageMetrics.localSize());
        }
    }

}
