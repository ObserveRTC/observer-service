package org.observertc.schemas.v210.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for Inbound Audio Tracks. A combination of Codec metadata carrying inbound and remote outbound RTP stats measurements
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class InboundAudioTrackReport {
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
	* Represents the timestamp at which the last packet was received on the corresponded synchronization source (ssrc)
	*/
	@JsonProperty("lastPacketReceivedTimestamp")
	public Long lastPacketReceivedTimestamp;
	/**
	* Total number of RTP header and padding bytes received over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("headerBytesReceived")
	public Long headerBytesReceived;
	/**
	* The total number of packets missed the playout point and therefore discarded by the jitterbuffer
	*/
	@JsonProperty("packetsDiscarded")
	public Integer packetsDiscarded;
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
	* This value is increased by the target jitter buffer delay every time a sample is emitted by the jitter buffer. The added target is the target delay, in seconds, at the time that the sample was emitted from the jitter buffer. 
	*/
	@JsonProperty("jitterBufferTargetDelay")
	public Double jitterBufferTargetDelay;
	/**
	* The total number of audio samples or video frames that have come out of the jitter buffer on the corresponded synchronization source (ssrc)
	*/
	@JsonProperty("jitterBufferEmittedCount")
	public Integer jitterBufferEmittedCount;
	/**
	* This metric is purely based on the network characteristics such as jitter and packet loss, and can be seen as the minimum obtainable jitter buffer delay if no external factors would affect it
	*/
	@JsonProperty("jitterBufferMinimumDelay")
	public Double jitterBufferMinimumDelay;
	/**
	* The total number of audio samples received on the corresponded RTP stream
	*/
	@JsonProperty("totalSamplesReceived")
	public Integer totalSamplesReceived;
	/**
	* The total number of samples decoded by the media decoder from the corresponded RTP stream
	*/
	@JsonProperty("concealedSamples")
	public Integer concealedSamples;
	/**
	* The total number of samples concealed from the corresponded RTP stream
	*/
	@JsonProperty("silentConcealedSamples")
	public Integer silentConcealedSamples;
	/**
	* The total number of concealed event emitted to the media codec by the corresponded jitterbuffer
	*/
	@JsonProperty("concealmentEvents")
	public Integer concealmentEvents;
	/**
	* The total number of samples inserted to decelarete the audio playout (happens when the jitterbuffer detects a shrinking buffer and need to increase the jitter buffer delay)
	*/
	@JsonProperty("insertedSamplesForDeceleration")
	public Integer insertedSamplesForDeceleration;
	/**
	* The total number of samples inserted to accelerate the audio playout (happens when the jitterbuffer detects a growing buffer and need to shrink the jitter buffer delay)
	*/
	@JsonProperty("removedSamplesForAcceleration")
	public Integer removedSamplesForAcceleration;
	/**
	* The current audio level
	*/
	@JsonProperty("audioLevel")
	public Integer audioLevel;
	/**
	* Represents the energy level reported by the media source
	*/
	@JsonProperty("totalAudioEnergy")
	public Integer totalAudioEnergy;
	/**
	* Represents the total duration of the audio samples the media source actually transconverted in seconds
	*/
	@JsonProperty("totalSamplesDuration")
	public Integer totalSamplesDuration;
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
	* Estimated round trip time for the SR reports based on DLRR reports on the corresponded RTP stream
	*/
	@JsonProperty("roundTripTime")
	public Double roundTripTime;
	/**
	*  Represents the cumulative sum of all round trip time measurements performed on the corresponded RTP stream
	*/
	@JsonProperty("totalRoundTripTime")
	public Double totalRoundTripTime;
	/**
	* Represents the total number of SR reports received with DLRR reports to be able to calculate the round trip time on the corresponded RTP stream
	*/
	@JsonProperty("roundTripTimeMeasurements")
	public Integer roundTripTimeMeasurements;
	/**
	* This metric can be used together with totalSamplesDuration to calculate the percentage of played out media being synthesized
	*/
	@JsonProperty("synthesizedSamplesDuration")
	public Integer synthesizedSamplesDuration;
	/**
	* The number of synthesized samples events.
	*/
	@JsonProperty("synthesizedSamplesEvents")
	public Integer synthesizedSamplesEvents;
	/**
	*  The playout delay includes the delay from being emitted to the actual time of playout on the device
	*/
	@JsonProperty("totalPlayoutDelay")
	public Double totalPlayoutDelay;
	/**
	* When audio samples are pulled by the playout device, this counter is incremented with the number of samples emitted for playout
	*/
	@JsonProperty("totalSamplesCount")
	public Integer totalSamplesCount;


	public static class Builder {

		private InboundAudioTrackReport result = new InboundAudioTrackReport();

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
		public Builder setTrackId(String value) {
			this.result.trackId = value;
			return this;
		}
		public Builder setSfuStreamId(String value) {
			this.result.sfuStreamId = value;
			return this;
		}
		public Builder setSfuSinkId(String value) {
			this.result.sfuSinkId = value;
			return this;
		}
		public Builder setRemoteTrackId(String value) {
			this.result.remoteTrackId = value;
			return this;
		}
		public Builder setRemoteUserId(String value) {
			this.result.remoteUserId = value;
			return this;
		}
		public Builder setRemoteClientId(String value) {
			this.result.remoteClientId = value;
			return this;
		}
		public Builder setRemotePeerConnectionId(String value) {
			this.result.remotePeerConnectionId = value;
			return this;
		}
		public Builder setSampleSeq(Integer value) {
			this.result.sampleSeq = value;
			return this;
		}
		public Builder setSsrc(Long value) {
			this.result.ssrc = value;
			return this;
		}
		public Builder setPacketsReceived(Integer value) {
			this.result.packetsReceived = value;
			return this;
		}
		public Builder setPacketsLost(Integer value) {
			this.result.packetsLost = value;
			return this;
		}
		public Builder setJitter(Double value) {
			this.result.jitter = value;
			return this;
		}
		public Builder setLastPacketReceivedTimestamp(Long value) {
			this.result.lastPacketReceivedTimestamp = value;
			return this;
		}
		public Builder setHeaderBytesReceived(Long value) {
			this.result.headerBytesReceived = value;
			return this;
		}
		public Builder setPacketsDiscarded(Integer value) {
			this.result.packetsDiscarded = value;
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
		public Builder setNackCount(Integer value) {
			this.result.nackCount = value;
			return this;
		}
		public Builder setTotalProcessingDelay(Double value) {
			this.result.totalProcessingDelay = value;
			return this;
		}
		public Builder setEstimatedPlayoutTimestamp(Long value) {
			this.result.estimatedPlayoutTimestamp = value;
			return this;
		}
		public Builder setJitterBufferDelay(Double value) {
			this.result.jitterBufferDelay = value;
			return this;
		}
		public Builder setJitterBufferTargetDelay(Double value) {
			this.result.jitterBufferTargetDelay = value;
			return this;
		}
		public Builder setJitterBufferEmittedCount(Integer value) {
			this.result.jitterBufferEmittedCount = value;
			return this;
		}
		public Builder setJitterBufferMinimumDelay(Double value) {
			this.result.jitterBufferMinimumDelay = value;
			return this;
		}
		public Builder setTotalSamplesReceived(Integer value) {
			this.result.totalSamplesReceived = value;
			return this;
		}
		public Builder setConcealedSamples(Integer value) {
			this.result.concealedSamples = value;
			return this;
		}
		public Builder setSilentConcealedSamples(Integer value) {
			this.result.silentConcealedSamples = value;
			return this;
		}
		public Builder setConcealmentEvents(Integer value) {
			this.result.concealmentEvents = value;
			return this;
		}
		public Builder setInsertedSamplesForDeceleration(Integer value) {
			this.result.insertedSamplesForDeceleration = value;
			return this;
		}
		public Builder setRemovedSamplesForAcceleration(Integer value) {
			this.result.removedSamplesForAcceleration = value;
			return this;
		}
		public Builder setAudioLevel(Integer value) {
			this.result.audioLevel = value;
			return this;
		}
		public Builder setTotalAudioEnergy(Integer value) {
			this.result.totalAudioEnergy = value;
			return this;
		}
		public Builder setTotalSamplesDuration(Integer value) {
			this.result.totalSamplesDuration = value;
			return this;
		}
		public Builder setDecoderImplementation(String value) {
			this.result.decoderImplementation = value;
			return this;
		}
		public Builder setPacketsSent(Integer value) {
			this.result.packetsSent = value;
			return this;
		}
		public Builder setBytesSent(Long value) {
			this.result.bytesSent = value;
			return this;
		}
		public Builder setRemoteTimestamp(Long value) {
			this.result.remoteTimestamp = value;
			return this;
		}
		public Builder setReportsSent(Integer value) {
			this.result.reportsSent = value;
			return this;
		}
		public Builder setRoundTripTime(Double value) {
			this.result.roundTripTime = value;
			return this;
		}
		public Builder setTotalRoundTripTime(Double value) {
			this.result.totalRoundTripTime = value;
			return this;
		}
		public Builder setRoundTripTimeMeasurements(Integer value) {
			this.result.roundTripTimeMeasurements = value;
			return this;
		}
		public Builder setSynthesizedSamplesDuration(Integer value) {
			this.result.synthesizedSamplesDuration = value;
			return this;
		}
		public Builder setSynthesizedSamplesEvents(Integer value) {
			this.result.synthesizedSamplesEvents = value;
			return this;
		}
		public Builder setTotalPlayoutDelay(Double value) {
			this.result.totalPlayoutDelay = value;
			return this;
		}
		public Builder setTotalSamplesCount(Integer value) {
			this.result.totalSamplesCount = value;
			return this;
		}
		public InboundAudioTrackReport build() {
			return this.result;
		}
	}
}