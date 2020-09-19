package org.observertc.webrtc.observer.evaluators.reportdrafts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.UUID;

@JsonTypeName("INITIATED_CALL")
public class InitiatedCallReportDraft extends ReportDraft {
	public static InitiatedCallReportDraft of(UUID serviceUUID, UUID callUUID, Long initiated) {
		InitiatedCallReportDraft result = new InitiatedCallReportDraft();
		result.callUUID = callUUID;
		result.serviceUUID = serviceUUID;
		result.initiated = initiated;
		return result;
	}

	@JsonCreator
	public InitiatedCallReportDraft() {
		super(ReportDraftType.INITIATED_CALL);
	}

	public UUID serviceUUID;
	public UUID callUUID;
	public Long initiated;
}
