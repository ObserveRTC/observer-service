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

package org.observertc.webrtc.observer.tasks;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Completable;
import java.util.Objects;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.CallPeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds a Peer Connection to the distributed database, and returns completed when its done.
 * <b>Warning 1:</b> This mechanism here does not use locks. It is going to be safe until a point
 * we rely on the fact that one PC joins to only one observer instance and sending samples to that one only.
 */
@Prototype
public class PeerConnectionJoinerTask extends TaskAbstract<Completable> {
	private enum State {
		CREATED,
		PC_IS_REGISTERED,
		PC_IS_ADDED,
		EXECUTED,
		ROLLEDBACK,
	}

	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionJoinerTask.class);

	private final CallPeerConnectionsRepository callPeerConnectionsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private PeerConnectionEntity entity;
	private State state = State.CREATED;


	public PeerConnectionJoinerTask(
			RepositoryProvider repositoryProvider,
			Provider<FencedLockAcquirer> lockProvider
	) {
		super();
		this.callPeerConnectionsRepository = repositoryProvider.getCallPeerConnectionsRepository();
		this.peerConnectionsRepository = repositoryProvider.getPeerConnectionsRepository();
	}

	public PeerConnectionJoinerTask forEntity(@NotNull PeerConnectionEntity entity) {
		this.entity = entity;
		return this;
	}

	@Override
	protected Completable doPerform() {
		return Completable
				.fromRunnable(this::execute)
				.doOnError(this::rollback);
	}

	private void execute() {
		switch (this.state) {
			default:
			case CREATED:
				this.registerPeerConnection();
				this.state = State.PC_IS_REGISTERED;
			case PC_IS_REGISTERED:
				this.addCallToPeerConnection();
				this.state = State.PC_IS_ADDED;
			case PC_IS_ADDED:
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
				return;
		}
	}

	private void rollback(Throwable t) {
		try {
			switch (this.state) {
				case EXECUTED:
				case PC_IS_ADDED:
					this.unregisterPeerConnection(t);
					this.state = State.PC_IS_REGISTERED;
				case PC_IS_REGISTERED:
					this.removeCallToPeerConnection(t);
					this.state = State.ROLLEDBACK;
				case CREATED:
				case ROLLEDBACK:
				default:
					return;
			}
		} catch (Throwable another) {
			logger.error("During rollback an error is occured", another);
		}

	}

	private void addCallToPeerConnection() {
		this.peerConnectionsRepository.save(this.entity.peerConnectionUUID, this.entity);
	}

	private void registerPeerConnection() {
		this.callPeerConnectionsRepository.add(this.entity.callUUID, this.entity.peerConnectionUUID);
	}

	private void removeCallToPeerConnection(Throwable exceptionInExecution) {
		try {
			this.callPeerConnectionsRepository.remove(this.entity.callUUID, this.entity.peerConnectionUUID);
		} catch (Exception ex2) {
			logger.error("During rollback the following error occured", ex2);
		}
	}

	private void unregisterPeerConnection(Throwable exceptionInExecution) {
		try {
			this.peerConnectionsRepository.rxDelete(this.entity.peerConnectionUUID);
		} catch (Exception ex) {
			logger.error("During rollback the following error occured", ex);
		}
	}

	@Override
	protected void validate() {
		super.validate();
		Objects.requireNonNull(this.entity);
		Objects.requireNonNull(this.entity.callUUID);
		Objects.requireNonNull(this.entity.peerConnectionUUID);
	}


}