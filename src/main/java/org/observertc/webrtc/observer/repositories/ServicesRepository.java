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
import org.observertc.webrtc.observer.entities.ServiceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

@Singleton
public class ServicesRepository extends MapRepositoryAbstract<String, ServiceEntity> {
	private static final Logger logger = LoggerFactory.getLogger(ServicesRepository.class);
	private static final String HAZELCAST_IMAP_NAME = "WebRTCObserverServices";

//	private final Map<UUID, String> serviceMap = new ConcurrentHashMap<>();
	private final Queue<ObserverConfig.ServiceConfiguration> messages;
	public ServicesRepository(ObserverHazelcast observerHazelcast, ObserverConfig config) {
		super(observerHazelcast, HAZELCAST_IMAP_NAME);
		this.messages = new LinkedList<>();
		if (Objects.nonNull(config.services)) {
			config.services.forEach(this.messages::add);
		}
	}

	@PostConstruct
	void setup() {
		if (0 < this.messages.size()) {
			while(!this.messages.isEmpty()) {
				ObserverConfig.ServiceConfiguration config = this.messages.poll();
				Optional<ServiceEntity> serviceEntityOptional = this.make(config);
				if (!serviceEntityOptional.isPresent()) {
					continue;
				}
				ServiceEntity entity = serviceEntityOptional.get();
				this.saveIfAbsent(entity.serviceName, entity);
			}
			return;
		}

	}

	private Optional<ServiceEntity> make(ObserverConfig.ServiceConfiguration config) {
		if (Objects.isNull(config)) {
			return Optional.empty();
		}

		ServiceEntity entity = ServiceEntity.of(config.name);
		if (Objects.nonNull(config.uuids)) {
			config.uuids.stream().forEach(entity.serviceUUIDs::add);
		}
		return Optional.of(entity);
	}
}
