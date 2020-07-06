package org.observertc.webrtc.observer.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum OutboundRTPType {
	OUTBOUND_RTP;

	@JsonValue
	public String toValue() {
		switch (this) {
			case OUTBOUND_RTP: return "outbound-rtp";
		}
		return null;
	}

	@JsonCreator
	public static OutboundRTPType forValue(String value) throws IOException {
		if (value.equals("outbound-rtp")) return OUTBOUND_RTP;
		throw new IOException("Cannot deserialize OutboundRTPType");
	}
}
