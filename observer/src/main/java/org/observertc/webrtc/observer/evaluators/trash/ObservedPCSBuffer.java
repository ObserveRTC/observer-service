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

package org.observertc.webrtc.observer.evaluators.trash;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
class ObservedPCSBuffer implements Observer<ObservedPCS>, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ObservedPCSBuffer.class);

	private Subject<UUID> expiredPCs;
	private volatile boolean disposed = false;
	private AtomicBoolean completedHolder = new AtomicBoolean(false);
	private AtomicReference<Throwable> throwableHolder = new AtomicReference<>();
	private final Observer<? super MediaStreamUpdate> child;
	private Disposable disposable;
	private final List<PeerConnections> peerConnections;
	private final int peerConnectionsLength;
	private final Scheduler.Worker worker;
	private final int periodInS;
	private final int pcExpirationTimeInS;

	public ObservedPCSBuffer(Observer<? super MediaStreamUpdate> child, Scheduler scheduler, int periodInS,
							 PublishSubject<UUID> expiredPeerConnections, int buffersNum, int pcExpirationTimeInS) {
		this.child = child;
		this.pcExpirationTimeInS = pcExpirationTimeInS;
		if (buffersNum < 1) {
			this.peerConnectionsLength = Math.max(1, Runtime.getRuntime().availableProcessors());
		} else {
			this.peerConnectionsLength = buffersNum;
		}
		this.peerConnections = new ArrayList<>();
		for (int i = 0; i < this.peerConnectionsLength; ++i) {
			PeerConnections peerconnections = new PeerConnections();
			this.peerConnections.add(peerconnections);
		}
		this.worker = scheduler.createWorker();
		this.periodInS = periodInS;
		this.expiredPCs = expiredPeerConnections;
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		this.disposable = d;
		this.worker.schedulePeriodically(this, this.periodInS, this.periodInS, TimeUnit.SECONDS);
	}

	@Override
	public void onNext(ObservedPCS observedPCS) {
		if (this.disposed) {
			logger.warn("Arrived after disposale. {}", observedPCS);
			return;
		}
		UUID peerConnectionUUID = observedPCS.peerConnectionUUID;
		int index = Math.abs(peerConnectionUUID.hashCode()) % this.peerConnectionsLength;
		PeerConnections peerConnections = this.peerConnections.get(index);
		try {
			peerConnections.add(peerConnectionUUID, observedPCS);
		} catch (Exception ex) {
			logger.warn("An error happened during buffering", ex);
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

	@Override
	public void run() {
		if (this.disposed) {
			return;
		}
		this.peerConnections
				.stream()
				.map(PeerConnections::retrieveActives)
				.flatMap(LinkedList::stream)
				.forEach(child::onNext);

		this.peerConnections
				.stream()
				.map(pc -> pc.retrieveExpired(this.pcExpirationTimeInS))
				.flatMap(LinkedList::stream)
				.forEach(expiredPCs::onNext);

		if (this.disposable.isDisposed()) {
			this.disposed = true;
		}

		Throwable throwable = this.throwableHolder.get();
		if (throwable != null) {
			this.child.onError(throwable);
			this.disposed = true;
		} else if (this.completedHolder.get()) {
			this.child.onComplete();
			this.disposed = true;
		}
	}
}
