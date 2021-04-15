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

package org.observertc.webrtc.observer.configs.stores;

import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
import org.observertc.webrtc.observer.entities.ServiceMapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Singleton
public class ServiceMapsStore extends StoreAbstract<String, ServiceMapEntity> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceMapsStore.class);

	@Inject
	ObserverConfigDispatcher configDispatcher;

	private final AtomicReference<Map<UUID, ServiceMapEntity>> serviceMapEntitiesHolder = new AtomicReference<>(Collections.EMPTY_MAP);

	@PostConstruct
	void setup() {
		ObserverConfig defaultConfig = configDispatcher.getConfig();
		this.process(defaultConfig);
		this.configDispatcher.onServiceMappingsChanged()
				.map(event -> event.config)
				.subscribe(this::process);
	}

	private void process(ObserverConfig observerConfig) {
		Map<String, ServiceMapEntity> serviceMaps = observerConfig.servicemappings
				.stream()
				.collect(Collectors.toMap(
						c -> c.name,
						c -> ServiceMapEntity.builder()
								.withServiceName(c.name)
								.withUUIDs(c.uuids)
								.build()
						)
				);
		this.setMap(serviceMaps);
		Map<UUID, ServiceMapEntity> serviceMapEntities = new HashMap<>();
		serviceMaps.values().stream()
				.forEach(serviceMapEntity -> {
					serviceMapEntity.uuids.stream().forEach(uuid -> {
						serviceMapEntities.put(uuid, serviceMapEntity);
					});
				});
		this.serviceMapEntitiesHolder.set(serviceMapEntities);
	}

	public Optional<ServiceMapEntity> findByUUID(UUID uuid) {
		Map<UUID, ServiceMapEntity> map = this.serviceMapEntitiesHolder.get();
		ServiceMapEntity result = map.get(uuid);
		if (Objects.isNull(result)) {
			return Optional.empty();
		} else {
			return Optional.of(result);
		}
	}
}
