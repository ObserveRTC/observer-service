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

package com.observertc.gatekeeper.webrtcstat.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum TrackType {
	TRACK;

	@JsonValue
	public String toValue() {
		switch (this) {
			case TRACK:
				return "track";
		}
		return null;
	}

	@JsonCreator
	public static TrackType forValue(String value) throws IOException {
		if (value.equals("track")) return TRACK;
		throw new IOException("Cannot deserialize TrackType");
	}
}

