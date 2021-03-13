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
import org.observertc.webrtc.observer.dto.PeerConnectionFilterDTO;
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
public class PeerConnectionFiltersRepository {
	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionFiltersRepository.class);

	@Inject
	HazelcastMaps hazelcastMaps;

	@Inject
	ObserverConfig config;

	@Inject
	ObserverHazelcast observerHazelcast;


	public PeerConnectionFiltersRepository() {

	}

	@PostConstruct
	void setup() {
		if (Objects.nonNull(config.pcFilters)) {
			for (PeerConnectionFilterDTO pcFilterDTO : config.pcFilters) {
				this.hazelcastMaps.getPeerConnectionFilterDTOs().put(pcFilterDTO.name, pcFilterDTO);
			}
		}
	}

	public Map<String, PeerConnectionFilterDTO> findAll() {
		return this.hazelcastMaps.getPeerConnectionFilterDTOs().entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue
				));
	}

	public Map<String, PeerConnectionFilterDTO> findByNames(Set<String> names) {
		return this.hazelcastMaps.getPeerConnectionFilterDTOs().getAll(names);
	}

	public Optional<PeerConnectionFilterDTO> findByName(String name) {
		var result = this.hazelcastMaps.getPeerConnectionFilterDTOs().get(name);
		if (Objects.isNull(result)) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	public PeerConnectionFilterDTO save(PeerConnectionFilterDTO pcFilterDTO) {
		var found = this.hazelcastMaps.getPeerConnectionFilterDTOs().putIfAbsent(pcFilterDTO.name, pcFilterDTO);
		if (Objects.nonNull(found)) {
			return found;
		}
		return pcFilterDTO;
	}

	public PeerConnectionFilterDTO delete(String filterName) {
		return this.hazelcastMaps.getPeerConnectionFilterDTOs().remove(filterName);
	}

	public PeerConnectionFilterDTO update(PeerConnectionFilterDTO pcFilterDTO) {
		var result = this.hazelcastMaps.getPeerConnectionFilterDTOs().put(pcFilterDTO.name, pcFilterDTO);
		if (Objects.isNull(result)) {
			return pcFilterDTO;
		}
		return result;
	}

}
