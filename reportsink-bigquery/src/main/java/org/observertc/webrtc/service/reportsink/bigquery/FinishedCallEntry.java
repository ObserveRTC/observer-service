package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.FinishedCallReport;

public class FinishedCallEntry implements BigQueryEntry {

	public static FinishedCallEntry from(FinishedCallReport finishedCallReport) {
		return new FinishedCallEntry()
				.withObserverUUID(finishedCallReport.observerUUID)
				.setCallUUID(finishedCallReport.callUUID)
				.setFinishedTimestamp(finishedCallReport.finished);
	}

	private static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	private static final String CALL_UUID_FIELD_NAME = "callUUID";
	private static final String FINISHED_TIMESTAMP_FIELD_NAME = "finished";

	private final Map<String, Object> values;

	public FinishedCallEntry() {
		this.values = new HashMap<>();
	}

	public FinishedCallEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public FinishedCallEntry setCallUUID(UUID value) {
		this.values.put(CALL_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public FinishedCallEntry setFinishedTimestamp(LocalDateTime value) {
		ZoneId zoneId = ZoneId.systemDefault();
		Long epoch = value.atZone(zoneId).toEpochSecond();
		this.values.put(FINISHED_TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	public UUID getObserverUUID() {
		String value = (String) this.values.get(OBSERVER_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public UUID getPeerConnectionUUID() {
		String value = (String) this.values.get(CALL_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public LocalDateTime getFinishedTimestamp() {
		Long value = (Long) this.values.get(FINISHED_TIMESTAMP_FIELD_NAME);
		if (value == null) {
			return null;
		}
		ZoneId zoneId = ZoneId.systemDefault();
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), zoneId);
	}

	public Map<String, Object> toMap() {
		return this.values;
	}
}
