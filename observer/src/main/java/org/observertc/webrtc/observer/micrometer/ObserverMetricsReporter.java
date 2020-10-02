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
import java.time.Duration;
import javax.inject.Singleton;
import org.slf4j.Logger;

@Singleton
public class ObserverMetricsReporter {
	private static final String METRIC_PREFIX = "WebRTC-Observer";

	private final MeterRegistry meterRegistry;

	private enum Metrics {
		FAILED_JSON_PARSE,

		OBSERVER_SSRC_BUFFER_SIZE,
		CALL_IDENTIFIER_EXECUTION_TIME_IN_MS,
		CALL_CLEANER_EXECUTION_TIME_IN_MS,
	}

	public ObserverMetricsReporter(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void setBufferSize(int size) {
		final String metricName = this.getMetricName(Metrics.OBSERVER_SSRC_BUFFER_SIZE);
		meterRegistry
				.gauge(metricName, size);

	}
	
	public void reportFailedJson(Logger logger, String serviceUUID, String serviceName, String mediaUnitID, String message) {
		
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
