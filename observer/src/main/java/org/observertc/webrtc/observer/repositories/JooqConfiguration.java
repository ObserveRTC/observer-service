package org.observertc.webrtc.observer.repositories;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("jooq")
public class JooqConfiguration {
	String dialect;
}
