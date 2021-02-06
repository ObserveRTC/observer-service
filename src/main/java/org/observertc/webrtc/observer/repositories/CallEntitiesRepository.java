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

import com.hazelcast.core.HazelcastInstance;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.entities.CallEntity;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class CallEntitiesRepository extends MapRepositoryAbstract<UUID, CallEntity> {
	private static final String HAZELCAST_MAP_CALLS_MAP_KEY = "WebRTCObserverCalls";

//	private final IMap<UUID, CallEntity> entities;

	public CallEntitiesRepository(ObserverHazelcast observerHazelcast) {
		super(observerHazelcast, HAZELCAST_MAP_CALLS_MAP_KEY);
		HazelcastInstance hazelcastInstance = observerHazelcast.getInstance();
	}
}
