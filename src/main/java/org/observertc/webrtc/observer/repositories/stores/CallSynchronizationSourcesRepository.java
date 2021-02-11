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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;

/**
 * Call to SSRC source keys
 */
@Singleton
public class CallSynchronizationSourcesRepository extends MultiMapRepositoryAbstract<UUID, String> {

	private static final Logger logger = LoggerFactory.getLogger(CallSynchronizationSourcesRepository.class);

	private static final String HAZELCAST_MAP_KEY = "WebRTCObserverCallSynchronozationSources";


	public CallSynchronizationSourcesRepository(ObserverHazelcast observerHazelcast) {
		super(observerHazelcast, HAZELCAST_MAP_KEY);
	}
}
