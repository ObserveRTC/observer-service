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
import org.observertc.webrtc.observer.models.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.SynchronizationSourcesRepository;
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
public class PeerConnectionDetailsFinderTask extends TaskAbstract<Optional<PeerConnectionDetailsFinderTask.Result>> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(PeerConnectionDetailsFinderTask.class);

	public class Result {
		public PeerConnectionEntity pcEntity;
		public Map<String, SynchronizationSourceEntity> ssrcEntities;
	}

	private final SynchronizationSourcesRepository SSRCRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private UUID pcUUID;
	private boolean collectSSRCEntities = true;

	public PeerConnectionDetailsFinderTask(
			RepositoryProvider repositoryProvider
	) {
		super();
		this.SSRCRepository = repositoryProvider.getSSRCRepository();
		this.peerConnectionsRepository = repositoryProvider.getPeerConnectionsRepository();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}


	public PeerConnectionDetailsFinderTask addPCUUID(@NotNull UUID pcUUID) {
		if (Objects.nonNull(this.pcUUID)) {
			if (!this.pcUUID.equals(pcUUID)) {
				throw new IllegalStateException("Only one pcUUID can be requested");
			}
		}
		this.pcUUID = pcUUID;
		return this;
	}

	public PeerConnectionDetailsFinderTask collectSSRCEntities(boolean value) {
		this.collectSSRCEntities = value;
		return this;
	}

	@Override
	protected Optional<Result> perform() {
		Optional<PeerConnectionEntity> peerConnectionEntityOptional = this.peerConnectionsRepository.find(this.pcUUID);
		if (!peerConnectionEntityOptional.isPresent()) {
			return Optional.empty();
		}
		Result result = new Result();
		PeerConnectionEntity pcEntity = peerConnectionEntityOptional.get();
		result.pcEntity = pcEntity;
		if (this.collectSSRCEntities && Objects.nonNull(pcEntity.SSRCs)) {
			Set<String> keys = pcEntity.SSRCs.stream()
					.map(key -> SynchronizationSourcesRepository.getKey(pcEntity.serviceUUID, key))
					.collect(Collectors.toSet());
			result.ssrcEntities = this.SSRCRepository.findAll(keys);
		} else {
			result.ssrcEntities = Collections.EMPTY_MAP;
		}
		return Optional.of(result);
	}

	protected void rollback(Throwable t) {
		// no reason to roll back anything
	}

	@Override
	protected void validate() {
		super.validate();
		Objects.requireNonNull(this.pcUUID);
	}
}