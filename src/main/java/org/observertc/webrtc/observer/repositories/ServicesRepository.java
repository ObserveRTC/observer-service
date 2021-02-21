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
import org.observertc.webrtc.observer.dto.ServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class ServicesRepository {
	private static final Logger logger = LoggerFactory.getLogger(ServicesRepository.class);

	@Inject
	ObserverHazelcast observerHazelcast;

	@Inject
	HazelcastMaps hazelcastMaps;

	@Inject
	ObserverConfig config;


	private final Map<UUID, String> serviceMap = new HashMap<>();

	public ServicesRepository() {

	}

	@PostConstruct
	void setup() {
		List<ObserverConfig.ServiceConfiguration> configs = this.config.services;
		if (Objects.nonNull(configs)) {
			for (ObserverConfig.ServiceConfiguration config : configs) {
				Optional<ServiceDTO> serviceEntityOptional = this.make(config);
				if (!serviceEntityOptional.isPresent()) {
					continue;
				}
				ServiceDTO entity = serviceEntityOptional.get();
				for (UUID serviceUUID : entity.serviceUUIDs) {
					this.serviceMap.put(serviceUUID, entity.serviceName);
				}
				this.hazelcastMaps.getServiceDTOs().putIfAbsent(entity.serviceName, entity);
			}
		}
	}

	public Map<String, ServiceDTO> getAllEntries() {
		Set<String> keys = this.hazelcastMaps.getServiceDTOs().keySet();
		return this.hazelcastMaps.getServiceDTOs().getAll(keys);
	}

	public boolean hasServiceUUID(UUID serviceUUID) {
		return this.serviceMap.containsKey(serviceUUID);
	}

	public String resolve(UUID serviceUUID) {
		return this.serviceMap.getOrDefault(serviceUUID, this.config.outboundReports.defaultServiceName);
	}

	private Optional<ServiceDTO> make(ObserverConfig.ServiceConfiguration config) {
		if (Objects.isNull(config)) {
			return Optional.empty();
		}

		ServiceDTO entity = ServiceDTO.of(config.name);
		if (Objects.nonNull(config.uuids)) {
			config.uuids.stream().forEach(entity.serviceUUIDs::add);
		}
		return Optional.of(entity);
	}

}
