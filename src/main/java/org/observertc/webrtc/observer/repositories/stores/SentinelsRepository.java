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

package org.observertc.webrtc.observer.repositories.stores;

import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.entities.SentinelEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

/**
 * Repositry to store information related to Peer Connections
 */
@Singleton
@Deprecated
public class SentinelsRepository extends MapRepositoryAbstract<String, SentinelEntity> {

	private static final Logger logger = LoggerFactory.getLogger(SentinelsRepository.class);

	private static final String HAZELCAST_MAP_KEY = "WebRTCObserverSentinels";

	private final Queue<ObserverConfig.SentinelConfig> messages;

	public SentinelsRepository(ObserverHazelcast observerHazelcast, ObserverConfig config) {
		super(observerHazelcast, HAZELCAST_MAP_KEY);
		this.messages = new LinkedList<>();
		if (Objects.nonNull(config.sentinels)) {
			config.sentinels.forEach(this.messages::add);
		}
	}

	@PostConstruct
	void setup() {
		if (0 < this.messages.size()) {
			while(!this.messages.isEmpty()) {
				ObserverConfig.SentinelConfig config = this.messages.poll();
				Optional<SentinelEntity> sentinelEntityHolder = this.make(config);
				if (!sentinelEntityHolder.isPresent()) {
					continue;
				}
				SentinelEntity entity = sentinelEntityHolder.get();
				this.saveIfAbsent(entity.name, entity);
			}
		}

	}

	private Optional<SentinelEntity> make(ObserverConfig.SentinelConfig config) {
		if (Objects.isNull(config)) {
			return Optional.empty();
		}

		SentinelEntity entity = SentinelEntity.of(config.name);
		if (Objects.nonNull(config.callFilters)) {
			entity.callFilters.addAll(config.callFilters);
		}
		if (Objects.nonNull(config.addresses)) {
			entity.addresses.addAll(config.addresses);
		}
		return Optional.of(entity);
	}
}
