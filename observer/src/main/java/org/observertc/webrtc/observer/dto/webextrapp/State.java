package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum State {
	FAILED, FROZEN, IN_PROGRESS, SUCCEEDED, WAITING, UNKNOWN;


	@JsonValue
	public String toValue() {
		switch (this) {
			case FAILED:
				return "failed";
			case FROZEN:
				return "frozen";
			case IN_PROGRESS:
				return "in-progress";
			case SUCCEEDED:
				return "succeeded";
			case WAITING:
				return "waiting";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(State.class);

	@JsonCreator
	public static State forValue(String value) throws IOException {
		if (value == null) {
			return UNKNOWN;
		}
		String name = value.toLowerCase();
		if (name.equals("failed")) return FAILED;
		if (name.equals("frozen")) return FROZEN;
		if (name.equals("in-progress")) return IN_PROGRESS;
		if (name.equals("succeeded")) return SUCCEEDED;
		if (name.equals("waiting")) return WAITING;
		logger.warn("Cannot deseerialize state for name {}", name);
		return UNKNOWN;
	}
}
