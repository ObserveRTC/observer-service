package org.observertc.webrtc.service.processors;

import io.micronaut.configuration.kafka.streams.ConfiguredStreamBuilder;
import io.micronaut.context.annotation.Factory;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.Processor;
import org.observertc.webrtc.common.reports.InboundStreamSampleReport;
import org.observertc.webrtc.common.reports.OutboundStreamSampleReport;
import org.observertc.webrtc.common.reports.RemoteInboundStreamSampleReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.KafkaTopicsConfiguration;
import org.observertc.webrtc.service.mediastreams.CallsEvaluator;
import org.observertc.webrtc.service.mediastreams.InboundStreamReportAggregator;
import org.observertc.webrtc.service.mediastreams.InboundStreamSampleMapper;
import org.observertc.webrtc.service.mediastreams.MediaStreamDemuxer;
import org.observertc.webrtc.service.mediastreams.MediaStreamEvaluator;
import org.observertc.webrtc.service.mediastreams.OutboundStreamReportAggregator;
import org.observertc.webrtc.service.mediastreams.OutboundStreamSampleMapper;
import org.observertc.webrtc.service.mediastreams.RemoteInboundStreamReportAggregator;
import org.observertc.webrtc.service.mediastreams.RemoteInboundStreamSampleMapper;
import org.observertc.webrtc.service.processors.mappers.JsonToPOJOMapper;
import org.observertc.webrtc.service.reportsink.ReportServiceProvider;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;
import org.observertc.webrtc.service.samples.OutboundStreamMeasurement;
import org.observertc.webrtc.service.samples.RemoteInboundStreamMeasurement;

@Factory
public class KafkaStreamsFactory {

	private static final String MEDIA_STREAM_EVALUATOR_PROCESS = "MediaStreamEvaluatorProcess";
	private static final String REPORT_SERVICE_PROCESS = "ReportServiceProcess";

	private final KafkaTopicsConfiguration kafkaTopicsConfiguration;
	private final CallsEvaluator callsEvaluator;
	private final ReportServiceProvider reportServiceProvider;
	private final MediaStreamEvaluator mediaStreamEvaluator;

	public KafkaStreamsFactory(KafkaTopicsConfiguration kafkaTopicsConfiguration,
							   MediaStreamEvaluator mediaStreamEvaluator,
							   CallsEvaluator callsEvaluator,
							   ReportServiceProvider reportServiceProvider) {
		this.kafkaTopicsConfiguration = kafkaTopicsConfiguration;
		this.callsEvaluator = callsEvaluator;
		this.reportServiceProvider = reportServiceProvider;
		this.mediaStreamEvaluator = mediaStreamEvaluator;
	}

	@Singleton
	@Named(MEDIA_STREAM_EVALUATOR_PROCESS)
	public KStream<UUID, ObserveRTCMediaStreamStatsSample> makeMediaStreamEvaluator(ConfiguredStreamBuilder builder) {
		boolean enabled = true;
		if (!enabled) {
			return null;
		}

		Properties props = builder.getConfiguration();
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		KStream<UUID, ObserveRTCMediaStreamStatsSample> source = builder
				.stream(this.kafkaTopicsConfiguration.observeRTCMediaStreamStatsSamples, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(ObserveRTCMediaStreamStatsSample.class)));

		MediaStreamDemuxer demuxer = new MediaStreamDemuxer(source);
		long windowSizeMs = TimeUnit.SECONDS.toMillis(30);

		TimeWindows aggregateTimeWindow =
				TimeWindows.of(Duration.ofMillis(windowSizeMs)).grace(Duration.ofMillis(windowSizeMs));

		demuxer.getInboundStreamBranch()
				.map(new InboundStreamSampleMapper())
				.groupByKey(Grouped.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(InboundStreamMeasurement.class)))
				.windowedBy(aggregateTimeWindow)
				.aggregate(
						() -> {
							return new InboundStreamSampleReport();
						},
						new InboundStreamReportAggregator(),
						Materialized.as("myStore3").with(Serdes.UUID(),
								new JsonToPOJOMapper<>(InboundStreamSampleReport.class)))
				.suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
				.toStream()
				.map(new KeyValueMapper<Windowed<UUID>, InboundStreamSampleReport, KeyValue<UUID, Report>>() {
					@Override
					public KeyValue<UUID, Report> apply(Windowed<UUID> key, InboundStreamSampleReport value) {
						return KeyValue.pair(key.key(), value);
					}
				})
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(), new JsonToPOJOMapper<>(Report.class)));

		demuxer.getOutboundStreamBranch()
				.map(new OutboundStreamSampleMapper())
				.groupByKey(Grouped.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(OutboundStreamMeasurement.class)))
				.windowedBy(aggregateTimeWindow)
				.aggregate(
						() -> {
							return new OutboundStreamSampleReport();
						},
						new OutboundStreamReportAggregator(),
						Materialized.as("myStore4").with(Serdes.UUID(),
								new JsonToPOJOMapper<>(OutboundStreamSampleReport.class)))
				.suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
				.toStream()
				.map(new KeyValueMapper<Windowed<UUID>, OutboundStreamSampleReport, KeyValue<UUID, Report>>() {
					@Override
					public KeyValue<UUID, Report> apply(Windowed<UUID> key, OutboundStreamSampleReport value) {
						return KeyValue.pair(key.key(), value);
					}
				})
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(), new JsonToPOJOMapper<>(Report.class)));


		demuxer.getRemoteInboundStreamBranch()
				.map(new RemoteInboundStreamSampleMapper())
				.groupByKey(Grouped.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(RemoteInboundStreamMeasurement.class)))
				.windowedBy(aggregateTimeWindow)
				.aggregate(
						() -> {
							return new RemoteInboundStreamSampleReport();
						},
						new RemoteInboundStreamReportAggregator(),
						Materialized.as("myStore5").with(Serdes.UUID(),
								new JsonToPOJOMapper<>(RemoteInboundStreamSampleReport.class)))
				.suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
				.toStream()
				.map(new KeyValueMapper<Windowed<UUID>, RemoteInboundStreamSampleReport, KeyValue<UUID, Report>>() {
					@Override
					public KeyValue<UUID, Report> apply(Windowed<UUID> key, RemoteInboundStreamSampleReport value) {
						return KeyValue.pair(key.key(), value);
					}
				})
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(), new JsonToPOJOMapper<>(Report.class)));


		demuxer.getDefaultOutputBranch()
				.transform(() -> this.callsEvaluator)
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(Report.class)));

		return source;
	}

	@Singleton
	@Named(REPORT_SERVICE_PROCESS)
	public KStream<UUID, Report> makeReportServiceProcessor(ConfiguredStreamBuilder builder) {
		// TODO: make the entire feature enabling configurable
		boolean enabled = true;
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


}
