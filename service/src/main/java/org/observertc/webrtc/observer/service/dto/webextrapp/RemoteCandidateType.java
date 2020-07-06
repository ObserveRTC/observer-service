package org.observertc.webrtc.observer.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum RemoteCandidateType {
	REMOTE_CANDIDATE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case REMOTE_CANDIDATE: return "remote-candidate";
		}
		return null;
	}

	@JsonCreator
	public static RemoteCandidateType forValue(String value) throws IOException {
		if (value.equals("remote-candidate")) return REMOTE_CANDIDATE;
		throw new IOException("Cannot deserialize RemoteCandidateType");
	}
}
