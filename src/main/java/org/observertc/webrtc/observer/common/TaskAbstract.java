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

package org.observertc.webrtc.observer.common;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.observertc.webrtc.observer.micrometer.FlawMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class TaskAbstract<T> implements AutoCloseable, Task<T> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskAbstract.class);
	private volatile boolean executed = false;
	private FlawMonitor flawMonitor;
	private Supplier<String> errorMessageSupplier = () -> "";
	private Logger onLogger = DEFAULT_LOGGER;
	private Logger defaultLogger = DEFAULT_LOGGER;
	private Level onErrorLogLevel = Level.ERROR;
	private MeterRegistry meterRegistry = null;
	private boolean rethrowException = false;
	private boolean succeeded = false;
	private int maxRetry = 1;
	private T result = null;
	private Supplier<AutoCloseable> lockProvider = () -> { return () -> {};};

	public TaskAbstract<T> execute() {
		this.validate();
		Throwable thrown = null;
		try {
			for (int run = 0; run < this.maxRetry; ++run) {
				try (var lock = this.lockProvider.get()){
					thrown = null;
					Instant started = Instant.now();
					T result = this.perform();
					if (Objects.nonNull(this.meterRegistry)) {
						this.meterRegistry
								.timer("observertc_tasks_execution_time_in_ms", List.of(Tag.of("task", this.getClass().getSimpleName())))
								.record(Duration.between(started, Instant.now()));
					}
					this.succeeded = true;
					this.result = result;
					break;
				} catch (Throwable ex) {
					thrown = ex;
					if (run < this.maxRetry - 1) {
						this.onLogger.warn("Unexpected error occurred Retry now.", ex);
					}
				}
			}
			if (Objects.nonNull(thrown)) {
				String exceptionMessage = this.getErrorMessage();
				if (Objects.nonNull(this.flawMonitor)) {
					this.flawMonitor
							.makeLogEntry()
							.withException(thrown)
							.withLogLevel(this.onErrorLogLevel)
							.withMessage(exceptionMessage)
							.withLogger(this.onLogger)
							.complete();
				} else {
					this.onLogger.error(exceptionMessage, thrown);
				}
				try {
					this.rollback(thrown);
				} catch (Throwable t) {
					this.onLogger.error("Unexpected exception occurred during rollback", t);
				}
				if (this.rethrowException) {
					throw new RuntimeException(thrown);
				}
			}
		} finally {
			this.executed = true;
		}
		return this;
	}

	protected Logger getLogger() {
		if (Objects.nonNull(this.onLogger)) {
			return this.onLogger;
		}
		if (Objects.nonNull(this.defaultLogger)) {
			return this.defaultLogger;
		}
		return DEFAULT_LOGGER;
	}

	protected void setDefaultLogger(Logger logger) {
		this.defaultLogger = logger;
	}

	protected abstract T perform() throws Throwable;

	protected void rollback(Throwable t) {
		this.onLogger.info("No Rollback has been implemented to this task ({})", this.getClass().getSimpleName());
		// no rollback
	}

	public T getResult() {
		if (!this.succeeded) {
			throw new IllegalStateException("Task has not succeeded, result is not ready");
		}
		return this.result;
	}

	TaskAbstract<T> withLockProvider(Supplier<AutoCloseable> value) {
		this.lockProvider = value;
		return this;
	}

	public T getResultOrDefault(T defaultValue) {
		if (!this.executed) {
			throw new IllegalStateException("The task has not been executed");
		}
		if (!this.succeeded) {
			return defaultValue;
		}
		return this.result;
	}

	public T getResultOrDefaultIfNull(T defaultValue) {
		if (!this.executed) {
			throw new IllegalStateException("The task has not been executed");
		}
		if (!this.succeeded) {
			return defaultValue;
		}
		if (Objects.isNull(this.result) ) {
			return defaultValue;
		}
		return this.result;
	}



	public TaskAbstract<T> withMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
		return this;
	}

	public TaskAbstract<T> withRethrowingExceptions(boolean value) {
		this.rethrowException = value;
		return this;
	}

	public TaskAbstract<T> withExceptionMessage(Supplier<String> messageSupplier) {
		this.errorMessageSupplier = messageSupplier;
		return this;
	}

	public TaskAbstract<T> withExceptionMessage(MessageFormatter messageFormatter) {
		this.errorMessageSupplier = messageFormatter::toString;
		return this;
	}


	public TaskAbstract<T> withLogger(Logger logger) {
		this.onLogger = logger;
		return this;
	}

	public TaskAbstract<T> withMeterRegistry(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		return this;
	}

	public TaskAbstract<T> withFlawMonitor(FlawMonitor flawMonitor) {
		this.flawMonitor = flawMonitor;
		return this;
	}

	public boolean succeeded() {
		if (!this.executed) {
			throw new IllegalStateException("The task has not been executed");
		}
		return succeeded;
	}

	@Override
	public void close() {

	}

	protected void validate() {
		if (this.executed) {
			throw new IllegalStateException("The task is already executed");
		}
	}

	private String getErrorMessage() {
		if (Objects.isNull(this.errorMessageSupplier)) {
			return "Unexpected error occurred in " + this.getClass().getSimpleName();
		}
		try {
			return this.errorMessageSupplier.get();
		} catch (Throwable ex) {
			this.onLogger.error("Can you check your error message supplier? It just caused another error", ex);
			return "Unexpected error occurred in " + this.getClass().getSimpleName();
		}

	}

}
