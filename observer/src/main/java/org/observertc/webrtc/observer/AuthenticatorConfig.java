package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("authenticator")
public class AuthenticatorConfig {

	public boolean enabled = false;

	public String URL = null;
	
}

