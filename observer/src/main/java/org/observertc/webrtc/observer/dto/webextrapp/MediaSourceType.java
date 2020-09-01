package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MediaSourceType {
	MEDIA_SOURCE, UNKNOWN;

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
			logger.warn("value is null for MediaSourceType");
			return UNKNOWN;
		}
		String name = value.toLowerCase();
		if (name.equals("media-source")) return MEDIA_SOURCE;
		logger.warn("Cannot deseerialize state for name {}", name);
		return UNKNOWN;
	}
}
