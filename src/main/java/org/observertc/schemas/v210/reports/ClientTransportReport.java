package org.observertc.schemas.v210.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for Client PeerConnection Transport. It is a combination of Transport report, sender, receiver, local, remote and candidate pair of ICE together with the used certificates
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientTransportReport {
	public static final String VERSION="2.0.4";
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
	* Represents the total number of packets sent on the corresponded transport
	*/
	@JsonProperty("packetsSent")
	public Integer packetsSent;
	/**
	* Represents the total number of packets received on the corresponded transport
	*/
	@JsonProperty("packetsReceived")
	public Integer packetsReceived;
	/**
	* Represents the total amount of bytes sent on the corresponded transport
	*/
	@JsonProperty("bytesSent")
	public Long bytesSent;
	/**
	* Represents the total amount of bytes received on the corresponded transport
	*/
	@JsonProperty("bytesReceived")
	public Long bytesReceived;
	/**
	* Represent the current role of ICE under DTLS Transport
	*/
	@JsonProperty("iceRole")
	public String iceRole;
	/**
	* Represent the current local username fragment used in message validation procedures for ICE under DTLS Transport
	*/
	@JsonProperty("iceLocalUsernameFragment")
	public String iceLocalUsernameFragment;
	/**
	* Represents the current state of DTLS for the peer connection transport layer
	*/
	@JsonProperty("dtlsState")
	public String dtlsState;
	/**
	* Represents the current transport state (RTCIceTransportState) of ICE for the peer connection transport layer
	*/
	@JsonProperty("iceTransportState")
	public String iceTransportState;
	/**
	* Represents the version number of the TLS used in the corresponded transport
	*/
	@JsonProperty("tlsVersion")
	public String tlsVersion;
	/**
	* Represents the name of the DTLS cipher used in the corresponded transport
	*/
	@JsonProperty("dtlsCipher")
	public String dtlsCipher;
	/**
	* Represents the name of the SRTP cipher used in the corresponded transport
	*/
	@JsonProperty("srtpCipher")
	public String srtpCipher;
	/**
	* Represents the name of the IANA TLS Supported Groups used in the corresponded transport
	*/
	@JsonProperty("tlsGroup")
	public String tlsGroup;
	/**
	* The total number of candidate pair changes over the peer connection
	*/
	@JsonProperty("selectedCandidatePairChanges")
	public Integer selectedCandidatePairChanges;
	/**
	* The address of the candidate (IPv4, IPv6, FQDN)
	*/
	@JsonProperty("localAddress")
	public String localAddress;
	/**
	* The locally used port to communicate with the remote peer
	*/
	@JsonProperty("localPort")
	public Integer localPort;
	/**
	* The protocol used by the local endpoint for the corresponded transport
	*/
	@JsonProperty("localProtocol")
	public String localProtocol;
	/**
	* The type of the ICE candidate used at the local endpoint on the corresponded transport
	*/
	@JsonProperty("localCandidateType")
	public String localCandidateType;
	/**
	* The url of the ICE server used by the local endpoint on the corresponded transport
	*/
	@JsonProperty("localCandidateICEServerUrl")
	public String localCandidateICEServerUrl;
	/**
	* The relay protocol of the ICE candidate used by the local endpoint on the corresponded transport
	*/
	@JsonProperty("localCandidateRelayProtocol")
	public String localCandidateRelayProtocol;
	/**
	* The address of the candidate (IPv4, IPv6, FQDN)
	*/
	@JsonProperty("remoteAddress")
	public String remoteAddress;
	/**
	* The remotely used port to communicate with the remote peer
	*/
	@JsonProperty("remotePort")
	public Integer remotePort;
	/**
	* The protocol used by the remote endpoint for the corresponded transport
	*/
	@JsonProperty("remoteProtocol")
	public String remoteProtocol;
	/**
	* The type of the ICE candidate used at the remote endpoint on the corresponded transport
	*/
	@JsonProperty("remoteCandidateType")
	public String remoteCandidateType;
	/**
	* The url of the ICE server used by the remote endpoint on the corresponded transport
	*/
	@JsonProperty("remoteCandidateICEServerUrl")
	public String remoteCandidateICEServerUrl;
	/**
	* The relay protocol of the ICE candidate used by the remote endpoint on the corresponded transport
	*/
	@JsonProperty("remoteCandidateRelayProtocol")
	public String remoteCandidateRelayProtocol;
	/**
	* The state of ICE Candidate Pairs (RTCStatsIceCandidatePairState) on the corresponded transport
	*/
	@JsonProperty("candidatePairState")
	public String candidatePairState;
	/**
	* The total number of packets sent using the last selected candidate pair over the corresponded transport
	*/
	@JsonProperty("candidatePairPacketsSent")
	public Integer candidatePairPacketsSent;
	/**
	* The total number of packets received using the last selected candidate pair over the corresponded transport
	*/
	@JsonProperty("candidatePairPacketsReceived")
	public Integer candidatePairPacketsReceived;
	/**
	* The total number of bytes sent using the last selected candidate pair over the corresponded transport
	*/
	@JsonProperty("candidatePairBytesSent")
	public Long candidatePairBytesSent;
	/**
	* The total number of bytes received using the last selected candidate pair over the corresponded transport
	*/
	@JsonProperty("candidatePairBytesReceived")
	public Long candidatePairBytesReceived;
	/**
	* Represents the timestamp at which the last packet was sent on the selected candidate pair, excluding STUN packets over the corresponded transport (UTC Epoch in ms)
	*/
	@JsonProperty("candidatePairLastPacketSentTimestamp")
	public Long candidatePairLastPacketSentTimestamp;
	/**
	* Represents the timestamp at which the last packet was received on the selected candidate pair, excluding STUN packets over the corresponded transport (UTC Epoch in ms)
	*/
	@JsonProperty("candidatePairLastPacketReceivedTimestamp")
	public Long candidatePairLastPacketReceivedTimestamp;
	/**
	* Represents the timestamp at which the first STUN request was sent on this particular candidate pair over the corresponded transport (UTC Epoch in ms)
	*/
	@JsonProperty("candidatePairFirstRequestTimestamp")
	public Long candidatePairFirstRequestTimestamp;
	/**
	* Represents the timestamp at which the last STUN request was sent on this particular candidate pair over the corresponded transport (UTC Epoch in ms)
	*/
	@JsonProperty("candidatePairLastRequestTimestamp")
	public Long candidatePairLastRequestTimestamp;
	/**
	* Represents the timestamp at which the last STUN response was received on this particular candidate pair over the corresponded transport (UTC Epoch in ms)
	*/
	@JsonProperty("candidatePairLastResponseTimestamp")
	public Long candidatePairLastResponseTimestamp;
	/**
	* Represents the sum of all round trip time measurements in seconds since the beginning of the session, based on STUN connectivity check over the corresponded transport
	*/
	@JsonProperty("candidatePairTotalRoundTripTime")
	public Double candidatePairTotalRoundTripTime;
	/**
	* Represents the last round trip time measurements in seconds based on STUN connectivity check over the corresponded transport
	*/
	@JsonProperty("candidatePairCurrentRoundTripTime")
	public Double candidatePairCurrentRoundTripTime;
	/**
	* The sum of the underlying cc algorithm provided outgoing bitrate for the RTP streams over the corresponded transport
	*/
	@JsonProperty("candidatePairAvailableOutgoingBitrate")
	public Double candidatePairAvailableOutgoingBitrate;
	/**
	* The sum of the underlying cc algorithm provided incoming bitrate for the RTP streams over the corresponded transport
	*/
	@JsonProperty("candidatePairAvailableIncomingBitrate")
	public Double candidatePairAvailableIncomingBitrate;
	/**
	* The total number of circuit breaker triggered over the corresponded transport using the selected candidate pair
	*/
	@JsonProperty("candidatePairCircuitBreakerTriggerCount")
	public Integer candidatePairCircuitBreakerTriggerCount;
	/**
	* Represents the total number of connectivity check requests received on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairRequestsReceived")
	public Integer candidatePairRequestsReceived;
	/**
	* Represents the total number of connectivity check requests sent on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairRequestsSent")
	public Integer candidatePairRequestsSent;
	/**
	* Represents the total number of connectivity check responses received on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairResponsesReceived")
	public Integer candidatePairResponsesReceived;
	/**
	* Represents the total number of connectivity check responses sent on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairResponsesSent")
	public Integer candidatePairResponsesSent;
	/**
	* Represents the total number of connectivity check retransmission received on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairRetransmissionReceived")
	public Integer candidatePairRetransmissionReceived;
	/**
	* Represents the total number of connectivity check retransmission sent on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairRetransmissionSent")
	public Integer candidatePairRetransmissionSent;
	/**
	* Represents the total number of consent requests sent on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairConsentRequestsSent")
	public Integer candidatePairConsentRequestsSent;
	/**
	* Represents the timestamp at which the latest valid STUN binding response expired on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairConsentExpiredTimestamp")
	public Long candidatePairConsentExpiredTimestamp;
	/**
	* Total amount of bytes for this candidate pair that have been discarded due to socket errors on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairBytesDiscardedOnSend")
	public Long candidatePairBytesDiscardedOnSend;
	/**
	* Total amount of packets for this candidate pair that have been discarded due to socket errors on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairPacketsDiscardedOnSend")
	public Integer candidatePairPacketsDiscardedOnSend;
	/**
	* Total number of bytes sent for connectivity checks on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairRequestBytesSent")
	public Long candidatePairRequestBytesSent;
	/**
	* Total number of bytes sent for consent requests on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairConsentRequestBytesSent")
	public Long candidatePairConsentRequestBytesSent;
	/**
	* Total number of bytes sent for connectivity check responses on the selected candidate pair using the corresponded transport
	*/
	@JsonProperty("candidatePairResponseBytesSent")
	public Long candidatePairResponseBytesSent;
	/**
	* The latest smoothed round-trip time value, corresponding to spinfo_srtt defined in [RFC6458] but converted to seconds. 
	*/
	@JsonProperty("sctpSmoothedRoundTripTime")
	public Double sctpSmoothedRoundTripTime;
	/**
	* The latest congestion window, corresponding to spinfo_cwnd.
	*/
	@JsonProperty("sctpCongestionWindow")
	public Double sctpCongestionWindow;
	/**
	* The latest receiver window, corresponding to sstat_rwnd.
	*/
	@JsonProperty("sctpReceiverWindow")
	public Double sctpReceiverWindow;
	/**
	* The latest maximum transmission unit, corresponding to spinfo_mtu.
	*/
	@JsonProperty("sctpMtu")
	public Integer sctpMtu;
	/**
	* The number of unacknowledged DATA chunks, corresponding to sstat_unackdata.
	*/
	@JsonProperty("sctpUnackData")
	public Integer sctpUnackData;


	public static class Builder {

		private ClientTransportReport result = new ClientTransportReport();

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
		public Builder setIceRole(String value) {
			this.result.iceRole = value;
			return this;
		}
		public Builder setIceLocalUsernameFragment(String value) {
			this.result.iceLocalUsernameFragment = value;
			return this;
		}
		public Builder setDtlsState(String value) {
			this.result.dtlsState = value;
			return this;
		}
		public Builder setIceTransportState(String value) {
			this.result.iceTransportState = value;
			return this;
		}
		public Builder setTlsVersion(String value) {
			this.result.tlsVersion = value;
			return this;
		}
		public Builder setDtlsCipher(String value) {
			this.result.dtlsCipher = value;
			return this;
		}
		public Builder setSrtpCipher(String value) {
			this.result.srtpCipher = value;
			return this;
		}
		public Builder setTlsGroup(String value) {
			this.result.tlsGroup = value;
			return this;
		}
		public Builder setSelectedCandidatePairChanges(Integer value) {
			this.result.selectedCandidatePairChanges = value;
			return this;
		}
		public Builder setLocalAddress(String value) {
			this.result.localAddress = value;
			return this;
		}
		public Builder setLocalPort(Integer value) {
			this.result.localPort = value;
			return this;
		}
		public Builder setLocalProtocol(String value) {
			this.result.localProtocol = value;
			return this;
		}
		public Builder setLocalCandidateType(String value) {
			this.result.localCandidateType = value;
			return this;
		}
		public Builder setLocalCandidateICEServerUrl(String value) {
			this.result.localCandidateICEServerUrl = value;
			return this;
		}
		public Builder setLocalCandidateRelayProtocol(String value) {
			this.result.localCandidateRelayProtocol = value;
			return this;
		}
		public Builder setRemoteAddress(String value) {
			this.result.remoteAddress = value;
			return this;
		}
		public Builder setRemotePort(Integer value) {
			this.result.remotePort = value;
			return this;
		}
		public Builder setRemoteProtocol(String value) {
			this.result.remoteProtocol = value;
			return this;
		}
		public Builder setRemoteCandidateType(String value) {
			this.result.remoteCandidateType = value;
			return this;
		}
		public Builder setRemoteCandidateICEServerUrl(String value) {
			this.result.remoteCandidateICEServerUrl = value;
			return this;
		}
		public Builder setRemoteCandidateRelayProtocol(String value) {
			this.result.remoteCandidateRelayProtocol = value;
			return this;
		}
		public Builder setCandidatePairState(String value) {
			this.result.candidatePairState = value;
			return this;
		}
		public Builder setCandidatePairPacketsSent(Integer value) {
			this.result.candidatePairPacketsSent = value;
			return this;
		}
		public Builder setCandidatePairPacketsReceived(Integer value) {
			this.result.candidatePairPacketsReceived = value;
			return this;
		}
		public Builder setCandidatePairBytesSent(Long value) {
			this.result.candidatePairBytesSent = value;
			return this;
		}
		public Builder setCandidatePairBytesReceived(Long value) {
			this.result.candidatePairBytesReceived = value;
			return this;
		}
		public Builder setCandidatePairLastPacketSentTimestamp(Long value) {
			this.result.candidatePairLastPacketSentTimestamp = value;
			return this;
		}
		public Builder setCandidatePairLastPacketReceivedTimestamp(Long value) {
			this.result.candidatePairLastPacketReceivedTimestamp = value;
			return this;
		}
		public Builder setCandidatePairFirstRequestTimestamp(Long value) {
			this.result.candidatePairFirstRequestTimestamp = value;
			return this;
		}
		public Builder setCandidatePairLastRequestTimestamp(Long value) {
			this.result.candidatePairLastRequestTimestamp = value;
			return this;
		}
		public Builder setCandidatePairLastResponseTimestamp(Long value) {
			this.result.candidatePairLastResponseTimestamp = value;
			return this;
		}
		public Builder setCandidatePairTotalRoundTripTime(Double value) {
			this.result.candidatePairTotalRoundTripTime = value;
			return this;
		}
		public Builder setCandidatePairCurrentRoundTripTime(Double value) {
			this.result.candidatePairCurrentRoundTripTime = value;
			return this;
		}
		public Builder setCandidatePairAvailableOutgoingBitrate(Double value) {
			this.result.candidatePairAvailableOutgoingBitrate = value;
			return this;
		}
		public Builder setCandidatePairAvailableIncomingBitrate(Double value) {
			this.result.candidatePairAvailableIncomingBitrate = value;
			return this;
		}
		public Builder setCandidatePairCircuitBreakerTriggerCount(Integer value) {
			this.result.candidatePairCircuitBreakerTriggerCount = value;
			return this;
		}
		public Builder setCandidatePairRequestsReceived(Integer value) {
			this.result.candidatePairRequestsReceived = value;
			return this;
		}
		public Builder setCandidatePairRequestsSent(Integer value) {
			this.result.candidatePairRequestsSent = value;
			return this;
		}
		public Builder setCandidatePairResponsesReceived(Integer value) {
			this.result.candidatePairResponsesReceived = value;
			return this;
		}
		public Builder setCandidatePairResponsesSent(Integer value) {
			this.result.candidatePairResponsesSent = value;
			return this;
		}
		public Builder setCandidatePairRetransmissionReceived(Integer value) {
			this.result.candidatePairRetransmissionReceived = value;
			return this;
		}
		public Builder setCandidatePairRetransmissionSent(Integer value) {
			this.result.candidatePairRetransmissionSent = value;
			return this;
		}
		public Builder setCandidatePairConsentRequestsSent(Integer value) {
			this.result.candidatePairConsentRequestsSent = value;
			return this;
		}
		public Builder setCandidatePairConsentExpiredTimestamp(Long value) {
			this.result.candidatePairConsentExpiredTimestamp = value;
			return this;
		}
		public Builder setCandidatePairBytesDiscardedOnSend(Long value) {
			this.result.candidatePairBytesDiscardedOnSend = value;
			return this;
		}
		public Builder setCandidatePairPacketsDiscardedOnSend(Integer value) {
			this.result.candidatePairPacketsDiscardedOnSend = value;
			return this;
		}
		public Builder setCandidatePairRequestBytesSent(Long value) {
			this.result.candidatePairRequestBytesSent = value;
			return this;
		}
		public Builder setCandidatePairConsentRequestBytesSent(Long value) {
			this.result.candidatePairConsentRequestBytesSent = value;
			return this;
		}
		public Builder setCandidatePairResponseBytesSent(Long value) {
			this.result.candidatePairResponseBytesSent = value;
			return this;
		}
		public Builder setSctpSmoothedRoundTripTime(Double value) {
			this.result.sctpSmoothedRoundTripTime = value;
			return this;
		}
		public Builder setSctpCongestionWindow(Double value) {
			this.result.sctpCongestionWindow = value;
			return this;
		}
		public Builder setSctpReceiverWindow(Double value) {
			this.result.sctpReceiverWindow = value;
			return this;
		}
		public Builder setSctpMtu(Integer value) {
			this.result.sctpMtu = value;
			return this;
		}
		public Builder setSctpUnackData(Integer value) {
			this.result.sctpUnackData = value;
			return this;
		}
		public ClientTransportReport build() {
			return this.result;
		}
	}
}