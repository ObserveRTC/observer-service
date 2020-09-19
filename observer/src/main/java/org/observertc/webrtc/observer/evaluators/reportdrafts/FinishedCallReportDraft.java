package org.observertc.webrtc.observer.evaluators.reportdrafts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.UUID;

@JsonTypeName("FINISHED_CALL")
public class FinishedCallReportDraft extends ReportDraft {
	public static FinishedCallReportDraft of(UUID serviceUUID, UUID callUUID, Long finished) {
		FinishedCallReportDraft result = new FinishedCallReportDraft();
		result.callUUID = callUUID;
		result.serviceUUID = serviceUUID;
		result.finished = finished;
		return result;
	}

	@JsonCreator
	public FinishedCallReportDraft() {
		super(ReportDraftType.FINISHED_CALL);
	}

	public UUID serviceUUID;
	public UUID callUUID;
	public Long finished;
}
