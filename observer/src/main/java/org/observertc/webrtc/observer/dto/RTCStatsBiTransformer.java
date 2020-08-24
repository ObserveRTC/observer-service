package org.observertc.webrtc.observer.dto;

import org.observertc.webrtc.observer.dto.webextrapp.RTCStats;
import org.observertc.webrtc.observer.dto.webextrapp.RTCStatsType;

public interface RTCStatsBiTransformer<TMeta, TResult> {

	default TResult transform(RTCStats rtcStats, TMeta meta) {
		RTCStatsType type = rtcStats.getType();
		if (type == null) {
			return this.unprocessable(meta, rtcStats);
		}
		switch (type) {
			case REMOTE_INBOUND_RTP:
				return this.processRemoteInboundRTP(meta, rtcStats);
			case INBOUND_RTP:
				return this.processInboundRTP(meta, rtcStats);
			case OUTBOUND_RTP:
				return this.processOutboundRTP(meta, rtcStats);
			case CANDIDATE_PAIR:
				return this.processCandidatePair(meta, rtcStats);
			case TRACK:
				return this.processTrack(meta, rtcStats);
			case MEDIA_SOURCE:
				return this.processMediaSource(meta, rtcStats);
			default:
				return this.unprocessable(meta, rtcStats);
		}
	}

	TResult processInboundRTP(TMeta meta, RTCStats rtcStats);

	TResult processOutboundRTP(TMeta meta, RTCStats rtcStats);

	TResult processRemoteInboundRTP(TMeta meta, RTCStats rtcStats);

	TResult processTrack(TMeta meta, RTCStats rtcStats);

	TResult processMediaSource(TMeta meta, RTCStats rtcStats);

	TResult processCandidatePair(TMeta meta, RTCStats rtcStats);

	default TResult unprocessable(TMeta meta, RTCStats rtcStats) {
		return null;
	}


}
