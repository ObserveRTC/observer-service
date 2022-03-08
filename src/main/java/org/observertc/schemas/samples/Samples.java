package org.observertc.schemas.samples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Observer created reports related to events (call started, call ended, client joined, etc...) indicated by the incoming samples.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Samples {
	public static final String VERSION="2.0.0-beta.25";
	/**
	 * undefined
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SamplesMeta {
		/**
		 * Indicate the version of the schema for compatibility measures.
		 */
		@JsonProperty("schemaVersion")
		public String schemaVersion;
	}
	/**
	 * undefined
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ControlFlags {
		/**
		 * Indicate that the server should close the connection
		 */
		@JsonProperty("close")
		public Boolean close;
	}
	/**
	 * docs
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ClientSample {
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Engine {
			/**
			 * The name of the Engine
			 */
			@JsonProperty("name")
			public String name;
			/**
			 * The version of the engine
			 */
			@JsonProperty("version")
			public String version;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Platform {
			/**
			 * The name of the platform
			 */
			@JsonProperty("type")
			public String type;
			/**
			 * The name of the vendor
			 */
			@JsonProperty("vendor")
			public String vendor;
			/**
			 * The name of the model
			 */
			@JsonProperty("model")
			public String model;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Browser {
			/**
			 * The name of the operation system (e.g.: linux) the webrtc app uses
			 */
			@JsonProperty("name")
			public String name;
			/**
			 * The version of the operation system
			 */
			@JsonProperty("version")
			public String version;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class OperationSystem {
			/**
			 * The name of the operation system (e.g.: linux) the webrtc app uses
			 */
			@JsonProperty("name")
			public String name;
			/**
			 * The version of the operation system
			 */
			@JsonProperty("version")
			public String version;
			/**
			 * The name of the version of the operation system
			 */
			@JsonProperty("versionName")
			public String versionName;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class MediaDevice {
			/**
			 * the provided id of the media input / output
			 */
			@JsonProperty("id")
			public String id;
			/**
			 * The media kind of the media device
			 */
			@JsonProperty("kind")
			public String kind;
			/**
			 * The name of the device
			 */
			@JsonProperty("label")
			public String label;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class ExtensionStat {
			/**
			 * The type of the extension stats the custom app provides
			 */
			@JsonProperty("type")
			public String type;
			/**
			 * The payload of the extension stats the custom app provides
			 */
			@JsonProperty("payload")
			public String payload;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class PeerConnectionTransport {
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
			 * Represents the number of unique RTCDataChannels that have entered the "open" state during their lifetime.
			 */
			@JsonProperty("dataChannelsOpened")
			public Integer dataChannelsOpened;
			/**
			 * Represents the number of unique RTCDataChannels that had the "open" state, but now they are "closed"
			 */
			@JsonProperty("dataChannelsClosed")
			public Integer dataChannelsClosed;
			/**
			 * Represents the number of unique RTCDataChannels successfully requested from RTCPeerConnection.
			 */
			@JsonProperty("dataChannelsRequested")
			public Integer dataChannelsRequested;
			/**
			 * Represents the number of unique RTCDataChannels signaled in a ondatachannel event on the RTCPeerConnection.
			 */
			@JsonProperty("dataChannelsAccepted")
			public Integer dataChannelsAccepted;
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
			@JsonProperty("iceState")
			public String iceState;
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
			public Long candidatePairPacketsDiscardedOnSend;
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
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class MediaSourceStat {
			/**
			 * The unique identifier of the corresponded media track
			 */
			@JsonProperty("trackIdentifier")
			public String trackIdentifier;
			/**
			 * The type of the media the Mediasource produces.
			 */
			@JsonProperty("kind")
			public String kind;
			/**
			 * Flag indicating if the media source is relayed or not, meaning the local endpoint is not the actual source of the media, but a proxy for that media.
			 */
			@JsonProperty("relayedSource")
			public Boolean relayedSource;
			/**
			 * The value is between 0..1 (linear), where 1.0 represents 0 dBov, 0 represents silence, and 0.5 represents approximately 6 dBSPL change in the sound pressure level from 0 dBov.
			 */
			@JsonProperty("audioLevel")
			public Double audioLevel;
			/**
			 * The audio energy of the media source. For calculation see www.w3.org/TR/webrtc-stats/#dom-rtcaudiosourcestats-totalaudioenergy
			 */
			@JsonProperty("totalAudioEnergy")
			public Double totalAudioEnergy;
			/**
			 * The duration of the audio type media source
			 */
			@JsonProperty("totalSamplesDuration")
			public Double totalSamplesDuration;
			/**
			 * if echo cancellation is applied on the media source, then this number represents the loss calculation defined in www.itu.int/rec/T-REC-G.168-201504-I/en
			 */
			@JsonProperty("echoReturnLoss")
			public Double echoReturnLoss;
			/**
			 * www.itu.int/rec/T-REC-G.168-201504-I/en
			 */
			@JsonProperty("echoReturnLossEnhancement")
			public Double echoReturnLossEnhancement;
			/**
			 * The width, in pixels, of the last frame originating from the media source
			 */
			@JsonProperty("width")
			public Integer width;
			/**
			 * The height, in pixels, of the last frame originating from the media source
			 */
			@JsonProperty("height")
			public Integer height;
			/**
			 * The bitDepth, in pixels, of the last frame originating from the media source
			 */
			@JsonProperty("bitDepth")
			public Integer bitDepth;
			/**
			 * The total number of frames originated from the media source
			 */
			@JsonProperty("frames")
			public Long frames;
			/**
			 *  The number of frames origianted from the media source in the last second
			 */
			@JsonProperty("framesPerSecond")
			public Double framesPerSecond;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class MediaCodecStats {
			/**
			 * Payload type used in RTP encoding / decoding process.
			 */
			@JsonProperty("payloadType")
			public String payloadType;
			/**
			 * Indicates the role of the codec (encode or decode)
			 */
			@JsonProperty("codecType")
			public String codecType;
			/**
			 * The MIME type of the media. eg.: audio/opus.
			 */
			@JsonProperty("mimeType")
			public String mimeType;
			/**
			 * the clock rate used in RTP transport to generate the timestamp for the carried frames
			 */
			@JsonProperty("clockRate")
			public Integer clockRate;
			/**
			 * Audio Only. Represnts the number of chanels an audio media source have. Only interesting if stereo is presented
			 */
			@JsonProperty("channels")
			public Integer channels;
			/**
			 * The SDP line determines the codec
			 */
			@JsonProperty("sdpFmtpLine")
			public String sdpFmtpLine;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Certificate {
			/**
			 *  The fingerprint of the certificate.
			 */
			@JsonProperty("fingerprint")
			public String fingerprint;
			/**
			 * The hash function used to generate the fingerprint.
			 */
			@JsonProperty("fingerprintAlgorithm")
			public String fingerprintAlgorithm;
			/**
			 * The DER encoded base-64 representation of the certificate.
			 */
			@JsonProperty("base64Certificate")
			public String base64Certificate;
			/**
			 * The id of the next certificate in the certificate chain
			 */
			@JsonProperty("issuerCertificateId")
			public String issuerCertificateId;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class InboundAudioTrack {
			/**
			 * The id of the track
			 */
			@JsonProperty("trackId")
			public String trackId;
			/**
			 *  The unique generated identifier of the peer connection the inbound audio track belongs to
			 */
			@JsonProperty("peerConnectionId")
			public String peerConnectionId;
			/**
			 * The remote clientId the source outbound track belongs to
			 */
			@JsonProperty("remoteClientId")
			public String remoteClientId;
			/**
			 * The id of the sink this track belongs to in the SFU
			 */
			@JsonProperty("sfuSinkId")
			public String sfuSinkId;
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
			 * Represents the timestamp at which the last packet was received on the corresponded synchronization source (ssrc)
			 */
			@JsonProperty("lastPacketReceivedTimestamp")
			public Double lastPacketReceivedTimestamp;
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
			public Double perDscpPacketsReceived;
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
			public Double estimatedPlayoutTimestamp;
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
			 * Indicate if the last RTP packet received contained voice activity based on the presence of the V bit in the extension header
			 */
			@JsonProperty("voiceActivityFlag")
			public Boolean voiceActivityFlag;
			/**
			 * The total number of audio samples received on the corresponded RTP stream
			 */
			@JsonProperty("totalSamplesReceived")
			public Integer totalSamplesReceived;
			/**
			 * The total number of samples decoded on the corresponded RTP stream
			 */
			@JsonProperty("totalSamplesDecoded")
			public Integer totalSamplesDecoded;
			/**
			 * The total number of samples decoded with SILK on the corresponded RTP stream
			 */
			@JsonProperty("samplesDecodedWithSilk")
			public Integer samplesDecodedWithSilk;
			/**
			 * The total number of samples decodedd with CELT on the corresponded RTP stream
			 */
			@JsonProperty("samplesDecodedWithCelt")
			public Integer samplesDecodedWithCelt;
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
			public Double remoteTimestamp;
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
			public Double roundTripTimeMeasurements;
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
			public Long clockRate;
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
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class InboundVideoTrack {
			/**
			 * The id of the track
			 */
			@JsonProperty("trackId")
			public String trackId;
			/**
			 *  The unique generated identifier of the peer connection the inbound audio track belongs to
			 */
			@JsonProperty("peerConnectionId")
			public String peerConnectionId;
			/**
			 * The remote clientId the source outbound track belongs to
			 */
			@JsonProperty("remoteClientId")
			public String remoteClientId;
			/**
			 * The id of the sink this track belongs to in the SFU
			 */
			@JsonProperty("sfuSinkId")
			public String sfuSinkId;
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
			 * Represents the timestamp at which the last packet was received on the corresponded synchronization source (ssrc)
			 */
			@JsonProperty("lastPacketReceivedTimestamp")
			public Double lastPacketReceivedTimestamp;
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
			public Double perDscpPacketsReceived;
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
			public Double estimatedPlayoutTimestamp;
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
			 * The total number of frames dropped on the corresponded RTP stream
			 */
			@JsonProperty("framesDropped")
			public Integer framesDropped;
			/**
			 * The total number of frames partially lost on the corresponded RTP stream
			 */
			@JsonProperty("partialFramesLost")
			public Integer partialFramesLost;
			/**
			 * The total number of frames fully lost on the corresponded RTP stream
			 */
			@JsonProperty("fullFramesLost")
			public Integer fullFramesLost;
			/**
			 * The total number of keyframes decoded on the corresponded RTP stream
			 */
			@JsonProperty("keyFramesDecoded")
			public Integer keyFramesDecoded;
			/**
			 * The width of the frame of the video sent by the remote source on the corresponded RTP stream
			 */
			@JsonProperty("frameWidth")
			public Integer frameWidth;
			/**
			 * The height of the frame of the video sent by the remote source on the corresponded RTP stream
			 */
			@JsonProperty("frameHeight")
			public Integer frameHeight;
			/**
			 * The bit depth in pixels of the frame of the video sent by the remote source on the corresponded RTP stream
			 */
			@JsonProperty("frameBitDepth")
			public Integer frameBitDepth;
			/**
			 * The frame per seconds of the video sent by the remote source on the corresponded RTP stream
			 */
			@JsonProperty("framesPerSecond")
			public Double framesPerSecond;
			/**
			 * The QP sum (only interested in VP8,9) of the frame of the video sent by the remote source on the corresponded RTP stream
			 */
			@JsonProperty("qpSum")
			public Long qpSum;
			/**
			 * The total tiem spent on decoding video on the corresponded RTP stream
			 */
			@JsonProperty("totalDecodeTime")
			public Double totalDecodeTime;
			/**
			 * The total interframe delay
			 */
			@JsonProperty("totalInterFrameDelay")
			public Double totalInterFrameDelay;
			/**
			 * The total number FIR packets sent from this endpoint to the source on the corresponded RTP stream
			 */
			@JsonProperty("firCount")
			public Integer firCount;
			/**
			 * The total number of Picture Loss Indication sent on the corresponded RTP stream
			 */
			@JsonProperty("pliCount")
			public Integer pliCount;
			/**
			 * The total number of SLI indicator sent from the endpoint on the corresponded RTP stream
			 */
			@JsonProperty("sliCount")
			public Integer sliCount;
			/**
			 * The total number of frames received on the corresponded RTP stream.
			 */
			@JsonProperty("framesReceived")
			public Integer framesReceived;
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
			public Double remoteTimestamp;
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
			public Double roundTripTimeMeasurements;
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
			public Long clockRate;
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
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class OutboundAudioTrack {
			/**
			 * The id of the track
			 */
			@JsonProperty("trackId")
			public String trackId;
			/**
			 *  The unique generated identifier of the peer connection the inbound audio track belongs to
			 */
			@JsonProperty("peerConnectionId")
			public String peerConnectionId;
			/**
			 * The id of the SFU stream this track is related to
			 */
			@JsonProperty("sfuStreamId")
			public String sfuStreamId;
			/**
			 * The RTP SSRC field
			 */
			@JsonProperty("ssrc")
			public Long ssrc;
			/**
			 * The total number of packets sent on the corresponded synchronization source
			 */
			@JsonProperty("packetsSent")
			public Integer packetsSent;
			/**
			 * The total number of bytes sent on the corresponded synchronization source
			 */
			@JsonProperty("bytesSent")
			public Long bytesSent;
			/**
			 * If RTX is negotiated as a separate stream, this is the SSRC of the RTX stream that is associated with this stream's ssrc.
			 */
			@JsonProperty("rtxSsrc")
			public Long rtxSsrc;
			/**
			 *  The rid encoding parameter of the corresponded synchronization source
			 */
			@JsonProperty("rid")
			public String rid;
			/**
			 *  the timestamp the last packet was sent. (UTC epoch in ms)
			 */
			@JsonProperty("lastPacketSentTimestamp")
			public Long lastPacketSentTimestamp;
			/**
			 * Total number of RTP header and padding bytes sent over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("headerBytesSent")
			public Long headerBytesSent;
			/**
			 * Total number of RTP packets discarded at sender side over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("packetsDiscardedOnSend")
			public Integer packetsDiscardedOnSend;
			/**
			 * Total number of RTP bytes discarded at sender side over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("bytesDiscardedOnSend")
			public Long bytesDiscardedOnSend;
			/**
			 * Total number of FEC packets sent over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("fecPacketsSent")
			public Integer fecPacketsSent;
			/**
			 * Total number of retransmitted packets sent over the corresponding synchronization source (ssrc).
			 */
			@JsonProperty("retransmittedPacketsSent")
			public Integer retransmittedPacketsSent;
			/**
			 * Total number of retransmitted bytes sent over the corresponding synchronization source (ssrc).
			 */
			@JsonProperty("retransmittedBytesSent")
			public Long retransmittedBytesSent;
			/**
			 * Reflects the current encoder target in bits per second.
			 */
			@JsonProperty("targetBitrate")
			public Long targetBitrate;
			/**
			 * The total number of bytes of RTP coherent frames encoded completly depending on the frame size the encoder targets
			 */
			@JsonProperty("totalEncodedBytesTarget")
			public Long totalEncodedBytesTarget;
			/**
			 * The total number of delay packets buffered at the sender side in seconds over the corresponding synchronization source
			 */
			@JsonProperty("totalPacketSendDelay")
			public Double totalPacketSendDelay;
			/**
			 * The average RTCP interval between two consecutive compound RTCP packets sent for the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("averageRtcpInterval")
			public Double averageRtcpInterval;
			/**
			 * The total number of DSCP flagged RTP packets sent over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("perDscpPacketsSent")
			public Double perDscpPacketsSent;
			/**
			 * Count the total number of Negative ACKnowledgement (NACK) packets received over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("nackCount")
			public Integer nackCount;
			/**
			 * Indicate the name of the encoder implementation library
			 */
			@JsonProperty("encoderImplementation")
			public String encoderImplementation;
			/**
			 * The total number of samples sent over the corresponding synchronization source
			 */
			@JsonProperty("totalSamplesSent")
			public Integer totalSamplesSent;
			/**
			 * The total number of samples encoded by SILK portion in opus sent over the corresponding synchronization source
			 */
			@JsonProperty("samplesEncodedWithSilk")
			public Integer samplesEncodedWithSilk;
			/**
			 * The total number of samples encoded by CELT portion in opus sent over the corresponding synchronization source
			 */
			@JsonProperty("samplesEncodedWithCelt")
			public Integer samplesEncodedWithCelt;
			/**
			 * Indicate if the last RTP packet sent contained voice activity based on the presence of the V bit in the extension header
			 */
			@JsonProperty("voiceActivityFlag")
			public Boolean voiceActivityFlag;
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
			 * RTT measurement in seconds based on (most likely) SR, and RR belongs to the corresponded synchronization source
			 */
			@JsonProperty("roundTripTime")
			public Double roundTripTime;
			/**
			 * The sum of RTT measurements belongs to the corresponded synchronization source
			 */
			@JsonProperty("totalRoundTripTime")
			public Double totalRoundTripTime;
			/**
			 * The receiver reported fractional lost belongs to the corresponded synchronization source
			 */
			@JsonProperty("fractionLost")
			public Double fractionLost;
			/**
			 * The total number of RR reports received, which is the base of the remote inbound calculation on this source
			 */
			@JsonProperty("reportsReceived")
			public Integer reportsReceived;
			/**
			 * The total number of calculated RR measurements received on this source
			 */
			@JsonProperty("roundTripTimeMeasurements")
			public Integer roundTripTimeMeasurements;
			/**
			 * True if the corresponded media source is remote, false otherwise (or null depending on browser and version)
			 */
			@JsonProperty("relayedSource")
			public Boolean relayedSource;
			/**
			 * Represents the audio level reported by the media source
			 */
			@JsonProperty("audioLevel")
			public Double audioLevel;
			/**
			 * Represents the energy level reported by the media source
			 */
			@JsonProperty("totalAudioEnergy")
			public Double totalAudioEnergy;
			/**
			 * Represents the total duration of the audio samples the media source actually transconverted in seconds
			 */
			@JsonProperty("totalSamplesDuration")
			public Double totalSamplesDuration;
			/**
			 * Represents the echo cancellation in decibels corresponded to the media source.
			 */
			@JsonProperty("echoReturnLoss")
			public Double echoReturnLoss;
			/**
			 * Represents the echo cancellation in decibels added as a postprocessing by the library after the audio is catched from the emdia source.
			 */
			@JsonProperty("echoReturnLossEnhancement")
			public Double echoReturnLossEnhancement;
			/**
			 * Flag represents if the sender ended the media stream track or not.
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
			public Long clockRate;
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
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class OutboundVideoTrack {
			/**
			 * The id of the track
			 */
			@JsonProperty("trackId")
			public String trackId;
			/**
			 *  The unique generated identifier of the peer connection the inbound audio track belongs to
			 */
			@JsonProperty("peerConnectionId")
			public String peerConnectionId;
			/**
			 * The id of the SFU stream this track is related to
			 */
			@JsonProperty("sfuStreamId")
			public String sfuStreamId;
			/**
			 * The RTP SSRC field
			 */
			@JsonProperty("ssrc")
			public Long ssrc;
			/**
			 * The total number of packets sent on the corresponded synchronization source
			 */
			@JsonProperty("packetsSent")
			public Integer packetsSent;
			/**
			 * The total number of bytes sent on the corresponded synchronization source
			 */
			@JsonProperty("bytesSent")
			public Long bytesSent;
			/**
			 * If RTX is negotiated as a separate stream, this is the SSRC of the RTX stream that is associated with this stream's ssrc.
			 */
			@JsonProperty("rtxSsrc")
			public Long rtxSsrc;
			/**
			 *  The rid encoding parameter of the corresponded synchronization source
			 */
			@JsonProperty("rid")
			public String rid;
			/**
			 *  the timestamp the last packet was sent. (UTC epoch in ms)
			 */
			@JsonProperty("lastPacketSentTimestamp")
			public Long lastPacketSentTimestamp;
			/**
			 * Total number of RTP header and padding bytes sent over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("headerBytesSent")
			public Long headerBytesSent;
			/**
			 * Total number of RTP packets discarded at sender side over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("packetsDiscardedOnSend")
			public Integer packetsDiscardedOnSend;
			/**
			 * Total number of RTP bytes discarded at sender side over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("bytesDiscardedOnSend")
			public Long bytesDiscardedOnSend;
			/**
			 * Total number of FEC packets sent over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("fecPacketsSent")
			public Integer fecPacketsSent;
			/**
			 * Total number of retransmitted packets sent over the corresponding synchronization source (ssrc).
			 */
			@JsonProperty("retransmittedPacketsSent")
			public Integer retransmittedPacketsSent;
			/**
			 * Total number of retransmitted bytes sent over the corresponding synchronization source (ssrc).
			 */
			@JsonProperty("retransmittedBytesSent")
			public Long retransmittedBytesSent;
			/**
			 * Reflects the current encoder target in bits per second.
			 */
			@JsonProperty("targetBitrate")
			public Long targetBitrate;
			/**
			 * The total number of bytes of RTP coherent frames encoded completly depending on the frame size the encoder targets
			 */
			@JsonProperty("totalEncodedBytesTarget")
			public Long totalEncodedBytesTarget;
			/**
			 * The total number of delay packets buffered at the sender side in seconds over the corresponding synchronization source
			 */
			@JsonProperty("totalPacketSendDelay")
			public Double totalPacketSendDelay;
			/**
			 * The average RTCP interval between two consecutive compound RTCP packets sent for the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("averageRtcpInterval")
			public Double averageRtcpInterval;
			/**
			 * The total number of DSCP flagged RTP packets sent over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("perDscpPacketsSent")
			public Double perDscpPacketsSent;
			/**
			 * Count the total number of Negative ACKnowledgement (NACK) packets received over the corresponding synchronization source (ssrc)
			 */
			@JsonProperty("nackCount")
			public Integer nackCount;
			/**
			 * Indicate the name of the encoder implementation library
			 */
			@JsonProperty("encoderImplementation")
			public String encoderImplementation;
			/**
			 * The frame width in pixels of the frames targeted by the media encoder
			 */
			@JsonProperty("frameWidth")
			public Integer frameWidth;
			/**
			 * The frame width the media encoder targeted
			 */
			@JsonProperty("frameHeight")
			public Integer frameHeight;
			/**
			 * The frame depth in pixles on the corresponded RTP stream
			 */
			@JsonProperty("frameBitDepth")
			public Integer frameBitDepth;
			/**
			 * The encoded number of frames in the last second on the corresponded media source
			 */
			@JsonProperty("framesPerSecond")
			public Double framesPerSecond;
			/**
			 * TThe total number of frames sent on the corresponded RTP stream
			 */
			@JsonProperty("framesSent")
			public Integer framesSent;
			/**
			 * The total number of huge frames (avgFrameSize * 2.5) on the corresponded RTP stream
			 */
			@JsonProperty("hugeFramesSent")
			public Integer hugeFramesSent;
			/**
			 * The total number of frames encoded by the media source
			 */
			@JsonProperty("framesEncoded")
			public Integer framesEncoded;
			/**
			 * The total number of keyframes encoded on the corresponded RTP stream
			 */
			@JsonProperty("keyFramesEncoded")
			public Integer keyFramesEncoded;
			/**
			 * The total number of frames discarded on the corresponded RTP stream.
			 */
			@JsonProperty("framesDiscardedOnSend")
			public Integer framesDiscardedOnSend;
			/**
			 * The sum of the QP the media encoder provided on the corresponded RTP stream.
			 */
			@JsonProperty("qpSum")
			public Integer qpSum;
			/**
			 * The total time in seconds spent in encoding media frames for the corresponded RTP stream.
			 */
			@JsonProperty("totalEncodeTime")
			public Double totalEncodeTime;
			/**
			 * Time elapsed in seconds when the RTC connection has not limited the quality
			 */
			@JsonProperty("qualityLimitationDurationNone")
			public Double qualityLimitationDurationNone;
			/**
			 * Time elapsed in seconds the RTC connection had a limitation because of CPU
			 */
			@JsonProperty("qualityLimitationDurationCPU")
			public Double qualityLimitationDurationCPU;
			/**
			 * Time elapsed in seconds the RTC connection had a limitation because of Bandwidth
			 */
			@JsonProperty("qualityLimitationDurationBandwidth")
			public Double qualityLimitationDurationBandwidth;
			/**
			 * Time elapsed in seconds the RTC connection had a limitation because of Other factor
			 */
			@JsonProperty("qualityLimitationDurationOther")
			public Double qualityLimitationDurationOther;
			/**
			 * Indicate a reason for the quality limitation of the corresponded synchronization source
			 */
			@JsonProperty("qualityLimitationReason")
			public String qualityLimitationReason;
			/**
			 * The total number of resolution changes occured ont he corresponded RTP stream due to quality changes
			 */
			@JsonProperty("qualityLimitationResolutionChanges")
			public Integer qualityLimitationResolutionChanges;
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
			 * RTT measurement in seconds based on (most likely) SR, and RR belongs to the corresponded synchronization source
			 */
			@JsonProperty("roundTripTime")
			public Double roundTripTime;
			/**
			 * The sum of RTT measurements belongs to the corresponded synchronization source
			 */
			@JsonProperty("totalRoundTripTime")
			public Double totalRoundTripTime;
			/**
			 * The receiver reported fractional lost belongs to the corresponded synchronization source
			 */
			@JsonProperty("fractionLost")
			public Double fractionLost;
			/**
			 * The total number of RR reports received, which is the base of the remote inbound calculation on this source
			 */
			@JsonProperty("reportsReceived")
			public Integer reportsReceived;
			/**
			 * The total number of calculated RR measurements received on this source
			 */
			@JsonProperty("roundTripTimeMeasurements")
			public Integer roundTripTimeMeasurements;
			/**
			 * The total number of frames reported to be lost by the remote endpoit on the corresponded RTP stream
			 */
			@JsonProperty("framesDropped")
			public Integer framesDropped;
			/**
			 * The total number of partial frames reported to be lost by the remote endpoint on the corresponded RTP stream.
			 */
			@JsonProperty("partialFramesLost")
			public Integer partialFramesLost;
			/**
			 * The total number of full frames lost at the remote endpoint on the corresponded RTP stream.
			 */
			@JsonProperty("fullFramesList")
			public Integer fullFramesList;
			/**
			 * True if the corresponded media source is remote, false otherwise (or null depending on browser and version)
			 */
			@JsonProperty("relayedSource")
			public Boolean relayedSource;
			/**
			 * The width, in pixels, of the last frame originating from the media source
			 */
			@JsonProperty("width")
			public Integer width;
			/**
			 * The height, in pixels, of the last frame originating from the media source
			 */
			@JsonProperty("height")
			public Integer height;
			/**
			 * The bitDepth, in pixels, of the last frame originating from the media source
			 */
			@JsonProperty("bitDepth")
			public Integer bitDepth;
			/**
			 * The total number of frames originated from the media source
			 */
			@JsonProperty("frames")
			public Integer frames;
			/**
			 * Flag represents if the sender ended the media stream track or not.
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
			public Long clockRate;
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
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class IceLocalCandidate {
			/**
			 * Refers to the peer connection the local candidate belongs to
			 */
			@JsonProperty("peerConnectionId")
			public String peerConnectionId;
			/**
			 * The unique identifier of the local candidate
			 */
			@JsonProperty("id")
			public String id;
			/**
			 * The unique identifier of the transport the local candidate belongs to
			 */
			@JsonProperty("transportId")
			public String transportId;
			/**
			 * The address of the local endpoint (Ipv4, Ipv6, FQDN)
			 */
			@JsonProperty("address")
			public String address;
			/**
			 * The port number of the local endpoint the ICE uses
			 */
			@JsonProperty("port")
			public Integer port;
			/**
			 * The protocol for the ICE
			 */
			@JsonProperty("protocol")
			public String protocol;
			/**
			 * The type of the local candidate
			 */
			@JsonProperty("candidateType")
			public String candidateType;
			/**
			 * The priority of the local candidate
			 */
			@JsonProperty("priority")
			public Long priority;
			/**
			 * The url of the ICE server
			 */
			@JsonProperty("url")
			public String url;
			/**
			 * The relay protocol the local candidate uses
			 */
			@JsonProperty("relayProtocol")
			public String relayProtocol;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class IceRemoteCandidate {
			/**
			 * Refers to the peer connection the local candidate belongs to
			 */
			@JsonProperty("peerConnectionId")
			public String peerConnectionId;
			/**
			 * The unique identifier of the local candidate
			 */
			@JsonProperty("id")
			public String id;
			/**
			 * The unique identifier of the transport the local candidate belongs to
			 */
			@JsonProperty("transportId")
			public String transportId;
			/**
			 * The address of the local endpoint (Ipv4, Ipv6, FQDN)
			 */
			@JsonProperty("address")
			public String address;
			/**
			 * The port number of the local endpoint the ICE uses
			 */
			@JsonProperty("port")
			public Integer port;
			/**
			 * The protocol for the ICE
			 */
			@JsonProperty("protocol")
			public String protocol;
			/**
			 * The type of the local candidate
			 */
			@JsonProperty("candidateType")
			public String candidateType;
			/**
			 * The priority of the local candidate
			 */
			@JsonProperty("priority")
			public Long priority;
			/**
			 * The url of the ICE server
			 */
			@JsonProperty("url")
			public String url;
			/**
			 * The relay protocol the local candidate uses
			 */
			@JsonProperty("relayProtocol")
			public String relayProtocol;
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class DataChannel {
			/**
			 * Refers to the peer connection the local candidate belongs to
			 */
			@JsonProperty("peerConnectionId")
			public String peerConnectionId;
			/**
			 * Unique identifier of the data channel
			 */
			@JsonProperty("id")
			public String id;
			/**
			 * The label the data channel provided at the creation
			 */
			@JsonProperty("label")
			public String label;
			/**
			 * The address of the local endpoint (Ipv4, Ipv6, FQDN)
			 */
			@JsonProperty("address")
			public String address;
			/**
			 * The port number of the local endpoint the ICE uses
			 */
			@JsonProperty("port")
			public Integer port;
			/**
			 *  The protocol the data channel use to transfer data
			 */
			@JsonProperty("protocol")
			public String protocol;
			/**
			 * The unique identifier of the data channel
			 */
			@JsonProperty("dataChannelIdentifier")
			public Integer dataChannelIdentifier;
			/**
			 * The state of the data channel
			 */
			@JsonProperty("state")
			public String state;
			/**
			 * The total number of messages sent on this data channel. this is not equal to the number of packets sent, as messages are chunked to packets
			 */
			@JsonProperty("messagesSent")
			public Integer messagesSent;
			/**
			 * The amount of bytes sent on the corresponded data channel
			 */
			@JsonProperty("bytesSent")
			public Long bytesSent;
			/**
			 * The number of messages received on the corresponded data channel
			 */
			@JsonProperty("messagesReceived")
			public Integer messagesReceived;
			/**
			 * The amount of bytes received on the corresponded data channel
			 */
			@JsonProperty("bytesReceived")
			public Long bytesReceived;
		}
		/**
		 * If it is provided the server uses the given id to match clients in the same call. Must be a valid UUID.
		 */
		@JsonProperty("callId")
		public String callId;
		/**
		 * Unique id of the client providing samples. Must be a valid UUID
		 */
		@JsonProperty("clientId")
		public String clientId;
		/**
		 * The sequence number a source assigns to the sample. Every time the source make a sample at a client this number should be monothonically incremented.
		 */
		@JsonProperty("sampleSeq")
		public Integer sampleSeq;
		/**
		 * The WebRTC app configured room id the client joined for the call.
		 */
		@JsonProperty("roomId")
		public String roomId;
		/**
		 * The WebRTC app configured human readable user id the client is joined.
		 */
		@JsonProperty("userId")
		public String userId;
		/**
		 * WebRTC App provided information related to the engine the client uses.
		 */
		@JsonProperty("engine")
		public Engine engine;
		/**
		 * WebRTC App provided information related to the platform the client uses.
		 */
		@JsonProperty("platform")
		public Platform platform;
		/**
		 * WebRTC App provided information related to the browser the client uses.
		 */
		@JsonProperty("browser")
		public Browser browser;
		/**
		 * WebRTC App provided information related to the operation system the client uses.
		 */
		@JsonProperty("os")
		public OperationSystem os;
		/**
		 * The WebRTC app provided List of the media constraints the client has.
		 */
		@JsonProperty("mediaConstraints")
		public String[] mediaConstraints;
		/**
		 * The WebRTC app provided List of the media devices the client has.
		 */
		@JsonProperty("mediaDevices")
		public MediaDevice[] mediaDevices;
		/**
		 * The WebRTC app provided List of user media errors the client has.
		 */
		@JsonProperty("userMediaErrors")
		public String[] userMediaErrors;
		/**
		 * The WebRTC app provided custom stats payload
		 */
		@JsonProperty("extensionStats")
		public ExtensionStat[] extensionStats;
		/**
		 * The WebRTC app provided List of ICE server the client used.
		 */
		@JsonProperty("iceServers")
		public String[] iceServers;
		/**
		 * Compound object related to Peer Connection Transport Stats
		 */
		@JsonProperty("pcTransports")
		public PeerConnectionTransport[] pcTransports;
		/**
		 * WebRTC App provided information related to the operation system the client uses.
		 */
		@JsonProperty("mediaSources")
		public MediaSourceStat[] mediaSources;
		/**
		 * List of codec the client has
		 */
		@JsonProperty("codecs")
		public MediaCodecStats[] codecs;
		/**
		 * List of certificates the client provided
		 */
		@JsonProperty("certificates")
		public Certificate[] certificates;
		/**
		 * List of compound measurements related to inbound audio tracks
		 */
		@JsonProperty("inboundAudioTracks")
		public InboundAudioTrack[] inboundAudioTracks;
		/**
		 * List of compound measurements related to inbound video tracks
		 */
		@JsonProperty("inboundVideoTracks")
		public InboundVideoTrack[] inboundVideoTracks;
		/**
		 * List of compound measurements related to outbound audio tracks
		 */
		@JsonProperty("outboundAudioTracks")
		public OutboundAudioTrack[] outboundAudioTracks;
		/**
		 * List of compound measurements related to outbound video tracks
		 */
		@JsonProperty("outboundVideoTracks")
		public OutboundVideoTrack[] outboundVideoTracks;
		/**
		 * List of local ICE candidates
		 */
		@JsonProperty("iceLocalCandidates")
		public IceLocalCandidate[] iceLocalCandidates;
		/**
		 * List of remote ICE candidates
		 */
		@JsonProperty("iceRemoteCandidates")
		public IceRemoteCandidate[] iceRemoteCandidates;
		/**
		 * List of Data channels
		 */
		@JsonProperty("dataChannels")
		public DataChannel[] dataChannels;
		/**
		 * The timestamp the sample is created in GMT
		 */
		@JsonProperty("timestamp")
		public Long timestamp;
		/**
		 * The offset from GMT in hours
		 */
		@JsonProperty("timeZoneOffsetInHours")
		public Integer timeZoneOffsetInHours;
		/**
		 * Special marker for the samples
		 */
		@JsonProperty("marker")
		public String marker;
	}
	/**
	 * docs
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SfuSample {
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuTransport {
			/**
			 * Flag indicate to not generate report from this sample
			 */
			@JsonProperty("noReport")
			public Boolean noReport;
			/**
			 * The generated unique identifier of the transport
			 */
			@JsonProperty("transportId")
			public String transportId;
			/**
			 * Flag to indicate that the transport is used as an internal transport between SFU instances
			 */
			@JsonProperty("internal")
			public Boolean internal;
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
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuInboundRtpPad {
			/**
			 * Flag indicate to not generate report from this sample
			 */
			@JsonProperty("noReport")
			public Boolean noReport;
			/**
			 * The id of the transport the RTP Pad uses.
			 */
			@JsonProperty("transportId")
			public String transportId;
			/**
			 * The id of the media stream the RTP pad belongs to. This id is to group rtp pads (e.g.: simulcast) carrying payloads to the same media.
			 */
			@JsonProperty("streamId")
			public String streamId;
			/**
			 * The id of Sfu pad.
			 */
			@JsonProperty("padId")
			public String padId;
			/**
			 * The synchronization source id of the RTP stream
			 */
			@JsonProperty("ssrc")
			public Long ssrc;
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
			public Long clockRate;
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
			public Long targetBitrate;
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
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuOutboundRtpPad {
			/**
			 * Flag indicate to not generate report from this sample
			 */
			@JsonProperty("noReport")
			public Boolean noReport;
			/**
			 * The id of the transport the RTP stream uses.
			 */
			@JsonProperty("transportId")
			public String transportId;
			/**
			 * The id of the stream this outbound RTP pad sinks the media from
			 */
			@JsonProperty("streamId")
			public String streamId;
			/**
			 * The id of a group of RTP pad sinks the media stream out from the SFU.
			 */
			@JsonProperty("sinkId")
			public String sinkId;
			/**
			 * The id of Sfu pad.
			 */
			@JsonProperty("padId")
			public String padId;
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
			public Long clockRate;
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
			public Long targetBitrate;
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
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuSctpChannel {
			/**
			 * Flag indicate to not generate report from this sample
			 */
			@JsonProperty("noReport")
			public Boolean noReport;
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
			 * The id of the sctp stream
			 */
			@JsonProperty("channelId")
			public String channelId;
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
		}
		/**
		 * undefined
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuExtensionStats {
			/**
			 * The type of the extension stats the custom app provides
			 */
			@JsonProperty("type")
			public String type;
			/**
			 * The payload of the extension stats the custom app provides
			 */
			@JsonProperty("payload")
			public String payload;
		}
		/**
		 * Unique generated id for the sfu samples are originated from
		 */
		@JsonProperty("sfuId")
		public String sfuId;
		/**
		 * The timestamp the sample is created in GMT
		 */
		@JsonProperty("timestamp")
		public Long timestamp;
		/**
		 * The offset from GMT in hours
		 */
		@JsonProperty("timeZoneOffsetInHours")
		public Integer timeZoneOffsetInHours;
		/**
		 * Special marker for the samples
		 */
		@JsonProperty("marker")
		public String marker;
		/**
		 * The Sfu Transports obtained measurements
		 */
		@JsonProperty("transports")
		public SfuTransport[] transports;
		/**
		 * The Sfu Inbound Rtp Pad obtained measurements
		 */
		@JsonProperty("inboundRtpPads")
		public SfuInboundRtpPad[] inboundRtpPads;
		/**
		 * The Sfu Outbound Rtp Pad obtained measurements
		 */
		@JsonProperty("outboundRtpPads")
		public SfuOutboundRtpPad[] outboundRtpPads;
		/**
		 * The Sfu Outbound Rtp Pad obtained measurements
		 */
		@JsonProperty("sctpChannels")
		public SfuSctpChannel[] sctpChannels;
		/**
		 * The Sfu provided custom stats payload
		 */
		@JsonProperty("extensionStats")
		public SfuExtensionStats[] extensionStats;
	}
	/**
	 * Additional meta information about the carried payloads
	 */
	@JsonProperty("meta")
	public SamplesMeta meta;
	/**
	 * Additional control flags indicate various operation has to be performed
	 */
	@JsonProperty("controlFlags")
	public ControlFlags controlFlags;
	/**
	 * Samples taken from the client
	 */
	@JsonProperty("clientSamples")
	public ClientSample[] clientSamples;
	/**
	 * Samples taken from an Sfu
	 */
	@JsonProperty("sfuSamples")
	public SfuSample[] sfuSamples;
}