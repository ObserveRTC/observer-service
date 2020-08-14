package org.observertc.webrtc.service.evaluators.valueadapters;

import java.util.function.Function;
import org.observertc.webrtc.common.reports.RemoteInboundRTPReport;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.samples.MediaStreamSample;

public class RemoteInboundRTPConverter implements Function<MediaStreamSample, RemoteInboundRTPReport> {


	@Override
	public RemoteInboundRTPReport apply(MediaStreamSample sample) {
		if (sample.rtcStats == null) {
			return null;
		}
		RTCStats rtcStats = sample.rtcStats;
		RemoteInboundRTPReport result = RemoteInboundRTPReport.of(
				sample.observerUUID,
				sample.peerConnectionUUID,
				NumberConverter.toLong(rtcStats.getSsrc()),
				sample.sampled,
				rtcStats.getCodecID(),
				rtcStats.getJitter(),
				NumberConverter.toInt(rtcStats.getPacketsLost()),
				rtcStats.getRoundTripTime(),
				MediaTypeConverter.fromKind(rtcStats.getKind())
		);
		return result;
	}
}
