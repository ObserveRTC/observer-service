package org.observertc.webrtc.service.processors;

import java.time.Duration;
import java.util.UUID;
import javax.inject.Singleton;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.observertc.webrtc.service.samples.ObserverSSRCPeerConnectionSample;

@Singleton
public class ObserverSSRCPeerConnectionSampleProcessor implements Processor<UUID, ObserverSSRCPeerConnectionSample> {

	private final ObserverSSRCBasedCallIdentifyPunctuator callIdentifyPunctuator;
	private final ObserverSSRCBasedCallIDCleanPunctuator callIDCleanPunctuator;
	private ProcessorContext context;


	public ObserverSSRCPeerConnectionSampleProcessor(ObserverSSRCBasedCallIdentifyPunctuator ssrcBasedCallIdentifyPunctuator,
													 ObserverSSRCBasedCallIDCleanPunctuator ssrcBasedCallIDCleanPunctuator) {
		this.callIdentifyPunctuator = ssrcBasedCallIdentifyPunctuator;
		this.callIDCleanPunctuator = ssrcBasedCallIDCleanPunctuator;
	}

	@Override
	public void init(ProcessorContext context) {
		// keep the processor context locally because we need it in punctuate() and commit()
		this.context = context;
		this.callIdentifyPunctuator.init(context);
		this.callIDCleanPunctuator.init(context);
		int updatePeriodInS = 10;
		this.context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this.callIdentifyPunctuator);
		this.context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this.callIDCleanPunctuator);
	}

	@Override
	public void close() {

	}

	@Override
	public void process(UUID pcUUID, ObserverSSRCPeerConnectionSample sample) {
		this.callIdentifyPunctuator.add(sample);
	}

}
