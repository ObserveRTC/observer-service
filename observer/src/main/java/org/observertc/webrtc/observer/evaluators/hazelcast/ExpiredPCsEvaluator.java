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

package org.observertc.webrtc.observer.evaluators.hazelcast;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.jooq.lambda.tuple.Tuple2;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.CallPeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.tasks.CallFinisherTask;
import org.observertc.webrtc.observer.tasks.PeerConnectionDetacherTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.observertc.webrtc.schemas.reports.DetachedPeerConnection;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

@Prototype
public class ExpiredPCsEvaluator implements Observer<Map<UUID, PCState>> {

	private static final Logger logger = LoggerFactory.getLogger(ExpiredPCsEvaluator.class);
	private final PublishSubject<Tuple2<UUID, Report>> reports = PublishSubject.create();

	private final FlawMonitor flawMonitor;
	private final CallPeerConnectionsRepository callPeerConnectionsRepository;
	private final TasksProvider tasksProvider;

	public ExpiredPCsEvaluator(
			MonitorProvider monitorProvider,
			RepositoryProvider repositoryProvider,
			TasksProvider tasksProvider) {
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass().getSimpleName());
		this.tasksProvider = tasksProvider;
		this.callPeerConnectionsRepository = repositoryProvider.getCallPeerConnectionsRepository();
	}

	public Subject<Tuple2<UUID, Report>> getReportsSubject() {
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
					.withMessage("Unhandled error occurred by executing {} ", PeerConnectionDetacherTask.class.getSimpleName())
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
					.withMessage("{} has not been found for {}", PeerConnectionEntity.class.getSimpleName(), pcState)
					.complete();
			return;
		}
		PeerConnectionEntity entity = entityHolder.get();
		Object payload = DetachedPeerConnection.newBuilder()
				.setMediaUnitId(entity.mediaUnitId)
				.setCallName(entity.callName)
				.setCallUUID(entity.callUUID.toString())
				.setUserId(entity.providedUserName)
				.setBrowserId(entity.browserId)
				.setPeerConnectionUUID(entity.peerConnectionUUID.toString())
				.build();

		Report report = Report.newBuilder()
				.setServiceUUID(entity.serviceUUID.toString())
				.setServiceName(entity.serviceName)
				.setMarker(entity.marker)
				.setType(ReportType.DETACHED_PEER_CONNECTION)
				.setTimestamp(pcState.updated)
				.setPayload(payload)
				.build();
		this.send(entity.peerConnectionUUID, report);

		Set<UUID> remainingPCUUIIDs = this.callPeerConnectionsRepository.find(entity.callUUID).stream().collect(Collectors.toSet());
		if (0 < remainingPCUUIIDs.size()) {
			return;
		}
		AtomicReference<CallEntity> callEntityHolder = new AtomicReference<>(null);
		error.set(null);
		try (CallFinisherTask task = this.tasksProvider.provideCallFinisherTask()) {
			task.forCallEntity(entity.callUUID)
					.perform()
					.subscribe(callEntityHolder::set, error::set);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("Unhandled error occured by executing {}", CallFinisherTask.class.getSimpleName())
					.complete();
		}

	}

	private void send(UUID sendKey, Report report) {
		Tuple2<UUID, Report> tuple = new Tuple2<>(sendKey, report);
		this.reports.onNext(tuple);
	}

}
