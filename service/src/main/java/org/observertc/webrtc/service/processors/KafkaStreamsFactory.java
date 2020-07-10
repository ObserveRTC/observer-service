package org.observertc.webrtc.service.processors;

import io.micronaut.configuration.kafka.streams.ConfiguredStreamBuilder;
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
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.Processor;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.KafkaTopicsConfiguration;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.dto.webextrapp.RTCStatsType;
import org.observertc.webrtc.service.mediastreams.MediaStreamEvaluator;
import org.observertc.webrtc.service.processors.mappers.JsonToPOJOMapper;
import org.observertc.webrtc.service.reportsink.ReportServiceProvider;
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
								rtcStats.getType().equals(RTCStatsType.OUTBOUND_RTP) ||
								rtcStats.getType().equals(RTCStatsType.REMOTE_INBOUND_RTP);
					}
				});
		source.transform(() -> this.mediaStreamEvaluator)
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
