package org.observertc.webrtc.service.processors.mediastreams.valueadapters;

import org.apache.commons.lang3.NotImplementedException;
import org.observertc.webrtc.common.reports.MediaType;
import org.observertc.webrtc.service.dto.webextrapp.Kind;
import org.observertc.webrtc.service.dto.webextrapp.QualityLimitationReason;

public class StringConverter {

	public static String toString(Object value) {
		if (value == null) {
			return null;
		}
		return value.toString();
	}
}
