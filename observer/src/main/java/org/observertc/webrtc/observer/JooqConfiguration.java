package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("jooq")
public class JooqConfiguration {
	public String dialect;
}
