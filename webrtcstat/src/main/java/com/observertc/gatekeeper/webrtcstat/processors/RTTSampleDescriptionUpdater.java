package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.dto.webextrapp.RTCStats;
import com.observertc.gatekeeper.webrtcstat.samples.MediaStreamSample;
import com.observertc.gatekeeper.webrtcstat.samples.ObserveRTCMediaStreamStatsSample;
import java.util.function.BiConsumer;

public class RTTSampleDescriptionUpdater implements BiConsumer<MediaStreamSample, ObserveRTCMediaStreamStatsSample> {

	private SampleDescriptionUpdater sampleDescriptionUpdater = new SampleDescriptionUpdater();

	public void accept(MediaStreamSample result, ObserveRTCMediaStreamStatsSample value) {
		RTCStats rtcStats = value.rtcStats;
		Double RTTInMs = rtcStats.getRoundTripTime();
		if (RTTInMs == null) {
			result.RTTInMs.empty += 1;
			return;
		}
		Integer RTTInMsValue = RTTInMs.intValue();
		this.sampleDescriptionUpdater.accept(result.RTTInMs, RTTInMsValue);
	}
}
