package org.observertc.webrtc.service.processors.mediastreams.valueadapters;

import org.apache.commons.lang3.NotImplementedException;
import org.observertc.webrtc.common.reports.MediaType;
import org.observertc.webrtc.service.dto.webextrapp.Kind;

public class MediaTypeConverter {

	public static MediaType fromKind(Kind kind) {
		if (kind == null) {
			return null;
		}
		switch (kind) {
			case AUDIO:
				return MediaType.AUDIO;
			case VIDEO:
				return MediaType.VIDEO;
			default:
				throw new NotImplementedException("MediaType for " + kind.name() + " is not implemented");
		}
	}
}
