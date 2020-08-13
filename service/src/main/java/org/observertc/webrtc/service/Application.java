package org.observertc.webrtc.service;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.runtime.Micronaut;


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

	public static ApplicationContext context;

	public static void main(String[] args) {
		context = Micronaut.
				run(Application.class);

	}
}