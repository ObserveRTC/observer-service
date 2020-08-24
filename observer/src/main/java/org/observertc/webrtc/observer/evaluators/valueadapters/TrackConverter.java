package org.observertc.webrtc.observer.evaluators.valueadapters;

import java.util.function.Function;
import org.observertc.webrtc.common.reports.TrackReport;
import org.observertc.webrtc.observer.samples.MediaStreamSample;
import org.observertc.webrtc.observer.dto.webextrapp.RTCStats;

public class TrackConverter implements Function<MediaStreamSample, TrackReport> {


	@Override
	public TrackReport apply(MediaStreamSample sample) {
		if (sample.rtcStats == null) {
			return null;
		}
		RTCStats rtcStats = sample.rtcStats;
		TrackReport result = TrackReport.of(
				sample.observerUUID,
				sample.peerConnectionUUID,
				sample.sampled,
				rtcStats.getID(),
				rtcStats.getAudioLevel(),
				NumberConverter.toInt(rtcStats.getConcealedSamples()),
				NumberConverter.toInt(rtcStats.getConcealmentEvents()),
				rtcStats.getDetached(),
				rtcStats.getEnded(),
				NumberConverter.toInt(rtcStats.getFrameHeight()),
				NumberConverter.toInt(rtcStats.getFramesDecoded()),
				NumberConverter.toInt(rtcStats.getFramesDropped()),
				NumberConverter.toInt(rtcStats.getFramesReceived()),
				NumberConverter.toInt(rtcStats.getFramesSent()),
				NumberConverter.toInt(rtcStats.getFrameWidth()),
				NumberConverter.toInt(rtcStats.getHugeFramesSent()),
				NumberConverter.toInt(rtcStats.getInsertedSamplesForDeceleration()),
				rtcStats.getJitterBufferDelay(),
				NumberConverter.toInt(rtcStats.getJitterBufferEmittedCount()),
				MediaTypeConverter.fromKind(rtcStats.getKind()),
				rtcStats.getRemoteSource(),
				NumberConverter.toInt(rtcStats.getRemovedSamplesForAcceleration()),
				NumberConverter.toInt(rtcStats.getSilentConcealedSamples()),
				rtcStats.getTotalAudioEnergy(),
				rtcStats.getTotalSamplesDuration(),
				NumberConverter.toInt(rtcStats.getTotalSamplesReceived()),
				rtcStats.getMediaSourceID()
		);
		return result;
	}
}
