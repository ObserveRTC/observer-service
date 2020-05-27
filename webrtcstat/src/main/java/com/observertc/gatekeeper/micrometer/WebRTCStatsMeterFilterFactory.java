package com.observertc.gatekeeper.micrometer;

import io.micrometer.core.instrument.config.MeterFilter;
import io.micronaut.context.annotation.Bean;
import javax.inject.Singleton;

public class WebRTCStatsMeterFilterFactory {
	/**
	 * Exclude metrics starting with jvm.
	 *
	 * @return meter filter
	 */
	@Bean
	@Singleton
	MeterFilter jvmExclusionFilter() {
		return MeterFilter.denyNameStartsWith("jvm");
	}

}
