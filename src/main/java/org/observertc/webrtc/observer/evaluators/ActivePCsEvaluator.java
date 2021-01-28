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
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.Task;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.monitors.ObserverMetrics;
import org.observertc.webrtc.observer.tasks.*;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.observertc.webrtc.observer.evaluators.Pipeline.REPORT_VERSION_NUMBER;


/**
 * Filter out every peer connection from the {@link PCState}s, which already exists,
 * register new SSRCs, if it occurs, and forwards all {@link PCState}s which
 * contains unkown peer connection UUID
 */
@Singleton
public class ActivePCsEvaluator implements Consumer<Map<UUID, PCState>> {

	private static final Logger logger = LoggerFactory.getLogger(ActivePCsEvaluator.class);

	private final Subject<Report> reports = PublishSubject.create();
	private final FlawMonitor flawMonitor;
	private final TasksProvider tasksProvider;

	@Inject
	ObserverMetrics observerMetrics;

	@Inject
	ObserverConfig.EvaluatorsConfig config;

	public ActivePCsEvaluator(
			MonitorProvider monitorProvider,
			TasksProvider tasksProvider
	) {

		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
		this.tasksProvider = tasksProvider;
	}

	public Observable<Report> getObservableReports() {
		return this.reports;
	}

	@Override
	public void accept(Map<UUID, PCState> peerConnectionStates) throws Throwable {
		if (peerConnectionStates.size() < 1) {
			return;
		}

		Map<UUID, PCState> existsPeerConnections = new HashMap<>();
		Map<UUID, PCState> newPeerConnections = new HashMap<>();
		Task<Collection<PeerConnectionEntity>> task = this.tasksProvider.getPeerConnectionFinderTask()
				.addPCUUIDs(peerConnectionStates.keySet())
				.withLogger(logger)
				.withFlawMonitor(this.flawMonitor)
				;

		if (!task.execute().succeeded()) {
			return;
		}

		Map<UUID, PeerConnectionEntity> activePCs = task.getResultOrDefault(new HashSet<>())
				.stream().collect(Collectors.toMap(e -> e.peerConnectionUUID, Function.identity()));
		Iterator<PCState> it = peerConnectionStates.values().iterator();

		while(it.hasNext()) {
			PCState pcState = it.next();
			if (activePCs.containsKey(pcState.peerConnectionUUID)) {
				existsPeerConnections.put(pcState.peerConnectionUUID, pcState);
			} else {
				newPeerConnections.put(pcState.peerConnectionUUID, pcState);
			}
		}

		if (0 < existsPeerConnections.size()) {
			this.update(existsPeerConnections);
		}

		if (0 < newPeerConnections.size()) {
			this.add(newPeerConnections);
		}
	}

	private void update(@NonNull Map<UUID, PCState> peerConnectionStates) {
		if (peerConnectionStates.size() < 1) {
			return;
		}
		AtomicBoolean streamIsAdded = new AtomicBoolean(false);
		PeerConnectionsUpdaterTask task = this.tasksProvider.getPeerConnectionsUpdaterTask();
		peerConnectionStates.values().stream().forEach(
				pcState -> pcState.SSRCs.stream().forEach(ssrc -> {
					task.addStream(
							pcState.serviceUUID,
							pcState.peerConnectionUUID,
							ssrc);
					streamIsAdded.set(true);
				})
		);
		task.withLogger(logger)
				.withFlawMonitor(this.flawMonitor)
				.withExceptionMessage(() -> MessageFormatter.format("Cannot update peer connections. pcstates: {}", ObjectToString.toString(peerConnectionStates)).getMessage())
				.withLogger(logger);

		if (!streamIsAdded.get()) {
			return;
		}
		if (!task.execute().succeeded()) {
			return;
		}
	}

	private void add(Map<UUID, PCState> newPeerConnections) {
		Queue<PCState> pcStates = new LinkedList<>();
		pcStates.addAll(newPeerConnections.values());
		Set<UUID> pcDidNotHaveCalls = new HashSet<>();
		while (!pcStates.isEmpty()) {
			PCState pcState = pcStates.poll();
			SSRCEntityFinderTask task = this.tasksProvider.getSSRCFinderTask();
			task.forServiceUUID(pcState.serviceUUID)
					.forCallName(pcState.callName)
					.forSSRCs(pcState.SSRCs)
					.withMultipleResultsAllowed(false)
					.withFlawMonitor(this.flawMonitor)
					.withLogger(logger)
					.withMaxRetry(3)
			;
			if (!task.execute().succeeded()) {
				continue;
			}

			Set<UUID> callUUIDs = task.getResult();

			if (0 < callUUIDs.size()) {
				UUID callUUID = callUUIDs.stream().findFirst().get();
				this.addNewPeerConnection(callUUID, pcState);
				continue;
			}
			if (pcDidNotHaveCalls.contains(pcState.peerConnectionUUID)) {
				logger.warn("PCState {} has already seen in the process at newPeerConnections and tried to registered to a enw call. Now this state is dropped", pcState);
				continue;
			}

			if (!this.addNewCall(pcState)) {
				logger.warn("Call has not been added for pcUUID {}", pcState.peerConnectionUUID);
				continue;
			}
			pcDidNotHaveCalls.add(pcState.peerConnectionUUID);
			pcStates.add(pcState);
		}
	}

