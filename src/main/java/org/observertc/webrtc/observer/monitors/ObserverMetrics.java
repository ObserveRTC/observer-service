package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class ObserverMetrics {
    private static final String OBSERVERTC_PREFIX = "observertc";
    private static final String INITIATED_CALLS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "initiated_calls");
    private static final String FINISHED_CALLS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "finished_calls");
    private static final String JOINED_PCS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "joined_pcs");
    private static final String DETACHED_PCS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "detached_pcs");
    private static final String IMPAIRABLE_PCS_COUNTER_NAME = String.join("_", OBSERVERTC_PREFIX, "impairable_pcs");;

    private static final String SERVICE_TAG_NAME = "service";
    private static final String MEDIA_UNIT_TAG_NAME = "mediaunit";


    @Inject
    MeterRegistry meterRegistry;

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
}
