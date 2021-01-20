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
import org.observertc.webrtc.observer.repositories.hazelcast.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Adds a Peer Connection to the distributed database, and returns completed when its done.
 * <b>Warning 1:</b> This mechanism here does not use locks. It is going to be safe until a point
 * we rely on the fact that one PC joins to only one observer instance and sending samples to that one only.
 */
@Prototype
public class PeerConnectionsFinderTask extends TaskAbstract<Collection<PeerConnectionEntity>> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(PeerConnectionsFinderTask.class);
	private enum State {
		CREATED,
		CHECK_PC_ENTITIES,
		CHECK_CALL_ENTITIES,
		EXECUTED,
		ROLLEDBACK,
	}



	private final PeerConnectionsRepository peerConnectionsRepository;
	private final CallPeerConnectionsRepository callPeerConnectionsRepository;
	private State state = State.CREATED;

	private Map<UUID, PeerConnectionEntity> result = new HashMap<>();
	private Set<UUID> pcUUIDs = new HashSet<>();
	private Set<UUID> callUUIDs = new HashSet<>();

	public PeerConnectionsFinderTask(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.peerConnectionsRepository = repositoryProvider.getPeerConnectionsRepository();
		this.callPeerConnectionsRepository = repositoryProvider.getCallPeerConnectionsRepository();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}

	public PeerConnectionsFinderTask addPCUUIDs(@NotNull Set<UUID> keySet) {
		keySet.stream().filter(Objects::nonNull).forEach(this.pcUUIDs::add);
		return this;
	}

	public PeerConnectionsFinderTask forCallUUIDs(@NotNull Set<UUID> keySet) {
		keySet.stream().filter(Objects::nonNull).forEach(this.callUUIDs::add);
		return this;
	}


	@Override
	protected Collection<PeerConnectionEntity> perform() {
		switch (this.state) {
			default:
			case CREATED:
				this.state = State.CHECK_PC_ENTITIES;
			case CHECK_PC_ENTITIES:
				this.collectPCEntities();
				this.state = State.CHECK_CALL_ENTITIES;
			case CHECK_CALL_ENTITIES:
				this.collectCallEntities();
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
		}
		if (this.result.size() < 1) {
			return Collections.EMPTY_LIST;
		}
		return this.result.values();
	}

	@Override
	protected void rollback(Throwable t) {
		switch (this.state) {
			case EXECUTED:
			case CHECK_PC_ENTITIES:
				this.state = State.ROLLEDBACK;
			case CREATED:
			case ROLLEDBACK:
			default:
				return;
		}
	}

	@Override
	protected void validate() {
		if (this.pcUUIDs.size() < 1 && this.callUUIDs.size() < 1) {
			throw new IllegalStateException("Cannot execute a task with no UUIDs to find");
		}
		super.validate();
	}

	private void collectPCEntities() {
		if (this.pcUUIDs.size() < 1) {
			return;
		}
		Map<UUID, PeerConnectionEntity> foundEntities = this.peerConnectionsRepository.findAll(this.pcUUIDs);
		if (foundEntities.size() < 1) {
			return;
		}
		this.result.putAll(foundEntities);
	}

	private void collectCallEntities() {
		if (this.callUUIDs.size() < 1) {
			return;
		}
		Map<UUID, Collection<UUID>> foundPCUUIDs = this.callPeerConnectionsRepository.findAll(this.callUUIDs);
		if (foundPCUUIDs.size() < 1) {
			return;
		}
		Set<UUID> keys = foundPCUUIDs.values().stream().flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<UUID, PeerConnectionEntity> foundEntities = this.peerConnectionsRepository.findAll(keys);
		if (foundEntities.size() < 1) {
			return;
		}
		this.result.putAll(foundEntities);
	}
}