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
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.monitors.ObserverMetrics;
import org.observertc.webrtc.observer.repositories.CallsRepository;
import org.observertc.webrtc.schemas.reports.DetachedPeerConnection;
import org.observertc.webrtc.schemas.reports.FinishedCall;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@Inject
	ObserverMetrics observerMetrics;

	@Inject
	ObserverConfig.EvaluatorsConfig config;

	@Inject
	CallsRepository calls;

	@PostConstruct
	void setup() {

	}

	public ExpiredPCsEvaluator(MonitorProvider monitorProvider) {
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
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

			PeerConnectionEntity pcEntity = this.detachPeerConnection(pcState);
			logger.info("Peer Connection {} is unregistered to Call {}.", pcState, pcEntity.callUUID);

			if (Objects.isNull(pcEntity)) {
				logger.warn("Detach process has failed {}", pcState);
				continue;
			}

			Optional<CallEntity> callEntityHolder = this.calls.findCall(pcEntity.callUUID);
			if (!callEntityHolder.isPresent()) {
				logger.warn("Peer connection {} does not belong to any call", pcEntity);
				continue;
			}
			CallEntity callEntity = callEntityHolder.get();

			if (0 < callEntity.peerConnections.size()) {
				continue;
			}

			this.finnishCall(callEntity.call.callUUID, pcState.updated);
			logger.info("Call is unregistered with a uuid: {}", callEntity);

		}
	}

	private PeerConnectionEntity detachPeerConnection(@NotNull PCState pcState) {

		PeerConnectionEntity pcEntity = this.calls.removePeerConnection(pcState.peerConnectionUUID);
		try {
			Object payload = DetachedPeerConnection.newBuilder()
					.setMediaUnitId(pcEntity.peerConnection.mediaUnitId)
					.setCallName(pcEntity.peerConnection.callName)
					.setTimeZoneId(pcState.timeZoneId)
					.setCallUUID(pcEntity.callUUID.toString())
					.setUserId(pcEntity.peerConnection.providedUserName)
					.setBrowserId(pcEntity.peerConnection.browserId)
					.setPeerConnectionUUID(pcEntity.peerConnection.peerConnectionUUID.toString())
					.build();

			Report report = Report.newBuilder()
					.setVersion(REPORT_VERSION_NUMBER)
					.setServiceUUID(pcEntity.serviceUUID.toString())
					.setServiceName(pcEntity.peerConnection.serviceName)
					.setMarker(pcEntity.peerConnection.marker)
					.setType(ReportType.DETACHED_PEER_CONNECTION)
					.setTimestamp(pcState.updated)
					.setPayload(payload)
					.build();
			this.reports.onNext(report);
			this.observerMetrics.incrementDetachedPCs(pcEntity.peerConnection.serviceName, pcEntity.peerConnection.mediaUnitId);
		} finally {
			return pcEntity;
		}
	}

	private void finnishCall(UUID callUUID, Long timestamp) {

		CallEntity callEntity = this.calls.removeCall(callUUID);

		FinishedCall payload = FinishedCall.newBuilder()
				.setCallName(callEntity.call.callName)
				.setCallUUID(callEntity.call.callUUID.toString())
				.build();
		Report report = Report.newBuilder()
				.setVersion(REPORT_VERSION_NUMBER)
				.setServiceUUID(callEntity.call.serviceUUID.toString())
				.setServiceName(callEntity.call.serviceName)
				.setMarker(callEntity.call.marker)
				.setType(ReportType.FINISHED_CALL)
				.setTimestamp(timestamp)
				.setPayload(payload)
				.build();
		this.reports.onNext(report);
		this.observerMetrics.incrementFinishedCall(callEntity.call.serviceName);
	}
}
