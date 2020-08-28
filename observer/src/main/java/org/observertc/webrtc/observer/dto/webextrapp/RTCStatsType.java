package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RTCStatsType {
	CANDIDATE_PAIR, INBOUND_RTP, MEDIA_SOURCE, OUTBOUND_RTP, REMOTE_INBOUND_RTP, TRACK;

	@JsonValue
	public String toValue() {
		switch (this) {
			case CANDIDATE_PAIR:
				return "candidate-pair";
			case INBOUND_RTP:
				return "inbound-rtp";
			case MEDIA_SOURCE:
				return "media-source";
			case OUTBOUND_RTP:
				return "outbound-rtp";
			case REMOTE_INBOUND_RTP:
				return "remote-inbound-rtp";
			case TRACK:
				return "track";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(RTCStatsType.class);


	@JsonCreator
	public static RTCStatsType forValue(String value) throws IOException {
		if (value == null) {
			return null;
		}
		String name = value.toLowerCase();
		if (name.equals("candidate-pair")) return CANDIDATE_PAIR;
		if (name.equals("inbound-rtp")) return INBOUND_RTP;
		if (name.equals("media-source")) return MEDIA_SOURCE;
		if (name.equals("outbound-rtp")) return OUTBOUND_RTP;
		if (name.equals("remote-inbound-rtp")) return REMOTE_INBOUND_RTP;
		if (name.equals("track")) return TRACK;
		logger.warn("Cannot deseerialize state for name {}", name);
		return null;
	}
}
