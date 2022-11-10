package org.observertc.schemas.v210.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for SFU Transport layer typically created to transfer RTP/SCTP/RTX streams to another client, SFU, MCU, or processing module.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class SFUTransportReport {
	public static final String VERSION="2.1.8";
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
	* The provided unique identifier of the SFU
	*/
	@JsonProperty("sfuId")
	public String sfuId;
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
	* Flag indicate if the sfu transport is used as an internal transport between SFUs
	*/
	@JsonProperty("internal")
	public Boolean internal;
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
	* The generated unique identifier of the transport
	*/
	@JsonProperty("transportId")
	public String transportId;
	/**
	* Represent the current value of the state attribute of the underlying RTCDtlsTransport.
	*/
	@JsonProperty("dtlsState")
	public String dtlsState;
	/**
	* Represent the current value of the state attribute of the underlying RTCIceTransport
	*/
	@JsonProperty("iceState")
	public String iceState;
	/**
	* Represents the the current value of the SCTP state of the transport of the SFU
	*/
	@JsonProperty("sctpState")
	public String sctpState;
	/**
	* Represent the current value of the role SFU takes place in ICE
	*/
	@JsonProperty("iceRole")
	public String iceRole;
	/**
	* The local address of the ICE candidate selected for the transport (IPv4, IPv6, FQDN)
	*/
	@JsonProperty("localAddress")
	public String localAddress;
	/**
	* The local port number
	*/
	@JsonProperty("localPort")
	public Integer localPort;
	/**
	* The protocol used by the transport
	*/
	@JsonProperty("protocol")
	public String protocol;
	/**
	* The remote address of the ICE candidate selected for the transport (IPv4, IPv6, FQDN)
	*/
	@JsonProperty("remoteAddress")
	public String remoteAddress;
	/**
	* The remote port number
	*/
	@JsonProperty("remotePort")
	public Integer remotePort;
	/**
	* The total amount of RTP bytes received on this transport
	*/
	@JsonProperty("rtpBytesReceived")
	public Long rtpBytesReceived;
	/**
	* The total amount of RTP bytes sent on this transport
	*/
	@JsonProperty("rtpBytesSent")
	public Long rtpBytesSent;
	/**
	* The total amount of RTP packets received on this transport
	*/
	@JsonProperty("rtpPacketsReceived")
	public Integer rtpPacketsReceived;
	/**
	* The total amount of RTP packets sent on this transport
	*/
	@JsonProperty("rtpPacketsSent")
	public Integer rtpPacketsSent;
	/**
	* The total amount of RTP packets lost on this transport
	*/
	@JsonProperty("rtpPacketsLost")
	public Integer rtpPacketsLost;
	/**
	* The total amount of RTX bytes received on this transport
	*/
	@JsonProperty("rtxBytesReceived")
	public Long rtxBytesReceived;
	/**
	* The total amount of RTX bytes sent on this transport
	*/
	@JsonProperty("rtxBytesSent")
	public Long rtxBytesSent;
	/**
	* The total amount of RTX packets received on this transport
	*/
	@JsonProperty("rtxPacketsReceived")
	public Integer rtxPacketsReceived;
	/**
	* The total amount of RTX packets sent on this transport
	*/
	@JsonProperty("rtxPacketsSent")
	public Integer rtxPacketsSent;
	/**
	* The total amount of RTX packets lost on this transport
	*/
	@JsonProperty("rtxPacketsLost")
	public Integer rtxPacketsLost;
	/**
	* The total amount of RTX packets discarded on this transport
	*/
	@JsonProperty("rtxPacketsDiscarded")
	public Integer rtxPacketsDiscarded;
	/**
	* The total amount of SCTP bytes received on this transport
	*/
	@JsonProperty("sctpBytesReceived")
	public Long sctpBytesReceived;
	/**
	* The total amount of SCTP bytes sent on this transport
	*/
	@JsonProperty("sctpBytesSent")
	public Long sctpBytesSent;
	/**
	* The total amount of SCTP packets received on this transport
	*/
	@JsonProperty("sctpPacketsReceived")
	public Integer sctpPacketsReceived;
	/**
	* The total amount of SCTP packets sent on this transport
	*/
	@JsonProperty("sctpPacketsSent")
	public Integer sctpPacketsSent;


	public static class Builder {

		private SFUTransportReport result = new SFUTransportReport();

		public Builder setServiceId(String value) {
			this.result.serviceId = value;
			return this;
		}
		public Builder setMediaUnitId(String value) {
			this.result.mediaUnitId = value;
			return this;
		}
		public Builder setSfuId(String value) {
			this.result.sfuId = value;
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
		public Builder setInternal(Boolean value) {
			this.result.internal = value;
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
		public Builder setTransportId(String value) {
			this.result.transportId = value;
			return this;
		}
		public Builder setDtlsState(String value) {
			this.result.dtlsState = value;
			return this;
		}
		public Builder setIceState(String value) {
			this.result.iceState = value;
			return this;
		}
		public Builder setSctpState(String value) {
			this.result.sctpState = value;
			return this;
		}
		public Builder setIceRole(String value) {
			this.result.iceRole = value;
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
		public Builder setProtocol(String value) {
			this.result.protocol = value;
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
		public Builder setRtpBytesReceived(Long value) {
			this.result.rtpBytesReceived = value;
			return this;
		}
		public Builder setRtpBytesSent(Long value) {
			this.result.rtpBytesSent = value;
			return this;
		}
		public Builder setRtpPacketsReceived(Integer value) {
			this.result.rtpPacketsReceived = value;
			return this;
		}
		public Builder setRtpPacketsSent(Integer value) {
			this.result.rtpPacketsSent = value;
			return this;
		}
		public Builder setRtpPacketsLost(Integer value) {
			this.result.rtpPacketsLost = value;
			return this;
		}
		public Builder setRtxBytesReceived(Long value) {
			this.result.rtxBytesReceived = value;
			return this;
		}
		public Builder setRtxBytesSent(Long value) {
			this.result.rtxBytesSent = value;
			return this;
		}
		public Builder setRtxPacketsReceived(Integer value) {
			this.result.rtxPacketsReceived = value;
			return this;
		}
		public Builder setRtxPacketsSent(Integer value) {
			this.result.rtxPacketsSent = value;
			return this;
		}
		public Builder setRtxPacketsLost(Integer value) {
			this.result.rtxPacketsLost = value;
			return this;
		}
		public Builder setRtxPacketsDiscarded(Integer value) {
			this.result.rtxPacketsDiscarded = value;
			return this;
		}
		public Builder setSctpBytesReceived(Long value) {
			this.result.sctpBytesReceived = value;
			return this;
		}
		public Builder setSctpBytesSent(Long value) {
			this.result.sctpBytesSent = value;
			return this;
		}
		public Builder setSctpPacketsReceived(Integer value) {
			this.result.sctpPacketsReceived = value;
			return this;
		}
		public Builder setSctpPacketsSent(Integer value) {
			this.result.sctpPacketsSent = value;
			return this;
		}
		public SFUTransportReport build() {
			return this.result;
		}
	}
}