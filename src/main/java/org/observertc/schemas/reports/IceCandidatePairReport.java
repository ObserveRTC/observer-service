package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for ICE candidate pairs
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class IceCandidatePairReport {
	public static final String VERSION="2.1.8";
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
	* The webrtc app provided label the peer connection is marked with
	*/
	@JsonProperty("label")
	public String label;
	/**
	* The sequence number of the sample the report is generated from
	*/
	@JsonProperty("sampleSeq")
	public Integer sampleSeq;
	/**
	* The identifier of the transport the ice candidate pair is negotiated on
	*/
	@JsonProperty("transportId")
	public String transportId;
	/**
	* The unique identifier of the candidate the negotiated pair is selected at local side
	*/
	@JsonProperty("localCandidateId")
	public String localCandidateId;
	/**
	* The unique identifier of the candidate the negotiated pair is selected at remote side
	*/
	@JsonProperty("remoteCandidateId")
	public String remoteCandidateId;
	/**
	* The state of ICE Candidate Pairs (RTCStatsIceState) on the corresponded transport
	*/
	@JsonProperty("state")
	public String state;
	/**
	* indicate if the ice candidate pair is nominated or not
	*/
	@JsonProperty("nominated")
	public Boolean nominated;
	/**
	* The total number of packets sent using the last selected candidate pair over the corresponded transport
	*/
	@JsonProperty("packetsSent")
	public Integer packetsSent;
	/**
	* The total number of packets received using the last selected candidate pair over the corresponded transport
	*/
	@JsonProperty("packetsReceived")
	public Integer packetsReceived;
	/**
	* The total number of bytes sent using the last selected candidate pair over the corresponded transport
	*/
	@JsonProperty("bytesSent")
	public Long bytesSent;
	/**
	* The total number of bytes received using the last selected candidate pair over the corresponded transport
	*/
	@JsonProperty("bytesReceived")
	public Long bytesReceived;
	/**
	* Represents the timestamp at which the last packet was sent on the selected candidate pair, excluding STUN packets over the corresponded transport (UTC Epoch in ms)
	*/
	@JsonProperty("lastPacketSentTimestamp")
	public Long lastPacketSentTimestamp;
	/**
	* Represents the timestamp at which the last packet was received on the selected candidate pair, excluding STUN packets over the corresponded transport (UTC Epoch in ms)
	*/
	@JsonProperty("lastPacketReceivedTimestamp")
	public Long lastPacketReceivedTimestamp;
	/**
	* Represents the sum of all round trip time measurements in seconds since the beginning of the session, based on STUN connectivity check over the corresponded transport
	*/
	@JsonProperty("totalRoundTripTime")
	public Double totalRoundTripTime;
	/**
	* Represents the last round trip time measurements in seconds based on STUN connectivity check over the corresponded transport
	*/
	@JsonProperty("currentRoundTripTime")
	public Double currentRoundTripTime;
	/**
	* The sum of the underlying cc algorithm provided outgoing bitrate for the RTP streams over the corresponded transport
	*/
	@JsonProperty("availableOutgoingBitrate")
	public Double availableOutgoingBitrate;
	/**
	* The sum of the underlying cc algorithm provided incoming bitrate for the RTP streams over the corresponded transport
	*/
	@JsonProperty("availableIncomingBitrate")
	public Double availableIncomingBitrate;
	/**
	* Represents the total number of connectivity check requests received on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("requestsReceived")
	public Integer requestsReceived;
	/**
	* Represents the total number of connectivity check requests sent on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("requestsSent")
	public Integer requestsSent;
	/**
	* Represents the total number of connectivity check responses received on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("responsesReceived")
	public Integer responsesReceived;
	/**
	* Represents the total number of connectivity check responses sent on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("responsesSent")
	public Integer responsesSent;
	/**
	* Represents the total number of consent requests sent on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("consentRequestsSent")
	public Integer consentRequestsSent;
	/**
	* Total amount of packets for this candidate pair that have been discarded due to socket errors on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("packetsDiscardedOnSend")
	public Integer packetsDiscardedOnSend;
	/**
	* Total amount of bytes for this candidate pair that have been discarded due to socket errors on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("bytesDiscardedOnSend")
	public Long bytesDiscardedOnSend;


	public static class Builder {

		private IceCandidatePairReport result = new IceCandidatePairReport();

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
		public Builder setLabel(String value) {
			this.result.label = value;
			return this;
		}
		public Builder setSampleSeq(Integer value) {
			this.result.sampleSeq = value;
			return this;
		}
		public Builder setTransportId(String value) {
			this.result.transportId = value;
			return this;
		}
		public Builder setLocalCandidateId(String value) {
			this.result.localCandidateId = value;
			return this;
		}
		public Builder setRemoteCandidateId(String value) {
			this.result.remoteCandidateId = value;
			return this;
		}
		public Builder setState(String value) {
			this.result.state = value;
			return this;
		}
		public Builder setNominated(Boolean value) {
			this.result.nominated = value;
			return this;
		}
		public Builder setPacketsSent(Integer value) {
			this.result.packetsSent = value;
			return this;
		}
		public Builder setPacketsReceived(Integer value) {
			this.result.packetsReceived = value;
			return this;
		}
		public Builder setBytesSent(Long value) {
			this.result.bytesSent = value;
			return this;
		}
		public Builder setBytesReceived(Long value) {
			this.result.bytesReceived = value;
			return this;
		}
		public Builder setLastPacketSentTimestamp(Long value) {
			this.result.lastPacketSentTimestamp = value;
			return this;
		}
		public Builder setLastPacketReceivedTimestamp(Long value) {
			this.result.lastPacketReceivedTimestamp = value;
			return this;
		}
		public Builder setTotalRoundTripTime(Double value) {
			this.result.totalRoundTripTime = value;
			return this;
		}
		public Builder setCurrentRoundTripTime(Double value) {
			this.result.currentRoundTripTime = value;
			return this;
		}
		public Builder setAvailableOutgoingBitrate(Double value) {
			this.result.availableOutgoingBitrate = value;
			return this;
		}
		public Builder setAvailableIncomingBitrate(Double value) {
			this.result.availableIncomingBitrate = value;
			return this;
		}
		public Builder setRequestsReceived(Integer value) {
			this.result.requestsReceived = value;
			return this;
		}
		public Builder setRequestsSent(Integer value) {
			this.result.requestsSent = value;
			return this;
		}
		public Builder setResponsesReceived(Integer value) {
			this.result.responsesReceived = value;
			return this;
		}
		public Builder setResponsesSent(Integer value) {
			this.result.responsesSent = value;
			return this;
		}
		public Builder setConsentRequestsSent(Integer value) {
			this.result.consentRequestsSent = value;
			return this;
		}
		public Builder setPacketsDiscardedOnSend(Integer value) {
			this.result.packetsDiscardedOnSend = value;
			return this;
		}
		public Builder setBytesDiscardedOnSend(Long value) {
			this.result.bytesDiscardedOnSend = value;
			return this;
		}
		public IceCandidatePairReport build() {
			return this.result;
		}
	}
}