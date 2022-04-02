package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for RTP streams going through the SFU
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class SfuOutboundRtpPadReport {
	public static final String VERSION="2.0.0-beta.54";
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
	* Unique identifier of the Sfu sink the event is related to
	*/
	@JsonProperty("sfuSinkId")
	public String sfuSinkId;
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
	* The callId the event belongs to
	*/
	@JsonProperty("callId")
	public String callId;
	/**
	* If the track id was provided by the Sfu, the observer can fill up the information of which client it belongs to
	*/
	@JsonProperty("clientId")
	public String clientId;
	/**
	* The id of the track the RTP stream related to at the client side
	*/
	@JsonProperty("trackId")
	public String trackId;
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
	* The total number of packets sent on the corresponded RTP stream.
	*/
	@JsonProperty("packetsSent")
	public Integer packetsSent;
	/**
	* The total number of discarded packets on the corresponded RTP stream.
	*/
	@JsonProperty("packetsDiscarded")
	public Integer packetsDiscarded;
	/**
	* The total number of packets retransmitted on the corresponded RTP stream.
	*/
	@JsonProperty("packetsRetransmitted")
	public Integer packetsRetransmitted;
	/**
	* The total number of packets failed to be encrypted on the corresponded RTP stream.
	*/
	@JsonProperty("packetsFailedEncryption")
	public Integer packetsFailedEncryption;
	/**
	* The total number of duplicated packets appeared on the corresponded RTP stream.
	*/
	@JsonProperty("packetsDuplicated")
	public Integer packetsDuplicated;
	/**
	* The total number of FEC packets sent on the corresponded RTP stream.
	*/
	@JsonProperty("fecPacketsSent")
	public Integer fecPacketsSent;
	/**
	* The total number of FEC packets discarded on the corresponded RTP stream.
	*/
	@JsonProperty("fecPacketsDiscarded")
	public Integer fecPacketsDiscarded;
	/**
	* The total amount of payload bytes sent on the corresponded RTP stream.
	*/
	@JsonProperty("bytesSent")
	public Long bytesSent;
	/**
	* The total number of SR reports sent by the corresponded RTP stream
	*/
	@JsonProperty("rtcpSrSent")
	public Integer rtcpSrSent;
	/**
	* The total number of RR reports received on the corresponded RTP stream
	*/
	@JsonProperty("rtcpRrReceived")
	public Integer rtcpRrReceived;
	/**
	* If rtx packets sent on the same stream then this number indicates how may has been sent
	*/
	@JsonProperty("rtxPacketsSent")
	public Integer rtxPacketsSent;
	/**
	* If rtx packets are received on the same stream then this number indicates how may has been discarded
	*/
	@JsonProperty("rtxPacketsDiscarded")
	public Integer rtxPacketsDiscarded;
	/**
	* The number of frames sent on the corresponded RTP stream
	*/
	@JsonProperty("framesSent")
	public Integer framesSent;
	/**
	* Indicate the number of frames the Sfu has been encoded
	*/
	@JsonProperty("framesEncoded")
	public Integer framesEncoded;
	/**
	* Indicate the number of keyframes the Sfu has been encoded on the corresponded RTP stream
	*/
	@JsonProperty("keyFramesEncoded")
	public Integer keyFramesEncoded;


	public static class Builder {

		private SfuOutboundRtpPadReport result = new SfuOutboundRtpPadReport();

		public Builder setServiceId(String value) { this.result.serviceId = value; return this; }
		public Builder setMediaUnitId(String value) { this.result.mediaUnitId = value; return this; }
		public Builder setSfuId(String value) { this.result.sfuId = value; return this; }
		public Builder setMarker(String value) { this.result.marker = value; return this; }
		public Builder setInternal(Boolean value) { this.result.internal = value; return this; }
		public Builder setTimestamp(Long value) { this.result.timestamp = value; return this; }
		public Builder setTransportId(String value) { this.result.transportId = value; return this; }
		public Builder setSfuStreamId(String value) { this.result.sfuStreamId = value; return this; }
		public Builder setSfuSinkId(String value) { this.result.sfuSinkId = value; return this; }
		public Builder setRtpPadId(String value) { this.result.rtpPadId = value; return this; }
		public Builder setSsrc(Long value) { this.result.ssrc = value; return this; }
		public Builder setCallId(String value) { this.result.callId = value; return this; }
		public Builder setClientId(String value) { this.result.clientId = value; return this; }
		public Builder setTrackId(String value) { this.result.trackId = value; return this; }
		public Builder setMediaType(String value) { this.result.mediaType = value; return this; }
		public Builder setPayloadType(Integer value) { this.result.payloadType = value; return this; }
		public Builder setMimeType(String value) { this.result.mimeType = value; return this; }
		public Builder setClockRate(Integer value) { this.result.clockRate = value; return this; }
		public Builder setSdpFmtpLine(String value) { this.result.sdpFmtpLine = value; return this; }
		public Builder setRid(String value) { this.result.rid = value; return this; }
		public Builder setRtxSsrc(Long value) { this.result.rtxSsrc = value; return this; }
		public Builder setTargetBitrate(Integer value) { this.result.targetBitrate = value; return this; }
		public Builder setVoiceActivityFlag(Boolean value) { this.result.voiceActivityFlag = value; return this; }
		public Builder setFirCount(Integer value) { this.result.firCount = value; return this; }
		public Builder setPliCount(Integer value) { this.result.pliCount = value; return this; }
		public Builder setNackCount(Integer value) { this.result.nackCount = value; return this; }
		public Builder setSliCount(Integer value) { this.result.sliCount = value; return this; }
		public Builder setPacketsLost(Integer value) { this.result.packetsLost = value; return this; }
		public Builder setPacketsSent(Integer value) { this.result.packetsSent = value; return this; }
		public Builder setPacketsDiscarded(Integer value) { this.result.packetsDiscarded = value; return this; }
		public Builder setPacketsRetransmitted(Integer value) { this.result.packetsRetransmitted = value; return this; }
		public Builder setPacketsFailedEncryption(Integer value) { this.result.packetsFailedEncryption = value; return this; }
		public Builder setPacketsDuplicated(Integer value) { this.result.packetsDuplicated = value; return this; }
		public Builder setFecPacketsSent(Integer value) { this.result.fecPacketsSent = value; return this; }
		public Builder setFecPacketsDiscarded(Integer value) { this.result.fecPacketsDiscarded = value; return this; }
		public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
		public Builder setRtcpSrSent(Integer value) { this.result.rtcpSrSent = value; return this; }
		public Builder setRtcpRrReceived(Integer value) { this.result.rtcpRrReceived = value; return this; }
		public Builder setRtxPacketsSent(Integer value) { this.result.rtxPacketsSent = value; return this; }
		public Builder setRtxPacketsDiscarded(Integer value) { this.result.rtxPacketsDiscarded = value; return this; }
		public Builder setFramesSent(Integer value) { this.result.framesSent = value; return this; }
		public Builder setFramesEncoded(Integer value) { this.result.framesEncoded = value; return this; }
		public Builder setKeyFramesEncoded(Integer value) { this.result.keyFramesEncoded = value; return this; }
		public SfuOutboundRtpPadReport build() {
			return this.result;
		}
	}
}