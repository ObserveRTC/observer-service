package org.observertc.webrtc.observer.service.processors;

import org.observertc.webrtc.observer.service.KafkaTopicsConfiguration;
import org.observertc.webrtc.observer.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.observer.service.dto.webextrapp.RTCStatsType;
import org.observertc.webrtc.observer.service.micrometer.WebRTCStatsReporter;
import org.observertc.webrtc.observer.service.processors.mappers.JsonToPOJOMapper;
import org.observertc.webrtc.observer.service.samples.ObserveRTCMediaStreamStatsSample;
import org.observertc.webrtc.observer.service.samples.ObserverSSRCPeerConnectionSample;
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
public class KafkaStreamsFactory {

	private static final String OBSERVER_SSRC_KAFKA_STREAM_PROCESS = "nameit1";
	private static final String OBSERVER_MEDIA_STREAM_KAFKA_STREAM_PROCESS = "nameit2";

	private final KafkaTopicsConfiguration topics;
	private final WebRTCStatsReporter webRTCStatsReporter;
	private final InteractiveQueryService interactiveQueryService;
	private final ObserverSSRCPeerConnectionSampleProcessor observerSSRCPeerConnectionSampleProcessor;
	private final InAndOutboundStreamStatsSamplesProcessor inAndOutboundStreamStatsSamplesProcessor;

	public KafkaStreamsFactory(InteractiveQueryService interactiveQueryService,
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
	@Named(OBSERVER_SSRC_KAFKA_STREAM_PROCESS)
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
	@Named(OBSERVER_MEDIA_STREAM_KAFKA_STREAM_PROCESS)
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
