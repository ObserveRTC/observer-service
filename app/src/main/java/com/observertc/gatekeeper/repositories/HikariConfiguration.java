package com.observertc.gatekeeper.repositories;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("hikari")
public class HikariConfiguration {
	//	@Value("${micronaut.application.hikari.poolName}")
	String poolName;

	//	@Value("${micronaut.application.hikari.maxPoolSize}")
	int maxPoolSize;

	//	@Value("${micronaut.application.hikari.minIdle}")
	int minIdle;

	//	@Value("${micronaut.application.hikari.username}")
	String username;

	//	@Value("${micronaut.application.hikari.password}")
	String password;

	//	@Value("${micronaut.application.hikari.jdbc}")
	String jdbc;
}
