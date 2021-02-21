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
import org.observertc.webrtc.observer.dto.SentinelFilterDTO;
import org.observertc.webrtc.observer.entities.SentinelEntity;
import org.observertc.webrtc.observer.repositories.tasks.FetchSentinelEntitiesTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class SentinelsRepository {
	private static final Logger logger = LoggerFactory.getLogger(SentinelsRepository.class);

	@Inject
	HazelcastMaps hazelcastMaps;

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

		if (Objects.nonNull(config.sentinelFilters)) {
			for (SentinelFilterDTO sentinelFilterDTO : config.sentinelFilters) {
				this.hazelcastMaps.getSentinelFilterDTOs().put(sentinelFilterDTO.name, sentinelFilterDTO);
			}
		}
	}

	public Optional<SentinelEntity> find(String name) {
		var task = sentinelEntitiesTaskProvider.get()
				.whereSentinelNames(name);

		if (!task.execute().succeeded()) {
			return Optional.empty();
		}

		return task.getResult().values().stream().findFirst();
	}

	public Map<String, SentinelEntity> fetchAll() {
		Set<String> sentinelNames = this.hazelcastMaps.getSentinelDTOs().keySet();

		var task = this.sentinelEntitiesTaskProvider.get()
				.whereSentinelNames(sentinelNames);

		if (!task.execute().succeeded()) {
			return Collections.EMPTY_MAP;
		}
		return task.getResult();
	}




}
