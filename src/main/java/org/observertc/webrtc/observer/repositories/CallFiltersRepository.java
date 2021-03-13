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
import org.observertc.webrtc.observer.dto.CallFilterDTO;
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
public class CallFiltersRepository {
	private static final Logger logger = LoggerFactory.getLogger(CallFiltersRepository.class);

	@Inject
	HazelcastMaps hazelcastMaps;

	@Inject
	ObserverConfig config;

	@Inject
	ObserverHazelcast observerHazelcast;


	public CallFiltersRepository() {

	}

	@PostConstruct
	void setup() {
		if (Objects.nonNull(config.callFilters)) {
			for (CallFilterDTO callFilterDTO : config.callFilters) {
				this.hazelcastMaps.getCallFilterDTOs().put(callFilterDTO.name, callFilterDTO);
			}
		}
	}

	public Map<String, CallFilterDTO> findAll() {
		return this.hazelcastMaps.getCallFilterDTOs().entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue
				));
	}

	public Map<String, CallFilterDTO> findByNames(Set<String> names) {
		return this.hazelcastMaps.getCallFilterDTOs().getAll(names);
	}

	public Optional<CallFilterDTO> findByName(String name) {
		var result = this.hazelcastMaps.getCallFilterDTOs().get(name);
		if (Objects.isNull(result)) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	public CallFilterDTO save(CallFilterDTO callFilterDTO) {
		var found = this.hazelcastMaps.getCallFilterDTOs().putIfAbsent(callFilterDTO.name, callFilterDTO);
		if (Objects.nonNull(found)) {
			return found;
		}
		return callFilterDTO;
	}

	public CallFilterDTO delete(String filterName) {
		return this.hazelcastMaps.getCallFilterDTOs().remove(filterName);
	}

	public CallFilterDTO update(CallFilterDTO callFilterDTO) {
		var result = this.hazelcastMaps.getCallFilterDTOs().put(callFilterDTO.name, callFilterDTO);
		if (Objects.isNull(result)) {
			return callFilterDTO;
		}
		return result;
	}

}
