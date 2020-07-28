package org.observertc.webrtc.service.processors.mediastreams.valueadapters;

import java.util.function.Function;
import org.observertc.webrtc.common.reports.OutboundRTPReport;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.samples.MediaStreamSample;

public class OutboundRTPConverter implements Function<MediaStreamSample, OutboundRTPReport> {

	@Override
	public OutboundRTPReport apply(MediaStreamSample sample) {
		if (sample.rtcStats == null) {
			return null;
		}
		RTCStats rtcStats = sample.rtcStats;
		OutboundRTPReport result = OutboundRTPReport.of(
				sample.observerUUID,
				sample.peerConnectionUUID,
				NumberConverter.toLong(rtcStats.getSsrc()),
				sample.sampled,
				NumberConverter.toLong(rtcStats.getBytesSent()),
				rtcStats.getEncoderImplementation(),
				NumberConverter.toInt(rtcStats.getFirCount()),
				NumberConverter.toInt(rtcStats.getFramesEncoded()),
				NumberConverter.toLong(rtcStats.getHeaderBytesSent()),
				NumberConverter.toInt(rtcStats.getKeyFramesEncoded()),
				NumberConverter.toInt(rtcStats.getNACKCount()),
				MediaTypeConverter.fromKind(rtcStats.getKind()),
				NumberConverter.toInt(rtcStats.getPacketsSent()),
				NumberConverter.toInt(rtcStats.getPliCount()),
				rtcStats.getQpSum(),
				StringConverter.toString(rtcStats.getQualityLimitationReason()),
				rtcStats.getQualityLimitationResolutionChanges(),
				NumberConverter.toLong(rtcStats.getRetransmittedBytesSent()),
				NumberConverter.toInt(rtcStats.getRetransmittedPacketsSent()),
				NumberConverter.toLong(rtcStats.getTotalEncodedBytesTarget()),
				NumberConverter.toLong(rtcStats.getTotalEncodeTime()),
				rtcStats.getTotalPacketSendDelay()
		);
		return result;
	}
}
