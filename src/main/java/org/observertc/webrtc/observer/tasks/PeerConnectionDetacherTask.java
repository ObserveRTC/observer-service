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
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.CallPeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.MediaUnitPeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Removes a Peer Connection to the distributed database, and returns completed when its done.
 * <b>Warning 1:</b> This mechanism here does not use locks. It is going to be safe until a point
 * we rely on the fact that one PC joins to only one observer instance and sending samples to that one only.
 */
@Prototype
public class PeerConnectionDetacherTask extends TaskAbstract<PeerConnectionEntity> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(PeerConnectionDetacherTask.class);
	private enum State {
		CREATED,
		PC_IS_UNREGISTERED,
		PC_IS_REMOVED_FROM_CALL,
		PC_IS_REMOVED_FROM_MEDIAUNIT,
		EXECUTED,
		ROLLEDBACK,
	}

	private final CallPeerConnectionsRepository callPeerConnectionsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final MediaUnitPeerConnectionsRepository mediaUnitPeerConnectionsRepository;
	private State state = State.CREATED;
	private UUID pcUUID;
	private PeerConnectionEntity removedPCEntity;

	public PeerConnectionDetacherTask(RepositoryProvider repositoryProvider
	) {
		super();
		this.callPeerConnectionsRepository = repositoryProvider.getCallPeerConnectionsRepository();
		this.peerConnectionsRepository = repositoryProvider.getPeerConnectionsRepository();
		this.mediaUnitPeerConnectionsRepository = repositoryProvider.getMediaUnitPeerConnectionsRepository();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}

	public PeerConnectionDetacherTask forPeerConnectionUUID(@NotNull UUID pcUUID) {
		this.pcUUID = pcUUID;
		return this;
	}

	@Override
	protected PeerConnectionEntity perform() {
		Optional<PeerConnectionEntity> pcEntityHolder = this.peerConnectionsRepository.find(this.pcUUID);
		if (!pcEntityHolder.isPresent()) {
			return null;
		}
		this.removedPCEntity = pcEntityHolder.get();
		switch (this.state) {
			default:
			case CREATED:
				this.unregisterPeerConnection();
				this.state = State.PC_IS_UNREGISTERED;
			case PC_IS_UNREGISTERED:
				this.removeCallToPeerConnection();
				this.state = State.PC_IS_REMOVED_FROM_CALL;
			case PC_IS_REMOVED_FROM_CALL:
				this.removePcFromMediaUnit();
				this.state = State.PC_IS_REMOVED_FROM_MEDIAUNIT;
			case PC_IS_REMOVED_FROM_MEDIAUNIT:
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
		}
		return this.removedPCEntity;
	}

	@Override
	protected void rollback(Throwable t) {
		switch (this.state) {
			case EXECUTED:
			case PC_IS_REMOVED_FROM_MEDIAUNIT:
				this.addPcToMediaUnit(t);
				this.state = State.PC_IS_REMOVED_FROM_CALL;
			case PC_IS_REMOVED_FROM_CALL:
				this.registerPeerConnection(t);
				this.state = State.PC_IS_UNREGISTERED;
			case PC_IS_UNREGISTERED:
				this.addCallToPeerConnection(t);
				this.state = State.ROLLEDBACK;
			case CREATED:
			case ROLLEDBACK:
			default:
				return;
		}
	}

	private void addPcToMediaUnit(Throwable exceptionInExecution) {
		try {
			this.mediaUnitPeerConnectionsRepository.add(this.removedPCEntity.mediaUnitId, this.removedPCEntity.peerConnectionUUID);
		} catch (Exception ex2) {
			this.getLogger().error("During rollback the following error occured", ex2);
		}
	}

	private void removePcFromMediaUnit() {
		this.mediaUnitPeerConnectionsRepository.remove(this.removedPCEntity.mediaUnitId, this.removedPCEntity.peerConnectionUUID);
	}

	private void addCallToPeerConnection(Throwable exceptionInExecution) {
		try {
			this.callPeerConnectionsRepository.add(this.removedPCEntity.callUUID, this.removedPCEntity.peerConnectionUUID);
		} catch (Exception ex2) {
			this.getLogger().error("During rollback the following error occured", ex2);
		}
	}

	private void registerPeerConnection(Throwable exceptionInExecution) {
		try {
			this.peerConnectionsRepository.save(this.removedPCEntity.peerConnectionUUID, this.removedPCEntity);
		} catch (Exception ex) {
			this.getLogger().error("During rollback the following error occured", ex);
		}
	}

	private void removeCallToPeerConnection() {
		this.callPeerConnectionsRepository.remove(this.removedPCEntity.callUUID, this.removedPCEntity.peerConnectionUUID);

	}

	private void unregisterPeerConnection() {
		this.peerConnectionsRepository.delete(this.removedPCEntity.peerConnectionUUID);

	}

	@Override
	protected void validate() {
		super.validate();
		Objects.requireNonNull(this.pcUUID);
	}


}