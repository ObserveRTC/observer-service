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
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.CallPeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.MediaUnitPeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Adds a Peer Connection to the distributed database, and returns completed when its done.
 * <b>Warning 1:</b> This mechanism here does not use locks. It is going to be safe until a point
 * we rely on the fact that one PC joins to only one observer instance and sending samples to that one only.
 */
@Prototype
public class PeerConnectionJoinerTask extends TaskAbstract<Void> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(PeerConnectionJoinerTask.class);
	private enum State {
		CREATED,
		PC_IS_REGISTERED,
		PC_IS_ADDED_TO_CALL,
		PC_IS_ADDED_TO_MEDIAUNIT,
		EXECUTED,
		ROLLEDBACK,
	}

	private final CallPeerConnectionsRepository callPeerConnectionsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final MediaUnitPeerConnectionsRepository mediaUnitPeerConnectionsRepository;
	private PeerConnectionEntity entity;
	private State state = State.CREATED;


	public PeerConnectionJoinerTask(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.callPeerConnectionsRepository = repositoryProvider.getCallPeerConnectionsRepository();
		this.peerConnectionsRepository = repositoryProvider.getPeerConnectionsRepository();
		this.mediaUnitPeerConnectionsRepository = repositoryProvider.getMediaUnitPeerConnectionsRepository();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}

	public PeerConnectionJoinerTask forEntity(@NotNull PeerConnectionEntity entity) {
		this.entity = entity;
		return this;
	}

	@Override
	protected Void perform() {
		switch (this.state) {
			default:
			case CREATED:
				this.registerPeerConnection();
				this.state = State.PC_IS_REGISTERED;
			case PC_IS_REGISTERED:
				this.addCallToPeerConnection();
				this.state = State.PC_IS_ADDED_TO_CALL;
			case PC_IS_ADDED_TO_CALL:
				this.addPcToMediaUnit();
				this.state = State.PC_IS_ADDED_TO_MEDIAUNIT;
			case PC_IS_ADDED_TO_MEDIAUNIT:
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
		}
		return null;
	}

	@Override
	protected void rollback(Throwable t) {
		try {
			switch (this.state) {
				case EXECUTED:
				case PC_IS_ADDED_TO_MEDIAUNIT:
					this.removePcFromMediaUnit(t);
					this.state = State.PC_IS_ADDED_TO_CALL;
				case PC_IS_ADDED_TO_CALL:
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
			this.getLogger().error("During rollback an error is occured", another);
		}

	}

	private void addPcToMediaUnit() {
		this.mediaUnitPeerConnectionsRepository.add(this.entity.mediaUnitId, this.entity.peerConnectionUUID);
	}

	private void removePcFromMediaUnit(Throwable exceptionInExecution) {
		try {
			this.mediaUnitPeerConnectionsRepository.remove(this.entity.mediaUnitId, this.entity.peerConnectionUUID);
		} catch (Exception ex) {
			this.getLogger().error("During rollback the following error occured", ex);
		}
	}

	private void addCallToPeerConnection() {
		this.callPeerConnectionsRepository.add(this.entity.callUUID, this.entity.peerConnectionUUID);
	}

	private void removeCallToPeerConnection(Throwable exceptionInExecution) {
		try {
			this.callPeerConnectionsRepository.remove(this.entity.callUUID, this.entity.peerConnectionUUID);
		} catch (Exception ex2) {
			this.getLogger().error("During rollback the following error occured", ex2);
		}
	}

	private void registerPeerConnection() {
		this.peerConnectionsRepository.save(this.entity.peerConnectionUUID, this.entity);
	}
	private void unregisterPeerConnection(Throwable exceptionInExecution) {
		try {
			this.peerConnectionsRepository.rxDelete(this.entity.peerConnectionUUID);
		} catch (Exception ex) {
			this.getLogger().error("During rollback the following error occured", ex);
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