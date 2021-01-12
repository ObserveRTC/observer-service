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

import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class PCObserver implements Observer<ObservedPCS> {
	private static final Logger logger = LoggerFactory.getLogger(PCObserver.class);

	private final PublishSubject<Map<UUID, PCState>> activePCs = PublishSubject.create();
	private final PublishSubject<Map<UUID, PCState>> expiredPCs = PublishSubject.create();
	private final List<PCStates> pcStates;
	private final int peerConnectionsLength;
	private final Scheduler.Worker worker;
	private final FlawMonitor flawMonitor;

	private volatile boolean disposed = false;
	private AtomicBoolean completedHolder = new AtomicBoolean(false);
	private AtomicReference<Throwable> throwableHolder = new AtomicReference<>();
	private Disposable disposable;
	private final ObserverConfig.PCObserverConfig config;

	public PCObserver(
			@Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService,
			MonitorProvider monitorProvider,
			ObserverConfig.PCObserverConfig config
	) {
		this.config = config;
		if (config.mediaStreamsBufferNums < 1) {
			this.peerConnectionsLength = Math.max(1, Runtime.getRuntime().availableProcessors());
		} else {
			this.peerConnectionsLength = config.mediaStreamsBufferNums;
		}

		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
		this.pcStates = new ArrayList<>();
		for (int i = 0; i < this.peerConnectionsLength; ++i) {
			PCStates peerconnections = new PCStates(config.peerConnectionMaxIdleTimeInS);
			this.pcStates.add(peerconnections);
		}
		Scheduler scheduler = Schedulers.from(executorService);
		this.worker = scheduler.createWorker();

		this.worker.schedulePeriodically(
				this::send,
				this.config.mediaStreamUpdatesFlushInS,
				this.config.mediaStreamUpdatesFlushInS,
				TimeUnit.SECONDS
		);
	}

	public Observable<Map<UUID, PCState>> getExpiredPCs() {
		return this.expiredPCs;
	}

	public Observable<Map<UUID, PCState>> getActivePCs() {
		return this.activePCs;
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		this.disposable = d;
	}

	@Override
	public void onNext(ObservedPCS observedPCS) {
		if (this.disposed) {
			logger.warn("Arrived after disposale. {}", observedPCS);
			return;
		}
		UUID peerConnectionUUID = observedPCS.peerConnectionUUID;
		if (Objects.isNull(peerConnectionUUID)) {
			logger.info("No PeerConnection UUID for message {}. It will be dropped from PCObserver",
					ObjectToString.toString(observedPCS));
			return;
		}
		int index = Math.abs(peerConnectionUUID.hashCode()) % this.peerConnectionsLength;
		PCStates PCStates = this.pcStates.get(index);
		try {
			PCStates.add(peerConnectionUUID, observedPCS);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withMessage("An unexpected error happened during buffering")
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.complete();

		}

	}

	@Override
	public void onError(Throwable t) {
		if (disposable.isDisposed()) {
			RxJavaPlugins.onError(t);
			return;
		}
		if (!this.throwableHolder.compareAndSet(null, t)) {
			RxJavaPlugins.onError(t);
			return;
		}
		this.disposable.dispose();
	}

	@Override
	public void onComplete() {
		if (disposable.isDisposed()) {
			return;
		}
		this.completedHolder.compareAndSet(false, true);
		this.disposable.dispose();
	}

	private void send() {
		Map<UUID, PCState> updatedPcStates = this.pcStates
				.stream()
				.map(PCStates::retrieveActives)
				.filter(list -> !list.isEmpty())
				.flatMap(LinkedList::stream)
				.collect(Collectors.toMap(pc -> pc.peerConnectionUUID, Function.identity()));
		this.activePCs.onNext(updatedPcStates);

		Map<UUID, PCState> expiredPcStates = this.pcStates
				.stream()
				.map(PCStates::retrieveExpired)
				.filter(list -> !list.isEmpty())
				.flatMap(LinkedList::stream)
				.collect(Collectors.toMap(pc -> pc.peerConnectionUUID, Function.identity()));
		this.expiredPCs.onNext(expiredPcStates);
	}
}
