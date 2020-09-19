package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("observer")
public class ObserverConfig {

	public AuthenticationConfig authentication;

	@ConfigurationProperties("authentication")
	public static class AuthenticationConfig {
		public String hashAlgorithm;
		public int stretching;
		public int saltSize;
	}


}

