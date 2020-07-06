package org.observertc.webrtc.observer.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum InboundRTPType {
	INBOUND_RTP;

	@JsonValue
	public String toValue() {
		switch (this) {
			case INBOUND_RTP: return "inbound-rtp";
		}
		return null;
	}

	@JsonCreator
	public static InboundRTPType forValue(String value) throws IOException {
		if (value.equals("inbound-rtp")) return INBOUND_RTP;
		throw new IOException("Cannot deserialize InboundRTPType");
	}
}
