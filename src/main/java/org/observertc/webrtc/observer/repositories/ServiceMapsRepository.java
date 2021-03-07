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
import org.observertc.webrtc.observer.entities.ServiceMapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class ServiceMapsRepository {
	private static final Logger logger = LoggerFactory.getLogger(ServiceMapsRepository.class);

	@Inject
	HazelcastMaps hazelcastMaps;

	@Inject
	ObserverConfig config;

	public ServiceMapsRepository() {

	}

	@PostConstruct
	void setup() {
		List<ObserverConfig.ServiceMapConfiguration> configs = this.config.servicemappings;
		if (Objects.nonNull(configs)) {
			for (ObserverConfig.ServiceMapConfiguration config : configs) {
				for (UUID uuid : config.uuids) {
					this.hazelcastMaps.getServiceToUUIDs().put(config.name, uuid);
					this.hazelcastMaps.getUuidToService().putIfAbsent(uuid, config.name);
				}
			}
		}
	}

	public Map<String, ServiceMapEntity> findAll() {
		ServiceMapEntity.Builders builders = ServiceMapEntity.builders();
		this.hazelcastMaps.getServiceToUUIDs().entrySet()
				.stream()
				.forEach(entry -> {
					builders.withUUID(entry.getKey(), entry.getValue());
				});
		return builders.build();
	}

	public Map<String, ServiceMapEntity> findByNames(Set<String> names) {
		ServiceMapEntity.Builders builders = ServiceMapEntity.builders();
		names.stream().forEach(name -> {
			var uuids = this.hazelcastMaps.getServiceToUUIDs().get(name);
			if (Objects.isNull(uuids) || uuids.size() < 1) {
				return;
			}
			builders.withUUIDs(name, uuids);
		});
		return builders.build();
	}

	public Optional<ServiceMapEntity> findByName(String name) {
		ServiceMapEntity.Builder builder = ServiceMapEntity.builder();
		Collection<UUID> uuids = this.hazelcastMaps.getServiceToUUIDs().get(name);
		if (Objects.isNull(uuids) || uuids.size() < 1) {
			return Optional.empty();
		}

		var entity = builder.withServiceName(name).withUUIDs(uuids).build();
		return Optional.of(entity);
	}

	public Map<String, ServiceMapEntity> findByUUIDs(Set<UUID> uuids) {
		var serviceNames = this.hazelcastMaps.getUuidToService().getAll(uuids);
		var names = serviceNames.values().stream().collect(Collectors.toSet());
		if (Objects.isNull(names) || names.size() < 1) {
			return Collections.EMPTY_MAP;
		}
		return this.findByNames(names);
	}

	public Optional<ServiceMapEntity> findByUUID(UUID uuid) {
		var serviceName = this.hazelcastMaps.getUuidToService().get(uuid);
		if (Objects.isNull(serviceName)) {
			return Optional.empty();
		}
		return this.findByName(serviceName);
	}

	public ServiceMapEntity save(ServiceMapEntity candidate) {
		Optional<ServiceMapEntity> foundEntity = this.findByName(candidate.name);
		if (foundEntity.isPresent()) {
			return foundEntity.get();
		}
		for (UUID uuid : candidate.uuids) {
			this.hazelcastMaps.getServiceToUUIDs().put(candidate.name, uuid);
			this.hazelcastMaps.getUuidToService().put(uuid, candidate.name);
		}
		return candidate;
	}

	public ServiceMapEntity delete(String serviceName) {
		Collection<UUID> removedUUIDs = this.hazelcastMaps.getServiceToUUIDs().remove(serviceName);
		if (Objects.isNull(removedUUIDs) || removedUUIDs.size() < 1) {
			return null;
		}
		for (UUID removedUUID : removedUUIDs) {
			this.hazelcastMaps.getUuidToService().remove(removedUUID);
		}
		return ServiceMapEntity.builder()
				.withServiceName(serviceName)
				.withUUIDs(removedUUIDs)
				.build();
	}

	public ServiceMapEntity update(ServiceMapEntity entity) {
		for (UUID uuid : entity.uuids) {
			this.hazelcastMaps.getServiceToUUIDs().put(entity.name, uuid);
			this.hazelcastMaps.getUuidToService().put(uuid, entity.name);
		}
		return this.findByName(entity.name).get();
	}

}
