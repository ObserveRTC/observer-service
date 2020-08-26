package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;

public class ICERemoteCandidateReport extends Report {

	public static ICERemoteCandidateReport of(UUID observerUUID,
											  UUID peerConnectionUUID,
											  String candidateID,
											  LocalDateTime timestamp,
											  CandidateType candidateType,
											  Boolean deleted,
											  String ipLSH,
											  String ipFlag,
											  Integer port,
											  Long priority,
											  ProtocolType protocol) {
		ICERemoteCandidateReport result = new ICERemoteCandidateReport();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.candidateID = candidateID;
		result.timestamp = timestamp;
		result.candidateType = candidateType;
		result.deleted = deleted;
		result.ipLSH = ipLSH;
		result.ipFlag = ipFlag;
		result.port = port;
		result.priority = priority;
		result.protocol = protocol;
		return result;
	}

	public UUID observerUUID;

	public UUID peerConnectionUUID;

	public String candidateID;

	public CandidateType candidateType;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime timestamp;

	public Boolean deleted;

	public String ipLSH;

	public String ipFlag;

	public Integer port;

	public Long priority;

	public ProtocolType protocol;

	@JsonCreator
	public ICERemoteCandidateReport() {
		super(ReportType.ICE_REMOTE_CANDIDATE_REPORT);
	}
}
