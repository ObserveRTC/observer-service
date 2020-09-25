//package org.observertc.webrtc.common.reports;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonTypeName;
//import java.util.UUID;
//
//@JsonTypeName("INITIATED_CALL")
//public class InitiatedCallReport extends Report {
//	public static InitiatedCallReport of(UUID observerUUID, UUID callUUID, Long initiated) {
//		InitiatedCallReport result = new InitiatedCallReport();
//		result.callUUID = callUUID;
//		result.observerUUID = observerUUID;
//		result.initiated = initiated;
//		return result;
//	}
//
//	@JsonCreator
//	public InitiatedCallReport() {
//		super(ReportType.INITIATED_CALL);
//	}
//
//	public UUID observerUUID;
//	public UUID callUUID;
//	public Long initiated;
//}
