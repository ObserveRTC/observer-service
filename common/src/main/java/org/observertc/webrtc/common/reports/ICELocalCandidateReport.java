//package org.observertc.webrtc.common.reports;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//public class ICELocalCandidateReport extends Report {
//
//	public static ICELocalCandidateReport of(UUID observerUUID,
//											 UUID peerConnectionUUID,
//											 String candidateID,
//											 LocalDateTime timestamp,
//											 CandidateType candidateType,
//											 Boolean deleted,
//											 String ipLSH,
//											 String ipFlag,
//											 CandidateNetworkType candidateNetworkType,
//											 Integer port,
//											 Long priority,
//											 ProtocolType protocol) {
//		ICELocalCandidateReport result = new ICELocalCandidateReport();
//		result.observerUUID = observerUUID;
//		result.peerConnectionUUID = peerConnectionUUID;
//		result.candidateID = candidateID;
//		result.timestamp = timestamp;
//		result.candidateType = candidateType;
//		result.deleted = deleted;
//		result.ipLSH = ipLSH;
//		result.ipFlag = ipFlag;
//		result.networkType = candidateNetworkType;
//		result.port = port;
//		result.priority = priority;
//		result.protocol = protocol;
//		return result;
//	}
//
//	public UUID observerUUID;
//
//	public UUID peerConnectionUUID;
//
//	public String candidateID;
//
//	public CandidateType candidateType;
//
//	public String ipFlag;
//
//	@JsonSerialize(using = LocalDateTimeSerializer.class)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	public LocalDateTime timestamp;
//
//	public Boolean deleted;
//
//	public String ipLSH;
//
//	public CandidateNetworkType networkType;
//
//	public Integer port;
//
//	public Long priority;
//
//	public ProtocolType protocol;
//
//
//	@JsonCreator
//	public ICELocalCandidateReport() {
//		super(ReportType.ICE_LOCAL_CANDIDATE_REPORT);
//	}
//}
