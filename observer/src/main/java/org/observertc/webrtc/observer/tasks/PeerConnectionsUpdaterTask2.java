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
import io.reactivex.rxjava3.core.Completable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.observertc.webrtc.observer.models.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.SynchronizationSourcesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds a Peer Connection to the distributed database, and returns completed when its done.
 * <b>Warning 1:</b> This mechanism here does not use locks. It is going to be safe until a point
 * we rely on the fact that one PC joins to only one observer instance and sending samples to that one only.
 */
@Prototype
public class PeerConnectionsUpdaterTask2 extends TaskAbstract<Completable> {
	private enum State {
		CREATED,
		EXISTING_ENTITIES_ARE_UPDATED,
		MISSING_ENTITIES_ARE_ADDED,
		EXECUTED,
		ROLLEDBACK,
	}

	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionsUpdaterTask2.class);

	private final SynchronizationSourcesRepository SSRCRepository;
	private State state = State.CREATED;
	private Map<String, UUID> streamKeysToPC = new HashMap<>();
	private Map<String, SynchronizationSourceEntity> selectedEntities = new HashMap<>();
	private Map<String, SynchronizationSourceEntity> foundEntities = new HashMap<>();
	private Map<String, SynchronizationSourceEntity> missingEntities = new HashMap<>();

	public PeerConnectionsUpdaterTask2(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.SSRCRepository = repositoryProvider.getSSRCRepository();
	}


	public PeerConnectionsUpdaterTask2 addStream(@NotNull UUID serviceUUD, @NotNull UUID pcUUID, @NotNull Long SSRC) {
		String key = SynchronizationSourcesRepository.getKey(serviceUUD, SSRC);
		SynchronizationSourceEntity entity = SynchronizationSourceEntity.of(serviceUUD, SSRC, null);
		this.selectedEntities.put(key, entity);
		this.streamKeysToPC.put(key, pcUUID);
		return this;
	}

	@Override
	protected Completable doPerform() {
		if (this.selectedEntities.size() < 1) {
			logger.info("No PC has selected to be updated");
			return Completable.complete();
		}
		return Completable
				.fromRunnable(this::execute)
				.doOnError(this::rollback);
	}

	private void execute() {
		switch (this.state) {
			default:
			case CREATED:
				this.selectEntities();
				this.state = State.EXISTING_ENTITIES_ARE_UPDATED;
			case EXISTING_ENTITIES_ARE_UPDATED:
				this.addMissingSSRCs();
				this.state = State.MISSING_ENTITIES_ARE_ADDED;
			case MISSING_ENTITIES_ARE_ADDED:
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
				return;
		}
	}

	private void rollback(Throwable t) {
		try {
			switch (this.state) {
				case EXECUTED:
				case MISSING_ENTITIES_ARE_ADDED:
					this.removeMissingSSRCs(t);
					this.state = State.EXISTING_ENTITIES_ARE_UPDATED;
				case EXISTING_ENTITIES_ARE_UPDATED:
					this.rollbackUpdateExistingSSRCs(t);
					this.state = State.ROLLEDBACK;
				case CREATED:
				case ROLLEDBACK:
				default:
					return;
			}
		} catch (Throwable another) {
			logger.error("During rollback an error is occured", another);
		}
	}

	private void selectEntities() {
		Map<String, SynchronizationSourceEntity> foundEntities = this.SSRCRepository.findAll(this.selectedEntities.keySet());
		Iterator<String> it = this.selectedEntities.keySet().iterator();
		for (; it.hasNext(); ) {
			String key = it.next();
			SynchronizationSourceEntity selectedEntity = this.selectedEntities.get(key);
			SynchronizationSourceEntity foundEntity = foundEntities.get(key);
			if (Objects.nonNull(foundEntity)) {
				this.foundEntities.put(key, foundEntity);
			} else if (Objects.nonNull(selectedEntity)) {
				this.missingEntities.put(key, selectedEntity);
			} else {
				logger.warn("Thee key for founded entities not matching with selected one. {} for pc {} is dropped",
						selectedEntity, this.streamKeysToPC.get(key));
				continue;
			}
		}
	}

	private void addMissingSSRCs() {
		if (this.missingEntities.size() < 1) {
			return;
		}
		Queue<SynchronizationSourceEntity> missingEntities = new LinkedList<>();
		this.missingEntities.values().stream().forEach(missingEntities::add);
		while (!missingEntities.isEmpty()) {
			SynchronizationSourceEntity missingEntity = missingEntities.poll();
			String key = SynchronizationSourcesRepository.getKey(missingEntity.serviceUUID, missingEntity.SSRC);
			UUID pcUUID = this.streamKeysToPC.get(key);
			if (Objects.isNull(pcUUID)) {
				logger.warn("Have not found pcUUID for entity {}", missingEntity);
				continue;
			}
			Optional<UUID> callUUIDHolder = this.foundEntities.entrySet()
					.stream()
					.filter(entry -> this.streamKeysToPC.get(entry.getKey()).equals(pcUUID))
					.map(entry -> entry.getValue().callUUID)
					.findFirst();
			if (!callUUIDHolder.isPresent()) {
				logger.warn("Did not found any callUUID for pcUUID {}, and sync entity {}", pcUUID, missingEntity);
				continue;
			}
			this.selectedEntities.get(key).callUUID = callUUIDHolder.get();
		}
		this.SSRCRepository.saveAll(this.selectedEntities);
	}

	private void removeMissingSSRCs(Throwable exceptionInOperation) {
		this.SSRCRepository.deleteAll(this.missingEntities.keySet());
	}

	private void rollbackUpdateExistingSSRCs(Throwable exceptionInOperation) {
		// That we should not and we cannot
	}

	@Override
	protected void validate() {
		super.validate();
	}

//	private class PCItem {
//		public Long SSRC;
//		public UUID pcUUID;
//		public UUID serviceUUID;
//	}
}