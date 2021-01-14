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
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.tasks.PeerConnectionsFinderTask;
import org.observertc.webrtc.observer.tasks.PeerConnectionsUpdaterTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Filter out every peer connection from the {@link PCState}s, which already exists,
 * register new SSRCs, if it occurs, and forwards all {@link PCState}s which
 * contains unkown peer connection UUID
 */
@Singleton
public class ActivePCsEvaluator implements Observer<Map<UUID, PCState>> {

	private static final Logger logger = LoggerFactory.getLogger(ActivePCsEvaluator.class);

	private final PublishSubject<Map<UUID, PCState>> newPeerConnections = PublishSubject.create();
	private Disposable subscription;

	private final FlawMonitor flawMonitor;
	private final TasksProvider tasksProvider;

	public ActivePCsEvaluator(
			MonitorProvider monitorProvider,
			TasksProvider tasksProvider
	) {

		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
		this.tasksProvider = tasksProvider;
	}

	public Observable<Map<UUID, PCState>> observableNewPeerConnections() {
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
		PeerConnectionsFinderTask task = this.tasksProvider.providePeerConnectionFinderTask()
				.addPCUUIDs(peerConnectionStates.keySet());

		task
				.withLogger(logger)
				.withFlawMonitor(this.flawMonitor)
				.execute();

		if (!task.succeeded()) {
			return;
		}

		Map<UUID, PeerConnectionEntity> activePCs = task.getResultOrDefault(new HashSet<>())
				.stream().collect(Collectors.toMap(e -> e.peerConnectionUUID, Function.identity()));
		Iterator<PCState> it = peerConnectionStates.values().iterator();
		for (; it.hasNext(); ) {
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
		if (peerConnectionStates.size() < 1) {
			return;
		}
		AtomicBoolean streamIsAdded = new AtomicBoolean(false);
		PeerConnectionsUpdaterTask task = this.tasksProvider.providePeerConnectionsUpdaterTask();
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
}
