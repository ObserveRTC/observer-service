package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A report created for observer generated events
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObserverEventReport {
	public static final String VERSION="2.0.1";
	public static Builder newBuilder() {
		return new Builder();
	}
	/**
	* The unique identifier of the service
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
	* The generated unique identifier of the call
	*/
	@JsonProperty("callId")
	public String callId;
	/**
	* webrtc app provided room id
	*/
	@JsonProperty("roomId")
	public String roomId;
	/**
	* The generated unique identifier of the client
	*/
	@JsonProperty("clientId")
	public String clientId;
	/**
	* webrtc app provided user identifier
	*/
	@JsonProperty("userId")
	public String userId;
	/**
	* The unique identifier of the peer connection
	*/
	@JsonProperty("peerConnectionId")
	public String peerConnectionId;
	/**
	* The timestamp of the sample the event related to
	*/
	@JsonProperty("sampleTimestamp")
	public Long sampleTimestamp;
	/**
	* The sequence number of the sample the event may related to
	*/
	@JsonProperty("sampleSeq")
	public Integer sampleSeq;
	/**
	* The name of the event
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

		private ObserverEventReport result = new ObserverEventReport();

		public Builder setServiceId(String value) {
			this.result.serviceId = value;
			return this;
		}
		public Builder setMediaUnitId(String value) {
			this.result.mediaUnitId = value;
			return this;
		}
		public Builder setMarker(String value) {
			this.result.marker = value;
			return this;
		}
		public Builder setTimestamp(Long value) {
			this.result.timestamp = value;
			return this;
		}
		public Builder setCallId(String value) {
			this.result.callId = value;
			return this;
		}
		public Builder setRoomId(String value) {
			this.result.roomId = value;
			return this;
		}
		public Builder setClientId(String value) {
			this.result.clientId = value;
			return this;
		}
		public Builder setUserId(String value) {
			this.result.userId = value;
			return this;
		}
		public Builder setPeerConnectionId(String value) {
			this.result.peerConnectionId = value;
			return this;
		}
		public Builder setSampleTimestamp(Long value) {
			this.result.sampleTimestamp = value;
			return this;
		}
		public Builder setSampleSeq(Integer value) {
			this.result.sampleSeq = value;
			return this;
		}
		public Builder setName(String value) {
			this.result.name = value;
			return this;
		}
		public Builder setMessage(String value) {
			this.result.message = value;
			return this;
		}
		public Builder setValue(String value) {
			this.result.value = value;
			return this;
		}
		public Builder setAttachments(String value) {
			this.result.attachments = value;
			return this;
		}
		public ObserverEventReport build() {
			return this.result;
		}
	}
}