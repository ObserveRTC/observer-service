package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* Metadata belongs to SFUs
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class SfuMetaReport {
	public static final String VERSION="2.0.0-beta.30";
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
	* The type of the meta data reported for the peer connection
	*/
	@JsonProperty("type")
	public String type;
	/**
	* The payload for the metadata reported for the peeer connection
	*/
	@JsonProperty("payload")
	public String payload;


	public static class Builder {

		private SfuMetaReport result = new SfuMetaReport();

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
		public Builder setType(String value) { this.result.type = value; return this; }
		public Builder setPayload(String value) { this.result.payload = value; return this; }
		public SfuMetaReport build() {
			return this.result;
		}
	}
}