package org.observertc.webrtc.observer.evaluators.valueadapters;

import java.util.function.Function;
import org.observertc.webrtc.common.reports.MediaSourceReport;
import org.observertc.webrtc.observer.samples.MediaStreamSample;
import org.observertc.webrtc.observer.dto.webextrapp.RTCStats;

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
