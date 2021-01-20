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
import org.observertc.webrtc.observer.models.ICEConnectionEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.ICEConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.PeerConnectionICEConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adds a Peer Connection to the distributed database, and returns completed when its done.
 * <b>Warning 1:</b> This mechanism here does not use locks. It is going to be safe until a point
 * we rely on the fact that one PC joins to only one observer instance and sending samples to that one only.
 */
@Prototype
public class ICEConnectionRemoverTask extends TaskAbstract<Void> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(ICEConnectionRemoverTask.class);
	private enum State {
		CREATED,
		COLLECT_ICE_CONNECTIONS,
		COLLECT_ICE_CONNECTIONS_BY_KEYS,
		ICE_CONNECTION_IS_UNREGISTERED,
		PC_IS_UNBOUND_TO_ICE_CONNECTION,
		EXECUTED,
		ROLLEDBACK,
	}



	private final ICEConnectionsRepository iceConnectionsRepository;
	private final PeerConnectionICEConnectionsRepository peerConnectionICEConnectionsRepository;
	private List<ICEConnectionEntity> entities;
	private Set<UUID> pcUUIDs = new HashSet<>();
	private Set<String> keys = new HashSet<>();
	private State state = State.CREATED;


	public ICEConnectionRemoverTask(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.iceConnectionsRepository = repositoryProvider.getICEConnectionsRepository();
		this.peerConnectionICEConnectionsRepository = repositoryProvider.getPeerConnectionICEConnectionsRepository();
		this.entities = new LinkedList<>();
		this.pcUUIDs = new HashSet<>();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}

	public ICEConnectionRemoverTask forICEConnectionEntity(@NotNull ICEConnectionEntity entity) {
		this.entities.add(entity);
		return this;
	}

	public ICEConnectionRemoverTask forICEConnectionKeys(@NotNull Set<String> keys) {
		this.keys.addAll(keys);
		return this;
	}

	public ICEConnectionRemoverTask forPcUUID(@NotNull UUID pcUUID) {
		this.pcUUIDs.add(pcUUID);
		return this;
	}

	public ICEConnectionRemoverTask forPcUUIDs(@NotNull Iterable<UUID> pcUUIDs) {
		pcUUIDs.forEach(this.pcUUIDs::add);
		return this;
	}


	@Override
	protected Void perform() {
		switch (this.state) {
			default:
			case CREATED:
				this.collectICEConnectionsByPCUUIDs();
				this.collectICEConnectionsByKeys();
				this.state = State.COLLECT_ICE_CONNECTIONS;
			case COLLECT_ICE_CONNECTIONS:
				this.removeICEConnectionsToPc();
				this.state = State.PC_IS_UNBOUND_TO_ICE_CONNECTION;
			case PC_IS_UNBOUND_TO_ICE_CONNECTION:
				this.unregisterICEConnections();
				this.state = State.ICE_CONNECTION_IS_UNREGISTERED;
			case ICE_CONNECTION_IS_UNREGISTERED:
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
		}
		return null;
	}

	@Override
	protected void rollback(Throwable t) {
		switch (this.state) {
			case EXECUTED:
			case PC_IS_UNBOUND_TO_ICE_CONNECTION:
				this.addICEConnectionsToPc(t);
				this.state = State.ICE_CONNECTION_IS_UNREGISTERED;
			case ICE_CONNECTION_IS_UNREGISTERED:
				this.registerICEConnections(t);
				this.state = State.ROLLEDBACK;
			case COLLECT_ICE_CONNECTIONS:
			case CREATED:
			case ROLLEDBACK:
			default:
				return;
		}
	}

	private void collectICEConnectionsByPCUUIDs() {
		if (this.pcUUIDs.size() < 1) {
			return;
		}
		Map<UUID, Collection<String>> collectedKeys = this.peerConnectionICEConnectionsRepository.findAll(this.pcUUIDs);
		Set<String> keys = collectedKeys.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
		Map<String, ICEConnectionEntity> collectedEntities = this.iceConnectionsRepository.findAll(keys);
		this.entities.addAll(collectedEntities.values());
	}

	private void collectICEConnectionsByKeys() {
		if (this.keys.size() < 1) {
			return;
		}
		Map<String, ICEConnectionEntity> collectedEntities = this.iceConnectionsRepository.findAll(this.keys);
		this.entities.addAll(collectedEntities.values());
	}

	private void removeICEConnectionsToPc() {
		if (this.entities.size() < 1) {
			return;
		}
		Iterator<ICEConnectionEntity> it = this.entities.iterator();
		while (it.hasNext()) {
			ICEConnectionEntity iceConnectionEntity = it.next();
			String key = ICEConnectionsRepository.getKey(iceConnectionEntity.pcUUID, iceConnectionEntity.localCandidateId, iceConnectionEntity.remoteCandidateId);
			this.peerConnectionICEConnectionsRepository.remove(iceConnectionEntity.pcUUID, key);
		}
	}

	private void addICEConnectionsToPc(Throwable t) {
		Iterator<ICEConnectionEntity> it = this.entities.iterator();
		Map<UUID, List<String>> pcsICEConnections = new HashMap<>();
		while (it.hasNext()) {
			ICEConnectionEntity iceConnectionEntity = it.next();
			List<String> iceConnections = pcsICEConnections.get(iceConnectionEntity.pcUUID);
			if (Objects.isNull(iceConnections)) {
				iceConnections = new LinkedList<>();
				pcsICEConnections.put(iceConnectionEntity.pcUUID, iceConnections);
			}
			String key = ICEConnectionsRepository.getKey(iceConnectionEntity.pcUUID, iceConnectionEntity.localCandidateId, iceConnectionEntity.remoteCandidateId);
			iceConnections.add(key);
		}
		Iterator<Map.Entry<UUID, List<String>>> it2 = pcsICEConnections.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry<UUID, List<String>> entry = it2.next();
			UUID pcUUID = entry.getKey();
			List<String> keys = entry.getValue();
			this.peerConnectionICEConnectionsRepository.addAll(pcUUID, keys);
		}
	}

	private void registerICEConnections(Throwable exceptionInExecution) {
		try {
			Map<String, ICEConnectionEntity> iceConnections =
					this.entities.stream().collect(Collectors.toMap(
							e -> ICEConnectionsRepository.getKey(e.pcUUID, e.localCandidateId, e.remoteCandidateId),
							Function.identity()
							)
					);
			this.iceConnectionsRepository.saveAll(iceConnections);
		} catch (Exception ex) {
			this.getLogger().error("During rollback the following error occurred", ex);
		}

	}

	private void unregisterICEConnections() {
		Set<String> keySet = this.entities.stream().map(
				e -> ICEConnectionsRepository.getKey(e.pcUUID, e.localCandidateId, e.remoteCandidateId)
		).collect(Collectors.toSet());
		this.iceConnectionsRepository.deleteAll(keySet);
	}

	@Override
	protected void validate() {
		super.validate();
	}


}