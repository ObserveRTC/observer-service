package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RTCStatsType {
	CANDIDATE_PAIR, INBOUND_RTP, MEDIA_SOURCE, OUTBOUND_RTP, REMOTE_INBOUND_RTP, TRACK,
	REMOTE_OUTBOUND_TYPE, // not part of the original schema
	DATA_CHANNEL, // not part of the original schema
	UNKNOWN;

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
			case REMOTE_OUTBOUND_TYPE:
				return "remote-outbound-rtp";
			case DATA_CHANNEL:
				return "data-channel";

		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(RTCStatsType.class);


	@JsonCreator
	public static RTCStatsType forValue(String value) throws IOException {
		if (value == null) {
			logger.warn("value is null for RTCStatsType");
			return UNKNOWN;
		}
		String name = value.toLowerCase();
		String secondary = name.replace("[^a-zA-Z0-9]", "");
		if (name.equals("candidate-pair") || name.equals("candidatepair")) return CANDIDATE_PAIR;
		if (name.equals("inbound-rtp") || name.equals("inboundrtp")) return INBOUND_RTP;
		if (name.equals("media-source") || name.equals("mediasource")) return MEDIA_SOURCE;
		if (name.equals("outbound-rtp") || name.equals("outboundrtp")) return OUTBOUND_RTP;
		if (name.equals("remote-inbound-rtp") || name.equals("remoteinboundrtp")) return REMOTE_INBOUND_RTP;
		if (name.equals("remote-outbound-rtp") || name.equals("remoteoutboundrtp")) return REMOTE_OUTBOUND_TYPE;
		if (name.equals("data-channel") || name.equals("datachannel")) return DATA_CHANNEL;
		if (name.equals("track")) return TRACK;
		logger.warn("Cannot deseerialize state for name {}", name);
		return UNKNOWN;
	}
}
