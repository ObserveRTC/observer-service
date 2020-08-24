package org.observertc.webrtc.reporter;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import java.time.temporal.ChronoUnit;
import org.observertc.webrtc.common.Sleeper;

public class Application {
	private static final String INITIAL_WAIT_IN_MS = "REPORTER_INITIAL_WAITING_TIME_IN_S";

	public static ApplicationContext context;

	public static void main(String[] args) {
		Sleeper.makeFromSystemEnv(INITIAL_WAIT_IN_MS, ChronoUnit.SECONDS).run();
		context = Micronaut.run(Application.class);
	}
}
