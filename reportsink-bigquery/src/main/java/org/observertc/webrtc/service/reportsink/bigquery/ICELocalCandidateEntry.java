package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.CandidateNetworkType;
import org.observertc.webrtc.common.reports.ICELocalCandidateReport;
import org.observertc.webrtc.common.reports.ProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ICELocalCandidateEntry implements BigQueryEntry {

	private static Logger logger = LoggerFactory.getLogger(ICELocalCandidateEntry.class);

	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String CANDIDATE_ID_FIELD_NAME = "CandidateID";
	public static final String DELETED_FIELD_NAME = "deleted";
	public static final String IP_LSH_FIELD_NAME = "ipLSH";
	public static final String NETWORK_TYPE_FIELD_NAME = "networkType";
	public static final String PORT_FIELD_NAME = "port";
	public static final String PRIORITY_FIELD_NAME = "priority";
	public static final String PROTOCOL_TYPE_FIELD_NAME = "protocolType";
	public static final String IP_FLAG_FIELD_NAME = "ipFlag";

	public static ICELocalCandidateEntry from(ICELocalCandidateReport iceLocalCandidateReport) {
		if (iceLocalCandidateReport == null) {
			return null;
		}
		return new ICELocalCandidateEntry()
				.withObserverUUID(iceLocalCandidateReport.observerUUID)
				.withPeerConnectionUUID(iceLocalCandidateReport.peerConnectionUUID)
				.withCandidateID(iceLocalCandidateReport.candidateID)
				.withTimestamp(iceLocalCandidateReport.timestamp)
				.withDeleted(iceLocalCandidateReport.deleted)
				.withIPLSH(iceLocalCandidateReport.ipLSH)
				.withNetworkType(iceLocalCandidateReport.networkType)
				.withPort(iceLocalCandidateReport.port)
				.withPriority(iceLocalCandidateReport.priority)
				.withProtocol(iceLocalCandidateReport.protocol)
				.withIPFlag(iceLocalCandidateReport.ipFlag)
				;


	}

	public ICELocalCandidateEntry withIPFlag(String value) {
		this.values.put(IP_FLAG_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withCandidateID(String value) {
		this.values.put(CANDIDATE_ID_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withDeleted(Boolean value) {
		this.values.put(DELETED_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withIPLSH(String value) {
		this.values.put(IP_LSH_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withNetworkType(CandidateNetworkType value) {
		String networkType = null;
		if (value != null) {
			networkType = value.name();
		}
		this.values.put(NETWORK_TYPE_FIELD_NAME, networkType);
		return this;
	}

	public ICELocalCandidateEntry withPort(Integer value) {
		this.values.put(PORT_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withPriority(Long value) {
		this.values.put(PRIORITY_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withProtocol(ProtocolType value) {
		String protocolType = null;
		if (value != null) {
			protocolType = value.name();
		}
		this.values.put(PROTOCOL_TYPE_FIELD_NAME, protocolType);
		return this;
	}

	public ICELocalCandidateEntry withTimestamp(LocalDateTime value) {
		if (value == null) {
			logger.warn("No valid sample timestamp");
			return this;
		}
		Long epoch = BigQueryServiceTimeConverter.getInstance().toEpoch(value);
		this.values.put(TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	private final Map<String, Object> values;

	public ICELocalCandidateEntry() {
		this.values = new HashMap<>();
	}

	public ICELocalCandidateEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public ICELocalCandidateEntry withPeerConnectionUUID(UUID value) {
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
