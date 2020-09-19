package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("hikari")
public class HikariConfiguration {

	public String poolName;

	public int maxPoolSize;

	public int minIdle;

	public String username;

	public String password;

	public String jdbcURL;

	public String jdbcDriver;

}
