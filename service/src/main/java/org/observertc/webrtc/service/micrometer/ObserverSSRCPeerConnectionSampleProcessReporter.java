package org.observertc.webrtc.service.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import javax.inject.Singleton;

/**
 * I hate the name! We need to refactor it something better.
 * The reason it is so long is because of the tuple.
 * We can either name the processor like Bart from Simpsonse,
 * or we can find some useful name.
 */
@Singleton
public class ObserverSSRCPeerConnectionSampleProcessReporter {
	private static final String METRIC_PREFIX = "ObserveRTC_samplesrocessor";

	private final MeterRegistry meterRegistry;

	private enum Metrics {
		OBSERVER_SSRC_BUFFER_SIZE,
		CALL_IDENTIFIER_EXECUTION_TIME_IN_MS,
		CALL_CLEANER_EXECUTION_TIME_IN_MS,
	}

	public ObserverSSRCPeerConnectionSampleProcessReporter(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void setBufferSize(int size) {
		final String metricName = this.getMetricName(Metrics.OBSERVER_SSRC_BUFFER_SIZE);
		meterRegistry
				.gauge(metricName, size);

	}

	public void setCallIdentificationExecutionTime(Duration duration) {
		final String metricName = this.getMetricName(Metrics.CALL_IDENTIFIER_EXECUTION_TIME_IN_MS);
		meterRegistry
				.gauge(metricName, duration.toMillis());

	}

	public void setCallCleaningExecutionTime(Duration duration) {
		final String metricName = this.getMetricName(Metrics.CALL_CLEANER_EXECUTION_TIME_IN_MS);
		meterRegistry
				.gauge(metricName, duration.toMillis());

	}

	private String getMetricName(Metrics metric) {
		return String.format("%s_%s", METRIC_PREFIX, metric.name().toLowerCase());
	}
}
