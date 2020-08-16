package org.observertc.webrtc.service.evaluators.valueadapters;

import java.nio.ByteBuffer;

public class NumberConverter {

	public static <T extends Number> Integer toInt(T value) {
		if (value == null) {
			return null;
		}
		return value.intValue();
	}

	public static <T extends Number> Short toShort(T value) {
		if (value == null) {
			return null;
		}
		return value.shortValue();
	}

	public static byte[] longToBytes(Long value) {
		if (value == null) {
			return null;
		}
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(value);
		return buffer.array();
	}

	public static <T extends Number> Double toDouble(T value) {
		if (value == null) {
			return null;
		}
		return value.doubleValue();
	}

	public static <T extends Number> Long toLong(T value) {
		if (value == null) {
			return null;
		}
		return value.longValue();
	}
}
