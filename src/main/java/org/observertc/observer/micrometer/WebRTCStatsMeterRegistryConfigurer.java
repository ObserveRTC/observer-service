/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.observer.micrometer;

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

	public Class<WebRTCStatsMeterRegistryConfigurer> getType() {
		return WebRTCStatsMeterRegistryConfigurer.class;
	}
}
