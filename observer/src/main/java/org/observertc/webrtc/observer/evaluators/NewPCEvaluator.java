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

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.jooq.lambda.tuple.Tuple2;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.tasks.CallFinderTask;
import org.observertc.webrtc.observer.tasks.CallInitializerTask;
import org.observertc.webrtc.observer.tasks.PeerConnectionJoinerTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.observertc.webrtc.schemas.reports.InitiatedCall;
import org.observertc.webrtc.schemas.reports.JoinedPeerConnection;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

@Prototype
public class NewPCEvaluator implements Observer<Map<UUID, PCState>> {

	private static final Logger logger = LoggerFactory.getLogger(NewPCEvaluator.class);

	private final PublishSubject<Tuple2<UUID, Report>> reports = PublishSubject.create();
	private Disposable subscription;

	private final FlawMonitor flawMonitor;
	private final TasksProvider tasksProvider;

	public NewPCEvaluator(
			MonitorProvider monitorProvider,
			TasksProvider tasksProvider
	) {
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass().getSimpleName());
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


	public Subject<Tuple2<UUID, Report>> getReports() {
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
		Set<UUID> callUUIDs = new HashSet<>();
		AtomicReference<Throwable> error = new AtomicReference<>(null);
		try (CallFinderTask task = this.tasksProvider.getCallFinderTask()) {
			task.forServiceUUID(pcState.serviceUUID)
					.forCallName(pcState.callName)
					.forSSRCs(pcState.SSRCs)
					.withMultipleResultsAllowed(false)
					.perform()
					.subscribe(callUUIDs::add, error::set);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Unhandled error occurred by executing {} ", CallFinderTask.class.getSimpleName())
					.complete();
			return Optional.empty();
		}
		if (error.get() != null) {
			this.flawMonitor.makeLogEntry()
					.withException(error.get())
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Error occurred by executing {} ", CallFinderTask.class.getSimpleName())
					.complete();
			return Optional.empty();
		}
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
		AtomicReference<Throwable> error = new AtomicReference<>(null);
		AtomicReference<UUID> callUUIDReference = new AtomicReference(null);
		try (CallInitializerTask task = this.tasksProvider.provideCallInitializerTask()) {
			task.forCallEntity(callEntity)
					.forSSRCs(pcState.SSRCs)
					.perform()
					.subscribe(callUUIDReference::set, error::set);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Unhandled error occurred by executing {} ", CallInitializerTask.class.getSimpleName())
					.complete();
			return Optional.empty();
		}
		if (error.get() != null) {
			this.flawMonitor.makeLogEntry()
					.withException(error.get())
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Error occurred by executing {} ", CallInitializerTask.class.getSimpleName())
					.complete();
			return Optional.empty();
		}
		UUID callUUID = callUUIDReference.get();
		if (callUUID == null) {
			return Optional.empty();
		}
		Object payload = InitiatedCall.newBuilder()
				.setCallUUID(callUUID.toString())
				.setCallName(callEntity.callName)
				.build();
		Report report = Report.newBuilder()
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
				pcState.browserID,
				pcState.userId,
				pcState.timeZoneID,
				pcState.created,
				pcState.updated,
				null,
				pcState.marker
		);
		AtomicReference<Throwable> error = new AtomicReference<>(null);
		AtomicBoolean performed = new AtomicBoolean(false);
		try (PeerConnectionJoinerTask task = this.tasksProvider.providePeerConnectionJoinerTask()) {
			task.forEntity(pcEntity)
					.perform()
					.subscribe(() -> performed.set(true), error::set);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(error.get())
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Error occurred by executing {} ", CallInitializerTask.class.getSimpleName())
					.complete();
			return false;
		}
		if (error.get() != null) {
			logger.warn("Exception happened during execution", error.get());
			return false;
		}

		JoinedPeerConnection joinedPC = JoinedPeerConnection.newBuilder()
				.setBrowserId(pcEntity.browserId)
				.setMediaUnitId(pcEntity.mediaUnitId)
				.setCallUUID(pcEntity.callUUID.toString())
				.setPeerConnectionUUID(pcEntity.peerConnectionUUID.toString())
				.build();

		Report report = Report.newBuilder()
				.setServiceUUID(pcEntity.serviceUUID.toString())
				.setServiceName(pcEntity.serviceName)
				.setMarker(pcEntity.marker)
				.setType(ReportType.JOINED_PEER_CONNECTION)
				.setTimestamp(pcEntity.joined)
				.setPayload(joinedPC)
				.build();
		this.send(pcEntity.peerConnectionUUID, report);
		return performed.get();
	}

	private void send(UUID sendKey, Report report) {
		Tuple2<UUID, Report> tuple = new Tuple2<>(sendKey, report);
		try {
			this.reports.onNext(tuple);
		} catch (Exception ex) {
			logger.error("error", ex);
		}

	}
}
