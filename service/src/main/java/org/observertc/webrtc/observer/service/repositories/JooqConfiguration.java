package org.observertc.webrtc.observer.service.repositories;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("jooq")
public class JooqConfiguration {
	String dialect;
}
