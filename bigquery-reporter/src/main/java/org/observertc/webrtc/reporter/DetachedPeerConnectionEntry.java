package org.observertc.webrtc.reporter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;

public class DetachedPeerConnectionEntry implements BigQueryEntry {

	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String CALL_UUID_FIELD_NAME = "callUUID";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String BROWSERID_TIMESTAMP_FIELD_NAME = "browserID";
	public static final String DETACHED_TIMESTAMP_FIELD_NAME = "detached";

	public static DetachedPeerConnectionEntry from(DetachedPeerConnectionReport detachedPeerConnectionReport) {
		return new DetachedPeerConnectionEntry()
				.withObserverUUID(detachedPeerConnectionReport.observerUUID)
				.withPeerConnectionUUID(detachedPeerConnectionReport.peerConnectionUUID)
				.withCallUUID(detachedPeerConnectionReport.callUUID)
				.withBrowserID(detachedPeerConnectionReport.browserID)
				.withDetachedTimestamp(detachedPeerConnectionReport.detached);
	}

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

	public DetachedPeerConnectionEntry withBrowserID(String browserId) {
		this.values.put(BROWSERID_TIMESTAMP_FIELD_NAME, browserId);
		return this;
	}

	public DetachedPeerConnectionEntry withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public DetachedPeerConnectionEntry withDetachedTimestamp(LocalDateTime value) {
		Long epoch = TimeConverter.GMTLocalDateTimeToEpoch(value);
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
		return TimeConverter.epochToGMTLocalDateTime(value);
	}

	public Map<String, Object> toMap() {
		return this.values;
	}
}
