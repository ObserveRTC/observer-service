package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Protocol {
	TCP, UDP, UNKNOWN;

	@JsonValue
	public String toValue() {
		switch (this) {
			case TCP:
				return "tcp";
			case UDP:
				return "udp";
		}
		return null;
	}


	private static final Logger logger = LoggerFactory.getLogger(Protocol.class);

	@JsonCreator
	public static Protocol forValue(String value) throws IOException {
		if (value == null) {
			logger.warn("value is null for Protocol");
			return UNKNOWN;
		}
		String name = value.toLowerCase();
		if (name.equals("tcp")) return TCP;
		if (name.equals("udp")) return UDP;
		logger.warn("Cannot deseerialize state for name {}", name);
		return UNKNOWN;
	}
}
