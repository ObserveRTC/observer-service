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
public class ICEConnectionAdderTask extends TaskAbstract<Void> {
	private enum State {
		CREATED,
		ICE_CONNECTION_IS_REGISTERED,
		PC_IS_BOUND_TO_ICE_CONNECTION,
		EXECUTED,
		ROLLEDBACK,
	}

	private static final Logger logger = LoggerFactory.getLogger(ICEConnectionAdderTask.class);

	private final ICEConnectionsRepository iceConnectionsRepository;
	private final PeerConnectionICEConnectionsRepository peerConnectionICEConnectionsRepository;
	private final List<ICEConnectionEntity> entities;
	private State state = State.CREATED;


	public ICEConnectionAdderTask(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.iceConnectionsRepository = repositoryProvider.getICEConnectionsRepository();
		this.peerConnectionICEConnectionsRepository = repositoryProvider.getPeerConnectionICEConnectionsRepository();
		this.entities = new LinkedList<>();
	}

	public ICEConnectionAdderTask forICEConnectionEntity(@NotNull ICEConnectionEntity entity) {
		this.entities.add(entity);
		return this;
	}

	@Override
	protected Void perform() {
		switch (this.state) {
			default:
			case CREATED:
				this.registerICEConnections();
				this.state = State.ICE_CONNECTION_IS_REGISTERED;
			case ICE_CONNECTION_IS_REGISTERED:
				this.addICEConnectionsToPc();
				this.state = State.PC_IS_BOUND_TO_ICE_CONNECTION;
			case PC_IS_BOUND_TO_ICE_CONNECTION:
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
				return null;
		}
	}


	@Override
	protected void rollback(Throwable t) {
		switch (this.state) {
			case EXECUTED:
				this.removeICEConnectionsToPc(t);
				this.state = State.PC_IS_BOUND_TO_ICE_CONNECTION;
			case PC_IS_BOUND_TO_ICE_CONNECTION:
				this.unregisterICEConnections(t);
				this.state = State.ROLLEDBACK;
			case CREATED:
			case ROLLEDBACK:
			default:
				return;
		}
	}

	private void removeICEConnectionsToPc(Throwable t) {
		Iterator<ICEConnectionEntity> it = this.entities.iterator();
		while (it.hasNext()) {
			ICEConnectionEntity iceConnectionEntity = it.next();
			String key = ICEConnectionsRepository.getKey(iceConnectionEntity.pcUUID, iceConnectionEntity.localCandidateId, iceConnectionEntity.remoteCandidateId);
			this.peerConnectionICEConnectionsRepository.remove(iceConnectionEntity.pcUUID, key);
		}
	}

	private void addICEConnectionsToPc() {
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

	private void registerICEConnections() {
		Map<String, ICEConnectionEntity> iceConnections =
				this.entities.stream().collect(Collectors.toMap(
						e -> ICEConnectionsRepository.getKey(e.pcUUID, e.localCandidateId, e.remoteCandidateId),
						Function.identity()
				)
		);
		this.iceConnectionsRepository.saveAll(iceConnections);
	}

	private void unregisterICEConnections(Throwable exceptionInExecution) {
		try {
			Set<String> keySet = this.entities.stream().map(
					e -> ICEConnectionsRepository.getKey(e.pcUUID, e.localCandidateId, e.remoteCandidateId)
			).collect(Collectors.toSet());
			this.iceConnectionsRepository.deleteAll(keySet);
		} catch (Exception ex) {
			logger.error("During rollback the following error occurred", ex);
		}
	}


	@Override
	protected void validate() {
		super.validate();
	}


}