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

package org.observertc.webrtc.observer.repositories.hazelcast;

import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.models.ICEConnectionEntity;

import javax.inject.Singleton;
import java.util.UUID;

/**
 * Repository to store the (serviceUUID, SSRC) -> calls binding.
 */
@Singleton
public class ICEConnectionsRepository extends MapRepositoryAbstract<String, ICEConnectionEntity> {

	private static final String HAZELCAST_IMAP_NAME = "WebRTCObserverICEConnections";

	public static String getKey(UUID pcUUID, String localCandidateId, String remoteCandidateId) {
		return String.format("%s-%s-%s", pcUUID.toString(), localCandidateId, remoteCandidateId);
	}

	public ICEConnectionsRepository(ObserverHazelcast observerHazelcast) {
		super(observerHazelcast, HAZELCAST_IMAP_NAME);
	}
}
