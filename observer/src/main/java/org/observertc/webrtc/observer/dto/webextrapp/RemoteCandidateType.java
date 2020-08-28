package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RemoteCandidateType {
	REMOTE_CANDIDATE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case REMOTE_CANDIDATE:
				return "remote-candidate";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(RemoteCandidateType.class);

	@JsonCreator
	public static RemoteCandidateType forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("remote-candidate")) return REMOTE_CANDIDATE;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
