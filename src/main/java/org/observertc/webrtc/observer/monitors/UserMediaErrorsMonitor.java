package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.health.HeartbeatEvent;
import javafx.util.Pair;
import org.observertc.webrtc.observer.repositories.hazelcast.UserMediaErrorsRepository;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.observertc.webrtc.observer.tasks.UserMediaErrorsExtractorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Singleton
@Requires(notEnv = Environment.TEST)
public class UserMediaErrorsMonitor implements ApplicationEventListener<HeartbeatEvent> {
    private static final String METRIC_NAME = "usermediaerrors";
    private static final String MEDIA_UNIT_TAGS_NAME = "mediaunit";
    private static final String SERVICE_UUID_TAG_NAME = "serviceUUID";
    private static final Logger logger = LoggerFactory.getLogger(UserMediaErrorsMonitor.class);

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    TasksProvider tasksProvider;

    private Instant lastExecuted = Instant.now();

    public UserMediaErrorsMonitor() {

    }

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        if (Duration.between(this.lastExecuted, Instant.now()).getSeconds() < 300) {
            return;
        }
        try {
            this.execute();
        } catch (Throwable t) {
            logger.error("Unexpected error occurred during execution", t);
        }
        this.lastExecuted = Instant.now();
    }

    @Timed(
            value = "observer_calls_monitor_execution",
            description = "Execution time of collecting and analyzing locally owned calls to provide metrics"
    )
    private void execute() {
        UserMediaErrorsExtractorTask task = this.tasksProvider.getUserMediaErrorExtractorTask();

        if (!task.execute().succeeded()) { // automatically rolled back
            return;
        }

        Map<String, Integer> locallyTrackedErrors = task.getResult();
        if (locallyTrackedErrors.size() < 1) {
            return;
        }
        Iterator<Map.Entry<String, Integer>> it = locallyTrackedErrors.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            Pair<UUID, String> pair = UserMediaErrorsRepository.splitKey(entry.getKey());
            Integer trackedErrors = entry.getValue();
            this.meterRegistry.counter(METRIC_NAME,
                    SERVICE_UUID_TAG_NAME, pair.getKey().toString(),
                    MEDIA_UNIT_TAGS_NAME, pair.getValue())
                    .increment(trackedErrors);
        }
    }
}
