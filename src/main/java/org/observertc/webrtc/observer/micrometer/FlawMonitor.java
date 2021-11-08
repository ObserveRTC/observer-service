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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Objects;

public class FlawMonitor {
	private static final Logger logger = LoggerFactory.getLogger(FlawMonitor.class);
	private final MeterRegistry meterRegistry;
	private String name;
	private String tag;
	private String value;
	private Logger defaultLogger;
	private Level defaultLogLevel;

	public FlawMonitor(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;

	}

	FlawMonitor withTag(String tag) {
		return this.withTag(tag, null);
	}

	FlawMonitor withTag(String tag, String value) {
		this.tag = tag;
		this.value = value;
		return this;
	}

	FlawMonitor withName(String name) {
		this.name = name;
		return this;
	}

	public FlawMonitor withDefaultLogger(Logger logger) {
		this.defaultLogger = logger;
		return this;
	}

	public FlawMonitor withDefaultLogLevel(Level level) {
		this.defaultLogLevel = level;
		return this;
	}

	public LogEntry makeLogEntry() {
		String value = this.value;
		if (Objects.isNull(value)) {
			value = "defaultTag";
		}
		return this.makeLogEntryFor(value);
	}

	public LogEntry makeLogEntryFor(String value) {
		LogEntry result = new LogEntry();
		if (Objects.nonNull(this.tag)) {
			result.withTag(this.tag, value);
		}
		if (Objects.isNull(this.name)) {
			logger.warn("No name provided for a flawmonitor, cannot provide metrics");
			return result;
		}
		Runnable report = () -> this.meterRegistry.counter(this.name, this.tag, value).increment();
		result.withPostAction(report);
		if (Objects.nonNull(this.defaultLogger)) {
			result.withLogger(this.defaultLogger);
		} else {
			result.withLogger(logger);
		}
		if (Objects.nonNull(this.defaultLogLevel)) {
			result.withLogLevel(this.defaultLogLevel);
		} else {
			result.withLogLevel(Level.WARN);
		}
		return result;
	}
}
