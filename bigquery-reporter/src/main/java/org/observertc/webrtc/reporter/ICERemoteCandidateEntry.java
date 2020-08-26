package org.observertc.webrtc.reporter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.CandidateType;
import org.observertc.webrtc.common.reports.ICERemoteCandidateReport;
import org.observertc.webrtc.common.reports.ProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ICERemoteCandidateEntry implements BigQueryEntry {

	private static Logger logger = LoggerFactory.getLogger(ICERemoteCandidateEntry.class);

	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String CANDIDATE_ID_FIELD_NAME = "CandidateID";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String CANDIDATE_TYPE_FIELD_NAME = "candidateType";
	public static final String DELETED_FIELD_NAME = "deleted";
	public static final String IP_LSH_FIELD_NAME = "ipLSH";
	public static final String PORT_FIELD_NAME = "port";
	public static final String PRIORITY_FIELD_NAME = "priority";
	public static final String PROTOCOL_TYPE_FIELD_NAME = "protocolType";
	public static final String IP_FLAG_FIELD_NAME = "ipFlag";

	public static ICERemoteCandidateEntry from(ICERemoteCandidateReport iceRemoteCandidateReport) {
		if (iceRemoteCandidateReport == null) {
			return null;
		}
		return new ICERemoteCandidateEntry()
				.withObserverUUID(iceRemoteCandidateReport.observerUUID)
				.withPeerConnectionUUID(iceRemoteCandidateReport.peerConnectionUUID)
				.withCandidateID(iceRemoteCandidateReport.candidateID)
				.withTimestamp(iceRemoteCandidateReport.timestamp)
				.withCandidateType(iceRemoteCandidateReport.candidateType)
				.withDeleted(iceRemoteCandidateReport.deleted)
				.withIPLSH(iceRemoteCandidateReport.ipLSH)
				.withPort(iceRemoteCandidateReport.port)
				.withPriority(iceRemoteCandidateReport.priority)
				.withProtocol(iceRemoteCandidateReport.protocol)
				.withIPFlag(iceRemoteCandidateReport.ipFlag)
				;
	}

	public ICERemoteCandidateEntry withIPFlag(String value) {
		this.values.put(IP_FLAG_FIELD_NAME, value);
		return this;
	}

	public ICERemoteCandidateEntry withDeleted(Boolean value) {
		this.values.put(DELETED_FIELD_NAME, value);
		return this;
	}

	public ICERemoteCandidateEntry withCandidateID(String value) {
		this.values.put(CANDIDATE_ID_FIELD_NAME, value);
		return this;
	}

	public ICERemoteCandidateEntry withCandidateType(CandidateType value) {
		String candidateType = null;
		if (value != null) {
			candidateType = value.name();
		}
		this.values.put(CANDIDATE_TYPE_FIELD_NAME, candidateType);
		return this;
	}

	public ICERemoteCandidateEntry withIPLSH(String value) {
		this.values.put(IP_LSH_FIELD_NAME, value);
		return this;
	}

	public ICERemoteCandidateEntry withPort(Integer value) {
		this.values.put(PORT_FIELD_NAME, value);
		return this;
	}

	public ICERemoteCandidateEntry withPriority(Long value) {
		this.values.put(PRIORITY_FIELD_NAME, value);
		return this;
	}

	public ICERemoteCandidateEntry withProtocol(ProtocolType value) {
		String protocolType = null;
		if (value != null) {
			protocolType = value.name();
		}
		this.values.put(PROTOCOL_TYPE_FIELD_NAME, protocolType);
		return this;
	}

	public ICERemoteCandidateEntry withTimestamp(LocalDateTime value) {
		if (value == null) {
			logger.warn("No valid sample timestamp");
			return this;
		}
		Long epoch = TimeConverter.GMTLocalDateTimeToEpoch(value);
		this.values.put(TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	private final Map<String, Object> values;

	public ICERemoteCandidateEntry() {
		this.values = new HashMap<>();
	}

	public ICERemoteCandidateEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public ICERemoteCandidateEntry withPeerConnectionUUID(UUID value) {
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
