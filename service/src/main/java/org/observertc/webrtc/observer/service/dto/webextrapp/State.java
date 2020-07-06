package org.observertc.webrtc.observer.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum State {
	FAILED, FROZEN, IN_PROGRESS, SUCCEEDED, WAITING;

	@JsonValue
	public String toValue() {
		switch (this) {
			case FAILED: return "failed";
			case FROZEN: return "frozen";
			case IN_PROGRESS: return "in-progress";
			case SUCCEEDED: return "succeeded";
			case WAITING: return "waiting";
		}
		return null;
	}

	@JsonCreator
	public static State forValue(String value) throws IOException {
		if (value.equals("failed")) return FAILED;
		if (value.equals("frozen")) return FROZEN;
		if (value.equals("in-progress")) return IN_PROGRESS;
		if (value.equals("succeeded")) return SUCCEEDED;
		if (value.equals("waiting")) return WAITING;
		throw new IOException("Cannot deserialize State");
	}
}
