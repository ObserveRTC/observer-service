package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum LocalCandidateType {
	LOCAL_CANDIDATE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case LOCAL_CANDIDATE: return "local-candidate";
		}
		return null;
	}

	@JsonCreator
	public static LocalCandidateType forValue(String value) throws IOException {
		if (value.equals("local-candidate")) return LOCAL_CANDIDATE;
		throw new IOException("Cannot deserialize LocalCandidateType");
	}
}
