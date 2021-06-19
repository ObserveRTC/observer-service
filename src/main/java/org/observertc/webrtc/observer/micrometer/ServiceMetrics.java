package org.observertc.webrtc.observer.micrometer;

import io.micrometer.core.instrument.MeterRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServiceMetrics {
    private static final String OBSERVERTC_PREFIX = "observertc";
    private static final String CLIENT_SAMPLES_PREFIX = "clientsamples";
    private static final String OBSERVERTC_CLIENT_SAMPLES_OPENED_WEBSOCKETS = String.join("_", OBSERVERTC_PREFIX, CLIENT_SAMPLES_PREFIX, "opened_websockets");
    private static final String OBSERVERTC_CLIENT_SAMPLES_CLOSED_WEBSOCKETS = String.join("_", OBSERVERTC_PREFIX, CLIENT_SAMPLES_PREFIX, "closed_websockets");
    private static final String OBSERVERTC_CLIENT_SAMPLES_RECEIVED = String.join("_", OBSERVERTC_PREFIX, CLIENT_SAMPLES_PREFIX, "received");

    private static final String SERVICE_TAG_NAME = "service";
    private static final String MEDIA_UNIT_TAG_NAME = "mediaunit";


    @Inject
    MeterRegistry meterRegistry;

    public void incrementClientSamplesOpenedWebsockets(String serviceId, String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_CLIENT_SAMPLES_OPENED_WEBSOCKETS, serviceId, mediaUnitId);
    }

    public void incrementClientSamplesClosedWebsockets(String serviceId, String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_CLIENT_SAMPLES_CLOSED_WEBSOCKETS, serviceId, mediaUnitId);
    }

    public void incrementClientSamplesReceived(String serviceId, String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_CLIENT_SAMPLES_RECEIVED, serviceId, mediaUnitId);
    }
}
