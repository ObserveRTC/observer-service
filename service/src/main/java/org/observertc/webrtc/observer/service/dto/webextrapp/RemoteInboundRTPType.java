package org.observertc.webrtc.observer.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum RemoteInboundRTPType {
	REMOTE_INBOUND_RTP;

	@JsonValue
	public String toValue() {
		switch (this) {
			case REMOTE_INBOUND_RTP: return "remote-inbound-rtp";
		}
		return null;
	}

	@JsonCreator
	public static RemoteInboundRTPType forValue(String value) throws IOException {
		if (value.equals("remote-inbound-rtp")) return REMOTE_INBOUND_RTP;
		throw new IOException("Cannot deserialize RemoteInboundRTPType");
	}
}
