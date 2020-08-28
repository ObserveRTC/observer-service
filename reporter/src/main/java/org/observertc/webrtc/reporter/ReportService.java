package org.observertc.webrtc.reporter;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import java.util.Iterator;
import java.util.List;
import javax.inject.Provider;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.reporter.bigquery.BigQueryReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(
		offsetReset = OffsetReset.EARLIEST,
		groupId = "observertc-webrtc-reporter-BigQueryReportService",
		sessionTimeout = "120000ms",
		pollTimeout = "15000ms",
		threads = 6,
		batch = true,
		properties = {
				@Property(name = ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, value = "10000"),
				@Property(name = ConsumerConfig.FETCH_MIN_BYTES_CONFIG, value = "10485760"),
				@Property(name = ConsumerConfig.MAX_POLL_RECORDS_CONFIG, value = "999"),
		}
)
@Prototype
public class ReportService {

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

	private final Reporter reporter;

	public ReportService(ReporterConfig config,
						 Provider<BigQueryReporter> bigQueryReporterProvider) {
		this.reporter = bigQueryReporterProvider.get();
	}

	@Topic(value = "${reporter.observeRTCReportsTopic}")
	public void receive(List<Report> reports) {
		Iterator<Report> it = reports.iterator();
		for (; it.hasNext(); ) {
			Report report = it.next();
			try {
				this.reporter.apply(report);
			} catch (Exception ex) {
				logger.error("Error during process", ex);
				continue;
			}
		}
		try {
			this.reporter.flush();
		} catch (Exception ex) {
			logger.error("Error during flushing", ex);
		}
	}
}
