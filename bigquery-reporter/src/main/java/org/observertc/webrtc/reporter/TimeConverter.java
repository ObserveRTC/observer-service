package org.observertc.webrtc.reporter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeConverter {

	private static final ZoneId GMT_ZONE_ID = ZoneId.of("GMT");

	public static LocalDateTime epochToGMTLocalDateTime(Long epoch) {
		if (epoch == null) {
			return null;
		}
		Instant epochInstant = Instant.ofEpochMilli(epoch);
		LocalDateTime result = LocalDateTime.ofInstant(epochInstant, GMT_ZONE_ID);
		return result;
	}

	public static Long GMTLocalDateTimeToEpoch(LocalDateTime incomingTs) {
		if (incomingTs == null) {
			return null;
		}
		Long result = incomingTs.atZone(GMT_ZONE_ID).toEpochSecond();
		return result;
	}
}
