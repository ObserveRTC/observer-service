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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.event.Level;

public class CountedLogMonitor {

	private final MeterRegistry meterRegistry;
	private final Logger logger;
	private final List<String> requiredTags = new ArrayList<>();
	private final Map<String, String> commonTags = new HashMap<>();
	private String metricName = "UNDEFINED";

	public CountedLogMonitor(Logger logger, MeterRegistry meterRegistry) {
		this.logger = logger;
		this.meterRegistry = meterRegistry;
	}

	public CountedLogMonitor withDefaultMetricName(String metricName) {
		this.metricName = metricName;
		return this;
	}

	public CountedLogMonitor withRequiredTags(Object... tagNames) {
		this.requiredTags.clear();
		for (int i = 0; i < tagNames.length; ++i) {
			Object tagName = tagNames[i];
			if (tagName == null) {
				throw new NullPointerException();
			}
			this.requiredTags.add(tagName.toString());
		}
		return this;
	}

	public CountedLogMonitor withCommonTags(Object... tags) {
		this.commonTags.clear();
		if (tags.length % 2 == 1) {
			throw new IllegalStateException("common tags must be paired");
		}
		for (int i = 0; i < tags.length; i += 2) {
			Object tagName = tags[i];
			Object tagValue = tags[i + 1];
			if (tagName == null || tagValue == null) {
				throw new NullPointerException();
			}
			this.commonTags.put(tagName.toString(), tagValue.toString());
		}
		return this;
	}

	public LogEntry makeEntry(Object... tagValues) {
		String metricName = this.metricName;
		return this.makeEntry(metricName, tagValues);
	}

	public LogEntry makeEntry(String metricName, Object... tagValues) {
		LogEntry result = new LogEntry(metricName);
		this.commonTags.entrySet().stream().forEach(entry -> {
			result.withTag(entry.getKey(), entry.getValue());
		});
		if (tagValues != null) {
			for (int i = 0; i < tagValues.length; ++i) {
				String tagName = this.requiredTags.get(i);
				Object tagValue = tagValues[i];
				result.withTag(tagName, tagValue.toString());
			}
		} else {
			if (0 < this.requiredTags.size()) {
				this.logger.error("Missing required tag values");
			}
		}
		return result;
	}

	public class LogEntry {
		private final List<String> tags = new LinkedList<>();
		private String metricName = CountedLogMonitor.this.metricName;
		private Level level = Level.DEBUG;
		private String message;
		private Throwable exception;

		public LogEntry(String metricName) {
			this.metricName = metricName;
		}

		public LogEntry withTag(String tagName, Object tagValue) {
			this.tags.add(tagName);
			this.tags.add(tagValue.toString());
			return this;
		}

		public LogEntry withCategory(String category) {
			return this.withTag("category", category);
		}

		public LogEntry withMessage(String message) {
			this.message = message;
			return this;
		}

		public LogEntry withLogLevel(Level level) {
			this.level = level;
			return this;
		}

		public LogEntry withException(Throwable exception) {
			this.exception = exception;
			return this;
		}

		public void log() {
			String[] tags = (String[]) this.tags.toArray();
			String metricName = String.join(".", this.getNamePrefix(), this.metricName);
			try {
				meterRegistry.counter(metricName, tags).increment();
			} catch (Exception ex) {
				logger.error("{} has an error", this.getClass().getName());
			}
			this.doLog();
		}

		private void doLog() {
			switch (this.level) {
				case ERROR:
					if (this.exception != null) {
						logger.error(this.message, this.exception);
					} else {
						logger.error(this.message);
					}
					break;
				case TRACE:
					if (this.exception != null) {
						logger.trace(this.message, this.exception);
					} else {
						logger.trace(this.message);
					}
					break;
				case INFO:
					if (this.exception != null) {
						logger.info(this.message, exception);
					} else {
						logger.info(this.message);
					}
					break;
				case WARN:
					if (this.exception != null) {
						logger.warn(this.message, this.exception);
					} else {
						logger.warn(this.message);
					}
					break;
				case DEBUG:
					if (this.exception != null) {
						logger.debug(this.message, this.exception);
					} else {
						logger.debug(this.message);
					}
					break;
				default:
			}
		}

		private String getNamePrefix() {
			switch (this.level) {
				case ERROR:
					return "errors";
				case TRACE:
					return "traces";
				case INFO:
					return "infos";
				case WARN:
					return "warnings";
				case DEBUG:
					return "debugs";
				default:
					return "undefined";
			}
		}
	}
}
