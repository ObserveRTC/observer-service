package org.observertc.webrtc.observer.service.micrometer;

import org.observertc.webrtc.observer.service.bigquery.CallReports;
import org.observertc.webrtc.observer.service.bigquery.StreamSample;
import org.observertc.webrtc.observer.service.samples.MediaStreamKey;
import org.observertc.webrtc.observer.service.samples.MediaStreamSample;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.inject.Singleton;

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

	private final CallReports callReports;

	public WebRTCStatsReporter(MeterRegistry meterRegistry, CallReports callReports) {
		this.meterRegistry = meterRegistry;
		this.callReports = callReports;
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
		this.callReports.joinedPeerConnections(observerUUID, callUUID, peerConnectionUUID, joined);
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
		this.callReports.detachedPeerConnections(observerUUID, callUUID, peerConnectionUUID, detached);
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
		this.callReports.initiatedCall(observerUUID, callUUID, initiated);
	}

	public void incrementFinishedCalls(UUID observerUUID, UUID callUUID, LocalDateTime finished) {
		final String metricName = this.getMetricName(Metrics.FINISHED_CALL);
//		meterRegistry
//				.counter(metricName,
//						Arrays.asList(
//								Tag.of(Tags.OBSERVER.name(), observerUUID.toString()),
//								Tag.of(Tags.CALL.name(), callUUID.toString())))
//				.increment();
		this.callReports.finishedCall(observerUUID, callUUID, finished);
	}

	public void reportMediaStreamSample(MediaStreamKey mediaStreamKey, MediaStreamSample mediaStreamSample) {
		StreamSample streamSample = StreamSample.from(mediaStreamKey, mediaStreamSample);
		this.callReports.reportStreamSample(streamSample);
	}

	private String getMetricName(Metrics metric) {
		return String.format("%s_%s", METRIC_PREFIX, metric.name().toLowerCase());
	}
}
