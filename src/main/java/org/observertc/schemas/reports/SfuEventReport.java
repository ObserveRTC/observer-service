package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* Events happened in calls.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class SfuEventReport {
	public static final String VERSION="2.0.0-beta.61";
	public static Builder newBuilder() {
		return new Builder();
	}
	/**
	* The service id the report belongs to
	*/
	@JsonProperty("serviceId")
	public String serviceId;
	/**
	* The media unit id the report belongs to
	*/
	@JsonProperty("mediaUnitId")
	public String mediaUnitId;
	/**
	* The marker the originated sample is reported with
	*/
	@JsonProperty("marker")
	public String marker;
	/**
	* The timestamp when the corresponded data is generated for the report (UTC Epoch in ms)
	*/
	@JsonProperty("timestamp")
	public Long timestamp;
	/**
	* The generated unique identifier of the SFU
	*/
	@JsonProperty("sfuId")
	public String sfuId;
	/**
	* The callId the event belongs to
	*/
	@JsonProperty("callId")
	public String callId;
	/**
	* SFU provided transport identifier
	*/
	@JsonProperty("transportId")
	public String transportId;
	/**
	* Unique identifier of the SFU stream id the rtp pad belongs to
	*/
	@JsonProperty("mediaStreamId")
	public String mediaStreamId;
	/**
	* Unique identifier of the SFU stream id the rtp pad belongs to
	*/
	@JsonProperty("mediaSinkId")
	public String mediaSinkId;
	/**
	* Unique identifier of the SCTP stream the event is related to
	*/
	@JsonProperty("sctpStreamId")
	public String sctpStreamId;
	/**
	* Unique identifier of the Sfu Pad the event is related to
	*/
	@JsonProperty("rtpPadId")
	public String rtpPadId;
	/**
	* The name of the event. Possible values are: SFU_JOINED, SFU_LEFT, SFU_TRANSPORT_OPENED, SFU_TRANSPORT_CLOSED, SFU_RTP_STREAM_ADDED, SFU_RTP_STREAM_REMOVED
	*/
	@JsonProperty("name")
	public String name;
	/**
	* the human readable message of the event
	*/
	@JsonProperty("message")
	public String message;
	/**
	* the value of the event
	*/
	@JsonProperty("value")
	public String value;
	/**
	* attachment the event may created with
	*/
	@JsonProperty("attachments")
	public String attachments;


	public static class Builder {

		private SfuEventReport result = new SfuEventReport();

		public Builder setServiceId(String value) { this.result.serviceId = value; return this; }
		public Builder setMediaUnitId(String value) { this.result.mediaUnitId = value; return this; }
		public Builder setMarker(String value) { this.result.marker = value; return this; }
		public Builder setTimestamp(Long value) { this.result.timestamp = value; return this; }
		public Builder setSfuId(String value) { this.result.sfuId = value; return this; }
		public Builder setCallId(String value) { this.result.callId = value; return this; }
		public Builder setTransportId(String value) { this.result.transportId = value; return this; }
		public Builder setMediaStreamId(String value) { this.result.mediaStreamId = value; return this; }
		public Builder setMediaSinkId(String value) { this.result.mediaSinkId = value; return this; }
		public Builder setSctpStreamId(String value) { this.result.sctpStreamId = value; return this; }
		public Builder setRtpPadId(String value) { this.result.rtpPadId = value; return this; }
		public Builder setName(String value) { this.result.name = value; return this; }
		public Builder setMessage(String value) { this.result.message = value; return this; }
		public Builder setValue(String value) { this.result.value = value; return this; }
		public Builder setAttachments(String value) { this.result.attachments = value; return this; }
		public SfuEventReport build() {
			return this.result;
		}
	}
}