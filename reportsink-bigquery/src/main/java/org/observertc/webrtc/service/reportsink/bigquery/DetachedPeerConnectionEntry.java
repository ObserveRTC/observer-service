package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.DetachedPeerConnection;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;

public class DetachedPeerConnectionEntry implements BigQueryEntry {
	public static DetachedPeerConnectionEntry from(DetachedPeerConnection detachedPeerConnection) {
		return new DetachedPeerConnectionEntry()
				.withObserverUUID(detachedPeerConnection.getObserverUUID())
				.withPeerConnectionUUID(detachedPeerConnection.getPeerConnectionUUID())
				.withCallUUID(detachedPeerConnection.getCallUUID())
				.withDetachedTimestamp(detachedPeerConnection.getTimestamp());
	}

	public static DetachedPeerConnectionEntry from(DetachedPeerConnectionReport detachedPeerConnectionReport) {
		return new DetachedPeerConnectionEntry()
				.withObserverUUID(detachedPeerConnectionReport.observerUUID)
				.withPeerConnectionUUID(detachedPeerConnectionReport.peerConnectionUUID)
				.withCallUUID(detachedPeerConnectionReport.callUUID)
				.withDetachedTimestamp(detachedPeerConnectionReport.detached);
	}

	private static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	private static final String CALL_UUID_FIELD_NAME = "callUUID";
	private static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	private static final String DETACHED_TIMESTAMP_FIELD_NAME = "detached";

	private final Map<String, Object> values;

	public DetachedPeerConnectionEntry() {
		this.values = new HashMap<>();
	}

	public DetachedPeerConnectionEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public DetachedPeerConnectionEntry withCallUUID(UUID value) {
		this.values.put(CALL_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public DetachedPeerConnectionEntry withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public DetachedPeerConnectionEntry withDetachedTimestamp(LocalDateTime value) {
		ZoneId zoneId = ZoneId.systemDefault();
		Long epoch = value.atZone(zoneId).toEpochSecond();
		this.values.put(DETACHED_TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	public UUID getObserverUUID() {
		String value = (String) this.values.get(OBSERVER_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public UUID getCallUUID() {
		String value = (String) this.values.get(CALL_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public UUID getPeerConnectionUUID() {
		String value = (String) this.values.get(PEER_CONNECTION_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public LocalDateTime getDetachedTimestamp() {
		Long value = (Long) this.values.get(DETACHED_TIMESTAMP_FIELD_NAME);
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
