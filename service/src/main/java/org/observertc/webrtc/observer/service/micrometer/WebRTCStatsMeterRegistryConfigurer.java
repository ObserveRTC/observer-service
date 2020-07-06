package org.observertc.webrtc.observer.service.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micronaut.configuration.metrics.aggregator.MeterRegistryConfigurer;
import javax.inject.Singleton;

@Singleton
public class WebRTCStatsMeterRegistryConfigurer implements MeterRegistryConfigurer {


	@Override
	public void configure(MeterRegistry meterRegistry) {
		meterRegistry.config().commonTags("tag1", "tag2");
	}

	@Override
	public boolean supports(MeterRegistry meterRegistry) {
		return meterRegistry instanceof SimpleMeterRegistry;
	}
}
