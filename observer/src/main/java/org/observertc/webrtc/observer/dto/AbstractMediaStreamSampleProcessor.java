//package org.observertc.webrtc.service.dto;
//
//import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
//import org.observertc.webrtc.service.dto.webextrapp.RTCStatsType;
//import org.observertc.webrtc.service.purgatory.MediaStreamSample;
//
//public class AbstractMediaStreamSampleProcessor implements MediaStreamSampleProcessor {
//
//	public void process(MediaStreamSample sample) {
//		if (sample.rtcStats == null) {
//			this.unprocessable(sample);
//			return;
//		}
//		RTCStats rtcStats = sample.rtcStats;
//		RTCStatsType type = rtcStats.getType();
//		if (type == null) {
//			this.unprocessable(sample);
//			return;
//		}
//		switch (type) {
//			case REMOTE_INBOUND_RTP:
//				this.processRemoteInboundRTP(sample);
//				break;
//			case INBOUND_RTP:
//				this.processInboundRTP(sample);
//				break;
//			case OUTBOUND_RTP:
//				this.processOutboundRTP(sample);
//				break;
//			default:
//				this.unprocessable(sample);
//				break;
//		}
//	}
//
//	@Override
//	public void processInboundRTP(MediaStreamSample sample) {
//
//	}
//
//	@Override
//	public void processOutboundRTP(MediaStreamSample sample) {
//
//	}
//
//	@Override
//	public void processRemoteInboundRTP(MediaStreamSample sample) {
//
//	}
//
//
//}
