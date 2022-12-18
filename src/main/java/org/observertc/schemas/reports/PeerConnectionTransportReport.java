package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for Client PeerConnection Transport.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class PeerConnectionTransportReport {
	public static final String VERSION="2.2.0";
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
	* The identifier of the candidate pair the transport currently uses
	*/
	@JsonProperty("selectedCandidatePairId")
	public String selectedCandidatePairId;
	/**
	* Represents the current transport state (RTCIceTransportState) of ICE for the peer connection transport layer
	*/
	@JsonProperty("iceState")
	public String iceState;
	/**
	* If DTLS negotiated it gives the id of the local certificate
	*/
	@JsonProperty("localCertificateId")
	public String localCertificateId;
	/**
	* If DTLS negotiated it gives the id of the remote certificate
	*/
	@JsonProperty("remoteCertificateId")
	public String remoteCertificateId;
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


	public static class Builder {

		private PeerConnectionTransportReport result = new PeerConnectionTransportReport();

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
		public Builder setSelectedCandidatePairId(String value) {
			this.result.selectedCandidatePairId = value;
			return this;
		}
		public Builder setIceState(String value) {
			this.result.iceState = value;
			return this;
		}
		public Builder setLocalCertificateId(String value) {
			this.result.localCertificateId = value;
			return this;
		}
		public Builder setRemoteCertificateId(String value) {
			this.result.remoteCertificateId = value;
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
		public PeerConnectionTransportReport build() {
			return this.result;
		}
	}
}