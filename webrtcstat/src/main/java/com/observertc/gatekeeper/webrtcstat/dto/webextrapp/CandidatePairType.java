package com.observertc.gatekeeper.webrtcstat.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum CandidatePairType {
	CANDIDATE_PAIR;

	@JsonValue
	public String toValue() {
		switch (this) {
			case CANDIDATE_PAIR: return "candidate-pair";
		}
		return null;
	}

	@JsonCreator
	public static CandidatePairType forValue(String value) throws IOException {
		if (value.equals("candidate-pair")) return CANDIDATE_PAIR;
		throw new IOException("Cannot deserialize CandidatePairType");
	}
}
