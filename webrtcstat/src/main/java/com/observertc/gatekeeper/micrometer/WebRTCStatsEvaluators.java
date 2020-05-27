package com.observertc.gatekeeper.micrometer;

import com.observertc.gatekeeper.dto.WebRTCStatDTO;
import io.micrometer.core.instrument.MeterRegistry;
import javax.inject.Singleton;

@Singleton
public class WebRTCStatsEvaluators {
	private final MeterRegistry meterRegistry;

	public WebRTCStatsEvaluators(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void accept(WebRTCStatDTO stats) {
		meterRegistry.counter("myCounter").increment();
	}
}
