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
import org.observertc.webrtc.common.reports.InboundStreamReport;
import org.observertc.webrtc.common.reports.OutboundStreamReport;
import org.observertc.webrtc.common.reports.RemoteInboundStreamReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.KafkaTopicsConfiguration;
import org.observertc.webrtc.service.ReportsConfig;
import org.observertc.webrtc.service.processors.mappers.JsonToPOJOMapper;
import org.observertc.webrtc.service.processors.mediastreams.InboundStreamReportAggregator;
import org.observertc.webrtc.service.processors.mediastreams.InboundStreamSampleMapper;
import org.observertc.webrtc.service.processors.mediastreams.MediaStreamDemuxer;
import org.observertc.webrtc.service.processors.mediastreams.MediaStreamKey;
import org.observertc.webrtc.service.processors.mediastreams.MediaStreamTransformer;
import org.observertc.webrtc.service.processors.mediastreams.OutboundStreamReportAggregator;
import org.observertc.webrtc.service.processors.mediastreams.OutboundStreamSampleMapper;
import org.observertc.webrtc.service.processors.mediastreams.RemoteInboundStreamReportAggregator;
import org.observertc.webrtc.service.processors.mediastreams.RemoteInboundStreamSampleMapper;
import org.observertc.webrtc.service.reportsink.ReportServiceProvider;
import org.observertc.webrtc.service.samples.ICEStatsSample;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.MediaStreamSample;
import org.observertc.webrtc.service.samples.OutboundStreamMeasurement;
import org.observertc.webrtc.service.samples.RemoteInboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class KafkaStreamsFactory {

	private static Logger logger = LoggerFactory.getLogger(KafkaStreamsFactory.class);
	private static final String MEDIA_STREAM_EVALUATOR_PROCESS = "MediaStreamEvaluatorProcess";
	private static final String ICE_STATS_EVALUATOR_PROCESS = "ICEStatsEvaluatorProcess";
	private static final String REPORT_SERVICE_PROCESS = "ReportServiceProcess";

	private final KafkaTopicsConfiguration kafkaTopicsConfiguration;
	private final MediaStreamTransformer mediaStreamTransformer;
	private final ReportServiceProvider reportServiceProvider;
	private final ReportsConfig reportsConfig;

	public KafkaStreamsFactory(KafkaTopicsConfiguration kafkaTopicsConfiguration,
							   ReportsConfig reportsConfig,
							   MediaStreamTransformer mediaStreamTransformer,
							   ReportServiceProvider reportServiceProvider) {
		this.kafkaTopicsConfiguration = kafkaTopicsConfiguration;
		this.mediaStreamTransformer = mediaStreamTransformer;
		this.reportServiceProvider = reportServiceProvider;
		this.reportsConfig = reportsConfig;
	}

	@Singleton
	@Named(MEDIA_STREAM_EVALUATOR_PROCESS)
	public KStream<UUID, MediaStreamSample> makeMediaStreamEvaluator(ConfiguredStreamBuilder builder) {
		ReportsConfig.StreamReportsConfig streamReportsConfig = this.reportsConfig.streamReports;

		Properties props = builder.getConfiguration();
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		if (!streamReportsConfig.enabled) {
			return this.makeStreamProcessorWithoutMediaStreamReporter(streamReportsConfig, builder);
		} else {
			return this.makeStreamProcessorWithMediaStreamReporter(streamReportsConfig, builder);
		}
	}

	@Singleton
	@Named(ICE_STATS_EVALUATOR_PROCESS)
	public KStream<UUID, ICEStatsSample> makeICEStatsEvaluator(ConfiguredStreamBuilder builder) {
		ReportsConfig.ICEReportsConfig ICEReportsConfig = this.reportsConfig.ICEReports;

		Properties props = builder.getConfiguration();
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		if (!ICEReportsConfig.enabled) {
			return null;
		}

		KStream<UUID, ICEStatsSample> source = builder
				.stream(this.kafkaTopicsConfiguration.observeRTCCIceStatsSample, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(ICEStatsSample.class)));

		source
				.transform(() -> new ICEStatsTransformer())
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(Report.class)));
		return source;
	}

	@Singleton
	@Named(REPORT_SERVICE_PROCESS)
	public KStream<UUID, Report> makeReportServiceProcessor(ConfiguredStreamBuilder builder) {
		Processor<UUID, Report> reportProcessor;
		try {
			reportProcessor = this.reportServiceProvider.getReportService();
		} catch (Exception ex) {
			logger.error("Error happened during the instantiation of the report service. The servic is not startd", ex);
			return null;
		}


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

	private KStream<UUID, MediaStreamSample> makeStreamProcessorWithoutMediaStreamReporter(ReportsConfig.StreamReportsConfig streamReportsConfig, ConfiguredStreamBuilder builder) {
		KStream<UUID, MediaStreamSample> source = builder
				.stream(this.kafkaTopicsConfiguration.observeRTCMediaStreamStatsSamples, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(MediaStreamSample.class)));

		source
				.transform(() -> this.mediaStreamTransformer)
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(Report.class)));
		return source;
	}

	private KStream<UUID, MediaStreamSample> makeStreamProcessorWithMediaStreamReporter(ReportsConfig.StreamReportsConfig streamReportsConfig, ConfiguredStreamBuilder builder) {
		KStream<UUID, MediaStreamSample> source = builder
				.stream(this.kafkaTopicsConfiguration.observeRTCMediaStreamStatsSamples, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(MediaStreamSample.class)));

		MediaStreamDemuxer demuxer = new MediaStreamDemuxer(source);
		long windowSizeMs = TimeUnit.SECONDS.toMillis(streamReportsConfig.aggregationTimeInS);

		TimeWindows aggregateTimeWindow =
				TimeWindows.of(Duration.ofMillis(windowSizeMs)).grace(Duration.ofMillis(windowSizeMs));

		demuxer.getInboundStreamBranch()
				.map(new InboundStreamSampleMapper())
				.groupByKey(Grouped.with(new JsonToPOJOMapper<>(MediaStreamKey.class),
						new JsonToPOJOMapper<>(InboundStreamMeasurement.class)))
				.windowedBy(aggregateTimeWindow)
				.aggregate(
						() -> {
							return new InboundStreamReport();
						},
						new InboundStreamReportAggregator(),
						Materialized.as("myStore3").with(new JsonToPOJOMapper<>(MediaStreamKey.class),
								new JsonToPOJOMapper<>(InboundStreamReport.class)))
				.suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
				.toStream()
				.map(new KeyValueMapper<Windowed<MediaStreamKey>, InboundStreamReport, KeyValue<UUID, Report>>() {
					@Override
					public KeyValue<UUID, Report> apply(Windowed<MediaStreamKey> key, InboundStreamReport value) {
						return KeyValue.pair(value.observerUUID, value);
					}
				})
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(), new JsonToPOJOMapper<>(Report.class)));

		demuxer.getOutboundStreamBranch()
				.map(new OutboundStreamSampleMapper())
				.groupByKey(Grouped.with(new JsonToPOJOMapper<>(MediaStreamKey.class),
						new JsonToPOJOMapper<>(OutboundStreamMeasurement.class)))
				.windowedBy(aggregateTimeWindow)
				.aggregate(
						() -> {
							return new OutboundStreamReport();
						},
						new OutboundStreamReportAggregator(),
						Materialized.as("myStore4").with(new JsonToPOJOMapper<>(MediaStreamKey.class),
								new JsonToPOJOMapper<>(OutboundStreamReport.class)))
				.suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
				.toStream()
				.map(new KeyValueMapper<Windowed<MediaStreamKey>, OutboundStreamReport, KeyValue<UUID, Report>>() {
					@Override
					public KeyValue<UUID, Report> apply(Windowed<MediaStreamKey> key, OutboundStreamReport value) {
						return KeyValue.pair(value.observerUUID, value);
					}
				})
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(), new JsonToPOJOMapper<>(Report.class)));


		demuxer.getRemoteInboundStreamBranch()
				.map(new RemoteInboundStreamSampleMapper())
				.groupByKey(Grouped.with(new JsonToPOJOMapper<>(MediaStreamKey.class),
						new JsonToPOJOMapper<>(RemoteInboundStreamMeasurement.class)))
				.windowedBy(aggregateTimeWindow)
				.aggregate(
						() -> {
							return new RemoteInboundStreamReport();
						},
						new RemoteInboundStreamReportAggregator(),
						Materialized.as("myStore5").with(new JsonToPOJOMapper<>(MediaStreamKey.class),
								new JsonToPOJOMapper<>(RemoteInboundStreamReport.class)))
				.suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
				.toStream()
				.map(new KeyValueMapper<Windowed<MediaStreamKey>, RemoteInboundStreamReport, KeyValue<UUID, Report>>() {
					@Override
					public KeyValue<UUID, Report> apply(Windowed<MediaStreamKey> key, RemoteInboundStreamReport value) {
						return KeyValue.pair(value.observerUUID, value);
					}
				})
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(), new JsonToPOJOMapper<>(Report.class)));


		demuxer.getDefaultOutputBranch()
				.transform(() -> this.mediaStreamTransformer)
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(Report.class)));

		return source;
	}

}
