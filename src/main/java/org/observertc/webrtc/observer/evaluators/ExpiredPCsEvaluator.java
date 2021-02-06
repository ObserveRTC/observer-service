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
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.monitors.ObserverMetrics;
import org.observertc.webrtc.observer.repositories.RepositoryProvider;
import org.observertc.webrtc.observer.tasks.CallDetailsFinderTask;
import org.observertc.webrtc.observer.tasks.CallFinisherTask;
import org.observertc.webrtc.observer.tasks.PeerConnectionDetacherTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.observertc.webrtc.schemas.reports.DetachedPeerConnection;
import org.observertc.webrtc.schemas.reports.FinishedCall;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.*;

import static org.observertc.webrtc.observer.evaluators.Pipeline.REPORT_VERSION_NUMBER;

@Singleton
public class ExpiredPCsEvaluator implements Consumer<Map<UUID, PCState>> {

	private static final Logger logger = LoggerFactory.getLogger(ExpiredPCsEvaluator.class);
	private final PublishSubject<Report> reports = PublishSubject.create();

	private final FlawMonitor flawMonitor;
	private final TasksProvider tasksProvider;

	@Inject
	ObserverMetrics observerMetrics;

	@Inject
	ObserverConfig.EvaluatorsConfig config;

	@PostConstruct
	void setup() {

	}

	public ExpiredPCsEvaluator(
			MonitorProvider monitorProvider,
			RepositoryProvider repositoryProvider,
			TasksProvider tasksProvider) {
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
		this.tasksProvider = tasksProvider;
	}


	public Observable<Report> getObservableReports() {
		return this.reports;
	}

	@Override
	public void accept(@NonNull Map<UUID, PCState> expiredPCStates) throws Throwable{
		if (expiredPCStates.size() < 1) {
			return;
		}
		Queue<PCState> pcStates = new LinkedList<>();
		pcStates.addAll(expiredPCStates.values());
		while (!pcStates.isEmpty()) {
			PCState pcState = pcStates.poll();
			CallDetailsFinderTask callDetailsFinderTask = this.tasksProvider.getCallDetailsFinderTask();
			callDetailsFinderTask
					.forPcUUID(pcState.peerConnectionUUID)
					.collectPeerConnectionUUIDs(true)
					.collectSynchronizationSourceKeys(false)
					.withFlawMonitor(this.flawMonitor)
					.withLogger(logger)
					.execute()
			;

			if (!callDetailsFinderTask.succeeded()) {
				continue;
			}
			Optional<CallDetailsFinderTask.Result> callDetailsHolder = callDetailsFinderTask.getResult();
			if (!callDetailsHolder.isPresent()) {
				logger.warn("Cannot find a call for the peer connection {}", pcState);
				continue;
			}
			CallDetailsFinderTask.Result callDetails = callDetailsHolder.get();

			if (!this.detachPeerConnection(pcState)) {
				logger.warn("Detach process has failed {}", pcState);
				continue;
			}

			if (!this.config.impairablePCsCallName.equals(pcState.callName)) {
				logger.info("Peer Connection {} is unregistered to Call {}.", pcState.peerConnectionUUID, callDetails.callUUID);
			}

			if (!callDetails.peerConnectionUUIDs.contains(pcState.peerConnectionUUID)) {
				logger.warn("Peer connection {} in call details has not found {}", pcState.peerConnectionUUID, ObjectToString.toString(callDetails));
			}
			if (1 < callDetails.peerConnectionUUIDs.size()) {
				continue;
			}

			if (!this.finnishCall(callDetails.callUUID, pcState.updated)) {
				logger.warn("Task to finish call {} is finished", callDetails.callUUID);
				continue;
			}
			if (!this.config.impairablePCsCallName.equals(callDetails.callEntity.callName)) {
				logger.info("Call is unregistered with a uuid: {}", callDetails.callUUID);
			}

		}
	}

	private boolean detachPeerConnection(@NotNull PCState pcState) {
		PeerConnectionDetacherTask task = this.tasksProvider.getPeerConnectionDetacherTask();
		task
				.forPeerConnectionUUID(pcState.peerConnectionUUID)
		;

		if (!task.execute().succeeded()) {
			return false;
		}

		PeerConnectionEntity entity = task.getResult();
		if (Objects.isNull(entity)) {
			this.flawMonitor.makeLogEntry()
					.withLogLevel(Level.WARN)
					.withMessage("Entity for PCState is null. {} report will not send. PCState: {}", ReportType.DETACHED_PEER_CONNECTION, pcState)
					.withLogger(logger)
					.complete();
			return false;
		}

		try {
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
			this.reports.onNext(report);
			this.observerMetrics.incrementDetachedPCs(entity.serviceName, entity.mediaUnitId);
		} finally {
			return true;
		}
	}

	private boolean finnishCall(UUID callUUID, Long timestamp) {
		CallFinisherTask task = this.tasksProvider.getCallFinisherTask();
		task
				.forCallEntity(callUUID)
				.withLogger(logger)
				.withFlawMonitor(flawMonitor)
				.execute()
		;
		if (!task.succeeded()) {
			return false;
		}
		CallEntity callEntity = task.getResult();
		if (Objects.isNull(callEntity)) {
			return false;
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
		this.reports.onNext(report);
		this.observerMetrics.incrementFinishedCall(callEntity.serviceName);
		return true;
	}
}
