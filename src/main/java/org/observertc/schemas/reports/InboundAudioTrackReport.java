package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for Inbound Audio Tracks. A combination of Codec metadata carrying inbound and remote outbound RTP stats measurements
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class InboundAudioTrackReport {
	public static final String VERSION="2.0.0-beta.61";
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
	* The webrtc app provided label the peer connection is labeled with
	*/
	@JsonProperty("label")
	public String label;
	/**
	* The id of the track
	*/
	@JsonProperty("trackId")
	public String trackId;
	/**
	* The id of the Sfu stream the media from
	*/
	@JsonProperty("sfuStreamId")
	public String sfuStreamId;
	/**
	* The id of the sink the Sfu streamed the media out
	*/
	@JsonProperty("sfuSinkId")
	public String sfuSinkId;
	/**
	* The id of the remote track this inbound track is originated from
	*/
	@JsonProperty("remoteTrackId")
	public String remoteTrackId;
	/**
	* The webrtc app provided user id the track belongs to, or if the webrtc app did not provided the observer tried to match it
	*/
	@JsonProperty("remoteUserId")
	public String remoteUserId;
	/**
	* The observer matched remote client Id
	*/
	@JsonProperty("remoteClientId")
	public String remoteClientId;
	/**
	* The observer matched remote Peer Connection Id
	*/
	@JsonProperty("remotePeerConnectionId")
	public String remotePeerConnectionId;
	/**
	* The sequence number of the sample the report is generated from
	*/
	@JsonProperty("sampleSeq")
	public Integer sampleSeq;
	/**
	* The RTP SSRC field
	*/
	@JsonProperty("ssrc")
	public Long ssrc;
	/**
	* The total number of packets received on the corresponded synchronization source
	*/
	@JsonProperty("packetsReceived")
	public Integer packetsReceived;
	/**
	* The total number of bytes received on the corresponded synchronization source
	*/
	@JsonProperty("packetsLost")
	public Integer packetsLost;
	/**
	* The corresponded synchronization source reported jitter
	*/
	@JsonProperty("jitter")
	public Double jitter;
	/**
	* The total number of packets missed the playout point and therefore discarded by the jitterbuffer
	*/
	@JsonProperty("packetsDiscarded")
	public Integer packetsDiscarded;
	/**
	* The total number of packets repaired by either FEC or due to retransmission on the corresponded synchronization source
	*/
	@JsonProperty("packetsRepaired")
	public Integer packetsRepaired;
	/**
	* The total number of packets lost in burst (RFC6958)
	*/
	@JsonProperty("burstPacketsLost")
	public Integer burstPacketsLost;
	/**
	* The total number of packets discarded in burst (RFC6958)
	*/
	@JsonProperty("burstPacketsDiscarded")
	public Integer burstPacketsDiscarded;
	/**
	* The total number of burst happened causes burstPacketsLost on the corresponding synchronization source
	*/
	@JsonProperty("burstLossCount")
	public Integer burstLossCount;
	/**
	* The total number of burst happened causes burstPacketsDiscarded on the corresponding synchronization source
	*/
	@JsonProperty("burstDiscardCount")
	public Integer burstDiscardCount;
	/**
	* The fraction of RTP packets lost during bursts proportionally to the total number of RTP packets expected in the bursts on the corresponding synchronization source
	*/
	@JsonProperty("burstLossRate")
	public Double burstLossRate;
	/**
	* The fraction of RTP packets discarded during bursts proportionally to the total number of RTP packets expected in the bursts on the corresponding synchronization source
	*/
	@JsonProperty("burstDiscardRate")
	public Double burstDiscardRate;
	/**
	* The fraction of RTP packets lost during gap proportionally to the total number of RTP packets expected in the bursts on the corresponding synchronization source
	*/
	@JsonProperty("gapLossRate")
	public Double gapLossRate;
	/**
	* The fraction of RTP packets discarded during gap proportionally to the total number of RTP packets expected in the bursts on the corresponding synchronization source
	*/
	@JsonProperty("gapDiscardRate")
	public Double gapDiscardRate;
	/**
	* Indicate if the last RTP packet received contained voice activity based on the presence of the V bit in the extension header
	*/
	@JsonProperty("voiceActivityFlag")
	public Boolean voiceActivityFlag;
	/**
	* Represents the timestamp at which the last packet was received on the corresponded synchronization source (ssrc)
	*/
	@JsonProperty("lastPacketReceivedTimestamp")
	public Long lastPacketReceivedTimestamp;
	/**
	* The average RTCP interval between two consecutive compound RTCP packets sent for the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("averageRtcpInterval")
	public Double averageRtcpInterval;
	/**
	* Total number of RTP header and padding bytes received over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("headerBytesReceived")
	public Long headerBytesReceived;
	/**
	* Total number of FEC packets received over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("fecPacketsReceived")
	public Integer fecPacketsReceived;
	/**
	* Total number of FEC packets discarded over the corresponding synchronization source (ssrc) due to 1) late arrive; 2) the target RTP packet has already been repaired.
	*/
	@JsonProperty("fecPacketsDiscarded")
	public Integer fecPacketsDiscarded;
	/**
	* Total number of bytes received over the corresponding synchronization source (ssrc) due to 1) late arrive; 2) the target RTP packet has already been repaired.
	*/
	@JsonProperty("bytesReceived")
	public Long bytesReceived;
	/**
	* Total number of packets received and failed to decrypt over the corresponding synchronization source (ssrc) due to 1) late arrive; 2) the target RTP packet has already been repaired.
	*/
	@JsonProperty("packetsFailedDecryption")
	public Integer packetsFailedDecryption;
	/**
	* Total number of packets identified as duplicated over the corresponding synchronization source (ssrc).
	*/
	@JsonProperty("packetsDuplicated")
	public Integer packetsDuplicated;
	/**
	* The total number of DSCP flagged RTP packets received over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("perDscpPacketsReceived")
	public Integer perDscpPacketsReceived;
	/**
	* Count the total number of Negative ACKnowledgement (NACK) packets sent and belongs to the corresponded synchronization source (ssrc)
	*/
	@JsonProperty("nackCount")
	public Integer nackCount;
	/**
	* The total processing delay in seconds spend on buffering RTP packets from received up until packets are decoded
	*/
	@JsonProperty("totalProcessingDelay")
	public Double totalProcessingDelay;
	/**
	* The estimated playout time of the corresponded synchronization source
	*/
	@JsonProperty("estimatedPlayoutTimestamp")
	public Long estimatedPlayoutTimestamp;
	/**
	* The total time of RTP packets spent in jitterbuffer waiting for frame completion due to network uncertenity.
	*/
	@JsonProperty("jitterBufferDelay")
	public Double jitterBufferDelay;
	/**
	* The total number of audio samples or video frames that have come out of the jitter buffer on the corresponded synchronization source (ssrc)
	*/
	@JsonProperty("jitterBufferEmittedCount")
	public Integer jitterBufferEmittedCount;
	/**
	* Indicate the name of the decoder implementation library
	*/
	@JsonProperty("decoderImplementation")
	public String decoderImplementation;
	/**
	* Total number of RTP packets sent at the remote endpoint to this endpoint on this synchronization source
	*/
	@JsonProperty("packetsSent")
	public Integer packetsSent;
	/**
	* Total number of payload bytes sent at the remote endpoint to this endpoint on this synchronization source
	*/
	@JsonProperty("bytesSent")
	public Long bytesSent;
	/**
	* The timestamp corresnponds to the time in UTC Epoch the remote endpoint reported the statistics belong to the sender side and correspond to the synchronization source (ssrc)
	*/
	@JsonProperty("remoteTimestamp")
	public Long remoteTimestamp;
	/**
	* The number of SR reports the remote endpoint sent corresponded to synchronization source (ssrc) this report belongs to
	*/
	@JsonProperty("reportsSent")
	public Integer reportsSent;
	/**
	* Flag represents if the receiver ended the media stream track or not.
	*/
	@JsonProperty("ended")
	public Boolean ended;
	/**
	* The type of the payload the RTP packet SSRC belongs to
	*/
	@JsonProperty("payloadType")
	public Integer payloadType;
	/**
	* the MIME type of the codec (e.g.: video/vp8)
	*/
	@JsonProperty("mimeType")
	public String mimeType;
	/**
	* The negotiated clock rate the RTP timestamp is generated of
	*/
	@JsonProperty("clockRate")
	public Integer clockRate;
	/**
	* The number of channels for audio is used (in stereo it is 2, otherwise it is most likely null)
	*/
	@JsonProperty("channels")
	public Integer channels;
	/**
	* The a=fmtp line in the SDP corresponding to the codec
	*/
	@JsonProperty("sdpFmtpLine")
	public String sdpFmtpLine;


	public static class Builder {

		private InboundAudioTrackReport result = new InboundAudioTrackReport();

		public Builder setServiceId(String value) { this.result.serviceId = value; return this; }
		public Builder setMediaUnitId(String value) { this.result.mediaUnitId = value; return this; }
		public Builder setMarker(String value) { this.result.marker = value; return this; }
		public Builder setTimestamp(Long value) { this.result.timestamp = value; return this; }
		public Builder setCallId(String value) { this.result.callId = value; return this; }
		public Builder setRoomId(String value) { this.result.roomId = value; return this; }
		public Builder setClientId(String value) { this.result.clientId = value; return this; }
		public Builder setUserId(String value) { this.result.userId = value; return this; }
		public Builder setPeerConnectionId(String value) { this.result.peerConnectionId = value; return this; }
		public Builder setLabel(String value) { this.result.label = value; return this; }
		public Builder setTrackId(String value) { this.result.trackId = value; return this; }
		public Builder setSfuStreamId(String value) { this.result.sfuStreamId = value; return this; }
		public Builder setSfuSinkId(String value) { this.result.sfuSinkId = value; return this; }
		public Builder setRemoteTrackId(String value) { this.result.remoteTrackId = value; return this; }
		public Builder setRemoteUserId(String value) { this.result.remoteUserId = value; return this; }
		public Builder setRemoteClientId(String value) { this.result.remoteClientId = value; return this; }
		public Builder setRemotePeerConnectionId(String value) { this.result.remotePeerConnectionId = value; return this; }
		public Builder setSampleSeq(Integer value) { this.result.sampleSeq = value; return this; }
		public Builder setSsrc(Long value) { this.result.ssrc = value; return this; }
		public Builder setPacketsReceived(Integer value) { this.result.packetsReceived = value; return this; }
		public Builder setPacketsLost(Integer value) { this.result.packetsLost = value; return this; }
		public Builder setJitter(Double value) { this.result.jitter = value; return this; }
		public Builder setPacketsDiscarded(Integer value) { this.result.packetsDiscarded = value; return this; }
		public Builder setPacketsRepaired(Integer value) { this.result.packetsRepaired = value; return this; }
		public Builder setBurstPacketsLost(Integer value) { this.result.burstPacketsLost = value; return this; }
		public Builder setBurstPacketsDiscarded(Integer value) { this.result.burstPacketsDiscarded = value; return this; }
		public Builder setBurstLossCount(Integer value) { this.result.burstLossCount = value; return this; }
		public Builder setBurstDiscardCount(Integer value) { this.result.burstDiscardCount = value; return this; }
		public Builder setBurstLossRate(Double value) { this.result.burstLossRate = value; return this; }
		public Builder setBurstDiscardRate(Double value) { this.result.burstDiscardRate = value; return this; }
		public Builder setGapLossRate(Double value) { this.result.gapLossRate = value; return this; }
		public Builder setGapDiscardRate(Double value) { this.result.gapDiscardRate = value; return this; }
		public Builder setVoiceActivityFlag(Boolean value) { this.result.voiceActivityFlag = value; return this; }
		public Builder setLastPacketReceivedTimestamp(Long value) { this.result.lastPacketReceivedTimestamp = value; return this; }
		public Builder setAverageRtcpInterval(Double value) { this.result.averageRtcpInterval = value; return this; }
		public Builder setHeaderBytesReceived(Long value) { this.result.headerBytesReceived = value; return this; }
		public Builder setFecPacketsReceived(Integer value) { this.result.fecPacketsReceived = value; return this; }
		public Builder setFecPacketsDiscarded(Integer value) { this.result.fecPacketsDiscarded = value; return this; }
		public Builder setBytesReceived(Long value) { this.result.bytesReceived = value; return this; }
		public Builder setPacketsFailedDecryption(Integer value) { this.result.packetsFailedDecryption = value; return this; }
		public Builder setPacketsDuplicated(Integer value) { this.result.packetsDuplicated = value; return this; }
		public Builder setPerDscpPacketsReceived(Integer value) { this.result.perDscpPacketsReceived = value; return this; }
		public Builder setNackCount(Integer value) { this.result.nackCount = value; return this; }
		public Builder setTotalProcessingDelay(Double value) { this.result.totalProcessingDelay = value; return this; }
		public Builder setEstimatedPlayoutTimestamp(Long value) { this.result.estimatedPlayoutTimestamp = value; return this; }
		public Builder setJitterBufferDelay(Double value) { this.result.jitterBufferDelay = value; return this; }
		public Builder setJitterBufferEmittedCount(Integer value) { this.result.jitterBufferEmittedCount = value; return this; }
		public Builder setDecoderImplementation(String value) { this.result.decoderImplementation = value; return this; }
		public Builder setPacketsSent(Integer value) { this.result.packetsSent = value; return this; }
		public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
		public Builder setRemoteTimestamp(Long value) { this.result.remoteTimestamp = value; return this; }
		public Builder setReportsSent(Integer value) { this.result.reportsSent = value; return this; }
		public Builder setEnded(Boolean value) { this.result.ended = value; return this; }
		public Builder setPayloadType(Integer value) { this.result.payloadType = value; return this; }
		public Builder setMimeType(String value) { this.result.mimeType = value; return this; }
		public Builder setClockRate(Integer value) { this.result.clockRate = value; return this; }
		public Builder setChannels(Integer value) { this.result.channels = value; return this; }
		public Builder setSdpFmtpLine(String value) { this.result.sdpFmtpLine = value; return this; }
		public InboundAudioTrackReport build() {
			return this.result;
		}
	}
}