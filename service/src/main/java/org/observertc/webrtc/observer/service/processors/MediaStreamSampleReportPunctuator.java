package org.observertc.webrtc.observer.service.processors;

import org.observertc.webrtc.observer.service.micrometer.WebRTCStatsReporter;
import org.observertc.webrtc.observer.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.observer.service.samples.MediaStreamKey;
import org.observertc.webrtc.observer.service.samples.MediaStreamSample;
import org.observertc.webrtc.observer.service.samples.OutboundStreamMeasurement;
import org.observertc.webrtc.observer.service.samples.SampleDescription;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Singleton;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MediaStreamSampleReportPunctuator implements Punctuator {

	private static final Logger logger = LoggerFactory.getLogger(MediaStreamSampleReportPunctuator.class);

	private LocalDateTime reported = null;
	private Map<MediaStreamKey, MediaStreamSample> mediaStreamSamples;
	private final Aggregator<MediaStreamKey, InboundStreamMeasurement, MediaStreamSample> inboundStreamMeasurementAggregator;
	private final Aggregator<MediaStreamKey, OutboundStreamMeasurement, MediaStreamSample> outboundStreamMeasurementAggregator;
	private final WebRTCStatsReporter webRTCStatsReporter;

	public MediaStreamSampleReportPunctuator(WebRTCStatsReporter webRTCStatsReporter) {
		this.mediaStreamSamples = new HashMap<>();
		this.inboundStreamMeasurementAggregator = new InboundStreamMeasurementAggregator();
		this.outboundStreamMeasurementAggregator = new OutboundStreamMeasurementAggregator();
		this.webRTCStatsReporter = webRTCStatsReporter;
	}

	@Override
	public void punctuate(long timestamp) {
		Iterator<Map.Entry<MediaStreamKey, MediaStreamSample>> it = this.mediaStreamSamples.entrySet().iterator();
		for (; it.hasNext(); ) {
			Map.Entry<MediaStreamKey, MediaStreamSample> entry = it.next();
			MediaStreamKey mediaStreamKey = entry.getKey();
			MediaStreamSample mediaStreamSample = entry.getValue();
			if (this.reported != null && mediaStreamSample.last.compareTo(this.reported) < 0) {
				it.remove();
				continue;
			}
			this.webRTCStatsReporter.reportMediaStreamSample(mediaStreamKey, mediaStreamSample);
			this.cleanMediaStream(mediaStreamSample);
		}
	}

	private void cleanMediaStream(MediaStreamSample mediaStreamSample) {
		mediaStreamSample.first = null;
		this.cleanSampleDescription(mediaStreamSample.bytesReceived);
		this.cleanSampleDescription(mediaStreamSample.bytesSent);
		this.cleanSampleDescription(mediaStreamSample.packetsSent);
		this.cleanSampleDescription(mediaStreamSample.packetsReceived);
		this.cleanSampleDescription(mediaStreamSample.packetsLost);
		this.cleanSampleDescription(mediaStreamSample.RTTInMs);
	}

	private void cleanSampleDescription(SampleDescription sampleDescription) {
		sampleDescription.sum = 0;
		sampleDescription.min = null;
		sampleDescription.max = null;
		sampleDescription.presented = 0;
		sampleDescription.empty = 0;
	}

	public void init(ProcessorContext context) {

	}

	public void addInboundStreamMeasurement(MediaStreamKey key, InboundStreamMeasurement measurement) {
		this.executeAggregator(this.inboundStreamMeasurementAggregator, key, measurement);
	}

	public void addOutboundStreamMeasurement(MediaStreamKey key, OutboundStreamMeasurement measurement) {
		this.executeAggregator(this.outboundStreamMeasurementAggregator, key, measurement);
	}


	private <T> void executeAggregator(Aggregator<MediaStreamKey, T, MediaStreamSample> aggregator, MediaStreamKey key,
									   T measurement) {
		MediaStreamSample mediaStreamSample = this.getMediaStreamSample(key);
		aggregator.apply(key, measurement, mediaStreamSample);
		this.postCheck(mediaStreamSample);
		this.mediaStreamSamples.put(key, mediaStreamSample);
	}

	private MediaStreamSample getMediaStreamSample(MediaStreamKey key) {
		MediaStreamSample result = this.mediaStreamSamples.get(key);
		if (result == null) {
			result = new MediaStreamSample();
		}
		return result;
	}

	private void postCheck(MediaStreamSample mediaStreamSample) {
		if (mediaStreamSample.last == null) {
			logger.warn("There was no last timestamp for the mediaStreamSample. It would crash the app, so we wet manually, but its " +
					"inaccurate");
			mediaStreamSample.last = LocalDateTime.now();
		}
	}
}
