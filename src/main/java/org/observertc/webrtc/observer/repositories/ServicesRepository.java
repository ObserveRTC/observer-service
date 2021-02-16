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

import com.hazelcast.map.IMap;
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

	private IMap<String, ServiceDTO> services;

//	private final Map<UUID, String> serviceMap = new ConcurrentHashMap<>();
	private final Queue<ObserverConfig.ServiceConfiguration> messages;
	public ServicesRepository(ObserverHazelcast observerHazelcast, ObserverConfig config) {
		this.messages = new LinkedList<>();
//		if (Objects.nonNull(config.services)) {
//			config.services.forEach(this.messages::add);
//		}
	}

	@PostConstruct
	void setup() {
		this.services = observerHazelcast.getInstance().getMap("observertc-services");
//		if (0 < this.messages.size()) {
//			while(!this.messages.isEmpty()) {
//				ObserverConfig.ServiceConfiguration config = this.messages.poll();
//				Optional<ServiceDTO> serviceEntityOptional = this.make(config);
//				if (!serviceEntityOptional.isPresent()) {
//					continue;
//				}
//				ServiceDTO entity = serviceEntityOptional.get();
//				this.services.putIfAbsent(entity.serviceName, entity);
//			}
//			return;
//		}
	}

	public Map<String, ServiceDTO> getAllEntries() {
		Set<String> keys = this.services.keySet();
		return this.services.getAll(keys);
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
