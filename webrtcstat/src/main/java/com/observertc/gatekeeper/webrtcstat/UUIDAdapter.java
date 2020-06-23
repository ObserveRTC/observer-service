package com.observertc.gatekeeper.webrtcstat;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

public class UUIDAdapter {
	/**
	 * Make bytes from UUID
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
	 * Make UUID from bytes
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
