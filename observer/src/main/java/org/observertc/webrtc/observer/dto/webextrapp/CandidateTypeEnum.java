package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum CandidateTypeEnum {
	HOST, PRFLX, RELAY, SRFLX;

	@JsonValue
	public String toValue() {
		switch (this) {
			case HOST: return "host";
			case PRFLX: return "prflx";
			case RELAY: return "relay";
			case SRFLX: return "srflx";
		}
		return null;
	}

	@JsonCreator
	public static CandidateTypeEnum forValue(String value) throws IOException {
		if (value.equals("host")) return HOST;
		if (value.equals("prflx")) return PRFLX;
		if (value.equals("relay")) return RELAY;
		if (value.equals("srflx")) return SRFLX;
		throw new IOException("Cannot deserialize CandidateTypeEnum");
	}
}
