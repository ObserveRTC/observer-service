package org.observertc.observer.metrics;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectUtil;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import io.micrometer.core.instrument.Tag;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.TaskAbstract;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.repositories.HamokStorages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Singleton
public class RepositoryMetrics {
    private static final Logger logger = LoggerFactory.getLogger(ReportMetrics.class);

    private static final String REPOSITORY_METRICS_PREFIX = "repository";

    private static final String TASK_EXECUTION_TIME_METRIC_NAME = "task_execution_time";
    private static final String TASK_EXECUTIONS_METRIC_NAME = "task_executions";
    private static final String TASK_TAG_NAME = "task";
    private static final String ENTRIES_METRIC_NAME = "entries";
    private static final String TASK_SUCCEEDED_TAG_NAME = "succeeded";
    private static final String MAP_TAG_NAME = "map";

    @Inject
    Metrics metrics;

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    ObserverConfig.MetricsConfig.RepositoryMetricsConfig config;

    private String taskExecutionTimeMetricName;
    private String taskExecutionsMetricName;
    private String entriesMetricName;

    private List<IMap> maps;
    private List<MultiMap> multiMaps;
    private Disposable timer = null;

    @PostConstruct
    void init() {
        this.taskExecutionTimeMetricName = metrics.getMetricName(REPOSITORY_METRICS_PREFIX, TASK_EXECUTION_TIME_METRIC_NAME);
        this.taskExecutionsMetricName = metrics.getMetricName(REPOSITORY_METRICS_PREFIX, TASK_EXECUTIONS_METRIC_NAME);
        this.entriesMetricName = metrics.getMetricName(REPOSITORY_METRICS_PREFIX, ENTRIES_METRIC_NAME);
        if (this.config.enabled) {
            var worker = Schedulers.computation().createWorker();
            this.timer = worker.schedulePeriodically(this::expose, this.config.exposePeriodInMin, this.config.exposePeriodInMin, TimeUnit.MINUTES);
            logger.info("Scheduler is added to expose metrics in every {} minutes", this.config.exposePeriodInMin);
        }
        this.maps = List.of(
                this.hazelcastMaps.getCalls(),
                this.hazelcastMaps.getServiceRoomToCallIds(),
                this.hazelcastMaps.getClients(),
                this.hazelcastMaps.getPeerConnections(),
                this.hazelcastMaps.getMediaTracks(),
                this.hazelcastMaps.getInboundTrackIdsToOutboundTrackIds(),
                this.hazelcastMaps.getSFUs(),
                this.hazelcastMaps.getSFUTransports(),
                this.hazelcastMaps.getSfuInternalInboundRtpPadIdToOutboundRtpPadId(),
                this.hazelcastMaps.getSfuStreams(),
                this.hazelcastMaps.getSfuSinks(),
                this.hazelcastMaps.getGeneralEntries(),
                this.hazelcastMaps.getWeakLocks(),
                this.hazelcastMaps.getSyncTaskStates(),
                this.hazelcastMaps.getRequests()

        );
        this.multiMaps = List.of(
                this.hazelcastMaps.getCallToClientIds(),
                this.hazelcastMaps.getClientToPeerConnectionIds(),
                this.hazelcastMaps.getPeerConnectionToInboundTrackIds(),
                this.hazelcastMaps.getPeerConnectionToOutboundTrackIds(),
                this.hazelcastMaps.getSfuStreamIdToRtpPadIds(),
                this.hazelcastMaps.getSfuSinkIdToRtpPadIds(),
                this.hazelcastMaps.getSfuStreamIdToInternalOutboundRtpPadIds()
        );
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
        if (this.timer != null && !this.timer.isDisposed()) {
            this.timer.dispose();
            logger.info("Scheduler is destroyed");
        }
    }

    private void expose() {
        this.maps.forEach(map -> this.report(map, map.size()));
        this.multiMaps.forEach(multimap -> this.report(multimap, multimap.size()));
    }

    private void report(DistributedObject distributedObject, Integer size) {
        var mapName = DistributedObjectUtil.getName(distributedObject);
        var tags = List.of(Tag.of(MAP_TAG_NAME, mapName));
        this.metrics.registry.gauge(this.entriesMetricName, tags, size);
    }
}
