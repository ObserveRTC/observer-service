package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CandidatePairType {
	CANDIDATE_PAIR;

	@JsonValue
	public String toValue() {
		switch (this) {
			case CANDIDATE_PAIR:
				return "candidate-pair";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(CandidatePairType.class);

	@JsonCreator
	public static CandidatePairType forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("candidate-pair")) return CANDIDATE_PAIR;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
