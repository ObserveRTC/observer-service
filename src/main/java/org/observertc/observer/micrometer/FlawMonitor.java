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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Objects;

public class FlawMonitor {
	private static final Logger logger = LoggerFactory.getLogger(FlawMonitor.class);
	private final ExposedMetrics exposedMetrics;
	private String moduleId;
	private Logger defaultLogger;
	private Level defaultLogLevel;

	public FlawMonitor(ExposedMetrics exposedMetrics) {
		this.exposedMetrics = exposedMetrics;

	}

	FlawMonitor withModuleId(String value) {
		this.moduleId = value;
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
		LogEntry result = new LogEntry();
		if (Objects.isNull(this.moduleId)) {
			logger.warn("No name provided for a flawmonitor, cannot provide metrics");
			return result;
		}
		Runnable report = () -> {
			try {
				this.exposedMetrics.incrementModuleFlaws(this.moduleId);
			} catch (Exception ex) {
				logger.error("Error while reporting error...", ex);
			}

		};
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
