package org.observertc.webrtc.observer;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.runtime.Micronaut;
import java.time.temporal.ChronoUnit;
import org.observertc.webrtc.common.Sleeper;


@TypeHint(
		typeNames = {
				"io.micronaut.caffeine.cache.SSAW",
				"io.micronaut.caffeine.cache.PSAW"
		},
		accessType = {
				TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS,
				TypeHint.AccessType.ALL_PUBLIC_METHODS,
				TypeHint.AccessType.ALL_DECLARED_FIELDS
		}
)
public class Application {
	private static final String INITIAL_WAIT_IN_MS = "OBSERVER_INITIAL_WAITING_TIME_IN_S";

	public static ApplicationContext context;

	public static void main(String[] args) {
		Sleeper.makeFromSystemEnv(INITIAL_WAIT_IN_MS, ChronoUnit.SECONDS).run();
		context = Micronaut.
				run(Application.class);

	}
}