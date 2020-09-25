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

package org.observertc.webrtc.common;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sleeper implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Sleeper.class);

	public static Sleeper makeFromSystemEnv(String envName, ChronoUnit unit) {
		String initialWaitStr = System.getenv(envName);
		if (initialWaitStr == null) {
			return new Sleeper(() -> 0);
		}
		try {
			int initialWait = Integer.parseInt(initialWaitStr);
			Long initialWaitInMs = Duration.of(initialWait, unit).toMillis();
			return new Sleeper(() -> initialWaitInMs.intValue());
		} catch (Exception ex) {
			logger.error("Error happened parsing " + envName, ex);
			return new Sleeper(() -> 0);
		}

	}

	private final Supplier<Integer> timeInMsProvider;
	private final boolean log;

	public Sleeper(Supplier<Integer> timeInMsProvider) {
		this(timeInMsProvider, false);
	}

	public Sleeper(Supplier<Integer> timeInMsProvider, boolean log) {
		this.timeInMsProvider = timeInMsProvider;
		this.log = log;
	}

	@Override
	public void run() {
		if (timeInMsProvider == null) {
			throw new NullPointerException();
		}
		int initialWaitInMs = this.timeInMsProvider.get();
		if (this.log) {
			logger.info("The configured sleeping time is {}ms", initialWaitInMs);
		}

		if (initialWaitInMs < 1) {
			return;
		}
		try {
			Thread.sleep(initialWaitInMs);
		} catch (InterruptedException e) {
			logger.error("Error happened in waiting ", e);
		}
	}
}
