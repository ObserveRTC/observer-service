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
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Prototype
public class CallDetailsFinderTask extends TaskAbstract<Optional<CallDetailsFinderTask.Result>> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(CallDetailsFinderTask.class);

	public static class Result {
		public UUID callUUID;
		public Collection<String> synchronizationSourceKeys;
		public Collection<UUID> peerConnectionUUIDs;
		public CallEntity callEntity;
	}

	private enum State {
		CREATED,
		CALL_ENTITY_IS_COLLECTED,
		CALL_PEER_CONNECTION_UUIDS_ARE_COLLECTED,
		EXECUTED,
		ROLLEDBACK,
	}


	private final CallEntitiesRepository callEntitiesRepository;
	private final CallPeerConnectionsRepository callPeerConnectionsRepository;
	private final CallSynchronizationSourcesRepository callSynchronizationSourcesRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private State state = State.CREATED;
	private boolean collectSynchronizationSourceKeys = true;
	private boolean collectPeerConnectionUUIDs = true;
	private UUID callUUID;
	private UUID pcUUID;

	public CallDetailsFinderTask(
			RepositoryProvider repositoryProvider,
			SSRCEntityFinderTask SSRCEntityFinderTask
	) {
		super();
		this.peerConnectionsRepository = repositoryProvider.getPeerConnectionsRepository();
		this.callEntitiesRepository = repositoryProvider.getCallEntitiesRepository();
		this.callPeerConnectionsRepository = repositoryProvider.getCallPeerConnectionsRepository();
		this.callSynchronizationSourcesRepository = repositoryProvider.getCallSynchronizationSourcesRepository();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}

	public CallDetailsFinderTask collectPeerConnectionUUIDs(boolean value) {
		this.collectPeerConnectionUUIDs = value;
		return this;
	}

	public CallDetailsFinderTask collectSynchronizationSourceKeys(boolean value) {
		this.collectSynchronizationSourceKeys = value;
		return this;
	}

	public CallDetailsFinderTask forCallUUID(UUID callUUID) {
		this.callUUID = callUUID;
		return this;
	}

	public CallDetailsFinderTask forPcUUID(UUID pcUUID) {
		this.pcUUID = pcUUID;
		return this;
	}

	@Override
	protected Optional<Result> perform() throws Exception {
		Result result = new Result();
		if (Objects.nonNull(this.pcUUID)) {
			Optional<PeerConnectionEntity> peerConnectionEntityHolder = this.peerConnectionsRepository.find(this.pcUUID);
			if (peerConnectionEntityHolder.isPresent()) {
				PeerConnectionEntity peerConnectionEntity = peerConnectionEntityHolder.get();
				if (Objects.nonNull(this.callUUID)) {
					if (!this.callUUID.equals(peerConnectionEntity.callUUID)) {
						getLogger().warn("The provided peer connection entity {}, has a different callUUID {}, which was provided", peerConnectionEntity, this.callUUID);
						return Optional.empty();
					}
				} else {
					this.callUUID = peerConnectionEntity.callUUID;
				}
			}
		}
		if (Objects.isNull(this.callUUID)) {
			return Optional.empty();
		}
		result.callUUID = this.callUUID;
		Optional<CallEntity> callEntityOptional = this.callEntitiesRepository.find(this.callUUID);
		if (!callEntityOptional.isPresent()) {
			return Optional.empty();
		}
		result.callEntity = callEntityOptional.get();
		this.state = State.CALL_ENTITY_IS_COLLECTED;
		if (this.collectPeerConnectionUUIDs) {
			result.peerConnectionUUIDs = this.callPeerConnectionsRepository.find(this.callUUID);
		} else {
			result.peerConnectionUUIDs = Collections.EMPTY_SET;
		}
		this.state = State.CALL_PEER_CONNECTION_UUIDS_ARE_COLLECTED;
		if (this.collectSynchronizationSourceKeys) {
			result.synchronizationSourceKeys = this.callSynchronizationSourcesRepository.find(this.callUUID);
		} else {
			result.synchronizationSourceKeys = Collections.EMPTY_SET;
		}
		return Optional.of(result);
	}

	@Override
	protected void validate() {
		super.validate();
		if (Objects.isNull(this.callUUID) && Objects.isNull(this.pcUUID)) {
			throw new IllegalStateException("Neither callUUID nor pcUUID is provided");
		}
	}

}