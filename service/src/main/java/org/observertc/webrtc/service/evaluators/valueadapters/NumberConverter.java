package org.observertc.webrtc.service.evaluators.valueadapters;

public class NumberConverter {

	public static <T extends Number> Integer toInt(T value) {
		if (value == null) {
			return null;
		}
		return value.intValue();
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
