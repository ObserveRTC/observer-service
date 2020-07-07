package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum RTCStatsType {
	CANDIDATE_PAIR, INBOUND_RTP, MEDIA_SOURCE, OUTBOUND_RTP, REMOTE_INBOUND_RTP, TRACK;

	@JsonValue
	public String toValue() {
		switch (this) {
			case CANDIDATE_PAIR: return "candidate-pair";
			case INBOUND_RTP: return "inbound-rtp";
			case MEDIA_SOURCE: return "media-source";
			case OUTBOUND_RTP: return "outbound-rtp";
			case REMOTE_INBOUND_RTP: return "remote-inbound-rtp";
			case TRACK: return "track";
		}
		return null;
	}

	@JsonCreator
	public static RTCStatsType forValue(String value) throws IOException {
		if (value.equals("candidate-pair")) return CANDIDATE_PAIR;
		if (value.equals("inbound-rtp")) return INBOUND_RTP;
		if (value.equals("media-source")) return MEDIA_SOURCE;
		if (value.equals("outbound-rtp")) return OUTBOUND_RTP;
		if (value.equals("remote-inbound-rtp")) return REMOTE_INBOUND_RTP;
		if (value.equals("track")) return TRACK;
		throw new IOException("Cannot deserialize RTCStatsType");
	}
}
