package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum QualityLimitationReason {
	BANDWIDTH, CPU, NONE, OTHER;

	@JsonValue
	public String toValue() {
		switch (this) {
			case BANDWIDTH: return "bandwidth";
			case CPU: return "cpu";
			case NONE: return "none";
			case OTHER: return "other";
		}
		return null;
	}

	@JsonCreator
	public static QualityLimitationReason forValue(String value) throws IOException {
		if (value.equals("bandwidth")) return BANDWIDTH;
		if (value.equals("cpu")) return CPU;
		if (value.equals("none")) return NONE;
		if (value.equals("other")) return OTHER;
		throw new IOException("Cannot deserialize QualityLimitationReason");
	}
}
