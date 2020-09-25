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

package org.observertc.webrtc.common.jobs;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTask implements Task {
	private static final Logger logger = LoggerFactory.getLogger(AbstractTask.class);
	private final Map<String, Object> results;
	private String name;
	private String description;
	private volatile boolean run = false;
	private final int retryNrOnExceptions;


	public AbstractTask(String name, int retryNrOnExceptions) {
		this.results = new HashMap<>();
		if (name == null) {
			this.name = this.getClass().getName();
		} else {
			this.name = name;
		}

		this.retryNrOnExceptions = retryNrOnExceptions;
	}

	public AbstractTask(int retryNrOnExceptions) {
		this(null, retryNrOnExceptions);
	}

	public AbstractTask(String name) {
		this(name, 3);
	}

	public String getName() {
		return this.name;
	}

	public Map<String, Object> getResults() {
		return this.results;
	}

	public <T> T getResult(String key) {
		return (T) this.results.get(key);
	}

	protected void setResult(String key, Object value) {
		this.results.put(key, value);
	}

	@Override
	public String getDescription() {
		return null;
	}

	public AbstractTask withDescription(String description) {
		this.description = description;
		return this;
	}

	public AbstractTask withName(String name) {
		this.name = name;
		return this;
	}


	public void execute(Map<String, Map<String, Object>> results) {
		if (this.run) {
			logger.warn("Something wrong. A task should not be tried to execute twice.");
			return;
		}
		this.run = true;
		Exception exception = null;
		for (int tried = 0; tried < this.retryNrOnExceptions; ++tried) {
			try {
				this.onExecution(results);
				return;
			} catch (Exception ex) {
				exception = ex;
				this.onExceptionAtRetry(ex, tried);
				continue;
			}
		}
		this.onExceptionAtFailedRetries(exception);
	}

	protected void reset() {
		this.run = false;
	}

	protected abstract void onExecution(Map<String, Map<String, Object>> results);

	protected void onExceptionAtRetry(Exception exception, int tried) {
		logger.warn("Exception occured during execution {}, retry: {}", exception, tried);
	}

	protected void onExceptionAtFailedRetries(Exception exception) {
		throw new RuntimeException(exception);
	}

}
