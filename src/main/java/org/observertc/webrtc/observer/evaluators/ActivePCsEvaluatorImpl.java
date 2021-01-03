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
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.tasks.PeerConnectionsUpdaterTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Filter out every peer connection from the {@link PCState}s, which already exists,
 * register new SSRCs, if it occurs, and forwards all {@link PCState}s which
 * contains unkown peer connection UUID
 */
@Prototype
public class ActivePCsEvaluatorImpl implements ActivePCsEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ActivePCsEvaluatorImpl.class);

	private final PublishSubject<Map<UUID, PCState>> newPeerConnections = PublishSubject.create();
	private Disposable subscription;

	private final FlawMonitor flawMonitor;
	private final TasksProvider tasksProvider;
	private final PeerConnectionsRepository PCsRepository;

	public ActivePCsEvaluatorImpl(
			MonitorProvider monitorProvider,
			TasksProvider tasksProvider,
			RepositoryProvider repositoryProvider
	) {

		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
		this.tasksProvider = tasksProvider;
		this.PCsRepository = repositoryProvider.getPeerConnectionsRepository();
	}

	public PublishSubject<Map<UUID, PCState>> getNewPeerConnectionsSubject() {
		return this.newPeerConnections;
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		this.subscription = d;
	}

	@Override
	public void onNext(@NonNull Map<UUID, PCState> peerConnectionStates) {
		if (this.subscription != null && this.subscription.isDisposed()) {
			logger.warn("Updated PCStates arrived, however the subscription for downstream is disposed");
			return;
		}
		if (peerConnectionStates.size() < 1) {
			return;
		}
		Map<UUID, PCState> existsPeerConnections = new HashMap<>();
		Map<UUID, PCState> newPeerConnections = new HashMap<>();
		try {
			Map<UUID, PeerConnectionEntity> activePCs;
			activePCs = this.PCsRepository.findAll(
					peerConnectionStates.keySet()
			);
			Iterator<PCState> it = peerConnectionStates.values().iterator();
			for (; it.hasNext(); ) {
				PCState pcState = it.next();
				if (activePCs.containsKey(pcState.peerConnectionUUID)) {
					existsPeerConnections.put(pcState.peerConnectionUUID, pcState);
				} else {
					newPeerConnections.put(pcState.peerConnectionUUID, pcState);
				}
			}
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Error during peer connections fetching")
					.complete();
			return;
		}
		try {
			if (0 < existsPeerConnections.size()) {
				this.update(existsPeerConnections);
			}
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Error happened during update")
					.complete();
		}

		this.newPeerConnections.onNext(newPeerConnections);
	}

	@Override
	public void onError(@NonNull Throwable e) {
		logger.error("Error is reported in the observer pipeline", e);
	}

	@Override
	public void onComplete() {
		logger.info("onComplete event is called");
	}

	private void update(@NonNull Map<UUID, PCState> peerConnectionStates) {
		AtomicReference<Throwable> error = new AtomicReference<>(null);
		AtomicBoolean performed = new AtomicBoolean(false);
		try (PeerConnectionsUpdaterTask task = this.tasksProvider.providePeerConnectionsUpdaterTask()) {
			peerConnectionStates.values().stream().forEach(
					pcState -> pcState.SSRCs.stream().forEach(ssrc ->
							task.addStream(
									pcState.serviceUUID,
									pcState.peerConnectionUUID,
									ssrc)
					)
			);
			task.perform()
					.subscribe(() -> performed.set(true), error::set);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Uncatched error happened during update task execution")
					.complete();
			return;
		}
		if (error.get() != null) {
			this.flawMonitor.makeLogEntry()
					.withException(error.get())
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("Error happened during update task execution")
					.complete();
			return;
		}
		if (!performed.get()) {
			this.flawMonitor.makeLogEntry()
					.withException(error.get())
					.withLogger(logger)
					.withLogLevel(Level.ERROR)
					.withMessage("PCState update task has not been completed successfully")
					.complete();
			return;
		}
	}
}
