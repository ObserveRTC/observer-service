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

package org.observertc.webrtc.observer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LogEntry {

	private final Consumer<String> logger;
	private final Map<String, String> tags = new HashMap<>();

	public LogEntry(Consumer<String> logger) {
		this.logger = logger;
	}

	public LogEntry(BiConsumer<String, Throwable> logger, Throwable t) {
		this.logger = str -> {
			logger.accept(str, t);
		};
	}

	public LogEntry addTag(Object tagName, Object tagValue) {
		return this;
	}

	public LogEntry addMessage(String message) {
		
		return this;
	}

	public void doLog() {

	}
}
