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

package org.observertc.webrtc.observer.repositories;

import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.dto.SentinelDTO;
import org.observertc.webrtc.observer.entities.SentinelEntity;
import org.observertc.webrtc.observer.repositories.tasks.FetchSentinelEntitiesTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class SentinelsRepository {
	private static final Logger logger = LoggerFactory.getLogger(SentinelsRepository.class);

	@Inject
	HazelcastMaps hazelcastMaps;

	@Inject
    CallFiltersRepository callFiltersRepository; // we must invoke this to have a proper result

	@Inject
	PeerConnectionFiltersRepository peerConnectionFiltersRepository; // we must invoke this to have a proper result

	@Inject
	Provider<FetchSentinelEntitiesTask> sentinelEntitiesTaskProvider;

	@Inject
	ObserverConfig config;

	@Inject
	ObserverHazelcast observerHazelcast;


	public SentinelsRepository() {

	}

	@PostConstruct
	void setup() {
		if (Objects.nonNull(config.sentinels)) {
			for (SentinelDTO sentinelDTO : config.sentinels) {
				this.hazelcastMaps.getSentinelDTOs().put(sentinelDTO.name, sentinelDTO);
			}
		}
	}

	public Optional<SentinelEntity> findEntity(String name) {
		var task = sentinelEntitiesTaskProvider.get()
				.whereSentinelNames(name);

		if (!task.execute().succeeded()) {
			return Optional.empty();
		}

		return task.getResult().values().stream().findFirst();
	}

	public Map<String, SentinelEntity> fetchAllEntities() {
		Set<String> sentinelNames = this.hazelcastMaps.getSentinelDTOs().keySet();

		var task = this.sentinelEntitiesTaskProvider.get()
				.whereSentinelNames(sentinelNames);

		if (!task.execute().succeeded()) {
			return Collections.EMPTY_MAP;
		}
		return task.getResult();
	}


	public Map<String, SentinelDTO> findAll() {
		return this.hazelcastMaps.getSentinelDTOs().entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue
				));
	}

	public Map<String, SentinelDTO> findByNames(Set<String> names) {
		return this.hazelcastMaps.getSentinelDTOs().getAll(names);
	}

	public Optional<SentinelDTO> findByName(String name) {
		var result = this.hazelcastMaps.getSentinelDTOs().get(name);
		if (Objects.isNull(result)) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	public SentinelDTO save(SentinelDTO sentinelDTO) {
		var found = this.hazelcastMaps.getSentinelDTOs().putIfAbsent(sentinelDTO.name, sentinelDTO);
		if (Objects.nonNull(found)) {
			return found;
		}
		return sentinelDTO;
	}

	public SentinelDTO delete(String filterName) {
		return this.hazelcastMaps.getSentinelDTOs().remove(filterName);
	}

	public SentinelDTO update(SentinelDTO sentinelDTO) {
		var result = this.hazelcastMaps.getSentinelDTOs().put(sentinelDTO.name, sentinelDTO);
		if (Objects.isNull(result)) {
			return sentinelDTO;
		}
		return result;
	}




}
