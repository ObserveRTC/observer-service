package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Singleton
public class ObserverMetrics {
    private static final String OBSERVERTC_PREFIX = "observertc";
    private static final String INITIATED_CALLS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "initiated_calls");
    private static final String FINISHED_CALLS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "finished_calls");
    private static final String JOINED_PCS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "joined_pcs");
    private static final String DETACHED_PCS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "detached_pcs");
    private static final String CALL_DURATIONS_IN_MINUTES_NAME = String.join("_", OBSERVERTC_PREFIX, "call_durations_in_mins");
    private static final String IMPAIRABLE_PCS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "impairable_pcs");;

    private static final String SERVICE_TAG_NAME = "service";
    private static final String MEDIA_UNIT_TAG_NAME = "mediaunit";


    @Inject
    MeterRegistry meterRegistry;

    private final Map<String, DistributionSummary> durations = new HashMap<>();

    public void incrementInitiatedCall(String service) {
        List<Tag> tags = List.of(
                Tag.of(SERVICE_TAG_NAME, service)
        );
        this.meterRegistry.counter(INITIATED_CALLS_COUNTER_NAME, tags).increment();
    }

    public void incrementFinishedCall(String service) {
        List<Tag> tags = List.of(
                Tag.of(SERVICE_TAG_NAME, service)
        );
        this.meterRegistry.counter(FINISHED_CALLS_COUNTER_NAME, tags).increment();
    }

    public void incrementJoinedPCs(String service, String mediaUnit) {
        List<Tag> tags = List.of(
                Tag.of(SERVICE_TAG_NAME, service),
                Tag.of(MEDIA_UNIT_TAG_NAME, mediaUnit)
        );
        this.meterRegistry.counter(JOINED_PCS_COUNTER_NAME, tags).increment();
    }

    public void incrementDetachedPCs(String service, String mediaUnit) {
        List<Tag> tags = List.of(
                Tag.of(SERVICE_TAG_NAME, service),
                Tag.of(MEDIA_UNIT_TAG_NAME, mediaUnit)
        );
        this.meterRegistry.counter(DETACHED_PCS_COUNTER_NAME, tags).increment();
    }

    public void incrementImpairedPCs(String service, String mediaUnit) {
        List<Tag> tags = List.of(
                Tag.of(SERVICE_TAG_NAME, service),
                Tag.of(MEDIA_UNIT_TAG_NAME, mediaUnit)
        );
        this.meterRegistry.counter(IMPAIRABLE_PCS_COUNTER_NAME, tags).increment();
    }

    public void addCallDuration(String serviceName, Duration duration) {
        DistributionSummary summary = this.durations.get(serviceName);
        if (Objects.isNull(summary)) {
            summary = this.meterRegistry.summary(CALL_DURATIONS_IN_MINUTES_NAME, List.of(Tag.of(SERVICE_TAG_NAME, serviceName)));
            this.durations.put(serviceName, summary);
        }
        summary.record(duration.getSeconds() / 60);
    }
}
