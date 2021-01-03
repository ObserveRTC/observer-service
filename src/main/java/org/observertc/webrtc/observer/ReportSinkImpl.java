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
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Function;

@Singleton
public class ReportSinkImpl implements Observer<ReportRecord> {
	private static final Logger logger = LoggerFactory.getLogger(ReportSinkImpl.class);
	private final Producer<UUID, byte[]> reportProducer;
	private final FlawMonitor flawMonitor;
	private final Function<Report, String> topicNameRouter;

	public ReportSinkImpl(
			ObserverConfig config,
			@KafkaClient("reportProducer") Producer<UUID, byte[]> reportProducer,
			MonitorProvider monitorProvider
	) {
		this.reportProducer = reportProducer;
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());

		Map<UUID, String> topicRoutes = new HashMap<>();
		if (Objects.nonNull(config.serviceMappings)) {
			config.serviceMappings.stream()
					.filter(smc -> Objects.nonNull(smc.forwardTopicName))
					.forEach(smc -> {
						logger.info("{} will be reported to topic name {}", smc.name, smc.forwardTopicName);
						smc.uuids.stream()
						.forEach(uuid -> {
							topicRoutes.put(uuid, smc.forwardTopicName);
						});
			});
		}

		if (0 < topicRoutes.size()) {
			this.topicNameRouter = report -> {
				UUID serviceUUID = UUID.fromString(report.getServiceUUID());
				return topicRoutes.getOrDefault(serviceUUID, config.outboundReports.defaultTopicName);
			};
		} else {
			this.topicNameRouter = report -> config.outboundReports.defaultTopicName;
		}
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
	public void onNext(@NonNull ReportRecord reportRecord) {
		UUID key = reportRecord.key;
		Report report = reportRecord.value;
		this.send(key, report);
	}

	@Override
	public void onError(@NonNull Throwable e) {

	}

	@Override
	public void onComplete() {

	}

	private Future<RecordMetadata> send(UUID key, Report report) {
		byte[] out;
		try {
			out = report.toByteBuffer().array();
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

		String topic = this.topicNameRouter.apply(report);
		return this.reportProducer.send(new ProducerRecord<UUID, byte[]>(
				topic,
				key,
				out));
	}
}
