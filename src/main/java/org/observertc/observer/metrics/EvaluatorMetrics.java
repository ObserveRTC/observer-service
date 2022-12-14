package org.observertc.observer.metrics;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;

@Singleton
public class EvaluatorMetrics {
    private static final String EVALUATOR_METRICS_PREFIX = "evaluators";

    private static final String EXECUTION_DURATIONS_TAG_NAME = "execution_durations";
    private static final String EVALUATOR_NAME_TAG_NAME = "component";
    private static final String COMMIT_CALL_ENTITIES_NAME_TAG_NAME = "commit_call_entities";
    private static final String COMMIT_SFU_ENTITIES_NAME_TAG_NAME = "commit_sfu_entities";
    private static final String OPERATION_SYSTEM_TAG_NAME = "observed_operation_system";


    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.SourceMetricsConfig config;

    private String executionDurations;

    @PostConstruct
    void setup() {
        this.executionDurations = metrics.getMetricName(EVALUATOR_METRICS_PREFIX, EXECUTION_DURATIONS_TAG_NAME);
    }

    public EvaluatorMetrics addTaskExecutionTime(String evaluatorName, Instant started, Instant ended) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry
                .timer(
                        this.executionDurations,
                        EVALUATOR_NAME_TAG_NAME, evaluatorName
                )
                .record(Duration.between(started, ended));
        return this;
    }

    public EvaluatorMetrics addCommitCallEntitiesExecutionTime(Instant started, Instant ended) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry
                .timer(
                        metrics.getMetricName(EVALUATOR_METRICS_PREFIX, COMMIT_CALL_ENTITIES_NAME_TAG_NAME)
                        )
                .record(Duration.between(started, ended));
        return this;
    }

    public EvaluatorMetrics addCommitSfuEntitiesExecutionTime(Instant started, Instant ended) {
        if (!this.config.enabled) {
            return this;
        }
        this.metrics.registry
                .timer(
                        metrics.getMetricName(EVALUATOR_METRICS_PREFIX, COMMIT_SFU_ENTITIES_NAME_TAG_NAME)
                        )
                .record(Duration.between(started, ended));
        return this;
    }

}
