package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LocalCandidateType {
	LOCAL_CANDIDATE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case LOCAL_CANDIDATE:
				return "local-candidate";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(LocalCandidateType.class);

	@JsonCreator
	public static LocalCandidateType forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("local-candidate")) return LOCAL_CANDIDATE;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
