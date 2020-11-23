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

package org.observertc.webrtc.observer.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import javax.inject.Singleton;
import org.slf4j.Logger;

@Singleton
public class MetricsReporter {
	private static final String METRIC_PREFIX = "webrtc.observer";
	private static final String WEBSOCKETS_METRIC_CATEGORY = "websockets";
	private static final String PROCESSED_MEDIA_UPDATES = "mediaUpdates";
	private final MeterRegistry meterRegistry;

	public MetricsReporter(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public CountedLogMonitor makeCountedLogMonitor(Logger logger) {
		CountedLogMonitor result = new CountedLogMonitor(logger, this.meterRegistry);
		return result;
	}

	public void incrementOpenedWebsocketConnectionsCounter() {
		String metricName = this.getMetricName(WEBSOCKETS_METRIC_CATEGORY, "opened");
		this.meterRegistry.counter(metricName).increment();
	}

	public void incrementClosedWebsocketConnectionsCounter() {
		String metricName = this.getMetricName(WEBSOCKETS_METRIC_CATEGORY, "closed");
		this.meterRegistry.counter(metricName).increment();
	}

	private String getMetricName(String... names) {
		return String.format("%s.%s", METRIC_PREFIX, String.join(".", names));
	}

	public void incrementProcessedMediaUpdates(int toProcess) {
		String metricName = this.getMetricName(PROCESSED_MEDIA_UPDATES);
		this.meterRegistry.counter(metricName).increment(toProcess);
	}
}
