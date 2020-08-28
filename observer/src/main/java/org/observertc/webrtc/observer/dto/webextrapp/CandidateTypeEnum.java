package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CandidateTypeEnum {
	HOST, PRFLX, RELAY, SRFLX;

	@JsonValue
	public String toValue() {
		switch (this) {
			case HOST:
				return "host";
			case PRFLX:
				return "prflx";
			case RELAY:
				return "relay";
			case SRFLX:
				return "srflx";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(CandidateTypeEnum.class);

	@JsonCreator
	public static CandidateTypeEnum forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("host")) return HOST;
		if (name.equals("prflx")) return PRFLX;
		if (name.equals("relay")) return RELAY;
		if (name.equals("srflx")) return SRFLX;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
