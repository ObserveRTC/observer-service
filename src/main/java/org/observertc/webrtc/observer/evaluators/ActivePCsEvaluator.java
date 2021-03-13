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
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.ObserverMetrics;
import org.observertc.webrtc.observer.repositories.CallsRepository;
import org.observertc.webrtc.observer.repositories.tasks.UpdatePCSSRCsTask;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;
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

	@Inject
	ObserverMetrics observerMetrics;

	@Inject
	ObserverConfig.EvaluatorsConfig config;

	@Inject
	CallsRepository calls;

	@Inject
	Provider<UpdatePCSSRCsTask> updatePCsTaskProvider;

	public ActivePCsEvaluator() {
	}

	public Observable<Report> getObservableReports() {
		return this.reports;
	}

	@Override
	public void accept(Map<UUID, PCState> newPeerConnections) throws Throwable {
		if (newPeerConnections.size() < 1) {
			return;
		}

		Map<UUID, PCState> existsPeerConnections = this.calls.filterExistingPeerConnectionUUIDs(newPeerConnections.keySet())
				.stream().map(newPeerConnections::remove).collect(Collectors.toMap(pcState -> pcState.peerConnectionUUID, Function.identity()));


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
		var task = updatePCsTaskProvider.get();
		for (PCState pcState : peerConnectionStates.values()) {
			task.withPeerConnectionSSRCs(pcState.serviceUUID,
					pcState.peerConnectionUUID,
					pcState.SSRCs
			);
		}
		task.execute();
	}

	private void add(Map<UUID, PCState> newPeerConnections) {
		Queue<PCState> pcStates = new LinkedList<>();
		pcStates.addAll(newPeerConnections.values());
		while (!pcStates.isEmpty()) {
			PCState pcState = pcStates.poll();
			Optional<CallEntity> maybeCallEntity = this.calls.findCall(pcState.serviceUUID, pcState.callName, pcState.SSRCs);
			CallEntity callEntity;
			if (maybeCallEntity.isPresent()) {
				callEntity = maybeCallEntity.get();
			} else {
				callEntity = this.addNewCall(pcState);
			}

			if (Objects.isNull(callEntity)) {
				logger.warn("No Call Entity has been added");
				return;
			}

			PeerConnectionEntity pcEntity = this.addNewPeerConnection(callEntity.call.callUUID, pcState);
		}
	}

	private PeerConnectionEntity addNewPeerConnection(UUID callUUID, PCState pcState) {

		PeerConnectionEntity pcEntity = PeerConnectionEntity.builder()
				.withPCDTO(PeerConnectionDTO.of(
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
						pcState.marker)
				)
				.withSSRCs(pcState.SSRCs)
				.withRemoteIPs(pcState.remoteAddresses)
				.build();

		if (!this.config.impairablePCsCallName.equals(pcEntity.peerConnection.callName)) {
			logger.info("Peer Connection {} is registered to Call {}.", pcState.peerConnectionUUID, callUUID);
		}

		pcEntity = this.calls.addPeerConnection(pcEntity);

		try {
			JoinedPeerConnection joinedPC = JoinedPeerConnection.newBuilder()
					.setBrowserId(pcEntity.peerConnection.browserId)
					.setMediaUnitId(pcEntity.peerConnection.mediaUnitId)
					.setTimeZoneId(pcState.timeZoneId)
					.setCallUUID(pcEntity.callUUID.toString())
					.setPeerConnectionUUID(pcEntity.peerConnection.peerConnectionUUID.toString())
					.build();

			Report report = Report.newBuilder()
					.setVersion(REPORT_VERSION_NUMBER)
					.setServiceUUID(pcEntity.serviceUUID.toString())
					.setServiceName(pcEntity.peerConnection.serviceName)
					.setMarker(pcEntity.peerConnection.marker)
					.setType(ReportType.JOINED_PEER_CONNECTION)
					.setTimestamp(pcEntity.peerConnection.joined)
					.setPayload(joinedPC)
					.build();

			if (pcState.SSRCs.size() < 1) {
				this.reportNoSSRC(pcEntity);
			}

			this.send(pcEntity.peerConnection.peerConnectionUUID, report);
			this.observerMetrics.incrementJoinedPCs(pcEntity.peerConnection.serviceName, pcEntity.peerConnection.mediaUnitId);
		} finally {
			return pcEntity;
		}

	}

	private CallEntity addNewCall(PCState pcState) {
		CallEntity callEntity;
		try {
			CallEntity candidate = CallEntity.builder()
					.withCallDTO(CallDTO.of(
							UUID.randomUUID(),
							pcState.serviceUUID,
							pcState.serviceName,
							pcState.created,
							pcState.callName,
							pcState.marker
					))
					.build();
			callEntity = this.calls.addCall(candidate);
		} catch (Exception ex) {
			logger.error("Unexpected exception occurred", ex);
			return null;
		}

		if (Objects.isNull(callEntity)) {
			logger.warn("CallEntity is null {}", pcState);
			return null;
		}

		if (!this.config.impairablePCsCallName.equals(callEntity.call.callName)) {
			logger.info("Call is registered with a uuid: {}", callEntity.call.callUUID);
		}

		try {
			Object payload = InitiatedCall.newBuilder()
					.setCallUUID(callEntity.call.callUUID.toString())
					.setCallName(callEntity.call.callName)
					.build();
			Report report = Report.newBuilder()
					.setVersion(REPORT_VERSION_NUMBER)
					.setServiceUUID(callEntity.call.serviceUUID.toString())
					.setServiceName(callEntity.call.serviceName)
					.setMarker(callEntity.call.marker)
					.setType(ReportType.INITIATED_CALL)
					.setTimestamp(callEntity.call.initiated)
					.setPayload(payload)
					.build();
			this.send(callEntity.call.serviceUUID, report);
			this.observerMetrics.incrementInitiatedCall(callEntity.call.serviceName);
		} finally {
			return callEntity;
		}
	}

	private void reportNoSSRC(PeerConnectionEntity pcEntity) {
		String message = MessageFormatter.format(
				"No SSRC has been found for peer connection entity {}",
				pcEntity
		).getMessage();

		ObserverEventReport observerEventReport = ObserverEventReport
				.newBuilder()
				.setUserId(pcEntity.peerConnection.providedUserName)
				.setBrowserId(pcEntity.peerConnection.browserId)
				.setMediaUnitId(pcEntity.peerConnection.mediaUnitId)
				.setCallName(pcEntity.peerConnection.callName)
				.setPeerConnectionUUID(pcEntity.peerConnection.peerConnectionUUID.toString())
				.setEventType("NoSSRC")
				.setMessage(message)
				.build();

		Report report = Report.newBuilder()
				.setVersion(REPORT_VERSION_NUMBER)
				.setServiceUUID(pcEntity.serviceUUID.toString())
				.setServiceName(pcEntity.peerConnection.serviceName)
				.setMarker(pcEntity.peerConnection.marker)
				.setType(ReportType.OBSERVER_EVENT)
				.setTimestamp(pcEntity.peerConnection.joined)
				.setPayload(observerEventReport)
				.build();

		this.send(pcEntity.peerConnection.peerConnectionUUID, report);
	}

	private void send(UUID sendKey, Report report) {
		try {
			this.reports.onNext(report);
		} catch (Exception ex) {
			logger.error("Unexpected error occrred", ex);
		}
	}
}
