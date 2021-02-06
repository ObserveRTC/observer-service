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
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.CallPeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.MediaUnitPeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Objects;

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
				this.peerConnectionsRepository.save(this.entity.peerConnectionUUID, this.entity);
				this.state = State.PC_IS_REGISTERED;
			case PC_IS_REGISTERED:
				this.callPeerConnectionsRepository.add(this.entity.callUUID, this.entity.peerConnectionUUID);
				this.state = State.PC_IS_ADDED_TO_CALL;
			case PC_IS_ADDED_TO_CALL:
				this.mediaUnitPeerConnectionsRepository.add(this.entity.mediaUnitId, this.entity.peerConnectionUUID);
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
					this.mediaUnitPeerConnectionsRepository.remove(this.entity.mediaUnitId, this.entity.peerConnectionUUID);
					this.state = State.PC_IS_ADDED_TO_CALL;
				case PC_IS_ADDED_TO_CALL:
					this.callPeerConnectionsRepository.remove(this.entity.callUUID, this.entity.peerConnectionUUID);
					this.state = State.PC_IS_REGISTERED;
				case PC_IS_REGISTERED:
					this.peerConnectionsRepository.rxDelete(this.entity.peerConnectionUUID);
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

	@Override
	protected void validate() {
		super.validate();
		Objects.requireNonNull(this.entity);
		Objects.requireNonNull(this.entity.callUUID);
		Objects.requireNonNull(this.entity.peerConnectionUUID);
	}


}