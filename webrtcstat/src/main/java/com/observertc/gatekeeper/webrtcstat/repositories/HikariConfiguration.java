package com.observertc.gatekeeper.webrtcstat.repositories;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("hikari")
public class HikariConfiguration {

	String poolName;

	int maxPoolSize;

	int minIdle;

	String username;

	String password;

	String jdbcURL;

	String jdbcDriver;

	String dialect;
}
