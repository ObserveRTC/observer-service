package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MediaSourceType {
	MEDIA_SOURCE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case MEDIA_SOURCE:
				return "media-source";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(MediaSourceType.class);

	@JsonCreator
	public static MediaSourceType forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("media-source")) return MEDIA_SOURCE;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
