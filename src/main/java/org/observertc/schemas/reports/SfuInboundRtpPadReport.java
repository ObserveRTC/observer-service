package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for RTP streams going through the SFU
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class SfuInboundRtpPadReport {
	public static final String VERSION="2.0.0-beta.65";
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
	* Flag indicate if the sfu rtp pad is used as an internal rtp session between SFUs
	*/
	@JsonProperty("internal")
	public Boolean internal;
	/**
	* The timestamp when the corresponded data is generated for the report (UTC Epoch in ms)
	*/
	@JsonProperty("timestamp")
	public Long timestamp;
	/**
	* The id of the transport the RTP stream uses.
	*/
	@JsonProperty("transportId")
	public String transportId;
	/**
	* Unique identifier of the Sfu stream the event is related to
	*/
	@JsonProperty("sfuStreamId")
	public String sfuStreamId;
	/**
	* The id of RTP pad.
	*/
	@JsonProperty("rtpPadId")
	public String rtpPadId;
	/**
	* The synchronization source id of the RTP stream
	*/
	@JsonProperty("ssrc")
	public Long ssrc;
	/**
	* The id of the track the RTP stream related to at the client side
	*/
	@JsonProperty("trackId")
	public String trackId;
	/**
	* If the track id was provided by the Sfu, the observer can fill up the information of which client it belongs to
	*/
	@JsonProperty("clientId")
	public String clientId;
	/**
	* The callId the event belongs to
	*/
	@JsonProperty("callId")
	public String callId;
	/**
	* the type of the media the stream carries ("audio" or "video")
	*/
	@JsonProperty("mediaType")
	public String mediaType;
	/**
	* The payload type field of the RTP header
	*/
	@JsonProperty("payloadType")
	public Integer payloadType;
	/**
	* The negotiated mimeType in the SDP
	*/
	@JsonProperty("mimeType")
	public String mimeType;
	/**
	* The clock rate of the media source the RTP header carries
	*/
	@JsonProperty("clockRate")
	public Integer clockRate;
	/**
	* The actual SDP line from the negotiation related to this RTP stream
	*/
	@JsonProperty("sdpFmtpLine")
	public String sdpFmtpLine;
	/**
	*  The rid parameter of the corresponded RTP stream
	*/
	@JsonProperty("rid")
	public String rid;
	/**
	* If RTX is negotiated as a separate stream, this is the SSRC of the RTX stream that is associated with this stream's ssrc. 
	*/
	@JsonProperty("rtxSsrc")
	public Long rtxSsrc;
	/**
	* he bitrate the corresponded stream targets.
	*/
	@JsonProperty("targetBitrate")
	public Integer targetBitrate;
	/**
	* The RTP header V flag indicate of the activity of the media source by the media codec if the RTP transport ships it through
	*/
	@JsonProperty("voiceActivityFlag")
	public Boolean voiceActivityFlag;
	/**
	* The total number FIR packets sent from this endpoint to the source on the corresponded RTP stream. Only for Video streams
	*/
	@JsonProperty("firCount")
	public Integer firCount;
	/**
	* The total number of Picture Loss Indication sent on the corresponded RTP stream. Only for Video streams
	*/
	@JsonProperty("pliCount")
	public Integer pliCount;
	/**
	* The total number of negative acknowledgement received on the corresponded RTP stream.
	*/
	@JsonProperty("nackCount")
	public Integer nackCount;
	/**
	* The total number of SLI indicator sent from the endpoint on the corresponded RTP stream. Only for Audio stream
	*/
	@JsonProperty("sliCount")
	public Integer sliCount;
	/**
	* The total number of packets lost on the corresponded RTP stream.
	*/
	@JsonProperty("packetsLost")
	public Integer packetsLost;
	/**
	* The total number of packets received on the corresponded RTP stream.
	*/
	@JsonProperty("packetsReceived")
	public Integer packetsReceived;
	/**
	* The total number of discarded packets on the corresponded RTP stream.
	*/
	@JsonProperty("packetsDiscarded")
	public Integer packetsDiscarded;
	/**
	* The total number of packets repaired by either retransmission or FEC on the corresponded RTP stream.
	*/
	@JsonProperty("packetsRepaired")
	public Integer packetsRepaired;
	/**
	* The total number of packets failed to be decrypted on the corresponded RTP stream.
	*/
	@JsonProperty("packetsFailedDecryption")
	public Integer packetsFailedDecryption;
	/**
	* The total number of duplicated packets appeared on the corresponded RTP stream.
	*/
	@JsonProperty("packetsDuplicated")
	public Integer packetsDuplicated;
	/**
	* The total number of FEC packets received on the corresponded RTP stream.
	*/
	@JsonProperty("fecPacketsReceived")
	public Integer fecPacketsReceived;
	/**
	* The total number of FEC packets discarded on the corresponded RTP stream.
	*/
	@JsonProperty("fecPacketsDiscarded")
	public Integer fecPacketsDiscarded;
	/**
	* The total amount of payload bytes received on the corresponded RTP stream.
	*/
	@JsonProperty("bytesReceived")
	public Long bytesReceived;
	/**
	* The total number of SR reports received by the corresponded RTP stream
	*/
	@JsonProperty("rtcpSrReceived")
	public Integer rtcpSrReceived;
	/**
	* The total number of RR reports sent on the corresponded RTP stream
	*/
	@JsonProperty("rtcpRrSent")
	public Integer rtcpRrSent;
	/**
	* If rtx packets are sent or received on the same stream then this number indicates how may has been sent
	*/
	@JsonProperty("rtxPacketsReceived")
	public Integer rtxPacketsReceived;
	/**
	* If rtx packets are received on the same stream then this number indicates how may has been discarded
	*/
	@JsonProperty("rtxPacketsDiscarded")
	public Integer rtxPacketsDiscarded;
	/**
	* The number of frames received on the corresponded RTP stream
	*/
	@JsonProperty("framesReceived")
	public Integer framesReceived;
	/**
	* Indicate the number of frames the Sfu has been decoded
	*/
	@JsonProperty("framesDecoded")
	public Integer framesDecoded;
	/**
	* Indicate the number of keyframes the Sfu has been decoded
	*/
	@JsonProperty("keyFramesDecoded")
	public Integer keyFramesDecoded;
	/**
	* The calculated fractionLost of the stream
	*/
	@JsonProperty("fractionLost")
	public Double fractionLost;
	/**
	* The calculated jitter of the stream
	*/
	@JsonProperty("jitter")
	public Double jitter;
	/**
	* The calculated RTT of the stream
	*/
	@JsonProperty("roundTripTime")
	public Double roundTripTime;


	public static class Builder {

		private SfuInboundRtpPadReport result = new SfuInboundRtpPadReport();

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
		public Builder setInternal(Boolean value) {
			this.result.internal = value;
			return this;
		}
		public Builder setTimestamp(Long value) {
			this.result.timestamp = value;
			return this;
		}
		public Builder setTransportId(String value) {
			this.result.transportId = value;
			return this;
		}
		public Builder setSfuStreamId(String value) {
			this.result.sfuStreamId = value;
			return this;
		}
		public Builder setRtpPadId(String value) {
			this.result.rtpPadId = value;
			return this;
		}
		public Builder setSsrc(Long value) {
			this.result.ssrc = value;
			return this;
		}
		public Builder setTrackId(String value) {
			this.result.trackId = value;
			return this;
		}
		public Builder setClientId(String value) {
			this.result.clientId = value;
			return this;
		}
		public Builder setCallId(String value) {
			this.result.callId = value;
			return this;
		}
		public Builder setMediaType(String value) {
			this.result.mediaType = value;
			return this;
		}
		public Builder setPayloadType(Integer value) {
			this.result.payloadType = value;
			return this;
		}
		public Builder setMimeType(String value) {
			this.result.mimeType = value;
			return this;
		}
		public Builder setClockRate(Integer value) {
			this.result.clockRate = value;
			return this;
		}
		public Builder setSdpFmtpLine(String value) {
			this.result.sdpFmtpLine = value;
			return this;
		}
		public Builder setRid(String value) {
			this.result.rid = value;
			return this;
		}
		public Builder setRtxSsrc(Long value) {
			this.result.rtxSsrc = value;
			return this;
		}
		public Builder setTargetBitrate(Integer value) {
			this.result.targetBitrate = value;
			return this;
		}
		public Builder setVoiceActivityFlag(Boolean value) {
			this.result.voiceActivityFlag = value;
			return this;
		}
		public Builder setFirCount(Integer value) {
			this.result.firCount = value;
			return this;
		}
		public Builder setPliCount(Integer value) {
			this.result.pliCount = value;
			return this;
		}
		public Builder setNackCount(Integer value) {
			this.result.nackCount = value;
			return this;
		}
		public Builder setSliCount(Integer value) {
			this.result.sliCount = value;
			return this;
		}
		public Builder setPacketsLost(Integer value) {
			this.result.packetsLost = value;
			return this;
		}
		public Builder setPacketsReceived(Integer value) {
			this.result.packetsReceived = value;
			return this;
		}
		public Builder setPacketsDiscarded(Integer value) {
			this.result.packetsDiscarded = value;
			return this;
		}
		public Builder setPacketsRepaired(Integer value) {
			this.result.packetsRepaired = value;
			return this;
		}
		public Builder setPacketsFailedDecryption(Integer value) {
			this.result.packetsFailedDecryption = value;
			return this;
		}
		public Builder setPacketsDuplicated(Integer value) {
			this.result.packetsDuplicated = value;
			return this;
		}
		public Builder setFecPacketsReceived(Integer value) {
			this.result.fecPacketsReceived = value;
			return this;
		}
		public Builder setFecPacketsDiscarded(Integer value) {
			this.result.fecPacketsDiscarded = value;
			return this;
		}
		public Builder setBytesReceived(Long value) {
			this.result.bytesReceived = value;
			return this;
		}
		public Builder setRtcpSrReceived(Integer value) {
			this.result.rtcpSrReceived = value;
			return this;
		}
		public Builder setRtcpRrSent(Integer value) {
			this.result.rtcpRrSent = value;
			return this;
		}
		public Builder setRtxPacketsReceived(Integer value) {
			this.result.rtxPacketsReceived = value;
			return this;
		}
		public Builder setRtxPacketsDiscarded(Integer value) {
			this.result.rtxPacketsDiscarded = value;
			return this;
		}
		public Builder setFramesReceived(Integer value) {
			this.result.framesReceived = value;
			return this;
		}
		public Builder setFramesDecoded(Integer value) {
			this.result.framesDecoded = value;
			return this;
		}
		public Builder setKeyFramesDecoded(Integer value) {
			this.result.keyFramesDecoded = value;
			return this;
		}
		public Builder setFractionLost(Double value) {
			this.result.fractionLost = value;
			return this;
		}
		public Builder setJitter(Double value) {
			this.result.jitter = value;
			return this;
		}
		public Builder setRoundTripTime(Double value) {
			this.result.roundTripTime = value;
			return this;
		}
		public SfuInboundRtpPadReport build() {
			return this.result;
		}
	}
}