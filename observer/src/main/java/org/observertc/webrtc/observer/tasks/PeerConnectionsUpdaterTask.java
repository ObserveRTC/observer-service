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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.jooq.lambda.tuple.Tuple2;
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
public class PeerConnectionsUpdaterTask extends TaskAbstract<Completable> {
	private enum State {
		CREATED,
		EXISTING_ENTITIES_ARE_UPDATED,
		MISSING_ENTITIES_ARE_ADDED,
		EXECUTED,
		ROLLEDBACK,
	}

	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionsUpdaterTask.class);

	private final SynchronizationSourcesRepository SSRCRepository;
	private Map<UUID, Set<Long>> peerConnectionsSSRCs = new HashMap<>();
	private Map<UUID, UUID> peerConnectionsServiceUUID = new HashMap<>();
	private State state = State.CREATED;
	private Map<UUID, Set<String>> pcsStreamKeys = new HashMap<>();
	private Map<String, Tuple2<UUID, Long>> missingStreamKeys;
	private Map<String, SynchronizationSourceEntity> foundEntities;
	private Map<String, SynchronizationSourceEntity> missingEntities;

	public PeerConnectionsUpdaterTask(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.SSRCRepository = repositoryProvider.getSSRCRepository();
	}

	public PeerConnectionsUpdaterTask addStream(@NotNull UUID serviceUUD, @NotNull UUID pcUUID, @NotNull Set<Long> SSRCs) {
		this.peerConnectionsServiceUUID.put(pcUUID, serviceUUD);
		this.peerConnectionsSSRCs.put(pcUUID, SSRCs);
		return this;
	}


	@Override
	protected Completable doPerform() {
		if (this.peerConnectionsSSRCs.size() < 1) {
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
				this.updateExistingSSRCs();
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

	private void updateExistingSSRCs() {
		Map<String, Tuple2<UUID, Long>> mappedStreamKeys = new HashMap<>();
		Iterator<Map.Entry<UUID, UUID>> it = this.peerConnectionsServiceUUID.entrySet().iterator();
		for (; it.hasNext(); ) {
			Map.Entry<UUID, UUID> entry = it.next();
			UUID pcUUID = entry.getKey();
			UUID serviceUUID = entry.getValue();
			Set<Long> SSRCs = this.peerConnectionsSSRCs.get(pcUUID);
			for (Long SSRC : SSRCs) {
				String streamKey = SynchronizationSourcesRepository.getKey(serviceUUID, SSRC);
				mappedStreamKeys.put(streamKey, new Tuple2<>(pcUUID, SSRC));
				Set<String> streamKeys = this.pcsStreamKeys.get(pcUUID);
				if (streamKeys == null) {
					streamKeys = new HashSet<>();
					this.pcsStreamKeys.put(pcUUID, streamKeys);
				}
				streamKeys.add(streamKey);
			}
		}
		this.foundEntities = this.SSRCRepository.findAll(mappedStreamKeys.keySet());
		this.missingStreamKeys =
				mappedStreamKeys.entrySet().stream()
						.filter(kv -> foundEntities.get(kv.getKey()) == null)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private void addMissingSSRCs() {
		if (this.missingStreamKeys.size() < 1) {
			return;
		}
		this.missingEntities = new HashMap<>();
		Iterator<Map.Entry<String, Tuple2<UUID, Long>>> it = this.missingStreamKeys.entrySet().iterator();
		for (; it.hasNext(); ) {
			Map.Entry<String, Tuple2<UUID, Long>> entry = it.next();
			String streamKey = entry.getKey();
			Tuple2<UUID, Long> tuple = entry.getValue();
			UUID pcUUID = tuple.v1;
			Long SSRC = tuple.v2;
			Set<String> belongingKeys = this.pcsStreamKeys.get(pcUUID);
			if (belongingKeys == null) {
				logger.warn("No existing key belongs to pc {}. SSRC {} s not going to be added", pcUUID, SSRC);
				continue;
			}
			Optional<SynchronizationSourceEntity> belongingEntityHolder =
					belongingKeys.stream().map(foundEntities::get)
							.filter(Objects::nonNull)
							.filter(s -> s.callUUID != null)
							.findFirst();
			if (!belongingEntityHolder.isPresent()) {
				logger.warn("No existing SSRC entity has found with a valid call UUID for pc {}, SSRC {}. this SSRC is not going to be " +
						"added", pcUUID, SSRC);
				continue;
			}
			UUID callUUID = belongingEntityHolder.get().callUUID;
			missingEntities.put(streamKey, SynchronizationSourceEntity.of(
					this.peerConnectionsServiceUUID.get(pcUUID),
					SSRC,
					callUUID
			));
		}
		this.SSRCRepository.saveAll(missingEntities);
	}

	private void removeMissingSSRCs(Throwable exceptionInOperation) {
		this.missingStreamKeys.keySet().stream().forEach(this.SSRCRepository::rxDelete);
	}

	private void rollbackUpdateExistingSSRCs(Throwable exceptionInOperation) {
		// That we should and cannot
	}

	@Override
	protected void validate() {
		super.validate();
	}


}