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

import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.entities.SynchronizationSourceEntity;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Repository to store the (serviceUUID, SSRC) -> calls binding.
 */
@Singleton
public class SynchronizationSourcesRepository extends MapRepositoryAbstract<String, SynchronizationSourceEntity> {

	private static final String HAZELCAST_IMAP_NAME = "WebRTCObserverSynchronizationSources";
	private static final String DELIMITER = "#!#";

	public static String getKey(UUID serviceUUID, Long SSRC) {
		return String.format("%s%s%d", serviceUUID.toString(), DELIMITER, SSRC);
	}

	public Map.Entry<UUID, Long> splitKey(String key) {
		if (Objects.isNull(key)) {
			return null;
		}
		String[] parts = key.split(DELIMITER);
		UUID uuid = UUID.fromString(parts[0]);
		Long SSRC = null;
		try {
			SSRC = Long.parseLong(parts[1]);
		} catch (Throwable t) {

		}
		return Map.entry(uuid, SSRC);
	}

	public SynchronizationSourcesRepository(ObserverHazelcast observerHazelcast) {
		super(observerHazelcast, HAZELCAST_IMAP_NAME);
	}
}
