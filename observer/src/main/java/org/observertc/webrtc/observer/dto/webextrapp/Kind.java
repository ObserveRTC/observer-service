package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Kind {
	AUDIO, VIDEO;

	@JsonValue
	public String toValue() {
		switch (this) {
			case AUDIO:
				return "audio";
			case VIDEO:
				return "video";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(Kind.class);

	@JsonCreator
	public static Kind forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("audio")) return AUDIO;
		if (name.equals("video")) return VIDEO;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
