package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class",
		defaultImpl = Report.class)
@JsonSubTypes({
		@JsonSubTypes.Type(value = InitiatedCallReport.class, name = "INITIATED_CALL"),
		@JsonSubTypes.Type(value = FinishedCallReport.class, name = "FINISHED_CALL"),
		@JsonSubTypes.Type(value = JoinedPeerConnectionReport.class, name = "JOINED_PEER_CONNECTION"),
		@JsonSubTypes.Type(value = DetachedPeerConnectionReport.class, name = "DETACHED_PEER_CONNECTION"),
		@JsonSubTypes.Type(value = MediaStreamReport.class, name = "MEDIA_STREAM_SAMPLE"),
		@JsonSubTypes.Type(value = InboundStreamReport.class, name = "INBOUND_STREAM_REPORT"),
		@JsonSubTypes.Type(value = OutboundStreamReport.class, name = "OUTBOUND_STREAM_REPORT"),
		@JsonSubTypes.Type(value = RemoteInboundStreamReport.class, name = "REMOTE_INBOUND_STREAM_REPORT"),
		@JsonSubTypes.Type(value = RemoteInboundRTPReport.class, name = "REMOTE_INBOUND_RTP_REPORT"),
		@JsonSubTypes.Type(value = InboundRTPReport.class, name = "INBOUND_RTP_REPORT"),
		@JsonSubTypes.Type(value = OutboundRTPReport.class, name = "OUTBOUND_RTP_REPORT"),
		@JsonSubTypes.Type(value = ICECandidatePairReport.class, name = "ICE_CANDIDATE_PAIR_REPORT"),
})
public abstract class Report {

	//	private String type;
//	/**
//	 * Holds information for polymorphic deserialization
//	 */
	public ReportType type;

	//
//	@JsonCreator
//	public Report() {
//	}
//
	protected Report(ReportType type) {
		this.type = type;
	}
//
//	public String getType() {
//		return this.type;
//	}
//
//	public void setType(String value) {
//		this.type = type;
//	}

}
