package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OutboundRTPType {
	OUTBOUND_RTP, UNKNOWN;

	@JsonValue
	public String toValue() {
		switch (this) {
			case OUTBOUND_RTP:
				return "outbound-rtp";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(OutboundRTPType.class);

	@JsonCreator
	public static OutboundRTPType forValue(String value) throws IOException {
		if (value == null) {
			logger.warn("value is null for OutboundRTPType");
			return UNKNOWN;
		}
		String name = value.toLowerCase();
		if (name.equals("outbound-rtp")) return OUTBOUND_RTP;
		logger.warn("Cannot deseerialize state for name {}", name);
		return UNKNOWN;
	}
}
