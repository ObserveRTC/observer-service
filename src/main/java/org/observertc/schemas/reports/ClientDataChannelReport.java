package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for PeerConnection Data Channel.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientDataChannelReport {
	public static final String VERSION="2.0.0";
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
	* The webrtc app provided label for the peer connection
	*/
	@JsonProperty("peerConnectionLabel")
	public String peerConnectionLabel;
	/**
	* The sequence number of the sample the report is generated from
	*/
	@JsonProperty("sampleSeq")
	public Integer sampleSeq;
	/**
	* The label of the data channel
	*/
	@JsonProperty("label")
	public String label;
	/**
	* The protocol used for the data channel
	*/
	@JsonProperty("protocol")
	public String protocol;
	/**
	* The state of the data channel
	*/
	@JsonProperty("state")
	public String state;
	/**
	* Represents the total number of API message events sent
	*/
	@JsonProperty("messagesSent")
	public Integer messagesSent;
	/**
	* Represents the total number of payload bytes sent on the corresponded data channel
	*/
	@JsonProperty("bytesSent")
	public Long bytesSent;
	/**
	* Represents the total number of API message events received on the corresponded data channel
	*/
	@JsonProperty("messagesReceived")
	public Integer messagesReceived;
	/**
	* Represents the total number of payload bytes received on the corresponded data channel
	*/
	@JsonProperty("bytesReceived")
	public Long bytesReceived;


	public static class Builder {

		private ClientDataChannelReport result = new ClientDataChannelReport();

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
		public Builder setPeerConnectionLabel(String value) {
			this.result.peerConnectionLabel = value;
			return this;
		}
		public Builder setSampleSeq(Integer value) {
			this.result.sampleSeq = value;
			return this;
		}
		public Builder setLabel(String value) {
			this.result.label = value;
			return this;
		}
		public Builder setProtocol(String value) {
			this.result.protocol = value;
			return this;
		}
		public Builder setState(String value) {
			this.result.state = value;
			return this;
		}
		public Builder setMessagesSent(Integer value) {
			this.result.messagesSent = value;
			return this;
		}
		public Builder setBytesSent(Long value) {
			this.result.bytesSent = value;
			return this;
		}
		public Builder setMessagesReceived(Integer value) {
			this.result.messagesReceived = value;
			return this;
		}
		public Builder setBytesReceived(Long value) {
			this.result.bytesReceived = value;
			return this;
		}
		public ClientDataChannelReport build() {
			return this.result;
		}
	}
}