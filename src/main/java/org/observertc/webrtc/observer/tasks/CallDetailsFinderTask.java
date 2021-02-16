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
import org.observertc.webrtc.observer.entities.OldCallEntity;
import org.observertc.webrtc.observer.entities.OldPeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.stores.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Prototype
@Deprecated
public class CallDetailsFinderTask extends TaskAbstract<Optional<CallDetailsFinderTask.Result>> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(CallDetailsFinderTask.class);

	public static class Result {
		public UUID callUUID;
		public Collection<String> synchronizationSourceKeys;
		public Collection<UUID> peerConnectionUUIDs;
		public OldCallEntity callEntity;
		public Set<String> browserIds = new HashSet<>();
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
	private boolean collectBrowserIds = false;
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

	public CallDetailsFinderTask collectBrowserIds(boolean value) {
		this.collectBrowserIds = value;
		if (value) {
			this.collectPeerConnectionUUIDs = true;
		}
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
			Optional<OldPeerConnectionEntity> peerConnectionEntityHolder = this.peerConnectionsRepository.find(this.pcUUID);
			if (peerConnectionEntityHolder.isPresent()) {
				OldPeerConnectionEntity oldPeerConnectionEntity = peerConnectionEntityHolder.get();
				if (Objects.nonNull(this.callUUID)) {
					if (!this.callUUID.equals(oldPeerConnectionEntity.callUUID)) {
						getLogger().warn("The provided peer connection entity {}, has a different callUUID {}, which was provided", oldPeerConnectionEntity, this.callUUID);
						return Optional.empty();
					}
				} else {
					this.callUUID = oldPeerConnectionEntity.callUUID;
				}
			}
		}
		if (Objects.isNull(this.callUUID)) {
			return Optional.empty();
		}
		result.callUUID = this.callUUID;
		Optional<OldCallEntity> callEntityOptional = this.callEntitiesRepository.find(this.callUUID);
		if (!callEntityOptional.isPresent()) {
			return Optional.empty();
		}
		result.callEntity = callEntityOptional.get();
		this.state = State.CALL_ENTITY_IS_COLLECTED;
		if (this.collectPeerConnectionUUIDs) {
			result.peerConnectionUUIDs = this.callPeerConnectionsRepository.find(this.callUUID);
			if (this.collectBrowserIds) {
				Set<UUID> pcUUIDs = new HashSet<>(result.peerConnectionUUIDs);
				this.peerConnectionsRepository.findAll(pcUUIDs)
					.values().stream()
					.map(pc -> pc.browserId)
					.filter(Objects::nonNull)
					.forEach(result.browserIds::add);
			}
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