package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.KafkaTopicsConfiguration;
import com.observertc.gatekeeper.webrtcstat.micrometer.WebRTCStatsReporter;
import com.observertc.gatekeeper.webrtcstat.model.SSRCMapEntry;
import com.observertc.gatekeeper.webrtcstat.processors.mappers.SSRCMapEntryMapper;
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
	private final SSRCMapEntriesProcessor ssrcMapSampleProcessor;

	public PeerConnectionSamplesProcessorFactory(InteractiveQueryService interactiveQueryService,
												 KafkaTopicsConfiguration sinksConfiguration,
												 WebRTCStatsReporter webRTCStatsReporter,
												 SSRCMapEntriesProcessor ssrcMapSampleProcessor) {
		this.interactiveQueryService = interactiveQueryService;
		this.topics = sinksConfiguration;
		this.webRTCStatsReporter = webRTCStatsReporter;
		this.ssrcMapSampleProcessor = ssrcMapSampleProcessor;
	}

	@Singleton
	@Named(NAME_IT_1_SAMPLES_AGGREGATOR)
	public KStream<UUID, SSRCMapEntry> makeSSRCMapEntries(ConfiguredStreamBuilder builder) {
		// TODO: make the entire feature enabling configurable
		boolean enabled = true;
		if (!enabled) {
			return null;
		}

		Properties props = builder.getConfiguration();
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		KStream<UUID, SSRCMapEntry> source = builder
				.stream(topics.SSRCMapEntries, Consumed.with(Serdes.UUID(), new SSRCMapEntryMapper()));

		source.process(() -> this.ssrcMapSampleProcessor);

		return source;
	}


}
