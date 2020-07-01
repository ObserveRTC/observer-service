package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.KafkaTopicsConfiguration;
import com.observertc.gatekeeper.webrtcstat.dto.webextrapp.RTCStats;
import com.observertc.gatekeeper.webrtcstat.dto.webextrapp.RTCStatsType;
import com.observertc.gatekeeper.webrtcstat.micrometer.WebRTCStatsReporter;
import com.observertc.gatekeeper.webrtcstat.processors.mappers.JsonToPOJOMapper;
import com.observertc.gatekeeper.webrtcstat.samples.ObserveRTCMediaStreamStatsSample;
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
import org.apache.kafka.streams.kstream.Predicate;

@Factory
public class PeerConnectionSamplesProcessorFactory {

	private static final String NAME_IT_1_SAMPLES_AGGREGATOR = "nameit1";
	private static final String NAME_IT_2_SAMPLES_AGGREGATOR = "nameit2";

	private final KafkaTopicsConfiguration topics;
	private final WebRTCStatsReporter webRTCStatsReporter;
	private final InteractiveQueryService interactiveQueryService;
	private final ObserverSSRCPeerConnectionSampleProcessor observerSSRCPeerConnectionSampleProcessor;
	private final InAndOutboundStreamStatsSamplesProcessor inAndOutboundStreamStatsSamplesProcessor;

	public PeerConnectionSamplesProcessorFactory(InteractiveQueryService interactiveQueryService,
												 KafkaTopicsConfiguration sinksConfiguration,
												 WebRTCStatsReporter webRTCStatsReporter,
												 ObserverSSRCPeerConnectionSampleProcessor observerSSRCPeerConnectionSampleProcessor,
												 InAndOutboundStreamStatsSamplesProcessor inAndOutboundStreamStatsSamplesProcessor) {
		this.interactiveQueryService = interactiveQueryService;
		this.topics = sinksConfiguration;
		this.webRTCStatsReporter = webRTCStatsReporter;
		this.observerSSRCPeerConnectionSampleProcessor = observerSSRCPeerConnectionSampleProcessor;
		this.inAndOutboundStreamStatsSamplesProcessor = inAndOutboundStreamStatsSamplesProcessor;
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

	@Singleton
	@Named(NAME_IT_2_SAMPLES_AGGREGATOR)
	public KStream<UUID, ObserveRTCMediaStreamStatsSample> makeObserveRTCMediaStreamStatsSampleProcessor(ConfiguredStreamBuilder builder) {
		// TODO: make the entire feature enabling configurable
		boolean enabled = true;
		if (!enabled) {
			return null;
		}

		Properties props = builder.getConfiguration();
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//		final Deserializer<Windowed<MediaStreamKey>> windowedDeserializer = WindowedSerdes.timeWindowedSerdeFrom(String.class).deserializer();
		KStream<UUID, ObserveRTCMediaStreamStatsSample> source = builder
				.stream(topics.observeRTCMediaStreamStatsSamples, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(ObserveRTCMediaStreamStatsSample.class)))
				.filter(new Predicate<UUID, ObserveRTCMediaStreamStatsSample>() {
					@Override
					public boolean test(UUID key, ObserveRTCMediaStreamStatsSample value) {
						if (value == null) {
							return false;
						}
						RTCStats rtcStats = value.rtcStats;
						if (rtcStats == null) {
							return false;
						}
						return rtcStats.getType().equals(RTCStatsType.INBOUND_RTP) ||
								rtcStats.getType().equals(RTCStatsType.OUTBOUND_RTP);
					}
				});
		source.process(() -> inAndOutboundStreamStatsSamplesProcessor);
		return source;
	}

}
