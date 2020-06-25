package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.KafkaTopicsConfiguration;
import com.observertc.gatekeeper.webrtcstat.micrometer.WebRTCStatsReporter;
import com.observertc.gatekeeper.webrtcstat.processors.mappers.JsonToPOJOMapper;
import com.observertc.gatekeeper.webrtcstat.samples.ObserverSSRCPeerConnectionSample;
import io.micronaut.configuration.kafka.streams.ConfiguredStreamBuilder;
import io.micronaut.configuration.kafka.streams.InteractiveQueryService;
import io.micronaut.context.annotation.Factory;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

@Factory
public class PeerConnectionSamplesProcessorFactory {

	private static final String NAME_IT_1_SAMPLES_AGGREGATOR = "nameit1";

	private final KafkaTopicsConfiguration topics;
	private final WebRTCStatsReporter webRTCStatsReporter;
	private final InteractiveQueryService interactiveQueryService;
	private final ObserverSSRCPeerConnectionSampleProcessor observerSSRCPeerConnectionSampleProcessor;

	public PeerConnectionSamplesProcessorFactory(InteractiveQueryService interactiveQueryService,
												 KafkaTopicsConfiguration sinksConfiguration,
												 WebRTCStatsReporter webRTCStatsReporter,
												 ObserverSSRCPeerConnectionSampleProcessor observerSSRCPeerConnectionSampleProcessor) {
		this.interactiveQueryService = interactiveQueryService;
		this.topics = sinksConfiguration;
		this.webRTCStatsReporter = webRTCStatsReporter;
		this.observerSSRCPeerConnectionSampleProcessor = observerSSRCPeerConnectionSampleProcessor;
	}

	@Singleton
	@Named(NAME_IT_1_SAMPLES_AGGREGATOR)
	public KStream<UUID, ObserverSSRCPeerConnectionSample> makeObserverSSRCPeerConnectionSampleProcessor(ConfiguredStreamBuilder builder) {
		// TODO: make the entire feature enabling configurable
		boolean enabled = true;
		if (!enabled) {
			return null;
		}

		Properties props = builder.getConfiguration();
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		KStream<UUID, ObserverSSRCPeerConnectionSample> source = builder
				.stream(topics.observerSSRCPeerConnectionSamples, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<ObserverSSRCPeerConnectionSample>(ObserverSSRCPeerConnectionSample.class)));

		source.process(() -> this.observerSSRCPeerConnectionSampleProcessor);

		return source;
	}


}
