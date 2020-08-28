package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum NetworkType {
	BLUETOOTH, CELLULAR, ETHERNET, UNKNOWN, VPN, WIFI, WIMAX;

	@JsonValue
	public String toValue() {
		switch (this) {
			case BLUETOOTH:
				return "bluetooth";
			case CELLULAR:
				return "cellular";
			case ETHERNET:
				return "ethernet";
			case UNKNOWN:
				return "unknown";
			case VPN:
				return "vpn";
			case WIFI:
				return "wifi";
			case WIMAX:
				return "wimax";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(NetworkType.class);

	@JsonCreator
	public static NetworkType forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("bluetooth")) return BLUETOOTH;
		if (name.equals("cellular")) return CELLULAR;
		if (name.equals("ethernet")) return ETHERNET;
		if (name.equals("unknown")) return UNKNOWN;
		if (name.equals("vpn")) return VPN;
		if (name.equals("wifi")) return WIFI;
		if (name.equals("wimax")) return WIMAX;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
