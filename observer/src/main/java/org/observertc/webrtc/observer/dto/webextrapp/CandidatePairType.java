package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CandidatePairType {
	CANDIDATE_PAIR, UNKNOWN;

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
			logger.warn("value is null for CandidatePairType");
			return UNKNOWN;
		}
		String name = value.toLowerCase();
		if (name.equals("candidate-pair")) return CANDIDATE_PAIR;
		logger.warn("Cannot deseerialize state for name {}", name);
		return UNKNOWN;
	}
}
