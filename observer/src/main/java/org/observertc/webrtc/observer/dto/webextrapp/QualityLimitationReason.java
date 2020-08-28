package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum QualityLimitationReason {
	BANDWIDTH, CPU, NONE, OTHER;

	@JsonValue
	public String toValue() {
		switch (this) {
			case BANDWIDTH:
				return "bandwidth";
			case CPU:
				return "cpu";
			case NONE:
				return "none";
			case OTHER:
				return "other";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(QualityLimitationReason.class);

	@JsonCreator
	public static QualityLimitationReason forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("bandwidth")) return BANDWIDTH;
		if (name.equals("cpu")) return CPU;
		if (name.equals("none")) return NONE;
		if (name.equals("other")) return OTHER;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
