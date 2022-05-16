package org.observertc.observer.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.TaskAbstract;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Singleton
public class ExposedMetrics {
    private static final String OBSERVERTC_PREFIX = "observertc";
    private static final String SAMPLES_PREFIX = "samples";
    private static final String OBSERVERTC_SAMPLES_OPENED_WEBSOCKETS = String.join("_", OBSERVERTC_PREFIX, SAMPLES_PREFIX, "opened_websockets");
    private static final String OBSERVERTC_SAMPLES_CLOSED_WEBSOCKETS = String.join("_", OBSERVERTC_PREFIX, SAMPLES_PREFIX, "closed_websockets");
    private static final String OBSERVERTC_SAMPLES_RECEIVED = String.join("_", OBSERVERTC_PREFIX, SAMPLES_PREFIX, "received");

    private static final String OBSERVERTC_MODULE_FLAWS = String.join("_", OBSERVERTC_PREFIX, "module_flaws");

    private static final String OBSERVERTC_TASK_EXECUTION_TIME = String.join("_", OBSERVERTC_PREFIX, "task_execution_time");
    private static final String OBSERVERTC_TASK_EXECUTIONS = String.join("_", OBSERVERTC_PREFIX, "task_executions");

    private static final String OBSERVERTC_GENERATED_REPORTS = String.join("_", OBSERVERTC_PREFIX, "generated_reports");


    private static final String SERVICE_TAG_NAME = "service";
    private static final String MEDIA_UNIT_TAG_NAME = "mediaunit";
    private static final String MODULE_TAG_NAME = "module";
    private static final String TASK_TAG_NAME = "task";
    private static final String TASK_SUCCEEDED_TAG_NAME = "succeeded";



    @Inject
    MeterRegistry meterRegistry;

    // TODO: Health Related metrics
    public static final String OBSERVERTC_EVALUATORS_ADD_NEW_CALL_ENTITIES_EXECUTION_TIME = "observertc-evaluators-add-new-call-entities";
    public static final String OBSERVERTC_EVALUATORS_ADD_NEW_SFU_ENTITIES_EXECUTION_TIME = "observertc-evaluators-add-new-sfu-entities";
    public static final String OBSERVERTC_EVALUATORS_COLLECT_CALL_SAMPLES_TIME = "observertc-evaluators-collect-call-samples";

    public static final String OBSERVERTC_EVALUATORS_COLLECT_SFU_SAMPLES_TIME = "observertc-evaluators-collect-sfu-samples";

    public static final String OBSERVERTC_EVALUATORS_DEMUX_COLLECTED_CALL_SAMPLES_TIME = "observertc-evaluators-demux-collected-call-samples";
    public static final String OBSERVERTC_EVALUATORS_DEMUX_COLLECTED_SFU_SAMPLES_TIME = "observertc-evaluators-demux-collected-sfu-samples";


    public void incrementSamplesOpenedWebsockets(String serviceId, String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_SAMPLES_OPENED_WEBSOCKETS, SERVICE_TAG_NAME, serviceId, MEDIA_UNIT_TAG_NAME, mediaUnitId);
    }

    public void incrementSamplesClosedWebsockets(String serviceId, String mediaUnitId) {
        this.meterRegistry.counter(OBSERVERTC_SAMPLES_CLOSED_WEBSOCKETS, SERVICE_TAG_NAME, serviceId, MEDIA_UNIT_TAG_NAME, mediaUnitId);
    }

    public void incrementSamplesReceived(String serviceId, String mediaUnitId) {
        this.incrementSamplesReceived(serviceId, mediaUnitId, 1);
    }

    public void incrementSamplesReceived(String serviceId, String mediaUnitId, int value) {
        this.meterRegistry.counter(OBSERVERTC_SAMPLES_RECEIVED, SERVICE_TAG_NAME, serviceId, MEDIA_UNIT_TAG_NAME, mediaUnitId).increment(value);
    }


    public void incrementGeneratedReports() {
        this.incrementGeneratedReports(1);
    }

    public void incrementGeneratedReports(int value) {
        this.meterRegistry.counter(OBSERVERTC_GENERATED_REPORTS).increment(value);
    }

    public void incrementModuleFlaws(String moduleId) {
        this.incrementModuleFlaws(moduleId, 1);
    }

    public void incrementModuleFlaws(String moduleId, int value) {
        this.meterRegistry.counter(OBSERVERTC_MODULE_FLAWS, MODULE_TAG_NAME, moduleId).increment(value);
    }

    public void processTaskStats(TaskAbstract.Stats stats) {
        if (Objects.isNull(stats)) {
            return;
        }
        if (Objects.nonNull(stats.started) && Objects.nonNull(stats.ended)) {
            this.addTaskExecutionTime(stats.taskName, stats.started, stats.ended);
        }
        this.incrementTaskExecutions(stats.taskName, stats.succeeded);
    }

    public void addTaskExecutionTime(String taskName, Instant started, Instant ended) {
        this.meterRegistry
                .timer(OBSERVERTC_TASK_EXECUTION_TIME, TASK_TAG_NAME, taskName)
                .record(Duration.between(started, ended));
    }

    public void incrementTaskExecutions(String taskName, boolean succeeded) {
        this.meterRegistry
                .counter(OBSERVERTC_TASK_EXECUTIONS, TASK_TAG_NAME, taskName, TASK_SUCCEEDED_TAG_NAME, succeeded ? "true" : "false")
                .increment();
    }
}
