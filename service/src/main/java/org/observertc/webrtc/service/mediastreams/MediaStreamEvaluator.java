package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.UUID;
import org.apache.kafka.streams.kstream.KStream;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.samples.InboundStreamAggregate;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;

@Prototype
public class MediaStreamEvaluator {
	private final MediaStreamEvaluatorConfiguration configuration;

	public MediaStreamEvaluator(MediaStreamEvaluatorConfiguration configuration) {
		this.configuration = configuration;
	}

	public void evaluate(KStream<UUID, ObserveRTCMediaStreamStatsSample> source) {

	}

	public KStream<MediaStreamKey, InboundStreamAggregate> getInboundStreamAggregateOutput() {

		return null;
	}

	public KStream<UUID, Report> getCallReportsOutput() {
		return null;
	}

//	private KStream<MediaStreamKey, InboundStreamAggregator> buildInboundStreamEvaluator() {
//		return null;
//	}
}
