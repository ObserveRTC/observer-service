package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type",
		defaultImpl = Report.class,
		visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = InitiatedCallReport.class, name = "INITIATED_CALL"),
		@JsonSubTypes.Type(value = FinishedCallReport.class, name = "FINISHED_CALL"),
		@JsonSubTypes.Type(value = JoinedPeerConnectionReport.class, name = "JOINED_PEER_CONNECTION"),
		@JsonSubTypes.Type(value = DetachedPeerConnectionReport.class, name = "DETACHED_PEER_CONNECTION"),
		@JsonSubTypes.Type(value = MediaStreamSampleReport.class, name = "MEDIA_STREAM_SAMPLE"),
})
public class Report {

	/**
	 * Holds information for polymorphic deserialization
	 */
	private ReportType type;

	@JsonCreator
	public Report() {
	}

	protected Report(ReportType type) {
		this.type = type;
	}

	public ReportType getType() {
		return this.type;
	}

	public void setType(ReportType value) {
		this.type = type;
	}

}
