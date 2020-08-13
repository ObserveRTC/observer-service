package org.observertc.webrtc.service.evaluators.valueadapters;

import java.util.function.Function;
import org.observertc.webrtc.common.reports.MediaSourceReport;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.purgatory.MediaStreamSample;

public class MediaSourceConverter implements Function<MediaStreamSample, MediaSourceReport> {


	@Override
	public MediaSourceReport apply(MediaStreamSample sample) {
		if (sample.rtcStats == null) {
			return null;
		}
		RTCStats rtcStats = sample.rtcStats;
		MediaSourceReport result = MediaSourceReport.of(
				sample.observerUUID,
				sample.peerConnectionUUID,
				sample.sampled,
				rtcStats.getID(),
				rtcStats.getAudioLevel(),
				rtcStats.getFramesPerSecond(),
				NumberConverter.toInt(rtcStats.getHeight()),
				NumberConverter.toInt(rtcStats.getWidth()),
				MediaTypeConverter.fromKind(rtcStats.getKind()),
				rtcStats.getTotalAudioEnergy(),
				rtcStats.getTotalSamplesDuration()
		);
		return result;
	}
}
