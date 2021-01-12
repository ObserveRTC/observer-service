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
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
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
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;


@Singleton
public class ReportSink implements Observer<ReportRecord> {

	public static final int REPORT_VERSION_NUMBER = 1;

	private static final Logger logger = LoggerFactory.getLogger(ReportSink.class);
	private final Producer<UUID, byte[]> reportProducer;
	private final FlawMonitor flawMonitor;
	private final Function<Report, String> topicNameRouter;
	private final Subject<ReportRecord> bypassingInput = PublishSubject.create();
	private final Map<ReportType, Boolean> permissions;
	/**
	 * {@link ReportRecord} received on this interface will not be checked if
	 * they can be send or not regarding to the {@link ObserverConfig.OutboundReportsConfig}.
	 * @return
	 */
	public Observer<ReportRecord> bypassInput() {
		return this.bypassingInput;
	}

	public ReportSink(
			ObserverConfig config,
			@KafkaClient("reportProducer") Producer<UUID, byte[]> reportProducer,
			MonitorProvider monitorProvider
	) {
		this.reportProducer = reportProducer;
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
		this.permissions = this.buildPermissionMap(config.outboundReports);

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
		this.bypassingInput.subscribe(this::onByPassedNext);
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {

	}

	@Override
	public void onNext(@NonNull ReportRecord reportRecord) {
		UUID key = reportRecord.key;
		Report report = reportRecord.value;
		Boolean sendingIsAllowed = this.permissions.get(report.getType());
		if (Objects.isNull(sendingIsAllowed)) {
			logger.warn("Received an unknown ReportType {} to check if it can be sent or not", report.getType());
		}else if (!sendingIsAllowed) {
			return;
		}

		this.send(key, report);
	}

	public void onByPassedNext(@NonNull ReportRecord reportRecord) {
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

	private Map<ReportType, Boolean> buildPermissionMap(ObserverConfig.OutboundReportsConfig config) {
		Map<ReportType, Boolean> result = new HashMap<>();
		result.put(ReportType.INITIATED_CALL, config.reportInitiatedCalls);
		result.put(ReportType.FINISHED_CALL, config.reportFinishedCalls);
		result.put(ReportType.JOINED_PEER_CONNECTION, config.reportJoinedPeerConnections);
		result.put(ReportType.DETACHED_PEER_CONNECTION, config.reportDetachedPeerConnections);
		result.put(ReportType.OBSERVER_EVENT, config.reportObserverEvents);
		result.put(ReportType.EXTENSION, config.reportExtensions);
		result.put(ReportType.INBOUND_RTP, config.reportInboundRTPs);
		result.put(ReportType.OUTBOUND_RTP, config.reportOutboundRTPs);
		result.put(ReportType.REMOTE_INBOUND_RTP, config.reportRemoteInboundRTPs);
		result.put(ReportType.ICE_CANDIDATE_PAIR, config.reportCandidatePairs);
		result.put(ReportType.ICE_LOCAL_CANDIDATE, config.reportLocalCandidates);
		result.put(ReportType.ICE_REMOTE_CANDIDATE, config.reportRemoteCandidates);
		result.put(ReportType.TRACK, config.reportTracks);
		result.put(ReportType.MEDIA_SOURCE, config.reportMediaSources);
		result.put(ReportType.USER_MEDIA_ERROR, config.reportUserMediaErrors);
		if (!config.enabled) {
			Set<ReportType> types = new HashSet<>(result.keySet());
			types.stream().forEach(type -> result.put(type, false));
		}
		return Collections.unmodifiableMap(result);
	}

	private Optional<Future<RecordMetadata>> send(UUID key, Report report) {
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
		return Optional.of(
				this.reportProducer.send(new ProducerRecord<UUID, byte[]>(
						topic,
						key,
						out))
		);
	}



//	public Optional<Future<RecordMetadata>> sendReport(UUID reportKey,
//											 UUID serviceUUID,
//											 String serviceName,
//											 String marker,
//											 ReportType type,
//											 Long timestamp,
//											 Object payload) {
//		String serviceUUIDStr = serviceUUID != null ? serviceUUID.toString() : null;
//		Report report = Report.newBuilder()
//				.setVersion(REPORT_VERSION_NUMBER)
//				.setServiceUUID(serviceUUIDStr)
//				.setServiceName(serviceName)
//				.setMarker(marker)
//				.setType(type)
//				.setTimestamp(timestamp)
//				.setPayload(payload)
//				.build();
//
//		return this.send(reportKey, report);
//	}
}