	private boolean addNewPeerConnection(UUID callUUID, PCState pcState) {

		PeerConnectionEntity pcEntity = PeerConnectionEntity.of(
				pcState.serviceUUID,
				pcState.serviceName,
				pcState.mediaUnitID,
				callUUID,
				pcState.callName,
				pcState.peerConnectionUUID,
				pcState.userId,
				pcState.browserId,
				pcState.timeZoneId,
				pcState.created,
				pcState.marker
		);
		pcEntity.SSRCs.addAll(pcState.SSRCs);
		if (!this.config.impairablePCsCallName.equals(pcEntity.callName)) {
			logger.info("Peer Connection {} is registered to Call {}.", pcState.peerConnectionUUID, callUUID);
		}

		PeerConnectionJoinerTask task = this.tasksProvider.getPeerConnectionJoinerTask();
		task.forEntity(pcEntity)
				.withLogger(logger)
				.withFlawMonitor(this.flawMonitor)
				.execute()
		;

		if (!task.succeeded()) {
			return false;
		}

		try {
			JoinedPeerConnection joinedPC = JoinedPeerConnection.newBuilder()
					.setBrowserId(pcEntity.browserId)
					.setMediaUnitId(pcEntity.mediaUnitId)
					.setTimeZoneId(pcState.timeZoneId)
					.setCallUUID(pcEntity.callUUID.toString())
					.setPeerConnectionUUID(pcEntity.peerConnectionUUID.toString())
					.build();

			Report report = Report.newBuilder()
					.setVersion(REPORT_VERSION_NUMBER)
					.setServiceUUID(pcEntity.serviceUUID.toString())
					.setServiceName(pcEntity.serviceName)
					.setMarker(pcEntity.marker)
					.setType(ReportType.JOINED_PEER_CONNECTION)
					.setTimestamp(pcEntity.joined)
					.setPayload(joinedPC)
					.build();

			if (pcState.SSRCs.size() < 1) {
				this.reportNoSSRC(pcEntity);
			}

			this.send(pcEntity.peerConnectionUUID, report);
			this.observerMetrics.incrementJoinedPCs(pcEntity.serviceName, pcEntity.mediaUnitId);
		} finally {
			return true;
		}

	}

	private boolean addNewCall(PCState pcState) {
		CallEntity callEntity = CallEntity.of(
				UUID.randomUUID(),
				pcState.serviceUUID,
				pcState.serviceName,
				pcState.created,
				pcState.callName,
				pcState.marker
		);
		CallInitializerTask task = this.tasksProvider.getCallInitializerTask();
		task
				.forCallEntity(callEntity)
				.forSSRCs(pcState.SSRCs)
				.withFlawMonitor(this.flawMonitor)
				.withLogger(logger)
		;
		if (!task.execute().succeeded()) {
			return false;
		}

		UUID callUUID = task.getResult();
		if (callUUID == null) {
			return false;
		}
		if (!this.config.impairablePCsCallName.equals(callEntity.callName)) {
			logger.info("Call is registered with a uuid: {}", callUUID);
		}

		try {
			Object payload = InitiatedCall.newBuilder()
					.setCallUUID(callUUID.toString())
					.setCallName(callEntity.callName)
					.build();
			Report report = Report.newBuilder()
					.setVersion(REPORT_VERSION_NUMBER)
					.setServiceUUID(callEntity.serviceUUID.toString())
					.setServiceName(callEntity.serviceName)
					.setMarker(callEntity.marker)
					.setType(ReportType.INITIATED_CALL)
					.setTimestamp(callEntity.initiated)
					.setPayload(payload)
					.build();
			this.send(callEntity.serviceUUID, report);
			this.observerMetrics.incrementInitiatedCall(callEntity.serviceName);
		} finally {
			return true;
		}
	}

	private void reportNoSSRC(PeerConnectionEntity pcEntity) {
		String message = MessageFormatter.format(
				"No SSRC has been found for peer connection entity {}",
				pcEntity
		).getMessage();

		ObserverEventReport observerEventReport = ObserverEventReport
				.newBuilder()
				.setUserId(pcEntity.providedUserName)
				.setBrowserId(pcEntity.browserId)
				.setMediaUnitId(pcEntity.mediaUnitId)
				.setCallName(pcEntity.callName)
				.setPeerConnectionUUID(pcEntity.peerConnectionUUID.toString())
				.setEventType("NoSSRC")
				.setMessage(message)
				.build();

		Report report = Report.newBuilder()
				.setVersion(REPORT_VERSION_NUMBER)
				.setServiceUUID(pcEntity.serviceUUID.toString())
				.setServiceName(pcEntity.serviceName)
				.setMarker(pcEntity.marker)
				.setType(ReportType.OBSERVER_EVENT)
				.setTimestamp(pcEntity.joined)
				.setPayload(observerEventReport)
				.build();

		this.send(pcEntity.peerConnectionUUID, report);
	}

	private void send(UUID sendKey, Report report) {
		try {
			this.reports.onNext(report);
		} catch (Exception ex) {
			logger.error("Unexpected error occrred", ex);
		}
	}
}
