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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;
import org.slf4j.Logger;

@Singleton
public class ObserverMetricsReporter {
	private static final String METRIC_PREFIX = "webrtc.observer";

	private static final String WEBSOCKETS_METRIC_CATEGORY = "websockets";
	private static final String MEDIASTREAMS_UPDATES_CATEGORY = "mediaStreamUpdater";
	private final MeterRegistry meterRegistry;
	private final Map<String, Integer> partialValues = new ConcurrentHashMap<>();
	private final AtomicInteger activeMediaStreams = new AtomicInteger(0);

	public ObserverMetricsReporter(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public CountedLogMonitor makeCountedLogMonitor(Logger logger) {
		CountedLogMonitor result = new CountedLogMonitor(logger, this.meterRegistry);
		return result;
	}

	public void incrementOpenedWebsocketConnectionsCounter(String serviceUUID, String mediaUnitID) {
		String metricName = this.getMetricName(WEBSOCKETS_METRIC_CATEGORY, "opened");
		this.meterRegistry.counter(metricName, "serviceUUID", serviceUUID.toString(), "mediaUnitId", mediaUnitID).increment();
	}

	public void incrementClosedWebsocketConnectionsCounter(String serviceUUID, String mediaUnitID) {
		String metricName = this.getMetricName(WEBSOCKETS_METRIC_CATEGORY, "closed");
		this.meterRegistry.counter(metricName, "serviceUUID", serviceUUID.toString(), "mediaUnitId", mediaUnitID).increment();
	}

	public void gaugeActiveMediaStreams(String mediaStreamUpdater, Integer activeStreams) { final String metricName = this.getMetricName(MEDIASTREAMS_UPDATES_CATEGORY);
		Integer total = this.activeMediaStreams.get();
		Integer previous = this.partialValues.getOrDefault(mediaStreamUpdater, 0);
		total = total - previous + activeStreams;
		this.meterRegistry.gauge(metricName, total);
		this.activeMediaStreams.set(total);
		this.partialValues.put(mediaStreamUpdater, activeStreams);
	}


	private String getMetricName(String... names) {
		return String.format("%s.%s", METRIC_PREFIX, String.join(".", names));
	}
}
