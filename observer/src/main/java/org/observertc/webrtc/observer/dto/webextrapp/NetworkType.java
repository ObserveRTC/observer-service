package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum NetworkType {
	BLUETOOTH, CELLULAR, ETHERNET, UNKNOWN, VPN, WIFI, WIMAX;

	@JsonValue
	public String toValue() {
		switch (this) {
			case BLUETOOTH: return "bluetooth";
			case CELLULAR: return "cellular";
			case ETHERNET: return "ethernet";
			case UNKNOWN: return "unknown";
			case VPN: return "vpn";
			case WIFI: return "wifi";
			case WIMAX: return "wimax";
		}
		return null;
	}

	@JsonCreator
	public static NetworkType forValue(String value) throws IOException {
		if (value.equals("bluetooth")) return BLUETOOTH;
		if (value.equals("cellular")) return CELLULAR;
		if (value.equals("ethernet")) return ETHERNET;
		if (value.equals("unknown")) return UNKNOWN;
		if (value.equals("vpn")) return VPN;
		if (value.equals("wifi")) return WIFI;
		if (value.equals("wimax")) return WIMAX;
		throw new IOException("Cannot deserialize NetworkType");
	}
}
