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
import org.observertc.webrtc.observer.entities.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.stores.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.stores.RepositoryProvider;
import org.observertc.webrtc.observer.repositories.stores.SynchronizationSourcesRepository;
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
public class PeerConnectionsUpdaterTask extends TaskAbstract<Void> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(PeerConnectionsUpdaterTask.class);
	private enum State {
		CREATED,
		EXISTING_ENTITIES_ARE_UPDATED,
		MISSING_ENTITIES_ARE_ADDED,
		EXECUTED,
		ROLLEDBACK,
	}

	private class PCStream {
		final UUID serviceUUID;
		final UUID pcUUID;
		final Long SSRC;

		private PCStream(UUID serviceUUID, UUID pcUUID, Long ssrc) {
			this.serviceUUID = serviceUUID;
			this.pcUUID = pcUUID;
			SSRC = ssrc;
		}
	}



	private final SynchronizationSourcesRepository SSRCRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private State state = State.CREATED;

	private Queue<PCStream> addedStreams = new LinkedList<>();
	private Queue<PCStream> missingStreams = new LinkedList<>();

	public PeerConnectionsUpdaterTask(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.SSRCRepository = repositoryProvider.getSSRCRepository();
		this.peerConnectionsRepository = repositoryProvider.getPeerConnectionsRepository();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}


	public PeerConnectionsUpdaterTask addStream(@NotNull UUID serviceUUD, @NotNull UUID pcUUID, @NotNull Long SSRC) {
		PCStream pcStream = new PCStream(serviceUUD, pcUUID, SSRC);
		this.addedStreams.add(pcStream);
		return this;
	}

	@Override
	protected Void perform() {
		switch (this.state) {
			default:
			case CREATED:
				this.collectMissingEntities();
				this.state = State.EXISTING_ENTITIES_ARE_UPDATED;
			case EXISTING_ENTITIES_ARE_UPDATED:
				this.addMissingPCStreams();
				this.state = State.MISSING_ENTITIES_ARE_ADDED;
			case MISSING_ENTITIES_ARE_ADDED:
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
		}
		return null;
	}

	protected void rollback(Throwable t) {
		try {
			switch (this.state) {
				case EXECUTED:
				case MISSING_ENTITIES_ARE_ADDED:
					this.removeMissingPCStreams(t);
					this.state = State.EXISTING_ENTITIES_ARE_UPDATED;
				case EXISTING_ENTITIES_ARE_UPDATED:
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
		if (this.addedStreams.size() < 1) {
			throw new IllegalStateException("No stream added to find");
		}
		super.validate();
	}

	private void collectMissingEntities() {
		Set<String> SSRCKeys = this.addedStreams
				.stream()
				.map(pcStream -> SynchronizationSourcesRepository.getKey(pcStream.serviceUUID, pcStream.SSRC))
				.collect(Collectors.toSet());

		Map<String, SynchronizationSourceEntity> foundEntities = this.SSRCRepository.findAll(SSRCKeys);
		this.addedStreams.stream()
				.filter(pcStream -> !foundEntities.containsKey(SynchronizationSourcesRepository.getKey(pcStream.serviceUUID, pcStream.SSRC)))
				.forEach(this.missingStreams::add);
	}

	private void addMissingPCStreams() {
		if (this.missingStreams.isEmpty()) {
			return;
		}
		this.missingStreams
				.stream()
				.forEach(this::addMissingPCStream);
	}

	private void addMissingPCStream(PCStream pcStream) {
		Optional<PeerConnectionEntity> pcEntityHolder = this.peerConnectionsRepository.find(pcStream.pcUUID);
		if (!pcEntityHolder.isPresent()) {
			this.getLogger().warn("Cannot find pcEntity for UUID {}", pcStream.pcUUID);
			return;
		}
		PeerConnectionEntity pcEntity = pcEntityHolder.get();
		if (Objects.isNull(pcEntity.callUUID)) {
			this.getLogger().warn("No callUUID exists in pcEntity {}", pcEntity);
			return;
		}

		this.SSRCRepository.save(
				SynchronizationSourcesRepository.getKey(pcStream.serviceUUID, pcStream.SSRC),
				SynchronizationSourceEntity.of(pcStream.serviceUUID, pcStream.SSRC, pcEntity.callUUID)
		);
	}


	private void removeMissingPCStreams(Throwable t) {
		try {
			if (this.missingStreams.isEmpty()) {
				return;
			}
			Set<String> keys = this.missingStreams
					.stream()
					.map(pcStream -> SynchronizationSourcesRepository.getKey(pcStream.serviceUUID, pcStream.SSRC))
					.collect(Collectors.toSet());
			this.SSRCRepository.removeAll(keys);
		} catch (Exception ex) {
			this.getLogger().warn("Exception occured during rollback process", ex);
		}
	}

}