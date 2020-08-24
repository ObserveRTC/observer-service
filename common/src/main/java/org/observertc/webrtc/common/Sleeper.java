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

	private Sleeper(Supplier<Integer> timeInMsProvider) {
		this.timeInMsProvider = timeInMsProvider;
	}

	@Override
	public void run() {
		if (timeInMsProvider == null) {
			throw new NullPointerException();
		}
		int initialWaitInMs = this.timeInMsProvider.get();
		logger.info("The configured initial waiting is {}ms", initialWaitInMs);
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
