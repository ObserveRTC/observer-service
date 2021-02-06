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
import org.observertc.webrtc.observer.entities.PCTrafficType;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Adds a Peer Connection to the distributed database, and returns completed when its done.
 * <b>Warning 1:</b> This mechanism here does not use locks. It is going to be safe until a point
 * we rely on the fact that one PC joins to only one observer instance and sending samples to that one only.
 */
@Prototype
public class PeerConnectionsTrafficUpdater extends TaskAbstract<Set<UUID>> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(PeerConnectionsTrafficUpdater.class);
	private enum State {
		CREATED,
		COLLECTED,
		SELECTED,
		UPDATED,
		EXECUTED,
		ROLLEDBACK,
	}

	private final PeerConnectionsRepository peerConnectionsRepository;
	private final Map<UUID, PCTrafficType> trafficStates = new HashMap<>();
	private Map<UUID, PeerConnectionEntity> selectedToUpdate = new HashMap<>();
	private Map<UUID, PeerConnectionEntity> collectedEntities = null;
	private State state = State.CREATED;

	public PeerConnectionsTrafficUpdater(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.peerConnectionsRepository = repositoryProvider.getPeerConnectionsRepository();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}


	public PeerConnectionsTrafficUpdater forTrafficState(UUID pcUUID, PCTrafficType pcTrafficType) {
		this.trafficStates.put(pcUUID, pcTrafficType);
		return this;
	}

	@Override
	protected Set<UUID> perform() {
		switch (this.state) {
			default:
			case CREATED:
				this.collectedEntities = this.peerConnectionsRepository.findAll(this.trafficStates.keySet());
				this.state = State.COLLECTED;
			case COLLECTED:
				Iterator<Map.Entry<UUID, PeerConnectionEntity>> it = this.collectedEntities.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<UUID, PeerConnectionEntity> entry = it.next();
					UUID pcUUID = entry.getKey();
					PeerConnectionEntity peerConnectionEntity = entry.getValue();
					PCTrafficType pcTrafficType = this.trafficStates.get(pcUUID);
					if (Objects.isNull(pcTrafficType)) {
						continue;
					}
					peerConnectionEntity.trafficType = pcTrafficType;
					this.selectedToUpdate.put(pcUUID, peerConnectionEntity);
				}
				this.state = State.SELECTED;
			case SELECTED:
				this.peerConnectionsRepository.saveAll(this.selectedToUpdate);
				this.state = State.UPDATED;
			case UPDATED:
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
		}
		return this.selectedToUpdate.keySet();
	}

	protected void rollback(Throwable t) {
		// ah... even if it does go wrong traffictypes are going to be overridden anyhow
	}

	@Override
	protected void validate() {
		super.validate();
	}

}