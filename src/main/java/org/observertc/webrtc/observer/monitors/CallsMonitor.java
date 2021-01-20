package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.health.HeartbeatEvent;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.monitorstats.CallStats;
import org.observertc.webrtc.observer.tasks.CallStatsMakerTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Singleton
@Requires(notEnv = Environment.TEST)
public class CallsMonitor implements ApplicationEventListener<HeartbeatEvent> {
    private static final String CALLS_MONITOR_NAME = "callstats_";
    private static final String P2P_CALLS_METRIC_NAME = CALLS_MONITOR_NAME.concat("peer_to_peer_calls");
    private static final String P2P_STREAMS_METRIC_NAME = CALLS_MONITOR_NAME.concat("peer_to_peer_streams");
    private static final String UNKNOWN_TYPE_CALLS_METRIC_NAME = CALLS_MONITOR_NAME.concat("unknown_type_calls");
    private static final String UNKNOWN_TYPE_CALLS_STREAMS_METRIC_NAME = CALLS_MONITOR_NAME.concat("unknown_type_streams");
    private static final String MEDIA_UNIT_TAGS_NAME = "mediaunit";

    private static final String MEDIA_UNIT_OWNED_CALLS_METRIC_NAME = CALLS_MONITOR_NAME.concat("mediaunit_owned_calls");
    private static final String MEDIA_UNIT_SHARED_CALLS_METRIC_NAME = CALLS_MONITOR_NAME.concat("mediaunit_shared_calls");
    private static final String MEDIA_UNIT_TOTAL_CALLS_METRIC_NAME = CALLS_MONITOR_NAME.concat("mediaunit_total_calls");
    private static final String MEDIA_UNIT_CONCURRENT_STREAMS_METRIC_NAME = CALLS_MONITOR_NAME.concat("mediaunit_concurrent_streams");
    private static final Logger logger = LoggerFactory.getLogger(CallsMonitor.class);

    @Inject
    ObserverConfig.MonitorsConfig.CallsMonitorConfig config;

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    TasksProvider tasksProvider;

    private Instant lastExecuted = Instant.now();
    private boolean enabled = true;
    private int consecutiveErrors = 0;

    public CallsMonitor() {

    }

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        if (!this.config.enabled || !this.enabled) {
            return;
        }
        if (Duration.between(this.lastExecuted, Instant.now()).getSeconds() < this.config.reportPeriodInS) {
            return;
        }
        logger.info("{} is started", CallsMonitor.class.getSimpleName());
        try {
            this.execute();
            this.consecutiveErrors = 0;
        } catch (Throwable t) {
            logger.error("Unexpected error occurred during execution", t);
            ++this.consecutiveErrors;
        }
        if (3 < this.consecutiveErrors) {
            logger.warn("Unexpected error occurred for monitor {} for {} times. The module will be shut down",
                    this.getClass().getSimpleName(),
                    this.consecutiveErrors
                    );
            this.enabled = false;
        }
        this.lastExecuted = Instant.now();
        logger.info("{} is ended", CallsMonitor.class.getSimpleName());
    }

    @Timed(
            value = "observer_calls_monitor_execution",
            description = "Execution time of collecting and analyzing locally owned calls to provide metrics"
    )
    private void execute() {
        CallStatsMakerTask task = this.tasksProvider.getCallStatsMakerTask();
        task
                .withLogger(logger)
                .execute();

        if (!task.succeeded()) {
            logger.info("{} has failed to collect stats", CallStatsMakerTask.class.getSimpleName());
            return;
        }
        CallStats callStats = task.getResult();

        if (Objects.isNull(callStats)) {
            logger.warn("No stats provided by {}", CallStatsMakerTask.class.getSimpleName());
            return;
        }
//        logger.info(ObjectToString.toString(callStats));
        this.meterRegistry.gauge(P2P_CALLS_METRIC_NAME, callStats.p2pCalls);
        this.meterRegistry.gauge(P2P_STREAMS_METRIC_NAME, callStats.p2pStreams);
        this.meterRegistry.gauge(UNKNOWN_TYPE_CALLS_METRIC_NAME, callStats.unknownTypeOfCalls);
        this.meterRegistry.gauge(UNKNOWN_TYPE_CALLS_STREAMS_METRIC_NAME, callStats.unknownTypeOfStreams);
        Iterator<Map.Entry<String, CallStats.MediaUnitStats>> it = callStats.mediaUnitStats.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, CallStats.MediaUnitStats> entry = it.next();
            String mediaUnitId = entry.getKey();
            CallStats.MediaUnitStats mediaUnitStats = entry.getValue();
            List<Tag> tags = Arrays.asList(Tag.of(MEDIA_UNIT_TAGS_NAME, mediaUnitId));
            this.meterRegistry.gauge(MEDIA_UNIT_OWNED_CALLS_METRIC_NAME, tags, mediaUnitStats.ownedCalls);
            this.meterRegistry.gauge(MEDIA_UNIT_SHARED_CALLS_METRIC_NAME, tags, mediaUnitStats.sharedCalls);
            this.meterRegistry.gauge(MEDIA_UNIT_TOTAL_CALLS_METRIC_NAME, tags, mediaUnitStats.totalCalls);
            this.meterRegistry.gauge(MEDIA_UNIT_CONCURRENT_STREAMS_METRIC_NAME, tags, mediaUnitStats.concurrentStreams);
        }
    }
}
