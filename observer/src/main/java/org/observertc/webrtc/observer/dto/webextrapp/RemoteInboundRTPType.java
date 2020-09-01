package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RemoteInboundRTPType {
	REMOTE_INBOUND_RTP, UNKNOWN;

	@JsonValue
	public String toValue() {
		switch (this) {
			case REMOTE_INBOUND_RTP:
				return "remote-inbound-rtp";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(RemoteInboundRTPType.class);

	@JsonCreator
	public static RemoteInboundRTPType forValue(String value) throws IOException {
		if (value == null) {
			logger.warn("value is null for RemoteInboundRTPType");
			return UNKNOWN;
		}
		String name = value.toLowerCase();
		if (name.equals("remote-inbound-rtp")) return REMOTE_INBOUND_RTP;
		logger.warn("Cannot deseerialize state for name {}", name);
		return UNKNOWN;
	}
}
