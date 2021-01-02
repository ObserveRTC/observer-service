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

package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class SessionMonitor {

	private final MeterRegistry meterRegistry;
	private final Counter added;
	private final Counter removed;

	public SessionMonitor(MeterRegistry meterRegistry, String name, String metricsAddedSuffix, String metricsRemovedSuffix) {
		this.meterRegistry = meterRegistry;
		this.added = this.meterRegistry.counter(String.format("%s-%s", name, metricsAddedSuffix));
		this.removed = this.meterRegistry.counter(String.format("%s-%s", name, metricsRemovedSuffix));
	}

	public SessionMonitor(MeterRegistry meterRegistry, String name) {
		this(meterRegistry, name, "added", "removed");
	}

	public void added(String sessionId) {
		this.added.increment();
	}

	public void removed(String sessionId) {
		this.removed.increment();
	}

}
