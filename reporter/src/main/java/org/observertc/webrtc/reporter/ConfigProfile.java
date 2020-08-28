package org.observertc.webrtc.reporter;

public enum ConfigProfile {
	BIGQUERY;

	public static ConfigProfile fromProfile(String profile) {
		return ConfigProfile.valueOf(profile.toUpperCase());
	}
}
