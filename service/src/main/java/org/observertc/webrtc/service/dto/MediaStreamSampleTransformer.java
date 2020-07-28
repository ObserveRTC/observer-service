package org.observertc.webrtc.service.dto;

import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.dto.webextrapp.RTCStatsType;
import org.observertc.webrtc.service.samples.MediaStreamSample;

public interface MediaStreamSampleTransformer<T> {

	default T transform(MediaStreamSample sample) {
		if (sample.rtcStats == null) {
			return this.unprocessable(sample);
		}
		RTCStats rtcStats = sample.rtcStats;
		RTCStatsType type = rtcStats.getType();
		if (type == null) {
			return this.unprocessable(sample);
		}
		switch (type) {
			case REMOTE_INBOUND_RTP:
				return this.processRemoteInboundRTP(sample);
			case INBOUND_RTP:
				return this.processInboundRTP(sample);
			case OUTBOUND_RTP:
				return this.processOutboundRTP(sample);
			case TRACK:
				return this.processTrack(sample);
			case MEDIA_SOURCE:
				return this.processMediaSource(sample);
			case CANDIDATE_PAIR:
				return this.processCandidatePair(sample);
			default:
				return this.unprocessable(sample);
		}
	}

	T processInboundRTP(MediaStreamSample sample);

	T processOutboundRTP(MediaStreamSample sample);

	T processRemoteInboundRTP(MediaStreamSample sample);

	T processTrack(MediaStreamSample sample);

	T processMediaSource(MediaStreamSample sample);

	T processCandidatePair(MediaStreamSample sample);

	default T unprocessable(MediaStreamSample sample) {
		return null;
	}
}
