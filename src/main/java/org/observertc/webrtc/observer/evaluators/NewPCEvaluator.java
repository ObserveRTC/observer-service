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
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.observertc.webrtc.observer.ReportRecord;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.tasks.CallFinderTask;
import org.observertc.webrtc.observer.tasks.CallInitializerTask;
import org.observertc.webrtc.observer.tasks.PeerConnectionJoinerTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.observertc.webrtc.observer.ReportSink.REPORT_VERSION_NUMBER;

@Singleton
public class NewPCEvaluator implements Observer<Map<UUID, PCState>> {

	private static final Logger logger = LoggerFactory.getLogger(NewPCEvaluator.class);

	private final PublishSubject<ReportRecord> reports = PublishSubject.create();
	private Disposable subscription;

	private final FlawMonitor flawMonitor;
	private final TasksProvider tasksProvider;

	public NewPCEvaluator(
			MonitorProvider monitorProvider,
			TasksProvider tasksProvider
	) {
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
		this.tasksProvider = tasksProvider;
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		this.subscription = d;
	}

	@Override
	public void onNext(@NonNull Map<UUID, PCState> peerConnectionStates) {
		if (this.subscription != null && this.subscription.isDisposed()) {
			logger.warn("Updates arrived, however the subscription is disposed");
			return;
		}
		if (peerConnectionStates.size() < 1) {
			return;
		}
		Deque<PCState> queue = new LinkedList<>();
		peerConnectionStates.values().stream()
				.filter(Objects::nonNull)
				.forEach(queue::addLast);
		this.process(queue);
	}

	@Override
	public void onError(@NonNull Throwable e) {
		logger.error("Error is reported in the observer pipeline", e);
	}

	@Override
	public void onComplete() {
		logger.info("onComplete event is called");
	}
	
	public Observable<ReportRecord> getReports() {
		return this.reports;
	}

	private void process(Deque<PCState> PCStates) {

		while (!PCStates.isEmpty()) {
			PCState pcState = PCStates.removeFirst();
			AtomicReference<UUID> result = new AtomicReference<>(null);
			AtomicReference<Throwable> error = new AtomicReference<>(null);
			this.makeObservable(this::findCallUUID, pcState)
					.retry(3)
					.subscribe(result::set, error::set);
			if (Objects.nonNull(error.get())) {
				this.flawMonitor.makeLogEntry()
						.withLogger(logger)
						.withLogLevel(Level.ERROR)
						.withException(error.get())
						.withMessage("Error occurred during finding call uuid for {}", pcState)
						.complete();
				continue;
			}
			if (Objects.isNull(result.get())) {
				this.makeObservable(this::addNewCall, pcState)
						.retry(3)
						.subscribe(result::set, error::set);
				if (Objects.nonNull(error.get())) {
					this.flawMonitor.makeLogEntry()
							.withLogger(logger)
							.withLogLevel(Level.ERROR)
							.withException(error.get())
							.withMessage("Error occurred during initializing a call for {}", pcState)
							.complete();
					continue;
				}
				if (Objects.isNull(result.get())) {
					this.flawMonitor.makeLogEntry()
							.withLogger(logger)
							.withLogLevel(Level.WARN)
							.withMessage("For some reason we do not have a call uuid for PCState {}", pcState)
							.complete();
					continue;
				}
			}

			UUID callUUID = result.get();
			this.addNewPeerConnection(callUUID, pcState);

		}
	}

	private Observable<UUID> makeObservable(Function<PCState, Optional<UUID>> operation, PCState pcState) {
		return Observable.<UUID>create(s -> {
			Optional<UUID> re = operation.apply(pcState);
			if (!re.isPresent()) {
				s.onComplete();
				return;
			}
			s.onNext(re.get());
		});
	}

	private Optional<UUID> findCallUUID(PCState pcState) {
		CallFinderTask task = this.tasksProvider.getCallFinderTask();
		task.forServiceUUID(pcState.serviceUUID)
				.forCallName(pcState.callName)
				.forSSRCs(pcState.SSRCs)
				.withMultipleResultsAllowed(false)
				.withFlawMonitor(this.flawMonitor)
				.withLogger(logger)
				.withMaxRetry(3)
		;
		if (!task.execute().succeeded()) {
			return Optional.empty();
		}

		Set<UUID> callUUIDs = task.getResult();
		return callUUIDs.stream().findFirst();
	}

	private Optional<UUID> addNewCall(PCState pcState) {

		CallEntity callEntity = CallEntity.of(
				UUID.randomUUID(),
				pcState.serviceUUID,
				pcState.serviceName,
				pcState.created,
				pcState.callName,
				pcState.marker
		);
		CallInitializerTask task = this.tasksProvider.provideCallInitializerTask();
		task
				.forCallEntity(callEntity)
				.forSSRCs(pcState.SSRCs)
				.withFlawMonitor(this.flawMonitor)
				.withLogger(logger)
		;
		if (!task.execute().succeeded()) {
			return Optional.empty();
		}


		UUID callUUID = task.getResult();
		logger.info("Call UUID {} is registered.", callUUID);
		if (callUUID == null) {
			return Optional.empty();
		}
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
		return Optional.of(callUUID);
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
		logger.info("PC UUID {} is registered to callUUID {}.", pcState.peerConnectionUUID, callUUID);
		PeerConnectionJoinerTask task = this.tasksProvider.providePeerConnectionJoinerTask();
		task.forEntity(pcEntity)
				.withLogger(logger)
				.withFlawMonitor(this.flawMonitor)
			;

		if (!task.execute().succeeded()) {
			return false;
		}

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
		this.send(pcEntity.peerConnectionUUID, report);

		if (pcState.SSRCs.size() < 1) {
			this.reportNoSSRC(pcEntity);
		}
		return true;
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
		ReportRecord reportRecord = ReportRecord.of(sendKey, report);
		try {
			this.reports.onNext(reportRecord);
		} catch (Exception ex) {
			logger.error("error", ex);
		}
	}
}
