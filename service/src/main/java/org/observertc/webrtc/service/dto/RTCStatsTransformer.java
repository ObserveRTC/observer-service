package org.observertc.webrtc.service.dto;

import java.util.function.Function;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.dto.webextrapp.RTCStatsType;

public interface RTCStatsTransformer<T> extends Function<RTCStats, T> {

	default T accept(RTCStats rtcStats) {
		return this.transform(rtcStats);
	}

	default T transform(RTCStats rtcStats) {
		RTCStatsType type = rtcStats.getType();
		if (type == null) {
			return this.unprocessable(rtcStats);
		}
		switch (type) {
			case REMOTE_INBOUND_RTP:
				return this.processRemoteInboundRTP(rtcStats);
			case INBOUND_RTP:
				return this.processInboundRTP(rtcStats);
			case OUTBOUND_RTP:
				return this.processOutboundRTP(rtcStats);
			case CANDIDATE_PAIR:
				return this.processCandidatePair(rtcStats);
			case TRACK:
				return this.processTrack(rtcStats);
			default:
				return this.unprocessable(rtcStats);
		}
	}

	T processInboundRTP(RTCStats rtcStats);

	T processOutboundRTP(RTCStats rtcStats);

	T processRemoteInboundRTP(RTCStats rtcStats);

	T processTrack(RTCStats rtcStats);

	T processCandidatePair(RTCStats rtcStats);

	default T unprocessable(RTCStats rtcStats) {
		return null;
	}


}
