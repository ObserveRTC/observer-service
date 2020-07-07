package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum CandidateType {
	LOCAL_CANDIDATE, REMOTE_CANDIDATE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case LOCAL_CANDIDATE: return "local-candidate";
			case REMOTE_CANDIDATE: return "remote-candidate";
		}
		return null;
	}

	@JsonCreator
	public static CandidateType forValue(String value) throws IOException {
		if (value.equals("local-candidate")) return LOCAL_CANDIDATE;
		if (value.equals("remote-candidate")) return REMOTE_CANDIDATE;
		throw new IOException("Cannot deserialize CandidateType");
	}
}
