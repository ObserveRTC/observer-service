package com.observertc.gatekeeper.webrtcstat.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum Protocol {
	TCP, UDP;

	@JsonValue
	public String toValue() {
		switch (this) {
			case TCP: return "tcp";
			case UDP: return "udp";
		}
		return null;
	}

	@JsonCreator
	public static Protocol forValue(String value) throws IOException {
		if (value.equals("tcp")) return TCP;
		if (value.equals("udp")) return UDP;
		throw new IOException("Cannot deserialize Protocol");
	}
}
