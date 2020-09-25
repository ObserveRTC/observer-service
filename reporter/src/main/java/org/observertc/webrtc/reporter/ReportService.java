/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.reporter;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.inject.Provider;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.observertc.webrtc.reporter.bigquery.BigQueryReporter;
import org.observertc.webrtc.schemas.reports.Report;
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
	private final SpecificDatumReader<Report> reader = new SpecificDatumReader<>(Report.class);

	private final Reporter reporter;

	public ReportService(ReporterConfig config,
						 Provider<BigQueryReporter> bigQueryReporterProvider) {
		this.reporter = bigQueryReporterProvider.get();
	}

	@Topic("${reporter.observeRTCReportsTopic}")
	public void receive(List<byte[]> bytesList) {
		Iterator<byte[]> it = bytesList.iterator();
		for (; it.hasNext(); ) {
			byte[] data = it.next();
			BinaryDecoder binDecoder = DecoderFactory.get().binaryDecoder(data, null);
			Report report = new Report();
			try {
				reader.read(report, binDecoder);
			} catch (IOException e) {
				logger.error("Error during process", e);
				continue;
			}
			try {
				this.reporter.accept(report);
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
