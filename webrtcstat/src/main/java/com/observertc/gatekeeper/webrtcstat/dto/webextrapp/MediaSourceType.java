package com.observertc.gatekeeper.webrtcstat.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum MediaSourceType {
	MEDIA_SOURCE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case MEDIA_SOURCE: return "media-source";
		}
		return null;
	}

	@JsonCreator
	public static MediaSourceType forValue(String value) throws IOException {
		if (value.equals("media-source")) return MEDIA_SOURCE;
		throw new IOException("Cannot deserialize MediaSourceType");
	}
}
