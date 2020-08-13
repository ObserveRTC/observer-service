package org.observertc.webrtc.service.evaluators;

import io.micronaut.configuration.kafka.streams.ConfiguredStreamBuilder;
import io.micronaut.context.annotation.Factory;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.Processor;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.KafkaTopicsConfiguration;
import org.observertc.webrtc.service.evaluators.mappers.JsonToPOJOMapper;
import org.observertc.webrtc.service.evaluators.mediastreams.ActiveStreamsEvaluator;
import org.observertc.webrtc.service.reportsink.ReportServiceProvider;
import org.observertc.webrtc.service.samples.WebExtrAppSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class KafkaStreamsFactory {

	private static Logger logger = LoggerFactory.getLogger(KafkaStreamsFactory.class);
	private static final String REPORT_SERVICE_PROCESS = "ReportServiceProcess";
	private static final String WEBEXTRAPP_SAMPLE_PROCESSES = "WebExtrAppSampleProcesses";


	private final KafkaTopicsConfiguration kafkaTopicsConfiguration;
	private final ReportServiceProvider reportServiceProvider;
	private final Provider<ActiveStreamsEvaluator> activeStreamsEvaluatorProvider;
	private final Provider<WebExtrAppSamplesEvaluator> webExtrAppSamplesEvaluatorProvider;

	public KafkaStreamsFactory(
			KafkaTopicsConfiguration kafkaTopicsConfiguration,
			Provider<ActiveStreamsEvaluator> activeStreamsEvaluatorProvider,
			Provider<WebExtrAppSamplesEvaluator> webExtrAppSamplesEvaluatorProvider,
			ReportServiceProvider reportServiceProvider) {
		this.kafkaTopicsConfiguration = kafkaTopicsConfiguration;
		this.activeStreamsEvaluatorProvider = activeStreamsEvaluatorProvider;
		this.webExtrAppSamplesEvaluatorProvider = webExtrAppSamplesEvaluatorProvider;
		this.reportServiceProvider = reportServiceProvider;
	}


	@Singleton
	@Named(WEBEXTRAPP_SAMPLE_PROCESSES)
	public KStream<UUID, WebExtrAppSample> makeWebExtrAppEvaluator(ConfiguredStreamBuilder builder) {

		Properties props = builder.getConfiguration();
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		KStream<UUID, WebExtrAppSample> source = builder
				.stream(this.kafkaTopicsConfiguration.webExtrAppSamples, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(WebExtrAppSample.class)));

		WebExtrAppSampleDemuxer demuxer = new WebExtrAppSampleDemuxer(source);

		demuxer.getMediaStatsStreams().transform(activeStreamsEvaluatorProvider::get)
				.to(this.kafkaTopicsConfiguration.observertcReports, Produced.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(Report.class)));

		demuxer.getDefaultStream().transform(webExtrAppSamplesEvaluatorProvider::get)
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
		KStream<UUID, Report> source = builder
				.stream(this.kafkaTopicsConfiguration.observertcReports, Consumed.with(Serdes.UUID(),
						new JsonToPOJOMapper<>(Report.class)));

		source.process(() -> reportProcessor);
		return source;
	}
}
