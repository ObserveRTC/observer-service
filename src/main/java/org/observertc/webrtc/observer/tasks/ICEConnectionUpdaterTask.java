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
import org.observertc.webrtc.observer.models.ICEConnectionEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.ICEConnectionsRepository;
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
public class ICEConnectionUpdaterTask extends TaskAbstract<Void> {

	private enum State {
		CREATED,
		ICE_CONNECTIONS_ARE_LOADED,
		ICE_CONNECTION_UPDATES_ARE_READY,
		ICE_CONNECTION_IS_UPDATED,
		EXECUTED,
		ROLLEDBACK,
	}

	private static final Logger logger = LoggerFactory.getLogger(ICEConnectionUpdaterTask.class);

	private final ICEConnectionsRepository iceConnectionsRepository;
	private final Map<String, ICEConnectionEntity> selectedEntities = new HashMap<>();
	private final Map<String, ICEConnectionEntity> updatedEntities = new HashMap<>();
	private Map<String, ICEConnectionEntity> loadedEntities;
	private State state = State.CREATED;


	public ICEConnectionUpdaterTask(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.iceConnectionsRepository = repositoryProvider.getICEConnectionsRepository();
	}

	public ICEConnectionUpdaterTask forICEConnectionEntity(@NotNull ICEConnectionEntity entity) {
		if (Objects.isNull(entity.pcUUID)) {
			logger.warn("Cannot update ICEConnection if the pcUUID is null in {}", entity);
			return this;
		}
		if (Objects.isNull(entity.localCandidateId)) {
			logger.warn("Cannot update ICEConnection if the pcUUID is null in {}", entity);
			return this;
		}
		if (Objects.isNull(entity.remoteCandidateId)) {
			logger.warn("Cannot update ICEConnection if the pcUUID is null in {}", entity);
			return this;
		}
		String key = ICEConnectionsRepository.getKey(entity.pcUUID, entity.localCandidateId, entity.remoteCandidateId);
		this.selectedEntities.put(key, entity);
		return this;
	}

	@Override
	protected Void perform() {
		switch (this.state) {
			default:
			case CREATED:
				this.loadICEConnections();
				this.state = State.ICE_CONNECTIONS_ARE_LOADED;
			case ICE_CONNECTIONS_ARE_LOADED:
				this.collectUpdatedICEConnections();
				this.state = State.ICE_CONNECTION_UPDATES_ARE_READY;
			case ICE_CONNECTION_UPDATES_ARE_READY:
				this.updateICEConnections();
				this.state = State.ICE_CONNECTION_IS_UPDATED;
			case ICE_CONNECTION_IS_UPDATED:
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
				this.revokeICEConnections(t);
				this.state = State.ICE_CONNECTION_IS_UPDATED;
			case ICE_CONNECTION_IS_UPDATED:
				this.state = State.ICE_CONNECTION_UPDATES_ARE_READY;
			case ICE_CONNECTION_UPDATES_ARE_READY:
				this.state = State.ICE_CONNECTION_IS_UPDATED;
			case ICE_CONNECTIONS_ARE_LOADED:
				this.state = State.ROLLEDBACK;
			case CREATED:
			case ROLLEDBACK:
			default:
				return;
		}
	}



	private void loadICEConnections() {
		Set<String> keys = this.selectedEntities.keySet();
		this.loadedEntities = this.iceConnectionsRepository.findAll(keys);
	}

	private<T> boolean nonNullAndDifferent(T loaded, T updated) {
		if (Objects.isNull(updated)) {
			return false;
		}
		if (Objects.isNull(loaded)) {
			return Objects.nonNull(updated);
		}
		return !loaded.equals(updated);
	}

	private void collectUpdatedICEConnections() {
		Iterator<Map.Entry<String, ICEConnectionEntity>> it = this.selectedEntities.entrySet().iterator();

		while(it.hasNext()) {
			Map.Entry<String, ICEConnectionEntity> entry = it.next();
			String key = entry.getKey();
			ICEConnectionEntity loadedEntity = this.loadedEntities.get(key);
			ICEConnectionEntity updatedEntity = entry.getValue();
			ICEConnectionEntity newEntity = ICEConnectionEntity.from(loadedEntity);
			boolean modified = false;
			if (this.nonNullAndDifferent(loadedEntity.mediaUnitId, updatedEntity.mediaUnitId)) {
				newEntity.mediaUnitId = updatedEntity.mediaUnitId;
				modified = true;
			}
			if (this.nonNullAndDifferent(loadedEntity.localCandidateType, updatedEntity.localCandidateType)) {
				newEntity.localCandidateType = updatedEntity.localCandidateType;
				modified = true;
			}
			if (this.nonNullAndDifferent(loadedEntity.remoteCandidateType, updatedEntity.remoteCandidateType)) {
				newEntity.remoteCandidateType = updatedEntity.remoteCandidateType;
				modified = true;
			}
			if (this.nonNullAndDifferent(loadedEntity.nominated, updatedEntity.nominated)) {
				newEntity.nominated = updatedEntity.nominated;
				modified = true;
			}
			if (this.nonNullAndDifferent(loadedEntity.state, updatedEntity.state)) {
				newEntity.state = updatedEntity.state;
				modified = true;
			}

			if (modified) {
				this.updatedEntities.put(key, newEntity);
			}
		}
	}

	private void updateICEConnections() {
		if (this.updatedEntities.size() < 1) {
			return;
		}
		this.iceConnectionsRepository.updateAll(this.updatedEntities);
	}

	private void revokeICEConnections(Throwable t) {
		if (this.updatedEntities.size() < 1) {
			return;
		}

		Map<String, ICEConnectionEntity> loadedSubset = this.updatedEntities
				.keySet().stream().collect(Collectors.toMap(
				Function.identity(),
				key -> this.loadedEntities.get(key)
		));
		this.iceConnectionsRepository.updateAll(loadedSubset);
	}

	@Override
	protected void validate() {
		super.validate();
	}


}