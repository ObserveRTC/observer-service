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

package org.observertc.webrtc.observer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.observer.micrometer.CountedLogMonitor;
import org.observertc.webrtc.observer.micrometer.ObserverMetricsReporter;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

@Prototype
public class ReportSink {
	private static final Logger logger = LoggerFactory.getLogger(ReportSink.class);
	private final Producer<UUID, byte[]> reportProducer;
	private final KafkaTopicsConfiguration.ObserveRTCReportsConfig config;
	private final DatumWriter<Report> datumWriter;
	private final ByteArrayOutputStream outputStream;
	private final Encoder encoder;
	private final ObserverMetricsReporter observerMetricsReporter;
	private final CountedLogMonitor countedLogMonitor;

	public ReportSink(
			ObserverMetricsReporter observerMetricsReporter,
			@KafkaClient("reportProducer") Producer<UUID, byte[]> reportProducer,
			KafkaTopicsConfiguration.ObserveRTCReportsConfig config
	) {
		this.reportProducer = reportProducer;
		this.config = config;
		this.outputStream = new ByteArrayOutputStream();
		this.datumWriter = new SpecificDatumWriter<>(Report.SCHEMA$);
		this.encoder = EncoderFactory.get().binaryEncoder(this.outputStream, null);
		this.observerMetricsReporter = observerMetricsReporter;
		this.countedLogMonitor = observerMetricsReporter
				.makeCountedLogMonitor(logger)
				.withDefaultMetricName("reportsink");
	}

	public Future<RecordMetadata> sendReport(UUID reportKey,
											 UUID serviceUUID,
											 String serviceName,
											 String customProvided,
											 ReportType type,
											 Long timestamp,
											 Object payload) {
		String serviceUUIDStr = serviceUUID != null ? serviceUUID.toString() : null;
		Report report = Report.newBuilder()
				.setServiceUUID(serviceUUIDStr)
				.setServiceName(serviceName)
				.setCustomProvided(customProvided)
				.setType(type)
				.setTimestamp(timestamp)
				.setPayload(payload)
				.build();

		this.outputStream.reset();
		try {
			this.datumWriter.write(report, encoder);
		} catch (Exception e) {
			this.countedLogMonitor
					.makeEntry()
					.withCategory("datumWriter")
					.withException(e)
					.withLogLevel(Level.ERROR)
					.log();
			return null;
		}
		byte[] out;
		try {
			this.encoder.flush();
			out = outputStream.toByteArray();
		} catch (IOException e) {
			this.countedLogMonitor
					.makeEntry()
					.withCategory("encoderFlush")
					.withException(e)
					.withLogLevel(Level.ERROR)
					.log();
			return null;
		}

		return this.reportProducer.send(new ProducerRecord<UUID, byte[]>(this.config.topicName, reportKey,
				out));
	}

	@Singleton
	static class CreateReportDraftSinkListener implements BeanCreatedEventListener<ReportSink> {

		@Inject
		KafkaTopicCreator topicCreator;

		@Inject
		KafkaTopicsConfiguration config;

		@Override
		public ReportSink onCreated(BeanCreatedEvent<ReportSink> event) {
			this.topicCreator.execute(config.reports);
			return event.getBean();
		}
	}

}
