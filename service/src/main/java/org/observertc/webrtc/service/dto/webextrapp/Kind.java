package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum Kind {
	AUDIO, VIDEO;

	@JsonValue
	public String toValue() {
		switch (this) {
			case AUDIO: return "audio";
			case VIDEO: return "video";
		}
		return null;
	}

	@JsonCreator
	public static Kind forValue(String value) throws IOException {
		if (value.equals("audio")) return AUDIO;
		if (value.equals("video")) return VIDEO;
		throw new IOException("Cannot deserialize Kind");
	}
}
