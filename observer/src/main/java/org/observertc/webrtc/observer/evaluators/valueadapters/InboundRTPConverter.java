package org.observertc.webrtc.observer.evaluators.valueadapters;

import java.util.function.Function;
import org.observertc.webrtc.common.reports.InboundRTPReport;
import org.observertc.webrtc.observer.samples.MediaStreamSample;
import org.observertc.webrtc.observer.dto.webextrapp.RTCStats;

public class InboundRTPConverter implements Function<MediaStreamSample, InboundRTPReport> {


	@Override
	public InboundRTPReport apply(MediaStreamSample sample) {
		if (sample.rtcStats == null) {
			return null;
		}
		RTCStats rtcStats = sample.rtcStats;
		InboundRTPReport result = InboundRTPReport.of(
				sample.observerUUID,
				sample.peerConnectionUUID,
				NumberConverter.toLong(rtcStats.getSsrc()),
				sample.sampled,
				NumberConverter.toLong(rtcStats.getBytesReceived()),
				rtcStats.getDecoderImplementation(),
				NumberConverter.toLong(rtcStats.getEstimatedPlayoutTimestamp()),
				NumberConverter.toInt(rtcStats.getFECPacketsDiscarded()),
				NumberConverter.toInt(rtcStats.getFECPacketsReceived()),
				NumberConverter.toInt(rtcStats.getFirCount()),
				NumberConverter.toInt(rtcStats.getFramesDecoded()),
				NumberConverter.toLong(rtcStats.getHeaderBytesReceived()),
				rtcStats.getJitter(),
				NumberConverter.toInt(rtcStats.getKeyFramesDecoded()),
				NumberConverter.toInt(rtcStats.getNACKCount()),
				NumberConverter.toLong(rtcStats.getLastPacketReceivedTimestamp()),
				MediaTypeConverter.fromKind(rtcStats.getKind()),
				NumberConverter.toInt(rtcStats.getPacketsLost()),
				NumberConverter.toInt(rtcStats.getPacketsReceived()),
				NumberConverter.toInt(rtcStats.getPliCount()),
				rtcStats.getQpSum(),
				rtcStats.getTotalDecodeTime(),
				rtcStats.getTotalInterFrameDelay(),
				rtcStats.getTotalSquaredInterFrameDelay()
		);
		return result;
	}
}
