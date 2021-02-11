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

package org.observertc.webrtc.observer.entities;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import org.observertc.webrtc.observer.common.UUIDAdapter;

import java.util.UUID;

public class EntityFactory implements PortableFactory {

	public static final UUID DEFAULT_UUID = UUID.fromString("7c55dd42-614d-42d9-9b1d-8fac560d71e3");
	public static final byte[] DEFAULT_UUID_BYTES = UUIDAdapter.toBytes(DEFAULT_UUID);
	public static final int FACTORY_ID = 10000000; // 2^30

	@Override
	public Portable create(int classId) {
		switch (classId) {
			case PeerConnectionEntity.CLASS_ID:
				return new PeerConnectionEntity();
			case SynchronizationSourceEntity.CLASS_ID:
				return new SynchronizationSourceEntity();
			case OldCallEntity.CLASS_ID:
				return new OldCallEntity();
			case WeakLockEntity.CLASS_ID:
				return new WeakLockEntity();
			case ServiceEntity.CLASS_ID:
				return new ServiceEntity();
			case SentinelEntity.CLASS_ID:
				return new SentinelEntity();
		}
		throw new IllegalArgumentException("Unsupported type " + classId);
	}
}
