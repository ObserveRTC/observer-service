// Converter.java

// To use this code, add the following Maven dependency to your project:
//
//
//     com.fasterxml.jackson.core     : jackson-databind          : 2.9.0
//     com.fasterxml.jackson.datatype : jackson-datatype-jsr310   : 2.9.0
//
// Import this package:
//
//     import com.observertc.gatekeeper.webrtcstat.dto.webextrapp.Converter;
//
// Then you can deserialize a JSON string with
//
//     MediaSource data = Converter.MediaSourceFromJsonString(jsonString);
//     CandidatePair data = Converter.CandidatePairFromJsonString(jsonString);
//     RemoteCandidate data = Converter.RemoteCandidateFromJsonString(jsonString);
//     LocalCandidate data = Converter.LocalCandidateFromJsonString(jsonString);
//     ICECandidate data = Converter.ICECandidateFromJsonString(jsonString);
//     Track data = Converter.TrackFromJsonString(jsonString);
//     OutboundRTP data = Converter.OutboundRTPFromJsonString(jsonString);
//     InboundRTP data = Converter.InboundRTPFromJsonString(jsonString);
//     RemoteInboundRTP data = Converter.RemoteInboundRTPFromJsonString(jsonString);
//     ObserveRTCCIceStats data = Converter.ObserveRTCCIceStatsFromJsonString(jsonString);
//     ObserveRTCStats data = Converter.ObserveRTCStatsFromJsonString(jsonString);
//     PeerConnectionSample data = Converter.PeerConnectionSampleFromJsonString(jsonString);

package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TrackType {
	TRACK, UNKNOWN;

	@JsonValue
	public String toValue() {
		switch (this) {
			case TRACK:
				return "track";
		}
		return null;
	}

	private static final Logger logger = LoggerFactory.getLogger(TrackType.class);

	@JsonCreator
	public static TrackType forValue(String value) throws IOException {
		if (value == null) {
			logger.warn("value is null for TrackType");
			return UNKNOWN;
		}
		String name = value.toLowerCase();
		if (name.equals("track")) return TRACK;
		logger.warn("Cannot deseerialize state for name {}", name);
		return UNKNOWN;
	}
}

