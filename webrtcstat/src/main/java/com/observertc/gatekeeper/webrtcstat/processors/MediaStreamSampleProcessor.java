package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.micrometer.WebRTCStatsReporter;
import com.observertc.gatekeeper.webrtcstat.samples.MediaStreamKey;
import com.observertc.gatekeeper.webrtcstat.samples.MediaStreamSample;
import java.util.UUID;
import javax.inject.Singleton;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

@Singleton
public class MediaStreamSampleProcessor implements Processor<String, MediaStreamSample> {

	private ProcessorContext context;
	private final WebRTCStatsReporter webRTCStatsReporter;

	public MediaStreamSampleProcessor(WebRTCStatsReporter webRTCStatsReporter) {
		this.webRTCStatsReporter = webRTCStatsReporter;
	}

	@Override
	public void init(ProcessorContext context) {
		// keep the processor context locally because we need it in punctuate() and commit()
		this.context = context;
	}

	@Override
	public void close() {

	}

	@Override
	public void process(String mediaStreamKeyString, MediaStreamSample mediaStreamSample) {
		String[] splitted = mediaStreamKeyString.split("::");
		MediaStreamKey mediaStreamKey = new MediaStreamKey();
		try {
			mediaStreamKey.observerUUID = UUID.fromString(splitted[0]);
			if (1 < splitted.length) {
				mediaStreamKey.peerConnectionUUID = UUID.fromString(splitted[1]);
				if (2 < splitted.length) {
					mediaStreamKey.SSRC = Long.valueOf(splitted[2]);
				}
			}
		} catch (Exception ex) {
//			mediaStreamKey.observerUUID = UUID.fromString("something");
//			mediaStreamKey.peerConnectionUUID = UUID.fromString("something");
		}

		this.webRTCStatsReporter.reportMediaStreamSample(mediaStreamKey, mediaStreamSample);
	}

}
