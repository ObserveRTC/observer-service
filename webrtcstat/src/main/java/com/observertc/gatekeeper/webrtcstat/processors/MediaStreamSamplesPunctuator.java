package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.micrometer.ObserverSSRCPeerConnectionSampleProcessReporter;
import com.observertc.gatekeeper.webrtcstat.micrometer.WebRTCStatsReporter;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Singleton;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MediaStreamSamplesPunctuator implements Punctuator {

	private static final Logger logger = LoggerFactory.getLogger(MediaStreamSamplesPunctuator.class);

	private final WebRTCStatsReporter webRTCStatsReporter;

	public MediaStreamSamplesPunctuator(
			WebRTCStatsReporter webRTCStatsReporter, ObserverSSRCPeerConnectionSampleProcessReporter observerSSRCPeerConnectionSampleProcessReporter) {
		this.webRTCStatsReporter = webRTCStatsReporter;
	}

	public void init(ProcessorContext context) {

	}

	/**
	 * This is the trigger method initiate the process of cleaning the calls
	 *
	 * @param timestamp
	 */
	@Override
	public void punctuate(long timestamp) {
		Instant started = Instant.now();
		try {
			this.punctuateProcess(timestamp);
		} catch (Exception ex) {
			logger.error("An exception occured during execution", ex);
		} finally {
			Duration duration = Duration.between(Instant.now(), started);
		}
	}

	public void punctuateProcess(long timestamp) {

	}
}
