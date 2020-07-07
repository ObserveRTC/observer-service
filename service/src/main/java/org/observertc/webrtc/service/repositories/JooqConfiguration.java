package org.observertc.webrtc.service.repositories;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("jooq")
public class JooqConfiguration {
	String dialect;
}
