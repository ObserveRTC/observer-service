package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum InboundRTPType {
	INBOUND_RTP;

	@JsonValue
	public String toValue() {
		switch (this) {
			case INBOUND_RTP:
				return "inbound-rtp";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(InboundRTPType.class);

	@JsonCreator
	public static InboundRTPType forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("inbound-rtp")) return INBOUND_RTP;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
