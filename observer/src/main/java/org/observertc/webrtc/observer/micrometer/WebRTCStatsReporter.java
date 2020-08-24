package org.observertc.webrtc.observer.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import javax.inject.Singleton;

@Singleton
public class WebRTCStatsReporter {
	private static final String METRIC_PREFIX = "ObserveRTC";

	private final MeterRegistry meterRegistry;

	public WebRTCStatsReporter(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

}
