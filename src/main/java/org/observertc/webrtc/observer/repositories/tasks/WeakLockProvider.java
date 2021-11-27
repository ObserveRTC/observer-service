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

package org.observertc.webrtc.observer.repositories.tasks;

import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.dto.WeakLockDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.UUID;

@Singleton
public class WeakLockProvider {
    private static final int DEFAULT_MAX_WAITING_TIME_IN_S = 15;
    private static final Logger logger = LoggerFactory.getLogger(WeakLockProvider.class);

    @Inject
    HazelcastMaps hazelcastMaps;

	@Inject
	ObserverHazelcast observerHazelcast;

    public WeakLockProvider() {

	}

	WeakSpinLock makeSpinLock(String name) {
        UUID endpointId = this.observerHazelcast.getLocalEndpointId();
        final String instance = Objects.isNull(endpointId) ? "noName" : endpointId.toString();
        final WeakLockDTO lockEntity = WeakLockDTO.of(name, instance);
        return new WeakSpinLock(hazelcastMaps.getWeakLocks(), lockEntity);
    }

    public AutoCloseable autoLock(String name) {
        return this.autoLock(name, DEFAULT_MAX_WAITING_TIME_IN_S);
    }

    public AutoCloseable autoLock(String name, int maxWaitingTimeInS) {
        WeakSpinLock spinLock = this.makeSpinLock(name);
        spinLock.lock(maxWaitingTimeInS);
        return spinLock::unlock;
    }


}
