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
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
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
import org.jooq.lambda.tuple.Tuple2;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

@Prototype
public class ReportSink implements Observer<Tuple2<UUID, Report>> {
	private static final Logger logger = LoggerFactory.getLogger(ReportSink.class);
	private final Producer<UUID, byte[]> reportProducer;
	private final FlawMonitor flawMonitor;
	private final DatumWriter<Report> datumWriter;
	private final ByteArrayOutputStream outputStream;
	private final Encoder encoder;
	private final ObserverConfig.KafkaTopicsConfiguration.ReportsConfig config;

	public ReportSink(
			ObserverConfig.KafkaTopicsConfiguration.ReportsConfig config,
			@KafkaClient("reportProducer") Producer<UUID, byte[]> reportProducer,
			MonitorProvider monitorProvider
	) {
		this.reportProducer = reportProducer;
		this.outputStream = new ByteArrayOutputStream();
		this.datumWriter = new SpecificDatumWriter<>(Report.SCHEMA$);
		this.encoder = EncoderFactory.get().binaryEncoder(this.outputStream, null);
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass().getSimpleName());
		this.config = config;
	}

	public Future<RecordMetadata> sendReport(UUID reportKey,
											 UUID serviceUUID,
											 String serviceName,
											 String marker,
											 ReportType type,
											 Long timestamp,
											 Object payload) {
		String serviceUUIDStr = serviceUUID != null ? serviceUUID.toString() : null;
		Report report = Report.newBuilder()
				.setServiceUUID(serviceUUIDStr)
				.setServiceName(serviceName)
				.setMarker(marker)
				.setType(type)
				.setTimestamp(timestamp)
				.setPayload(payload)
				.build();

		return this.send(reportKey, report);

	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {

	}

	@Override
	public void onNext(@NonNull Tuple2<UUID, Report> objects) {
		UUID key = objects.v1;
		Report report = objects.v2;
		this.send(key, report);
	}

	@Override
	public void onError(@NonNull Throwable e) {

	}

	@Override
	public void onComplete() {

	}

	private Future<RecordMetadata> send(UUID key, Report report) {
		this.outputStream.reset();
		try {
			this.datumWriter.write(report, encoder);
		} catch (Exception e) {
			this.flawMonitor
					.makeLogEntry()
					.withLogger(logger)
					.withMessage("error during serialization")
					.withException(e)
					.withLogLevel(Level.ERROR)
					.complete();
			return null;
		}
		byte[] out;
		try {
			this.encoder.flush();
			out = outputStream.toByteArray();
		} catch (IOException e) {
			this.flawMonitor
					.makeLogEntry()
					.withLogger(logger)
					.withMessage("Error during flushing")
					.withException(e)
					.withLogLevel(Level.ERROR)
					.complete();
			return null;
		}

		return this.reportProducer.send(new ProducerRecord<UUID, byte[]>(
				this.config.topicName,
				key,
				out));
	}
}
