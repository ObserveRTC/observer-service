package org.observertc.webrtc.service.processors;

import io.micronaut.configuration.kafka.streams.ConfiguredStreamBuilder;
import io.micronaut.context.annotation.Factory;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.processor.Processor;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.KafkaTopicsConfiguration;
import org.observertc.webrtc.service.dto.InboundStreamMeasurementDTO;
import org.observertc.webrtc.service.dto.OutboundStreamMeasurementDTO;
import org.observertc.webrtc.service.mediastreams.InboundStreamAggregator;
import org.observertc.webrtc.service.mediastreams.MediaStreamEvaluator;
import org.observertc.webrtc.service.mediastreams.OutboundStreamAggregator;
import org.observertc.webrtc.service.processors.mappers.JsonToPOJOMapper;
import org.observertc.webrtc.service.reportsink.ReportServiceProvider;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;

@Factory
public class KafkaStreamsFactory {

	private static final String MEDIA_STREAM_EVALUATOR_PROCESS = "MediaStreamEvaluatorProcess";
	private static final String REPORT_SERVICE_PROCESS = "ReportServiceProcess";

	private final KafkaTopicsConfiguration kafkaTopicsConfiguration;
	private final MediaStreamEvaluator mediaStreamEvaluator;
	private final ReportServiceProvider reportServiceProvider;

	public KafkaStreamsFactory(KafkaTopicsConfiguration kafkaTopicsConfiguration,
							   MediaStreamEvaluator mediaStreamEvaluator,
							   ReportServiceProvider reportServiceProvider) {
		this.kafkaTopicsConfiguration = kafkaTopicsConfiguration;
		this.mediaStreamEvaluator = mediaStreamEvaluator;
		this.reportServiceProvider = reportServiceProvider;
	}

	@Singleton
	@Named(MEDIA_STREAM_EVALUATOR_PROCESS)
	public KStream<UUID, ObserveRTCMediaStreamStatsSample> makeMediaStreamEvaluator(ConfiguredStreamBuilder builder) {
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
				.stream(this.kafkaTopicsConfiguration.observeRTCMediaStreamStatsSamples, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(ObserveRTCMediaStreamStatsSample.class)));

		MediaStreamDemuxer demuxer = new MediaStreamDemuxer(source);
		demuxer.getInboundStreamBranch()
				.groupBy(new KeyValueMapper<UUID, ObserveRTCMediaStreamStatsSample, MediaStreamKey>() {
					@Override
					public MediaStreamKey apply(UUID peerConnectionUUID, ObserveRTCMediaStreamStatsSample value) {
						MediaStreamKey key = new MediaStreamKey();
						key.observerUUID = value.observerUUID;
						key.peerConnectionUUID = peerConnectionUUID;
						if (value.rtcStats != null && value.rtcStats.getSsrc() != null) {
							key.SSRC = value.rtcStats.getSsrc().longValue();
						}
						return key;
					}
				}, Grouped.with(new JsonToPOJOMapper<>(MediaStreamKey.class), new JsonToPOJOMapper<>(ObserveRTCMediaStreamStatsSample.class)))
				.aggregate(() -> new InboundStreamMeasurementDTO(), new InboundStreamAggregator(),
						Materialized.with(new JsonToPOJOMapper<>(MediaStreamKey.class),
								new JsonToPOJOMapper<>(InboundStreamMeasurementDTO.class)))
				.suppress(Suppressed.untilTimeLimit(Duration.ofMillis(30000L), Suppressed.BufferConfig.unbounded()))
				.toStream()
				.to(this.kafkaTopicsConfiguration.inboundStreamMeasurements);

		demuxer.getOutboundStreamBranch()
				.groupBy(new KeyValueMapper<UUID, ObserveRTCMediaStreamStatsSample, MediaStreamKey>() {
					@Override
					public MediaStreamKey apply(UUID peerConnectionUUID, ObserveRTCMediaStreamStatsSample value) {
						MediaStreamKey key = new MediaStreamKey();
						key.observerUUID = value.observerUUID;
						key.peerConnectionUUID = peerConnectionUUID;
						if (value.rtcStats != null && value.rtcStats.getSsrc() != null) {
							key.SSRC = value.rtcStats.getSsrc().longValue();
						}
						return key;
					}
				}, Grouped.with(new JsonToPOJOMapper<>(MediaStreamKey.class), new JsonToPOJOMapper<>(ObserveRTCMediaStreamStatsSample.class)))
				.aggregate(() -> new OutboundStreamMeasurementDTO(), new OutboundStreamAggregator(),
						Materialized.with(new JsonToPOJOMapper<>(MediaStreamKey.class),
								new JsonToPOJOMapper<>(OutboundStreamMeasurementDTO.class)))
				.suppress(Suppressed.untilTimeLimit(Duration.ofMillis(30000L), Suppressed.BufferConfig.unbounded()))
				.toStream()
				.to(this.kafkaTopicsConfiguration.outboundStreamMeasurements);

		demuxer.getDefaultOutputBranch()
				.transform(() -> this.mediaStreamEvaluator)
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(Report.class)));

		return source;
	}

	@Singleton
	@Named(REPORT_SERVICE_PROCESS)
	public KStream<UUID, Report> makeReportServiceProcessor(ConfiguredStreamBuilder builder) {
		// TODO: make the entire feature enabling configurable
		boolean enabled = false;
		if (!enabled) {
			return null;
		}
		Processor<UUID, Report> reportProcessor = this.reportServiceProvider.getReportService();

		Properties props = builder.getConfiguration();
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//		final Deserializer<Windowed<MediaStreamKey>> windowedDeserializer = WindowedSerdes.timeWindowedSerdeFrom(String.class).deserializer();
		KStream<UUID, Report> source = builder
				.stream(this.kafkaTopicsConfiguration.observertcReports, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(Report.class)));

		source.process(() -> reportProcessor);
		return source;
	}

	@Singleton
	@Named("MediaStreamReportProcessor")
	public KStream<MediaStreamKey, OutboundStreamMeasurementDTO> makeMediaStreamReportProcessor(ConfiguredStreamBuilder builder) {
		// TODO: make the entire feature enabling configurable
		boolean enabled = false;
		if (!enabled) {
			return null;
		}

		Properties props = builder.getConfiguration();
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//		final Deserializer<Windowed<MediaStreamKey>> windowedDeserializer = WindowedSerdes.timeWindowedSerdeFrom(String.class).deserializer();
		KStream<MediaStreamKey, OutboundStreamMeasurementDTO> source = builder
				.stream(this.kafkaTopicsConfiguration.outboundStreamMeasurements, Consumed.with(new JsonToPOJOMapper<>(MediaStreamKey.class),
						new JsonToPOJOMapper<>(OutboundStreamMeasurementDTO.class)));
		final String str = "myInboundMeasurements";
		GlobalKTable<MediaStreamKey, InboundStreamMeasurementDTO> table =
				builder.globalTable(this.kafkaTopicsConfiguration.inboundStreamMeasurements, Materialized.as(str));
		
		return source;
	}

}
