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
//public class ICECandidatePairReport extends Report {
//
//	public static ICECandidatePairReport of(UUID observerUUID,
//											UUID peerConnectionUUID,
//											String candidateID,
//											LocalDateTime timestamp,
//											Boolean nominated,
//											Integer availableOutgoingBitrate,
//											Integer bytesReceived,
//											Integer bytesSent,
//											Integer consentRequestsSent,
//											Double currentRoundTripTime,
//											Integer priority,
//											Integer requestsReceived,
//											Integer requestsSent,
//											Integer responsesReceived,
//											Integer responsesSent,
//											CandidatePairState state,
//											Double totalRoundTripTime,
//											Boolean writable
//	) {
//		ICECandidatePairReport result = new ICECandidatePairReport();
//		result.observerUUID = observerUUID;
//		result.peerConnectionUUID = peerConnectionUUID;
//		result.candidateID = candidateID;
//		result.timestamp = timestamp;
//		result.nominated = nominated;
//		result.availableOutgoingBitrate = availableOutgoingBitrate;
//		result.bytesReceived = bytesReceived;
//		result.bytesSent = bytesSent;
//		result.consentRequestsSent = consentRequestsSent;
//		result.currentRoundTripTime = currentRoundTripTime;
//		result.priority = priority;
//		result.requestsReceived = requestsReceived;
//		result.requestsSent = requestsSent;
//		result.responsesReceived = responsesReceived;
//		result.responsesSent = responsesSent;
//		result.state = state;
//		result.totalRoundTripTime = totalRoundTripTime;
//		result.writable = writable;
//		// TODO: do it
//		return result;
//	}
//
//	public UUID observerUUID;
//
//	public UUID peerConnectionUUID;
//
//	public String candidateID;
//
//	@JsonSerialize(using = LocalDateTimeSerializer.class)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	public LocalDateTime timestamp;
//
//	public Boolean nominated;
//
//	public Integer availableOutgoingBitrate;
//
//	public Integer bytesReceived;
//
//	public Integer bytesSent;
//
//	public Integer consentRequestsSent;
//
//	public Double currentRoundTripTime;
//
//	public Integer priority;
//
//	public Integer requestsReceived;
//
//	public Integer requestsSent;
//
//	public Integer responsesReceived;
//
//	public Integer responsesSent;
//
//	public CandidatePairState state;
//
//	public Double totalRoundTripTime;
//
//	public Boolean writable;
//
//	@JsonCreator
//	public ICECandidatePairReport() {
//		super(ReportType.ICE_CANDIDATE_PAIR_REPORT);
//	}
//}
