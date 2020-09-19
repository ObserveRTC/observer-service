package org.observertc.webrtc.observer.evaluators.valueadapters;

import io.micronaut.context.annotation.Prototype;
import java.nio.ByteBuffer;

@Prototype
public class NumberConverter {

	public <T extends Number> Integer toInt(T value) {
		if (value == null) {
			return null;
		}
		return value.intValue();
	}

	public <T extends Number> Short toShort(T value) {
		if (value == null) {
			return null;
		}
		return value.shortValue();
	}

	public byte[] longToBytes(Long value) {
		if (value == null) {
			return null;
		}
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(value);
		return buffer.array();
	}

	public <T extends Number> Double toDouble(T value) {
		if (value == null) {
			return null;
		}
		return value.doubleValue();
	}

	public <T extends Number> Long toLong(T value) {
		if (value == null) {
			return null;
		}
		return value.longValue();
	}

	public <T extends Number> Float toFloat(T value) {
		if (value == null) {
			return null;
		}
		return value.floatValue();
	}

}
