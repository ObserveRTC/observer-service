package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for SCTP streams going through the SFU
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class SfuSctpStreamReport {
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
	* Flag indicate if the sctp channel is used as an internal transport between SFUs
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
	* The id of the transport the RTP stream uses.
	*/
	@JsonProperty("transportId")
	public String transportId;
	/**
	* The id of the sctp stream
	*/
	@JsonProperty("streamId")
	public String streamId;
	/**
	* The label of the sctp stream
	*/
	@JsonProperty("label")
	public String label;
	/**
	* The protocol used to establish an sctp stream
	*/
	@JsonProperty("protocol")
	public String protocol;
	/**
	* The latest smoothed round-trip time value, corresponding to spinfo_srtt defined in [RFC6458] but converted to seconds. If there has been no round-trip time measurements yet, this value is undefined.
	*/
	@JsonProperty("sctpSmoothedRoundTripTime")
	public Double sctpSmoothedRoundTripTime;
	/**
	* The latest congestion window, corresponding to spinfo_cwnd defined in [RFC6458].
	*/
	@JsonProperty("sctpCongestionWindow")
	public Double sctpCongestionWindow;
	/**
	* The latest receiver window, corresponding to sstat_rwnd defined in [RFC6458].
	*/
	@JsonProperty("sctpReceiverWindow")
	public Double sctpReceiverWindow;
	/**
	* The latest maximum transmission unit, corresponding to spinfo_mtu defined in [RFC6458].
	*/
	@JsonProperty("sctpMtu")
	public Integer sctpMtu;
	/**
	* The number of unacknowledged DATA chunks, corresponding to sstat_unackdata defined in [RFC6458].
	*/
	@JsonProperty("sctpUnackData")
	public Integer sctpUnackData;
	/**
	* The number of message received on the corresponded SCTP stream.
	*/
	@JsonProperty("messageReceived")
	public Integer messageReceived;
	/**
	* The number of message sent on the corresponded SCTP stream.
	*/
	@JsonProperty("messageSent")
	public Integer messageSent;
	/**
	* The number of bytes received on the corresponded SCTP stream.
	*/
	@JsonProperty("bytesReceived")
	public Long bytesReceived;
	/**
	* The number of bytes sent on the corresponded SCTP stream.
	*/
	@JsonProperty("bytesSent")
	public Long bytesSent;


	public static class Builder {

		private SfuSctpStreamReport result = new SfuSctpStreamReport();

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
		public Builder setStreamId(String value) {
			this.result.streamId = value;
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
		public Builder setMessageReceived(Integer value) {
			this.result.messageReceived = value;
			return this;
		}
		public Builder setMessageSent(Integer value) {
			this.result.messageSent = value;
			return this;
		}
		public Builder setBytesReceived(Long value) {
			this.result.bytesReceived = value;
			return this;
		}
		public Builder setBytesSent(Long value) {
			this.result.bytesSent = value;
			return this;
		}
		public SfuSctpStreamReport build() {
			return this.result;
		}
	}
}