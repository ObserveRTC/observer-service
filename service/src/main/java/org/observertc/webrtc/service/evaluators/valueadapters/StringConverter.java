package org.observertc.webrtc.service.evaluators.valueadapters;

public class StringConverter {

	public static String toString(Object value) {
		if (value == null) {
			return null;
		}
		return value.toString();
	}
}
