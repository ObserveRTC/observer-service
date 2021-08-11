package org.observertc.webrtc.observer.micrometer;

import io.micrometer.core.instrument.MeterRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExposedMetrics {
    private static final String OBSERVERTC_PREFIX = "observertc";
    private static final String CLIENT_SAMPLES_PREFIX = "clientsamples";
    private static final String OBSERVERTC_CLIENT_SAMPLES_OPENED_WEBSOCKETS = String.join("_", OBSERVERTC_PREFIX, CLIENT_SAMPLES_PREFIX, "opened_websockets");
    private static final String OBSERVERTC_CLIENT_SAMPLES_CLOSED_WEBSOCKETS = String.join("_", OBSERVERTC_PREFIX, CLIENT_SAMPLES_PREFIX, "closed_websockets");
    private static final String OBSERVERTC_CLIENT_SAMPLES_RECEIVED = String.join("_", OBSERVERTC_PREFIX, CLIENT_SAMPLES_PREFIX, "received");

    private static final String SFU_SAMPLES_PREFIX = "sfusamples";
    private static final String OBSERVERTC_SFU_SAMPLES_OPENED_WEBSOCKETS = String.join("_", OBSERVERTC_PREFIX, SFU_SAMPLES_PREFIX, "opened_websockets");
    private static final String OBSERVERTC_SFU_SAMPLES_CLOSED_WEBSOCKETS = String.join("_", OBSERVERTC_PREFIX, SFU_SAMPLES_PREFIX, "closed_websockets");
    private static final String OBSERVERTC_SFU_SAMPLES_RECEIVED = String.join("_", OBSERVERTC_PREFIX, CLIENT_SAMPLES_PREFIX, "received");

    private static final String SERVICE_TAG_NAME = "service";
    private static final String MEDIA_UNIT_TAG_NAME = "mediaunit";


    @Inject
    MeterRegistry meterRegistry;

    // TODO: Health Related metrics
    public static final String OBSERVERTC_EVALUATORS_ADD_NEW_CALL_ENTITIES_EXECUTION_TIME = "observertc-evaluators-add-new-call-entities";
    public static final String OBSERVERTC_EVALUATORS_ADD_NEW_SFU_ENTITIES_EXECUTION_TIME = "observertc-evaluators-add-new-sfu-entities";
    public static final String OBSERVERTC_EVALUATORS_COLLECT_CALL_SAMPLES_TIME = "observertc-evaluators-collect-call-samples";

    public static final String OBSERVERTC_EVALUATORS_COLLECT_SFU_SAMPLES_TIME = "observertc-evaluators-collect-sfu-samples";

    public static final String OBSERVERTC_EVALUATORS_DEMUX_COLLECTED_CALL_SAMPLES_TIME = "observertc-evaluators-demux-collected-call-samples";
    public static final String OBSERVERTC_EVALUATORS_DEMUX_COLLECTED_SFU_SAMPLES_TIME = "observertc-evaluators-demux-collected-sfu-samples";



    // TODO: Client Samples Related Metrics

    public void incrementClientSamplesOpenedWebsockets(String serviceId, String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_CLIENT_SAMPLES_OPENED_WEBSOCKETS, SERVICE_TAG_NAME, serviceId, MEDIA_UNIT_TAG_NAME, mediaUnitId);
    }

    public void incrementClientSamplesClosedWebsockets(String serviceId, String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_CLIENT_SAMPLES_CLOSED_WEBSOCKETS, SERVICE_TAG_NAME, serviceId, MEDIA_UNIT_TAG_NAME, mediaUnitId);
    }

    public void incrementClientSamplesReceived(String serviceId, String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_CLIENT_SAMPLES_RECEIVED, SERVICE_TAG_NAME, serviceId, MEDIA_UNIT_TAG_NAME, mediaUnitId);
    }

    // TODO: Sfu Samples Related Metrics
    public void incrementSfuSamplesOpenedWebsockets( String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_SFU_SAMPLES_OPENED_WEBSOCKETS, MEDIA_UNIT_TAG_NAME, mediaUnitId);
    }

    public void incrementSfuSamplesClosedWebsockets(String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_SFU_SAMPLES_CLOSED_WEBSOCKETS, MEDIA_UNIT_TAG_NAME, mediaUnitId);
    }

    public void incrementSfuSamplesReceived(String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_SFU_SAMPLES_RECEIVED, MEDIA_UNIT_TAG_NAME, mediaUnitId);
    }
}
