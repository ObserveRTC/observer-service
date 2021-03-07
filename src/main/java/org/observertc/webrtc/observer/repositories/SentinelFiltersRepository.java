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
import org.observertc.webrtc.observer.dto.SentinelFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class SentinelFiltersRepository {
	private static final Logger logger = LoggerFactory.getLogger(SentinelFiltersRepository.class);

	@Inject
	HazelcastMaps hazelcastMaps;

	@Inject
	ObserverConfig config;

	@Inject
	ObserverHazelcast observerHazelcast;


	public SentinelFiltersRepository() {

	}

	@PostConstruct
	void setup() {
		if (Objects.nonNull(config.sentinelFilters)) {
			for (SentinelFilterDTO sentinelFilterDTO : config.sentinelFilters) {
				this.hazelcastMaps.getSentinelFilterDTOs().put(sentinelFilterDTO.name, sentinelFilterDTO);
			}
		}
	}

	public Map<String, SentinelFilterDTO> findAll() {
		return this.hazelcastMaps.getSentinelFilterDTOs().entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue
				));
	}

	public Map<String, SentinelFilterDTO> findByNames(Set<String> names) {
		return this.hazelcastMaps.getSentinelFilterDTOs().getAll(names);
	}

	public Optional<SentinelFilterDTO> findByName(String name) {
		var result = this.hazelcastMaps.getSentinelFilterDTOs().get(name);
		if (Objects.isNull(result)) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	public SentinelFilterDTO save(SentinelFilterDTO sentinelFilterDTO) {
		var found = this.hazelcastMaps.getSentinelFilterDTOs().putIfAbsent(sentinelFilterDTO.name, sentinelFilterDTO);
		if (Objects.nonNull(found)) {
			return found;
		}
		return sentinelFilterDTO;
	}

	public SentinelFilterDTO delete(String filterName) {
		return this.hazelcastMaps.getSentinelFilterDTOs().remove(filterName);
	}

	public SentinelFilterDTO update(SentinelFilterDTO sentinelFilterDTO) {
		var result = this.hazelcastMaps.getSentinelFilterDTOs().put(sentinelFilterDTO.name, sentinelFilterDTO);
		if (Objects.isNull(result)) {
			return sentinelFilterDTO;
		}
		return result;
	}

}
