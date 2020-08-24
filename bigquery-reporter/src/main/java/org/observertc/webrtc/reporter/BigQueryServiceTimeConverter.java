package org.observertc.webrtc.reporter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class BigQueryServiceTimeConverter {
	private static BigQueryServiceTimeConverter INSTANCE = null;

	public static void construct(ZoneId incomingTimestampsZoneId) {
		if (INSTANCE != null) {
			return;
		}
		INSTANCE = new BigQueryServiceTimeConverter(incomingTimestampsZoneId);
	}

	public static BigQueryServiceTimeConverter getInstance() {
		return INSTANCE;
	}

	private final ZoneId incomingTimestampsZoneId;

	private BigQueryServiceTimeConverter(ZoneId incomingTimestampsZoneId) {
		this.incomingTimestampsZoneId = incomingTimestampsZoneId;
	}


	public Long toEpoch(LocalDateTime incomingTs) {
		if (incomingTs == null) {
			return null;
		}
		Long result = incomingTs.atZone(this.incomingTimestampsZoneId).toEpochSecond();
		return result;
//		LocalDateTime result = incomingTs.atZone(this.incomingTimestampsZoneId)
//				.withZoneSameInstant(this.GMTZoneId)
//				.toLocalDateTime();
//		return result;
	}

	public LocalDateTime toLocalDateTime(Long epoch) {
		if (epoch == null) {
			return null;
		}
		LocalDateTime result = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), this.incomingTimestampsZoneId);
		return result;
	}
}
