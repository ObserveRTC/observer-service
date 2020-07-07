package org.observertc.webrtc.service.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.inject.Singleton;
import org.observertc.webrtc.service.samples.MediaStreamAggregate;
import org.observertc.webrtc.service.samples.MediaStreamKey;

@Singleton
public class WebRTCStatsReporter {
	private static final String METRIC_PREFIX = "ObserveRTC";

	private enum Columns {
		EVENT,
		OBSERVER,
		CALL,
		PEERCONNECTION
	}

	private final MeterRegistry meterRegistry;

	private enum Metrics {
		INITIATED_CALL,
		FINISHED_CALL,
		JOINED_PEERCONNECTION,
		DETACHED_PEERCONNECTION
	}

	private enum Tags {
		OBSERVER,
		CALL,
		PEERCONNECTION
	}

	public WebRTCStatsReporter(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void incrementJoinedPeerConnections(UUID observerUUID, UUID callUUID, UUID peerConnectionUUID, LocalDateTime joined) {
		final String metricName = this.getMetricName(Metrics.JOINED_PEERCONNECTION);
//		meterRegistry
//				.counter(metricName,
//						Arrays.asList(
//								Tag.of(Tags.OBSERVER.name(), observerUUID.toString()),
//								Tag.of(Tags.CALL.name(), callUUID.toString()),
//								Tag.of(Tags.PEERCONNECTION.name(), peerConnectionUUID.toString())))
//				.increment();
	}

	public void incrementDetachedPeerConnections(UUID observerUUID, UUID callUUID, UUID peerConnectionUUID, LocalDateTime detached) {
		final String metricName = this.getMetricName(Metrics.DETACHED_PEERCONNECTION);
//		meterRegistry
//				.counter(metricName,
//						Arrays.asList(
//								Tag.of(Tags.OBSERVER.name(), observerUUID.toString()),
//								Tag.of(Tags.CALL.name(), callUUID.toString()),
//								Tag.of(Tags.PEERCONNECTION.name(), peerConnectionUUID.toString())))
//				.increment();
	}

	public void incrementInitiatedCalls(UUID observerUUID, UUID callUUID, LocalDateTime initiated) {
		final String metricName = this.getMetricName(Metrics.INITIATED_CALL);
//		meterRegistry
//				.counter(metricName,
//						Arrays.asList(
//								Tag.of(Tags.OBSERVER.name(), observerUUID.toString()),
//								Tag.of(Tags.CALL.name(), callUUID.toString())))
//
//				.increment();
	}

	public void incrementFinishedCalls(UUID observerUUID, UUID callUUID, LocalDateTime finished) {
		final String metricName = this.getMetricName(Metrics.FINISHED_CALL);
//		meterRegistry
//				.counter(metricName,
//						Arrays.asList(
//								Tag.of(Tags.OBSERVER.name(), observerUUID.toString()),
//								Tag.of(Tags.CALL.name(), callUUID.toString())))
//				.increment();
	}

	public void reportMediaStreamSample(MediaStreamKey mediaStreamKey, MediaStreamAggregate mediaStreamAggregate) {
	}

	private String getMetricName(Metrics metric) {
		return String.format("%s_%s", METRIC_PREFIX, metric.name().toLowerCase());
	}
}
