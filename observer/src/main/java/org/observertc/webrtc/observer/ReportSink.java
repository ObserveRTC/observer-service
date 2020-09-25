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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Future;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class ReportSink {
	private static final Logger logger = LoggerFactory.getLogger(ReportSink.class);
	private final Producer<UUID, byte[]> reportProducer;
	private final KafkaTopicsConfiguration.ObserveRTCReportsConfig config;
	private final DatumWriter<Report> datumWriter;
	private final ByteArrayOutputStream outputStream;
	private final Encoder encoder;

	public ReportSink(
			@KafkaClient("reportProducer") Producer<UUID, byte[]> reportProducer,
			KafkaTopicsConfiguration.ObserveRTCReportsConfig config
	) {
		this.reportProducer = reportProducer;
		this.config = config;
		this.outputStream = new ByteArrayOutputStream();
		this.datumWriter = new SpecificDatumWriter<>(Report.SCHEMA$);
		this.encoder = EncoderFactory.get().binaryEncoder(this.outputStream, null);
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
			logger.error("Error during serialization", e);
			return null;
		}
		byte[] out;
		try {
			this.encoder.flush();
			out = outputStream.toByteArray();
		} catch (IOException e) {
			logger.error("Error in streaming", e);
			return null;
		}
		return this.reportProducer.send(new ProducerRecord<UUID, byte[]>(this.config.topicName, reportKey,
				out));
	}

}
