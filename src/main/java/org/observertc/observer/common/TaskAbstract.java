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

package org.observertc.observer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TaskAbstract<T> implements AutoCloseable, Task<T> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskAbstract.class);
	private volatile boolean executed = false;
	private Supplier<String> errorMessageSupplier = () -> "";
	private Logger onLogger = DEFAULT_LOGGER;
	private Logger defaultLogger = DEFAULT_LOGGER;
	private Consumer<Throwable> exceptionHandler = null;
	private boolean rethrowException = false;
	private boolean succeeded = false;
	private int maxRetry = 1;
	private T result = null;
	private Supplier<AutoCloseable> lockProvider = () -> { return () -> {};};
	private Consumer<Stats> statsConsumer = input -> {};
	private Runnable finalAction = () -> {};
	private String name = null;

	public static class Stats {
		public Instant started = Instant.now();
		public Instant ended;
		public int run = 0;
		public boolean succeeded;
		public String taskName;
	}

	public TaskAbstract<T> execute() {
		this.validate();
		Stats stats = new Stats();
		stats.taskName = this.getClass().getSimpleName();
		Throwable thrown = null;
		int run = 0;
		try {
			for (run = 0; run < this.maxRetry; ++run) {
				try (var lock = this.lockProvider.get()){
					thrown = null;
					T result = this.perform();
					this.succeeded = true;
					this.result = result;
					break;
				} catch (Throwable ex) {
					thrown = ex;
					if (exceptionHandler != null) {
						exceptionHandler.accept(ex);
					}
					if (run < this.maxRetry - 1) {
						this.onLogger.warn("Unexpected error occurred Retry now.", ex);
					}
				}
			}
			if (Objects.nonNull(thrown)) {
				String exceptionMessage = this.getErrorMessage();
				this.onLogger.error(exceptionMessage, thrown);
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
			stats.ended = Instant.now();
			stats.run = run;
			stats.succeeded = this.succeeded;
			try {
				if (Objects.nonNull(this.statsConsumer)) {
					this.statsConsumer.accept(stats);
				}
			} catch (Exception ex) {
				this.onLogger.error("Error while forwarding task stats", ex);
			}
			try {
				finalAction.run();
			} catch (Exception ex) {
				this.onLogger.warn("Error while executing final action", ex);
			}
		}
		return this;
	}

	public String getName() {
		if (this.name != null) {
			return this.name;
		}
		return this.getClass().getSimpleName();
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

	protected TaskAbstract<T> setName(String taskName) {
		this.name = taskName;
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

	public TaskAbstract<T> withExceptionHandler(Consumer<Throwable> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
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

	public TaskAbstract<T> withFinalAction(Runnable action) {
		this.finalAction = action;
		return this;
	}

	public TaskAbstract<T> withStatsConsumer(Consumer<Stats> statsConsumer) {
		this.statsConsumer = statsConsumer;
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
