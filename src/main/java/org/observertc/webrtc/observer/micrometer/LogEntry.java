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

import io.micronaut.context.annotation.Prototype;
import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

@Prototype
public class LogEntry {
	private final List<String> tags = new LinkedList<>();
	private Level level = Level.DEBUG;
	private String message;
	private Throwable exception;
	private Logger logger;
	private Marker marker;
	private Runnable postAction;

	public LogEntry() {

	}

	public LogEntry withLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	public LogEntry withMarker(Marker marker) {
		this.marker = marker;
		return this;
	}

	public LogEntry withTag(String tagName, Object tagValue) {
		this.tags.add(tagName);
		this.tags.add(tagValue.toString());
		return this;
	}

	public LogEntry withMessage(@NotNull String message) {
		this.message = message;
		return this;
	}

	public LogEntry withMessage(@NotNull String message, Object... args) {
		this.message = MessageFormatter.arrayFormat(message, args).getMessage();
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

	LogEntry withPostAction(Runnable action) {
		this.postAction = action;
		return this;
	}

	public void complete() {
		StringBuilder tags = new StringBuilder();
		String message = this.message;
		for (int i = 0, c = this.tags.size(); i < c; i += 2) {
			String tag = this.tags.get(i);
			String value = this.tags.get(i + 1);
			tags.append(tag);
			tags.append(": ");
			tags.append(value);
		}
		if (0 < tags.length()) {
			message += "\n\nTags\n" + tags.toString();
		}
		this.doLog(message);
		if (this.postAction != null) {
			this.postAction.run();
		}
	}

	private void doLog(String message) {
		switch (this.level) {
			case ERROR:
				if (this.marker != null) {
					if (this.exception != null) {
						logger.error(this.marker, message, this.exception);
					} else {
						logger.error(this.marker, message);
					}
				} else {
					if (this.exception != null) {
						logger.error(message, this.exception);
					} else {
						logger.error(message);
					}
				}
				break;
			case TRACE:
				if (this.marker != null) {
					if (this.exception != null) {
						logger.trace(this.marker, message, this.exception);
					} else {
						logger.trace(this.marker, message);
					}
				} else {
					if (this.exception != null) {
						logger.trace(this.marker, message, this.exception);
					} else {
						logger.trace(this.marker, message);
					}
				}

				break;
			case INFO:
				if (this.marker != null) {
					if (this.exception != null) {
						logger.info(this.marker, message, exception);
					} else {
						logger.info(this.marker, message);
					}
				} else {
					if (this.exception != null) {
						logger.info(this.marker, message, exception);
					} else {
						logger.info(this.marker, message);
					}
				}

				break;
			case WARN:
				if (this.marker != null) {
					if (this.exception != null) {
						logger.warn(this.marker, message, this.exception);
					} else {
						logger.warn(this.marker, message);
					}
				} else {
					if (this.exception != null) {
						logger.warn(this.marker, message, this.exception);
					} else {
						logger.warn(this.marker, message);
					}
				}

				break;
			case DEBUG:
				if (this.marker != null) {
					if (this.exception != null) {
						logger.debug(this.marker, message, this.exception);
					} else {
						logger.debug(this.marker, message);
					}
				} else {
					if (this.exception != null) {
						logger.debug(this.marker, message, this.exception);
					} else {
						logger.debug(this.marker, message);
					}
				}

				break;
			default:
		}
	}

	public String toString() {
		return ObjectToString.toString(this);
	}
}
