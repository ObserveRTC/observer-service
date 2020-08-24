package org.observertc.webrtc.observer.evaluators.valueadapters;

import java.util.NoSuchElementException;
import org.observertc.webrtc.common.reports.MediaType;
import org.observertc.webrtc.observer.dto.webextrapp.Kind;

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
				throw new NoSuchElementException("MediaType for " + kind.name() + " is not implemented");
		}
	}
}
