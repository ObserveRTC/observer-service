package org.observertc.webrtc.observer.evaluators.reportdrafts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class",
		defaultImpl = ReportDraft.class)
@JsonSubTypes({
		@JsonSubTypes.Type(value = InitiatedCallReportDraft.class, name = "INITIATED_CALL"),
		@JsonSubTypes.Type(value = FinishedCallReportDraft.class, name = "FINISHED_CALL"),
})
public abstract class ReportDraft {

	public ReportDraftType type;

	public Long created;

	public Long processed;

	protected ReportDraft(ReportDraftType type) {
		this.type = type;
	}

}
