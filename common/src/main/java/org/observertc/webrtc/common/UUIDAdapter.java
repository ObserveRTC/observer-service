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

package org.observertc.webrtc.common;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

public class UUIDAdapter {
	/**
	 * Make bytes from UUID
	 *
	 * @param uuid
	 * @return
	 */
	public static byte[] toBytes(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());

		return bb.array();
	}

	/**
	 * Make bytes from UUID, but returns defaultValue if UUID is null instead of throwing NullPointerExceptoin
	 *
	 * @param uuid
	 * @return
	 */
	public static byte[] toBytesOrDefault(UUID uuid, byte[] defaultValue) {
		if (uuid == null) {
			return defaultValue;
		}
		return UUIDAdapter.toBytes(uuid);
	}

	/**
	 * Make UUID from bytes
	 *
	 * @param bytes
	 * @return
	 */
	public static UUID toUUID(byte[] bytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		Long high = byteBuffer.getLong();
		Long low = byteBuffer.getLong();

		return new UUID(high, low);
	}

	/**
	 * Make UUID from bytes, but returns with the default value if the byte is null
	 *
	 * @param bytes
	 * @return
	 */
	public static UUID toUUIDOrDefault(byte[] bytes, UUID defaultValue) {
		if (bytes == null) {
			return defaultValue;
		}
		return UUIDAdapter.toUUID(bytes);
	}

	/**
	 * Try parsing a string as UUID
	 *
	 * @param candidate
	 * @return
	 */
	public static Optional<UUID> tryParse(String candidate) {
		if (candidate == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(UUID.fromString(candidate));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
