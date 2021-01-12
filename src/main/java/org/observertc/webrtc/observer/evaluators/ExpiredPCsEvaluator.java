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

package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.observertc.webrtc.observer.ReportRecord;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.tasks.CallFinisherTask;
import org.observertc.webrtc.observer.tasks.PeerConnectionDetacherTask;
import org.observertc.webrtc.observer.tasks.PeerConnectionsFinderTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.observertc.webrtc.schemas.reports.DetachedPeerConnection;
import org.observertc.webrtc.schemas.reports.FinishedCall;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.observertc.webrtc.observer.ReportSink.REPORT_VERSION_NUMBER;

@Singleton
public class ExpiredPCsEvaluator implements Observer<Map<UUID, PCState>> {

	private static final Logger logger = LoggerFactory.getLogger(ExpiredPCsEvaluator.class);
	private final PublishSubject<ReportRecord> reports = PublishSubject.create();

	private final FlawMonitor flawMonitor;
	private final TasksProvider tasksProvider;

	public ExpiredPCsEvaluator(
			MonitorProvider monitorProvider,
			RepositoryProvider repositoryProvider,
			TasksProvider tasksProvider) {
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
		this.tasksProvider = tasksProvider;
	}


	public Observable<ReportRecord> observableReports() {
		return this.reports;
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {

	}

	@Override
	public void onNext(@NonNull Map<UUID, PCState> expiredPCStates) {
		if (expiredPCStates.size() < 1) {
			return;
		}

		for (PCState expiredPCState : expiredPCStates.values()) {
			try {
				this.process(expiredPCState);
			} catch (Exception ex) {
				this.flawMonitor.makeLogEntry()
						.withException(ex)
						.withLogger(logger)
						.withLogLevel(Level.WARN)
						.withMessage("Unhandled error occurred by processing an expiredPCState {}", expiredPCState)
						.complete();
			}
		}

	}

	@Override
	public void onError(@NonNull Throwable e) {

	}

	@Override
	public void onComplete() {

	}

	/**
	 * This is a transaction!
	 *
	 * @param pcState
	 */
	private void process(@NotNull PCState pcState) {
		AtomicReference<Throwable> error = new AtomicReference<>(null);
		AtomicReference<PeerConnectionEntity> entityHolder = new AtomicReference<>(null);
		try (PeerConnectionDetacherTask task = this.tasksProvider.providePeerConnectionDetacherTask()) {
			task.forPeerConnectionUUID(pcState.peerConnectionUUID)
					.perform()
					.subscribe(entityHolder::set, error::set);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("Unexpected error occurred by executing {} ", PeerConnectionDetacherTask.class.getSimpleName())
					.complete();
			return;
		}
		if (error.get() != null) {
			this.flawMonitor.makeLogEntry()
					.withException(error.get())
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("Error occurred by executing {} ", PeerConnectionDetacherTask.class.getSimpleName())
					.complete();
			return;
		}
		if (entityHolder.get() == null) {
			this.flawMonitor.makeLogEntry()
					.withException(error.get())
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("{} has not been found for {}", PeerConnectionEntity.class.getSimpleName(), pcState.toString())
					.complete();
			return;
		}
		PeerConnectionEntity entity = entityHolder.get();
		Object payload = DetachedPeerConnection.newBuilder()
				.setMediaUnitId(entity.mediaUnitId)
				.setCallName(entity.callName)
				.setTimeZoneId(pcState.timeZoneId)
				.setCallUUID(entity.callUUID.toString())
				.setUserId(entity.providedUserName)
				.setBrowserId(entity.browserId)
				.setPeerConnectionUUID(entity.peerConnectionUUID.toString())
				.build();

		Report report = Report.newBuilder()
				.setVersion(REPORT_VERSION_NUMBER)
				.setServiceUUID(entity.serviceUUID.toString())
				.setServiceName(entity.serviceName)
				.setMarker(entity.marker)
				.setType(ReportType.DETACHED_PEER_CONNECTION)
				.setTimestamp(pcState.updated)
				.setPayload(payload)
				.build();
		this.send(entity.peerConnectionUUID, report);

		Set<PeerConnectionEntity> remainingPCs = new HashSet<>();
		try (PeerConnectionsFinderTask task = this.tasksProvider.providePeerConnectionFinderTask()) {
			task.forCallUUIDs(Set.of(entity.callUUID))
					.perform()
					.subscribe(remainingPCs::add);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Unexpected error occurred by executing {}", PeerConnectionsFinderTask.class.getSimpleName())
					.complete();
			return;
		}
		logger.info("PC UUID {} is unregistered.", pcState.peerConnectionUUID);
		if (remainingPCs.size() < 1) {
			this.finnishCall(entity.callUUID, pcState.updated);
			return;
		}

	}

	private void finnishCall(UUID callUUID, Long timestamp) {
		AtomicReference<CallEntity> callEntityHolder = new AtomicReference<>(null);
		AtomicReference<Throwable> error = new AtomicReference<>(null);
		try (CallFinisherTask task = this.tasksProvider.provideCallFinisherTask()) {
			task.forCallEntity(callUUID)
					.perform()
					.subscribe(callEntityHolder::set, error::set);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Unexpected error occurred by executing {}", CallFinisherTask.class.getSimpleName())
					.complete();
		}

		if (Objects.isNull(callEntityHolder.get())) {
			logger.info("Call UUID {} is not found. Already unregistered?", callUUID);
//			this.flawMonitor.makeLogEntry()
//					.withLogger(logger)
//					.withLogLevel(Level.WARN)
//					.withMessage("No call has found for call UUID {}", callUUID)
//					.complete();
			return;
		}
		logger.info("Call UUID {} is unregistered.", callUUID);
		CallEntity callEntity = callEntityHolder.get();

		if (Objects.nonNull(error.get())) {
			this.flawMonitor.makeLogEntry()
					.withException(error.get())
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("An error occurred by executing {} CallUUID {} may not be finished, and not be reported as finished", CallFinisherTask.class.getSimpleName(), callUUID)
					.complete();
			return;
		}

		FinishedCall payload = FinishedCall.newBuilder()
				.setCallName(callEntity.callName)
				.setCallUUID(callEntity.callUUID.toString())
				.build();
		Report report = Report.newBuilder()
				.setVersion(REPORT_VERSION_NUMBER)
				.setServiceUUID(callEntity.serviceUUID.toString())
				.setServiceName(callEntity.serviceName)
				.setMarker(callEntity.marker)
				.setType(ReportType.FINISHED_CALL)
				.setTimestamp(timestamp)
				.setPayload(payload)
				.build();
		logger.info("Call UUID {} is not found. Already unregistered?", callUUID);
		this.send(callEntity.serviceUUID, report);
	}

	private void send(UUID sendKey, Report report) {
		ReportRecord reportRecord = ReportRecord.of(sendKey, report);
		this.reports.onNext(reportRecord);
	}

}
