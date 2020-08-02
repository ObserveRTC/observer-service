package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.CandidatePairState;
import org.observertc.webrtc.common.reports.ICECandidatePairReport;

public class ICECandidatePairEntry implements BigQueryEntry {

	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String WRITABLE_FIELD_NAME = "writable";
	public static final String TOTAL_ROUND_TRIP_TIME_FIELD_NAME = "totalRoundTripTime";
	public static final String STATE_FIELD_NAME = "state";
	public static final String NOMINATED_FIELD_NAME = "nominated";
	public static final String AVAILABLE_OUTGOING_BITRATE_FIELD_NAME = "availableOutgoingBitrate";
	public static final String BYTES_RECEIVED_FIELD_NAME = "bytesReceived";
	public static final String BYTES_SENT_FIELD_NAME = "bytesSent";
	public static final String CONSENT_REQUESTS_SENT_FIELD_NAME = "consentRequests";
	public static final String CURRENT_ROUND_TRIP_TIME_FIELD_NAME = "currentRoundTripTime";
	public static final String PRIORITY_FIELD_NAME = "priority";
	public static final String REQUESTS_RECEIVED_FIELD_NAME = "requestsReceived";
	public static final String REQUESTS_SENT_FIELD_NAME = "requestsSent";
	public static final String RESPONSES_RECEIVED_FIELD_NAME = "responsesReceived";
	public static final String RESPONSES_SENT_FIELD_NAME = "responsesSent";

	public static ICECandidatePairEntry from(ICECandidatePairReport iceCandidatePairReport) {
		if (iceCandidatePairReport == null) {
			return null;
		}
		return new ICECandidatePairEntry()
				.withObserverUUID(iceCandidatePairReport.observerUUID)
				.withPeerConnectionUUID(iceCandidatePairReport.peerConnectionUUID)
				.withTimestamp(iceCandidatePairReport.timestamp)
				.withNominated(iceCandidatePairReport.nominated)
				.withAvailableOutgoingBitrate(iceCandidatePairReport.availableOutgoingBitrate)
				.withBytesReceived(iceCandidatePairReport.bytesReceived)
				.withBytesSent(iceCandidatePairReport.bytesSent)
				.withConsentRequestsSent(iceCandidatePairReport.consentRequestsSent)
				.withCurrentRoundTripTime(iceCandidatePairReport.currentRoundTripTime)
				.withPriority(iceCandidatePairReport.priority)
				.withRequestsReceived(iceCandidatePairReport.requestsReceived)
				.withRequestsSent(iceCandidatePairReport.requestsSent)
				.withResponseReceived(iceCandidatePairReport.responsesReceived)
				.withResponseSent(iceCandidatePairReport.responsesSent)
				.withState(iceCandidatePairReport.state)
				.withTotalRoundTripTime(iceCandidatePairReport.totalRoundTripTime)
				.withWritable(iceCandidatePairReport.writable);


	}

	private ICECandidatePairEntry withWritable(Boolean value) {
		this.values.put(WRITABLE_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withTotalRoundTripTime(Double value) {
		this.values.put(TOTAL_ROUND_TRIP_TIME_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withState(CandidatePairState value) {
		String state = null;
		if (value != null) {
			state = value.name();
		}
		this.values.put(STATE_FIELD_NAME, state);
		return this;
	}

	private ICECandidatePairEntry withResponseSent(Integer value) {
		this.values.put(RESPONSES_SENT_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withResponseReceived(Integer value) {
		this.values.put(RESPONSES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withRequestsSent(Integer value) {
		this.values.put(REQUESTS_SENT_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withRequestsReceived(Integer value) {
		this.values.put(REQUESTS_RECEIVED_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withPriority(Integer value) {
		this.values.put(PRIORITY_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withCurrentRoundTripTime(Double value) {
		this.values.put(CURRENT_ROUND_TRIP_TIME_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withConsentRequestsSent(Integer value) {
		this.values.put(CONSENT_REQUESTS_SENT_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withBytesSent(Integer value) {
		this.values.put(BYTES_SENT_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withBytesReceived(Integer value) {
		this.values.put(BYTES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withAvailableOutgoingBitrate(Integer value) {
		this.values.put(AVAILABLE_OUTGOING_BITRATE_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withNominated(Boolean value) {
		this.values.put(NOMINATED_FIELD_NAME, value);
		return this;
	}

	private ICECandidatePairEntry withTimestamp(LocalDateTime value) {
		this.values.put(TIMESTAMP_FIELD_NAME, value);
		return this;
	}

	private final Map<String, Object> values;

	public ICECandidatePairEntry() {
		this.values = new HashMap<>();
	}

	public ICECandidatePairEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public ICECandidatePairEntry withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
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
		String value = (String) this.values.get(PEER_CONNECTION_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public Map<String, Object> toMap() {
		return this.values;
	}
}
