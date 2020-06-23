package com.observertc.gatekeeper.webrtcstat.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Arrays;
import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class WebRTCStatsReporter {
	private final MeterRegistry meterRegistry;

	private enum Tags {
		OBSERVER,
		CALL,
		PEERCONNECTION
	}

	public WebRTCStatsReporter(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void incrementJoinedPeerConnections(UUID observerUUID, UUID callUUID, int numberOfPeerConnections) {
		meterRegistry
				.counter("ObserveRTC_joinedPeerConnections",
						Arrays.asList(
								Tag.of(Tags.OBSERVER.name(), observerUUID.toString()),
								Tag.of(Tags.CALL.name(), callUUID.toString())))
				.increment(numberOfPeerConnections);

	}

	public void incrementDetachedPeerConnections(UUID observerUUID, UUID callUUID, int numberOfPeerConnections) {
		meterRegistry
				.counter("ObserveRTC_detachedPeerConnections",
						Arrays.asList(
								Tag.of(Tags.OBSERVER.name(), observerUUID.toString()),
								Tag.of(Tags.CALL.name(), callUUID.toString())))
				.increment(numberOfPeerConnections);

	}

	public void incrementNumberOfInitiatedCalls(UUID observerUUID) {
		meterRegistry
				.counter("ObserveRTC_initiatedCalls",
						Arrays.asList(
								Tag.of(Tags.OBSERVER.name(), observerUUID.toString())))
				.increment();

	}

	public void incrementNumberOfFinishedCalls(UUID observerUUID) {
		meterRegistry
				.counter("ObserveRTC_finishedCalls",
						Arrays.asList(
								Tag.of(Tags.OBSERVER.name(), observerUUID.toString())))
				.increment();

	}
}
