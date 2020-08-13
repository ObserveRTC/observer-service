package org.observertc.webrtc.service;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("observer")
public class ObserverConfig {

	public AuthenticationConfig authentication;

	public String timeZoneID;

	@ConfigurationProperties("authentication")
	public static class AuthenticationConfig {
		public String hashAlgorithm;
		public int stretching;
		public int saltSize;
	}


}

