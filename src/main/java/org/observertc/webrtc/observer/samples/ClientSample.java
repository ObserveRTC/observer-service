package org.observertc.webrtc.observer.samples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
/** 
* undefined
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public  class ClientSample { 

	/**
	* The unique generated client id the report is generated from
	*/
	@JsonProperty("clientId")
	public String clientId;
	
	/**
	* The sequence number a source assigns to the sample. 
Every time the source make a sample at a client 
this number is monothonically incremented.
	*/
	@JsonProperty("sampleSeq")
	public int sampleSeq;
	
	/**
	* The WebRTC app configured room id the client was at the call.
If it is configured, then every sample carries this information.
	*/
	@JsonProperty("roomId")
	public String roomId;
	
	/**
	* The WebRTC app configured user id of the client.
If it is configured, then every sample carries this information.
	*/
	@JsonProperty("userId")
	public String userId;
	
	/**
	* The engine
	*/
	@JsonProperty("engine")
	public Engine engine;
	
	/**
	* The platform
	*/
	@JsonProperty("platform")
	public Platform platform;
	
	/**
	* Details of the browser the client has
	*/
	@JsonProperty("browser")
	public Browser browser;
	
	/**
	* Details about the operation system the client has
	*/
	@JsonProperty("os")
	public OperationSystem os;
	
	/**
	* List of the media constraints the client has

Only presented if any changes occurred in the client
	*/
	@JsonProperty("mediaConstraints")
	public String[] mediaConstraints;
	
	/**
	* List of the media devices the client has.
	*/
	@JsonProperty("mediaDevices")
	public String[] mediaDevices;
	
	/**
	* List of user media errors

Only presented if any changes occurred in the client
	*/
	@JsonProperty("userMediaErrors")
	public String[] userMediaErrors;
	
	/**
	* List of the extension stats added by the webrtc app
	*/
	@JsonProperty("extensionStats")
	public String[] extensionStats;
	
	/**
	* List of ICE server the client has

Only presented if any changes occurred in the client
	*/
	@JsonProperty("iceServers")
	public String[] iceServers;
	
	/**
	* List of the peer connection transport object.
	*/
	@JsonProperty("pcTransports")
	public PeerConnectionTransport[] pcTransports;
	
	/**
	* A list of media sources a client uses.
This attribute only updates if there is a change in the list of source.

Only presented if any changes occurred in the client
	*/
	@JsonProperty("mediaSources")
	public MediaSourceStat[] mediaSources;
	
	/**
	* List of codec the client has

Only presented if any changes occurred in the client
	*/
	@JsonProperty("codecs")
	public Codec[] codecs;
	
	/**
	* The certificates the client provided

Only presented if any changes occurred in the client
	*/
	@JsonProperty("certificates")
	public Certificate[] certificates;
	
	/**
	* The inbound audio track statistics
	*/
	@JsonProperty("inboundAudioTracks")
	public InboundAudioTrack[] inboundAudioTracks;
	
	/**
	* The inbound video track statistics
	*/
	@JsonProperty("inboundVideoTracks")
	public InboundVideoTrack[] inboundVideoTracks;
	
	/**
	* The outbound audio track statistics
	*/
	@JsonProperty("outboundAudioTracks")
	public OutboundAudioTrack[] outboundAudioTracks;
	
	/**
	* The outbound video track statistics
	*/
	@JsonProperty("outboundVideoTracks")
	public OutboundVideoTrack[] outboundVideoTracks;
	
	/**
	* Local ICE candidates

Only presented if any changes occurred in the client
	*/
	@JsonProperty("iceLocalCandidates")
	public ICELocalCandidate[] iceLocalCandidates;
	
	/**
	* Remote ICE candidates

Only presented if any changes occurred in the client
	*/
	@JsonProperty("iceRemoteCandidates")
	public ICERemoteCandidate[] iceRemoteCandidates;
	
	/**
	* Data channels
	*/
	@JsonProperty("dataChannels")
	public DataChannel[] dataChannels;
	
	/**
	* The timestamp when the sample is created
	*/
	@JsonProperty("timestamp")
	public long timestamp;
	
	/** 
	* Engine
	*/
	
	public static class Engine { 
	
			/**
		* The name of
		*/
		@JsonProperty("name")
		public String name;
		
			/**
		* The version of
		*/
		@JsonProperty("version")
		public String version;
		
	}
	/** 
	* Platform infromation
	*/
	
	public static class Platform { 
	
			/**
		* The type of the platform
		*/
		@JsonProperty("type")
		public String type;
		
			/**
		* The vendor of the platform
		*/
		@JsonProperty("vendor")
		public String vendor;
		
			/**
		* The model of the platform
		*/
		@JsonProperty("model")
		public String model;
		
	}
	/** 
	* Browser infromation
	*/
	
	public static class Browser { 
	
			/**
		* The name of the browser
		*/
		@JsonProperty("name")
		public String name;
		
			/**
		* The version of
		*/
		@JsonProperty("version")
		public String version;
		
	}
	/** 
	* undefined
	*/
	
	public static class OperationSystem { 
	
			/**
		* Name of the operation system.
		*/
		@JsonProperty("name")
		public String name;
		
			/**
		* The version number of the operation system
		*/
		@JsonProperty("version")
		public String version;
		
			/**
		* The version name of the operation system
		*/
		@JsonProperty("versionName")
		public String versionName;
		
	}
	/** 
	* A compounded object built up by using 
 * RTCTransportStats
 * RTCSctpTransportStats
 * RTCIceCandidateStats
 * RTCIceCandidatePairStats
 * RTCCertificateStats

from https://www.w3.org/TR/webrtc-stats/
	*/
	
	public static class PeerConnectionTransport { 
	
			/**
		* The unique generated id for the peer connection
		*/
		@JsonProperty("peerConnectionId")
		public String peerConnectionId;
		
			/**
		* The webrtc app provided label to the peer connection
		*/
		@JsonProperty("label")
		public String label;
		
			/**
		* Represents the number of unique RTCDataChannels that have entered the "open" state during their lifetime.
		*/
		@JsonProperty("dataChannelsOpened")
		public int dataChannelsOpened;
		
			/**
		* Represents the number of unique RTCDataChannels that had the "open" state, but now they are "closed"
		*/
		@JsonProperty("dataChannelsClosed")
		public int dataChannelsClosed;
		
			/**
		* Represents the number of unique RTCDataChannels successfully requested from RTCPeerConnection.
		*/
		@JsonProperty("dataChannelsRequested")
		public int dataChannelsRequested;
		
			/**
		* Represents the number of unique RTCDataChannels signaled in a ondatachannel event on the RTCPeerConnection.
		*/
		@JsonProperty("dataChannelsAccepted")
		public int dataChannelsAccepted;
		
			/**
		* Represents the total number of packets sent over this transport.
		*/
		@JsonProperty("packetsSent")
		public int packetsSent;
		
			/**
		* Represents the total number of packets received on this transport.
		*/
		@JsonProperty("packetsReceived")
		public int packetsReceived;
		
			/**
		* Represents the total number of payload bytes sent on this RTCIceTransport, i.e., not including headers, padding or ICE connectivity checks.
		*/
		@JsonProperty("bytesSent")
		public long bytesSent;
		
			/**
		* Represents the total number of payload bytes received on this RTCIceTransport, i.e., not including headers, padding or ICE connectivity checks.
		*/
		@JsonProperty("bytesReceived")
		public long bytesReceived;
		
			/**
		* Set to the current value of the role attribute of the underlying RTCDtlsTransport.iceTransport.
		*/
		@JsonProperty("iceRole")
		public String iceRole;
		
			/**
		* Set to the current value of the local username fragment used in message validation procedures
		*/
		@JsonProperty("iceLocalUsernameFragment")
		public String iceLocalUsernameFragment;
		
			/**
		* Set to the current value of the state attribute of the underlying RTCDtlsTransport.
		*/
		@JsonProperty("dtlsState")
		public String dtlsState;
		
			/**
		* Set to the current value of the state attribute of the underlying RTCIceTransport.
		*/
		@JsonProperty("iceState")
		public String iceState;
		
			/**
		* It is a unique identifier that is associated to the object that was inspected to produce the RTCIceCandidatePairStats associated with this transport.
		*/
		@JsonProperty("selectedCandidatePairId")
		public String selectedCandidatePairId;
		
			/**
		* For components where DTLS is negotiated, give local certificate.
		*/
		@JsonProperty("localCertificateId")
		public String localCertificateId;
		
			/**
		* For components where DTLS is negotiated, give remote certificate.
		*/
		@JsonProperty("remoteCertificateId")
		public String remoteCertificateId;
		
			/**
		* The tls version of the peer connection when the DTLS negotiation is complete
		*/
		@JsonProperty("tlsVersion")
		public String tlsVersion;
		
			/**
		* Descriptive name of the cipher suite used for the DTLS transport, as defined in the "Description" column of the IANA cipher suite registry
		*/
		@JsonProperty("dtlsCipher")
		public String dtlsCipher;
		
			/**
		* Descriptive name of the protection profile used for the SRTP transport, as defined in the "Profile" column of the IANA DTLS-SRTP protection profile registry
		*/
		@JsonProperty("srtpCipher")
		public String srtpCipher;
		
			/**
		* Descriptive name of the group used for the encryption, as defined in the "Description" column of the IANA TLS Supported Groups registry
		*/
		@JsonProperty("tlsGroup")
		public String tlsGroup;
		
			/**
		* The number of times that the selected candidate pair of this transport has changed. Going from not having a selected candidate pair to having a selected candidate pair, or the other way around, also increases this counter. It is initially zero and becomes one when an initial candidate pair is selected.
		*/
		@JsonProperty("selectedCandidatePairChanges")
		public int selectedCandidatePairChanges;
		
			/**
		* The latest smoothed round-trip time value, corresponding to spinfo_srtt defined in [RFC6458] but converted to seconds. If there has been no round-trip time measurements yet, this value is undefined.
		*/
		@JsonProperty("sctpSmoothedRoundTripTime")
		public double sctpSmoothedRoundTripTime;
		
			/**
		* The latest congestion window, corresponding to spinfo_cwnd defined in [RFC6458].
		*/
		@JsonProperty("sctpCongestionWindow")
		public double sctpCongestionWindow;
		
			/**
		* The latest receiver window, corresponding to sstat_rwnd defined in [RFC6458].
		*/
		@JsonProperty("sctpReceiverWindow")
		public double sctpReceiverWindow;
		
			/**
		* The latest maximum transmission unit, corresponding to spinfo_mtu defined in [RFC6458].
		*/
		@JsonProperty("sctpMtu")
		public int sctpMtu;
		
			/**
		* The number of unacknowledged DATA chunks, corresponding to sstat_unackdata defined in [RFC6458].
		*/
		@JsonProperty("sctpUnackData")
		public int sctpUnackData;
		
			/**
		* The state of ICE Candidate used for the peer connection
		*/
		@JsonProperty("candidatePairState")
		public String candidatePairState;
		
			/**
		* The total number of packets sent on the peer connection using the selected candidate pair.
		*/
		@JsonProperty("candidatePairPacketsSent")
		public int candidatePairPacketsSent;
		
			/**
		* The total number of packets received on the peer connection using the selected candidate pair.
		*/
		@JsonProperty("candidatePairPacketsReceived")
		public int candidatePairPacketsReceived;
		
			/**
		* The total number of payload bytes sent on the peer connection using the selected candidate pair.
		*/
		@JsonProperty("candidatePairBytesSent")
		public int candidatePairBytesSent;
		
			/**
		* The total number of payload bytes received on the peer connection using the selected candidate pair.
		*/
		@JsonProperty("candidatePairBytesReceived")
		public int candidatePairBytesReceived;
		
			/**
		* The timestamp of the last sent packet on the peer connection using the selected ICE Candidate pair.
		*/
		@JsonProperty("candidatePairLastPacketSentTimestamp")
		public int candidatePairLastPacketSentTimestamp;
		
			/**
		* The timestamp of the last received packet on the peer connection using the selected ICE Candidate pair.
		*/
		@JsonProperty("candidatePairLastPacketReceivedTimestamp")
		public int candidatePairLastPacketReceivedTimestamp;
		
			/**
		* The timestamp of the first request sent on the peer connection to select a candidate pair
		*/
		@JsonProperty("candidatePairFirstRequestTimestamp")
		public int candidatePairFirstRequestTimestamp;
		
			/**
		* The timestamp of the last request sent on the peer connection to select a candidate pair
		*/
		@JsonProperty("candidatePairLastRequestTimestamp")
		public int candidatePairLastRequestTimestamp;
		
			/**
		* The timestamp of the last response received on tthe peer connection using the selected candidate pair
		*/
		@JsonProperty("candidatePairLastResponseTimestamp")
		public int candidatePairLastResponseTimestamp;
		
			/**
		* the sum of all round trip time measurements in seconds reported by STUN packet using the selected candidate pair on the peer connection
		*/
		@JsonProperty("candidatePairTotalRoundTripTime")
		public int candidatePairTotalRoundTripTime;
		
			/**
		* The latest round trip time calculated from STUN connectivity checks
		*/
		@JsonProperty("candidatePairCurrentRoundTripTime")
		public int candidatePairCurrentRoundTripTime;
		
			/**
		* Reported by the underlying congestion control algorithm on this peer connection using the selected ICE candidate pair
		*/
		@JsonProperty("candidatePairAvailableOutgoingBitrate")
		public int candidatePairAvailableOutgoingBitrate;
		
			/**
		* Reported by the underlying congestion control algorithm on this peer connection using the selected ICE candidate pair
		*/
		@JsonProperty("candidatePairAvailableIncomingBitrate")
		public int candidatePairAvailableIncomingBitrate;
		
			/**
		* The total number of circuit breaker condition happened on the peer connection using the selected candidate pair
		*/
		@JsonProperty("candidatePairCircuitBreakerTriggerCount")
		public int candidatePairCircuitBreakerTriggerCount;
		
			/**
		* The total number of requests received for connectivity check on the peer connection using the selected ice candidate pair
		*/
		@JsonProperty("candidatePairRequestsReceived")
		public int candidatePairRequestsReceived;
		
			/**
		* The total number of requests sent for connectivity check on the peer connection using the selected ice candidate pair
		*/
		@JsonProperty("candidatePairRequestsSent")
		public int candidatePairRequestsSent;
		
			/**
		* The total number of responses received for connectivity check on the peer connection using the selected ice candidate pair
		*/
		@JsonProperty("candidatePairResponsesReceived")
		public int candidatePairResponsesReceived;
		
			/**
		* The total number of responses sent for connectivity check on the peer connection using the selected ice candidate pair
		*/
		@JsonProperty("candidatePairResponsesSent")
		public int candidatePairResponsesSent;
		
			/**
		* The total number of retransmission received on the peer connection using the selected ice candidate pair
		*/
		@JsonProperty("candidatePairRetransmissionReceived")
		public int candidatePairRetransmissionReceived;
		
			/**
		* The total number of retransmission sent on the peer connection using the selected ice candidate pair
		*/
		@JsonProperty("candidatePairRetransmissionSent")
		public int candidatePairRetransmissionSent;
		
			/**
		* The total number of consent requests sent on the peer connection using the selected ice candidate pair
		*/
		@JsonProperty("candidatePairConsentRequestsSent")
		public int candidatePairConsentRequestsSent;
		
			/**
		* The total number of consent expired on the peer connection using the selected ice candidate pair
		*/
		@JsonProperty("candidatePairConsentExpiredTimestamp")
		public long candidatePairConsentExpiredTimestamp;
		
			/**
		* The total number packet discarded before sending on the peer connection using the selected candidate pair
		*/
		@JsonProperty("candidatePairPacketsDiscardedOnSend")
		public int candidatePairPacketsDiscardedOnSend;
		
			/**
		* The total number bytes discarded before sending on the peer connection using the selected candidate pair
		*/
		@JsonProperty("candidatePairBytesDiscardedOnSend")
		public long candidatePairBytesDiscardedOnSend;
		
			/**
		* The total number bytes sent as a request on the peer connection using the selected candidate pair
		*/
		@JsonProperty("candidatePairRequestBytesSent")
		public long candidatePairRequestBytesSent;
		
			/**
		* The total number bytes sent in consent packets on the peer connection using the selected candidate pair
		*/
		@JsonProperty("candidatePairConsentRequestBytesSent")
		public long candidatePairConsentRequestBytesSent;
		
			/**
		* The total number bytes sent as response packets on the peer connection using the selected candidate pair
		*/
		@JsonProperty("candidatePairResponseBytesSent")
		public int candidatePairResponseBytesSent;
		
			/**
		* The local address of the ICE candidate at the local endpoint (IPv4, IPv6, FQDN)
		*/
		@JsonProperty("localAddress")
		public String localAddress;
		
			/**
		* The port number used by the local ICE candidate for connectivity

Possible values: UDP, TCP
		*/
		@JsonProperty("localPort")
		public int localPort;
		
			/**
		* The protocol used by the local ICE candidate for connectivity
		*/
		@JsonProperty("localProtocol")
		public String localProtocol;
		
			/**
		* The type of the candidate used for communication.

Possible values: host, srflx, prflx, relay
		*/
		@JsonProperty("localCandidateType")
		public String localCandidateType;
		
			/**
		* It is the protocol used by the endpoint to communicate with the TURN server.

Possible values: UDP, TCP, TLS
		*/
		@JsonProperty("localRelayProtocol")
		public String localRelayProtocol;
		
			/**
		* The local address of the ICE candidate at the remote endpoint (IPv4, IPv6, FQDN)
		*/
		@JsonProperty("remoteAddress")
		public String remoteAddress;
		
			/**
		* The port number used by the remote ICE candidate for connectivity

Possible values: UDP, TCP
		*/
		@JsonProperty("remotePort")
		public int remotePort;
		
			/**
		* The protocol used by the remote ICE candidate for connectivity
		*/
		@JsonProperty("remoteProtocol")
		public String remoteProtocol;
		
			/**
		* The type of the remote candidate used for communication.

Possible values: host, srflx, prflx, relay
		*/
		@JsonProperty("remoteCandidateType")
		public String remoteCandidateType;
		
			/**
		* It is the protocol used by the remote endpoint to communicate with the TURN server.

Possible values: UDP, TCP, TLS
		*/
		@JsonProperty("remoteRelayProtocol")
		public String remoteRelayProtocol;
		
			/**
		* Client calculated metric.
The total number of media packets sent by all tracks using the peer connection.

Note: Take care of the fact that tracks are attached and detached significantly changing the value of this field
		*/
		@JsonProperty("sentMediaPackets")
		public int sentMediaPackets;
		
			/**
		* Client calculated metric.
The total number of media packets received by all tracks using the peer connection.

Note: Take care of the fact that tracks are attached and detached significantly changing the value of this field
		*/
		@JsonProperty("receivedMediaPackets")
		public int receivedMediaPackets;
		
			/**
		* Client calculated metric.
The total number of media packets lost by all tracks using the peer connection.

Note: Take care of the fact that tracks are attached and detached significantly changing the value of this field
		*/
		@JsonProperty("lostMediaPackets")
		public int lostMediaPackets;
		
			/**
		* Client calculated metric.
A smoothed average value calculated by averaging all of the video tracks sent on the peer connection
		*/
		@JsonProperty("videoRttAvg")
		public int videoRttAvg;
		
			/**
		* Client calculated metric.
A smoothed average value calculated by averaging all of the audio tracks sent on the peer connection
		*/
		@JsonProperty("audioRttAvg")
		public int audioRttAvg;
		
	}
	/** 
	* Represents the WebRTC Stats defined [RTCMediaSourceStats](https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats)

NOTE: This name is postfixed with "stat" in order to avoid collision of the MediaSource name part of the standard library and picked up by the schema transpiler
	*/
	
	public static class MediaSourceStat { 
	
			/**
		* The unique generated identifier the corresponded track has
		*/
		@JsonProperty("trackIdentifier")
		public String trackIdentifier;
		
			/**
		* The type of the media the Mediasource produces.

Possible values are: "audio", "video"
		*/
		@JsonProperty("kind")
		public String kind;
		
			/**
		* Flag indicating if the media source is relayed or not, meaning the local endpoint is not the actual source of the media, but a proxy for that media.
		*/
		@JsonProperty("relayedSource")
		public boolean relayedSource;
		
			/**
		* the audio level of the media source.
		*/
		@JsonProperty("audioLevel")
		public double audioLevel;
		
			/**
		* The audio energy of the media source

For calculation see https://www.w3.org/TR/webrtc-stats/#dom-rtcaudiosourcestats-totalaudioenergy
		*/
		@JsonProperty("totalAudioEnergy")
		public double totalAudioEnergy;
		
			/**
		* The duration of the audio type media source
		*/
		@JsonProperty("totalSamplesDuration")
		public double totalSamplesDuration;
		
			/**
		* if echo cancellation is applied on the media source, then 
this number represents the loss calculation defined in https://www.itu.int/rec/T-REC-G.168-201504-I/en
		*/
		@JsonProperty("echoReturnLoss")
		public double echoReturnLoss;
		
			/**
		* similar to the echo return loss calculation
		*/
		@JsonProperty("echoReturnLossEnhancement")
		public double echoReturnLossEnhancement;
		
			/**
		* The width, in pixels, of the last frame originating from the media source
		*/
		@JsonProperty("width")
		public int width;
		
			/**
		* The height, in pixels, of the last frame originating from the media source
		*/
		@JsonProperty("height")
		public int height;
		
			/**
		* The bit depth per pixels, of the last frame originating from the media source
		*/
		@JsonProperty("bitDepth")
		public int bitDepth;
		
			/**
		* The total number of frames originated from the media source
		*/
		@JsonProperty("frames")
		public int frames;
		
			/**
		* The number of frames origianted from the media source in the last second
		*/
		@JsonProperty("framesPerSecond")
		public double framesPerSecond;
		
	}
	/** 
	* The Media Codec the client uses to encode / decode certain media

Fields related to [RTCCodecStats](https://www.w3.org/TR/webrtc-stats/#dom-rtccodecstats)
	*/
	
	public static class Codec { 
	
			/**
		* Payload type used in RTP encoding / decoding process.
		*/
		@JsonProperty("payloadType")
		public int payloadType;
		
			/**
		* Either "encode", or "decode" depending on the role the codec plays in the client
		*/
		@JsonProperty("codecType")
		public String codecType;
		
			/**
		* the unique identifier for the peer connection transport
		*/
		@JsonProperty("transportId")
		public String transportId;
		
			/**
		* The MIME type of the media. eg.: audio/opus
		*/
		@JsonProperty("mimeType")
		public String mimeType;
		
			/**
		* the clock rate used in RTP transport to generate the timestamp for the carried frames
		*/
		@JsonProperty("clockRate")
		public long clockRate;
		
			/**
		* Audio Only. Represnts the number of chanels an audio media source have. Only interesting if stereo is presented
		*/
		@JsonProperty("channels")
		public int channels;
		
			/**
		* The SDP line determines the codec
		*/
		@JsonProperty("sdpFmtpLine")
		public String sdpFmtpLine;
		
			/**
		* The peer connection id the codec is related to
		*/
		@JsonProperty("peerConnectionId")
		public String peerConnectionId;
		
	}
	/** 
	* Information about a certificate used by the ICE pair on peer connection
	*/
	
	public static class Certificate { 
	
			/**
		* The fingerprint of the certificate
		*/
		@JsonProperty("fingerprint")
		public String fingerprint;
		
			/**
		* The hash function used to generate the fingerprint
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
	* A combination of InboundRTPStat, RemoteInboundRTPStat, Receiver, and Codec webrtc stat standard exposed object at the client specific for audio tracks
	*/
	
	public static class InboundAudioTrack { 
	
			/**
		* The SSRC identifier of the corresponded RTP stream.
		*/
		@JsonProperty("ssrc")
		public long ssrc;
		
			/**
		* The total number of packets received on the corresponded RTP stream,
		*/
		@JsonProperty("packetsReceived")
		public int packetsReceived;
		
			/**
		* The total number of packets lost on the corresponded RTP stream
		*/
		@JsonProperty("packetsLost")
		public int packetsLost;
		
			/**
		* The last RR reported jitter on the corresponded RTP stream
		*/
		@JsonProperty("jitter")
		public double jitter;
		
			/**
		* The total number of discarded packets on the corresponded RTP stream.
		*/
		@JsonProperty("packetsDiscarded")
		public int packetsDiscarded;
		
			/**
		* The total number of packets repaired by either retransmission or FEC on the corresponded RTP stream.
		*/
		@JsonProperty("packetsRepaired")
		public int packetsRepaired;
		
			/**
		* The number of packets lost in burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstPacketsLost")
		public int burstPacketsLost;
		
			/**
		* The total number of packets discarded during a burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstPacketsDiscarded")
		public int burstPacketsDiscarded;
		
			/**
		* The total number of burst lost happened on the coerresponded RTP stream
		*/
		@JsonProperty("burstLossCount")
		public int burstLossCount;
		
			/**
		* The number of burst discards happened on the corresponded RTP stream.
		*/
		@JsonProperty("burstDiscardCount")
		public int burstDiscardCount;
		
			/**
		* The loss rate during burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstLossRate")
		public double burstLossRate;
		
			/**
		* The discard rate during burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstDiscardRate")
		public int burstDiscardRate;
		
			/**
		* The loss rate during a gap period on the corresponded RTP stream.
		*/
		@JsonProperty("gapLossRate")
		public double gapLossRate;
		
			/**
		* The discard rate during a gap period on the corresponded RTP stream
		*/
		@JsonProperty("gapDiscardRate")
		public double gapDiscardRate;
		
			/**
		* The RTP header V flag indicate of the activity of the media source by the media codec if the RTP transport ships it through
		*/
		@JsonProperty("voiceActivityFlag")
		public boolean voiceActivityFlag;
		
			/**
		* The RTP timestamp of the last received packet on the corresponded RTP stream
		*/
		@JsonProperty("lastPacketReceivedTimestamp")
		public boolean lastPacketReceivedTimestamp;
		
			/**
		* The RTCP average interval of sending compound RTCP reports
		*/
		@JsonProperty("averageRtcpInterval")
		public double averageRtcpInterval;
		
			/**
		* The total amount of header bytes received on the corresponded RTP stream.
		*/
		@JsonProperty("headerBytesReceived")
		public long headerBytesReceived;
		
			/**
		* The total number of FEC packets received on the corresponded RTP stream.
		*/
		@JsonProperty("fecPacketsReceived")
		public int fecPacketsReceived;
		
			/**
		* The total number of FEC packets discafrded on the corresponded RTP stream.
		*/
		@JsonProperty("fecPacketsDiscarded")
		public int fecPacketsDiscarded;
		
			/**
		* The total amount of payload bytes received on the corresponded RTP stream
		*/
		@JsonProperty("bytesReceived")
		public long bytesReceived;
		
			/**
		* The total number of packets failed to be decrypted on the corresponded RTP stream
		*/
		@JsonProperty("packetsFailedDecryption")
		public int packetsFailedDecryption;
		
			/**
		* The total number of duplicated packets appeared on the corresponded RTP stream.
		*/
		@JsonProperty("packetsDuplicated")
		public int packetsDuplicated;
		
			/**
		* The ratio of the DSCP packets on the corresponded RTP straem
		*/
		@JsonProperty("perDscpPacketsReceived")
		public double perDscpPacketsReceived;
		
			/**
		* The total number of negative acknowledgement received on the corresponded RTP stream
		*/
		@JsonProperty("nackCount")
		public int nackCount;
		
			/**
		* The total processing delay of the RTP packets from the moment they received until the moment the jitter buffer emits them on the corresponded RTP strema.
		*/
		@JsonProperty("totalProcessingDelay")
		public double totalProcessingDelay;
		
			/**
		* The estimated timestamp of the jitterbuffer emits the RTP packets on the corresponded RTP stream.
		*/
		@JsonProperty("estimatedPlayoutTimestamp")
		public double estimatedPlayoutTimestamp;
		
			/**
		* The total delay encountered by the jitter buffer for the RTP stream to allevaite the effect of jitter on the transport.
		*/
		@JsonProperty("jitterBufferDelay")
		public double jitterBufferDelay;
		
			/**
		* The total number of emits happened for the corresponded RTP stream
		*/
		@JsonProperty("jitterBufferEmittedCount")
		public int jitterBufferEmittedCount;
		
			/**
		* The total number of audio samples received on the corresponded RTP stream
		*/
		@JsonProperty("totalSamplesReceived")
		public int totalSamplesReceived;
		
			/**
		* The total number of samples decoded on the corresponded RTP stream
		*/
		@JsonProperty("totalSamplesDecoded")
		public int totalSamplesDecoded;
		
			/**
		* The total number of samples decoded with SILK on the corresponded RTP stream
		*/
		@JsonProperty("samplesDecodedWithSilk")
		public int samplesDecodedWithSilk;
		
			/**
		* The total number of samples decodedd with CELT on the corresponded RTP stream
		*/
		@JsonProperty("samplesDecodedWithCelt")
		public int samplesDecodedWithCelt;
		
			/**
		* The total number of samples decoded by the media decoder from the corresponded RTP stream
		*/
		@JsonProperty("concealedSamples")
		public int concealedSamples;
		
			/**
		* The total number of samples concealed from the corresponded RTP stream
		*/
		@JsonProperty("silentConcealedSamples")
		public int silentConcealedSamples;
		
			/**
		* The total number of concealed event emitted to the media codec by the corresponded jitterbuffer
		*/
		@JsonProperty("concealmentEvents")
		public int concealmentEvents;
		
			/**
		* The total number of samples inserted to decelarete the audio playout (happens when the jitterbuffer detects a shrinking buffer and need to increase the jitter buffer delay)
		*/
		@JsonProperty("insertedSamplesForDeceleration")
		public int insertedSamplesForDeceleration;
		
			/**
		* The total number of samples inserted to accelerate the audio playout (happens when the jitterbuffer detects a growing buffer and need to shrink the jitter buffer delay)
		*/
		@JsonProperty("removedSamplesForAcceleration")
		public int removedSamplesForAcceleration;
		
			/**
		* The level of audio played out from the corresponded RTP stream
		*/
		@JsonProperty("audioLevel")
		public double audioLevel;
		
			/**
		* the sum of level of energy of the microphone of the audio of the remote media source
		*/
		@JsonProperty("totalAudioEnergy")
		public double totalAudioEnergy;
		
			/**
		* The total duration of the effective samples of the audio source
		*/
		@JsonProperty("totalSamplesDuration")
		public double totalSamplesDuration;
		
			/**
		* The library implements the decoder for the media source
		*/
		@JsonProperty("decoderImplementation")
		public String decoderImplementation;
		
			/**
		* The total number of packets sent by the remote endpoint on the corresponded RTP stream
		*/
		@JsonProperty("packetsSent")
		public int packetsSent;
		
			/**
		* The total amount of bytes sent by the remote endpoint on the corresponded RTP stream
		*/
		@JsonProperty("bytesSent")
		public long bytesSent;
		
			/**
		* The remote timestamp of the RTCP packets reported in the SR
		*/
		@JsonProperty("remoteTimestamp")
		public double remoteTimestamp;
		
			/**
		* The total number of SR reports sent by the remote endpoint on the corresponded RTP stream
		*/
		@JsonProperty("reportsSent")
		public int reportsSent;
		
			/**
		* Flag indicate if the MediaTrack has been eded or not
		*/
		@JsonProperty("ended")
		public boolean ended;
		
			/**
		* The type of the payload the RTP stream carries
		*/
		@JsonProperty("payloadType")
		public int payloadType;
		
			/**
		* The type of the codec role inthe endpoint.

Possible values are: "audio", and "video"
		*/
		@JsonProperty("codecType")
		public String codecType;
		
			/**
		* The MIME type of the media codec
		*/
		@JsonProperty("mimeType")
		public String mimeType;
		
			/**
		* the clock rate of the media source generates samples or frames
		*/
		@JsonProperty("clockRate")
		public long clockRate;
		
			/**
		* The number of channels the media source has.
		*/
		@JsonProperty("channels")
		public int channels;
		
			/**
		* The corresponded SDP line in SDP negotiation
		*/
		@JsonProperty("sdpFmtpLine")
		public String sdpFmtpLine;
		
			/**
		* The identifier of the MediaTrack the client plays the audio out
		*/
		@JsonProperty("trackId")
		public String trackId;
		
			/**
		* The unique generated identifier of the peer connection the inbound audio track belongs to
		*/
		@JsonProperty("peerConnectionId")
		public String peerConnectionId;
		
	}
	/** 
	* A compound stat object used by the client giving information about a video track 
used by the client as inbound
	*/
	
	public static class InboundVideoTrack { 
	
			/**
		* The SSRC identifier of the corresponded RTP stream.
		*/
		@JsonProperty("ssrc")
		public long ssrc;
		
			/**
		* The total number of packets received on the corresponded RTP stream,
		*/
		@JsonProperty("packetsReceived")
		public int packetsReceived;
		
			/**
		* The total number of packets lost on the corresponded RTP stream
		*/
		@JsonProperty("packetsLost")
		public int packetsLost;
		
			/**
		* The last RR reported jitter on the corresponded RTP stream
		*/
		@JsonProperty("jitter")
		public double jitter;
		
			/**
		* The total number of discarded packets on the corresponded RTP stream.
		*/
		@JsonProperty("packetsDiscarded")
		public int packetsDiscarded;
		
			/**
		* The total number of packets repaired by either retransmission or FEC on the corresponded RTP stream.
		*/
		@JsonProperty("packetsRepaired")
		public int packetsRepaired;
		
			/**
		* The number of packets lost in burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstPacketsLost")
		public int burstPacketsLost;
		
			/**
		* The total number of packets discarded during a burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstPacketsDiscarded")
		public int burstPacketsDiscarded;
		
			/**
		* The total number of burst lost happened on the coerresponded RTP stream
		*/
		@JsonProperty("burstLossCount")
		public int burstLossCount;
		
			/**
		* The number of burst discards happened on the corresponded RTP stream.
		*/
		@JsonProperty("burstDiscardCount")
		public int burstDiscardCount;
		
			/**
		* The loss rate during burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstLossRate")
		public double burstLossRate;
		
			/**
		* The discard rate during burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstDiscardRate")
		public int burstDiscardRate;
		
			/**
		* The loss rate during a gap period on the corresponded RTP stream.
		*/
		@JsonProperty("gapLossRate")
		public double gapLossRate;
		
			/**
		* The discard rate during a gap period on the corresponded RTP stream
		*/
		@JsonProperty("gapDiscardRate")
		public double gapDiscardRate;
		
			/**
		* The total number of frames dropped on the corresponded RTP stream
		*/
		@JsonProperty("framesDropped")
		public double framesDropped;
		
			/**
		* The total number of frames partially lost on the corresponded RTP stream
		*/
		@JsonProperty("partialFramesLost")
		public double partialFramesLost;
		
			/**
		* The total number of frames fully lost on the corresponded RTP stream
		*/
		@JsonProperty("fullFramesLost")
		public int fullFramesLost;
		
			/**
		* The total number of frames decoded on the corresponded RTP stream
		*/
		@JsonProperty("framesDecoded")
		public int framesDecoded;
		
			/**
		* The total number of keyframes decoded on the corresponded RTP stream
		*/
		@JsonProperty("keyFramesDecoded")
		public int keyFramesDecoded;
		
			/**
		* The width of the frame of the video sent by the remote source on the corresponded RTP stream
		*/
		@JsonProperty("frameWidth")
		public int frameWidth;
		
			/**
		* The height of the frame of the video sent by the remote source on the corresponded RTP stream
		*/
		@JsonProperty("frameHeight")
		public int frameHeight;
		
			/**
		* The bit depth in pixels of the frame of the video sent by the remote source on the corresponded RTP stream
		*/
		@JsonProperty("frameBitDepth")
		public int frameBitDepth;
		
			/**
		* The frame per seconds of the video sent by the remote source on the corresponded RTP stream
		*/
		@JsonProperty("framesPerSecond")
		public double framesPerSecond;
		
			/**
		* The QP sum (only interested in VP8,9) of the frame of the video sent by the remote source on the corresponded RTP stream
		*/
		@JsonProperty("qpSum")
		public long qpSum;
		
			/**
		* The total tiem spent on decoding video on the corresponded RTP stream
		*/
		@JsonProperty("totalDecodeTime")
		public long totalDecodeTime;
		
			/**
		* The total interframe delay
		*/
		@JsonProperty("totalInterFrameDelay")
		public long totalInterFrameDelay;
		
			/**
		* The squere total of the interframe delay (together with teh interframe delay you can calculate the variance)
		*/
		@JsonProperty("totalSquaredInterFrameDelay")
		public long totalSquaredInterFrameDelay;
		
			/**
		* The RTP timestamp of the last received packet on the corresponded RTP stream
		*/
		@JsonProperty("lastPacketReceivedTimestamp")
		public boolean lastPacketReceivedTimestamp;
		
			/**
		* The RTCP average interval of sending compound RTCP reports
		*/
		@JsonProperty("averageRtcpInterval")
		public double averageRtcpInterval;
		
			/**
		* The total amount of header bytes received on the corresponded RTP stream.
		*/
		@JsonProperty("headerBytesReceived")
		public long headerBytesReceived;
		
			/**
		* The total number of FEC packets received on the corresponded RTP stream.
		*/
		@JsonProperty("fecPacketsReceived")
		public int fecPacketsReceived;
		
			/**
		* The total number of FEC packets discafrded on the corresponded RTP stream.
		*/
		@JsonProperty("fecPacketsDiscarded")
		public int fecPacketsDiscarded;
		
			/**
		* The total amount of payload bytes received on the corresponded RTP stream
		*/
		@JsonProperty("bytesReceived")
		public long bytesReceived;
		
			/**
		* The total number of packets failed to be decrypted on the corresponded RTP stream
		*/
		@JsonProperty("packetsFailedDecryption")
		public int packetsFailedDecryption;
		
			/**
		* The total number of duplicated packets appeared on the corresponded RTP stream.
		*/
		@JsonProperty("packetsDuplicated")
		public int packetsDuplicated;
		
			/**
		* The ratio of the DSCP packets on the corresponded RTP straem
		*/
		@JsonProperty("perDscpPacketsReceived")
		public double perDscpPacketsReceived;
		
			/**
		* The total number FIR packets sent from this endpoint to the source on the corresponded RTP stream
		*/
		@JsonProperty("firCount")
		public int firCount;
		
			/**
		* The total number of Picture Loss Indication sent on the corresponded RTP stream
		*/
		@JsonProperty("pliCount")
		public int pliCount;
		
			/**
		* The total number of negative acknowledgement received on the corresponded RTP stream
		*/
		@JsonProperty("nackCount")
		public int nackCount;
		
			/**
		* The total number of SLI indicator sent from the endpoint on the corresponded RTP stream
		*/
		@JsonProperty("sliCount")
		public int sliCount;
		
			/**
		* The total processing delay of the RTP packets from the moment they received until the moment the jitter buffer emits them on the corresponded RTP strema.
		*/
		@JsonProperty("totalProcessingDelay")
		public double totalProcessingDelay;
		
			/**
		* The estimated timestamp of the jitterbuffer emits the RTP packets on the corresponded RTP stream.
		*/
		@JsonProperty("estimatedPlayoutTimestamp")
		public double estimatedPlayoutTimestamp;
		
			/**
		* The total delay encountered by the jitter buffer for the RTP stream to allevaite the effect of jitter on the transport.
		*/
		@JsonProperty("jitterBufferDelay")
		public double jitterBufferDelay;
		
			/**
		* The total number of emits happened for the corresponded RTP stream.
		*/
		@JsonProperty("jitterBufferEmittedCount")
		public int jitterBufferEmittedCount;
		
			/**
		* The total number of frames received on the corresponded RTP stream.
		*/
		@JsonProperty("framesReceived")
		public int framesReceived;
		
			/**
		* The library implements the decoder for the media source
		*/
		@JsonProperty("decoderImplementation")
		public String decoderImplementation;
		
			/**
		* The total number of packets sent by the remote endpoint on the corresponded RTP stream
		*/
		@JsonProperty("packetsSent")
		public int packetsSent;
		
			/**
		* The total amount of bytes sent by the remote endpoint on the corresponded RTP stream
		*/
		@JsonProperty("bytesSent")
		public long bytesSent;
		
			/**
		* The remote timestamp of the RTCP packets reported in the SR
		*/
		@JsonProperty("remoteTimestamp")
		public double remoteTimestamp;
		
			/**
		* The total number of SR reports sent by the remote endpoint on the corresponded RTP stream
		*/
		@JsonProperty("reportsSent")
		public int reportsSent;
		
			/**
		* Flag indicate if the MediaTrack has been eded or not
		*/
		@JsonProperty("ended")
		public boolean ended;
		
			/**
		* The type of the payload the RTP stream carries
		*/
		@JsonProperty("payloadType")
		public int payloadType;
		
			/**
		* The type of the codec role inthe endpoint.

Possible values are: "audio", and "video"
		*/
		@JsonProperty("codecType")
		public String codecType;
		
			/**
		* The MIME type of the media codec
		*/
		@JsonProperty("mimeType")
		public String mimeType;
		
			/**
		* the clock rate of the media source generates samples or frames
		*/
		@JsonProperty("clockRate")
		public long clockRate;
		
			/**
		* The corresponded SDP line in SDP negotiation
		*/
		@JsonProperty("sdpFmtpLine")
		public String sdpFmtpLine;
		
			/**
		* The identifier of the MediaTrack the client plays the audio out
		*/
		@JsonProperty("trackId")
		public String trackId;
		
			/**
		* The unique generated identifier of the peer connection the inbound audio track belongs to
		*/
		@JsonProperty("peerConnectionId")
		public String peerConnectionId;
		
	}
	/** 
	* A compound object giving information about the audio track the client uses
	*/
	
	public static class OutboundAudioTrack { 
	
			/**
		* The SSRC identifier of the corresponded RTP stream
		*/
		@JsonProperty("ssrc")
		public long ssrc;
		
			/**
		* The total number of packets sent on the corresponded RTP stream
		*/
		@JsonProperty("packetsSent")
		public int packetsSent;
		
			/**
		* The total amount of payload bytes sent on the corresponded RTP stream
		*/
		@JsonProperty("bytesSent")
		public long bytesSent;
		
			/**
		* If RTX is negotiated as a separate stream, this is the SSRC of the RTX stream that is associated with this stream's ssrc.
		*/
		@JsonProperty("rtxSsrc")
		public int rtxSsrc;
		
			/**
		* The rid parameter of the corresponded RTP stream
		*/
		@JsonProperty("rid")
		public long rid;
		
			/**
		* The last RTP packet sent timestamp
		*/
		@JsonProperty("lastPacketSentTimestamp")
		public long lastPacketSentTimestamp;
		
			/**
		* The total amount of header bytes sent on the corresponded RTP stream
		*/
		@JsonProperty("headerBytesSent")
		public long headerBytesSent;
		
			/**
		* The packets discarded at sending on the corresponded RTP stream
		*/
		@JsonProperty("packetsDiscardedOnSend")
		public int packetsDiscardedOnSend;
		
			/**
		* The bytes discarded at sending on the corresponded RTP stream.
		*/
		@JsonProperty("bytesDiscardedOnSend")
		public long bytesDiscardedOnSend;
		
			/**
		* The total number of FEC packets sent on the corresponded RTP stream.
		*/
		@JsonProperty("fecPacketsSent")
		public int fecPacketsSent;
		
			/**
		* The total number of retransmitted packets sent on the corresponded RTP stream.
		*/
		@JsonProperty("retransmittedPacketsSent")
		public int retransmittedPacketsSent;
		
			/**
		* The total number of retransmitted bytes sent on the corresponded RTP stream
		*/
		@JsonProperty("retransmittedBytesSent")
		public long retransmittedBytesSent;
		
			/**
		* The media codec targeted bit rate
		*/
		@JsonProperty("targetBitrate")
		public long targetBitrate;
		
			/**
		* The total number of samples the media source sent
		*/
		@JsonProperty("totalSamplesSent")
		public int totalSamplesSent;
		
			/**
		* The total number of samples encoded with SILK
		*/
		@JsonProperty("samplesEncodedWithSilk")
		public int samplesEncodedWithSilk;
		
			/**
		* The total number of samples encoded with CELT
		*/
		@JsonProperty("samplesEncodedWithCelt")
		public int samplesEncodedWithCelt;
		
			/**
		* The media encoder voice activity flag shipped to teh RTP strem by adding a V flag indicator to the headers
		*/
		@JsonProperty("voiceActivityFlag")
		public boolean voiceActivityFlag;
		
			/**
		* The total amount of delay in seconds the packets subjected to wait before sending. This can be either because of a pace bufffer, or other enforced waiting.
		*/
		@JsonProperty("totalPacketSendDelay")
		public double totalPacketSendDelay;
		
			/**
		* The average RTCP interval for SR compound packets
		*/
		@JsonProperty("averageRtcpInterval")
		public double averageRtcpInterval;
		
			/**
		* The ratio of the DSCP packets sent on the corresponded RTP stream.
		*/
		@JsonProperty("perDscpPacketsSent")
		public double perDscpPacketsSent;
		
			/**
		* The total number of negative acknowledgement sent on the corresponded RTP stream
		*/
		@JsonProperty("nackCount")
		public int nackCount;
		
			/**
		* The libray name of the media encoder
		*/
		@JsonProperty("encoderImplementation")
		public String encoderImplementation;
		
			/**
		* The total number of packets received on the corresponded RTP stream,
		*/
		@JsonProperty("packetsReceived")
		public int packetsReceived;
		
			/**
		* The total number of packets lost on the corresponded RTP stream
		*/
		@JsonProperty("packetsLost")
		public int packetsLost;
		
			/**
		* The last RR reported jitter on the corresponded RTP stream
		*/
		@JsonProperty("jitter")
		public double jitter;
		
			/**
		* The total number of discarded packets on the corresponded RTP stream.
		*/
		@JsonProperty("packetsDiscarded")
		public int packetsDiscarded;
		
			/**
		* The total number of packets repaired by either retransmission or FEC on the corresponded RTP stream.
		*/
		@JsonProperty("packetsRepaired")
		public int packetsRepaired;
		
			/**
		* The number of packets lost in burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstPacketsLost")
		public int burstPacketsLost;
		
			/**
		* The total number of packets discarded during a burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstPacketsDiscarded")
		public int burstPacketsDiscarded;
		
			/**
		* The total number of burst lost happened on the coerresponded RTP stream
		*/
		@JsonProperty("burstLossCount")
		public int burstLossCount;
		
			/**
		* The number of burst discards happened on the corresponded RTP stream.
		*/
		@JsonProperty("burstDiscardCount")
		public int burstDiscardCount;
		
			/**
		* The loss rate during burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstLossRate")
		public double burstLossRate;
		
			/**
		* The discard rate during burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstDiscardRate")
		public int burstDiscardRate;
		
			/**
		* The loss rate during a gap period on the corresponded RTP stream.
		*/
		@JsonProperty("gapLossRate")
		public double gapLossRate;
		
			/**
		* The discard rate during a gap period on the corresponded RTP stream
		*/
		@JsonProperty("gapDiscardRate")
		public double gapDiscardRate;
		
			/**
		* The last RTT measurements based on the last SR-RR
		*/
		@JsonProperty("roundTripTime")
		public double roundTripTime;
		
			/**
		* The total sum of the RTT measurements on the corresponded RTP stream
		*/
		@JsonProperty("totalRoundTripTime")
		public double totalRoundTripTime;
		
			/**
		* The last RR reported fractional lost
		*/
		@JsonProperty("fractionLost")
		public double fractionLost;
		
			/**
		* The number of RR compound report received on the corresponded RTP stream
		*/
		@JsonProperty("reportsReceived")
		public int reportsReceived;
		
			/**
		* The number of RTT measurement calculated on the corresponded RTP stream
		*/
		@JsonProperty("roundTripTimeMeasurements")
		public int roundTripTimeMeasurements;
		
			/**
		* Flag indicating if the media source is relayed or not, meaning the local endpoint is not the actual source of the media, but a proxy for that media.
		*/
		@JsonProperty("relayedSource")
		public boolean relayedSource;
		
			/**
		* the audio level of the media source.
		*/
		@JsonProperty("audioLevel")
		public double audioLevel;
		
			/**
		* The audio energy of the media source

For calculation see https://www.w3.org/TR/webrtc-stats/#dom-rtcaudiosourcestats-totalaudioenergy
		*/
		@JsonProperty("totalAudioEnergy")
		public double totalAudioEnergy;
		
			/**
		* The duration of the audio type media source
		*/
		@JsonProperty("totalSamplesDuration")
		public double totalSamplesDuration;
		
			/**
		* if echo cancellation is applied on the media source, then 
this number represents the loss calculation defined in https://www.itu.int/rec/T-REC-G.168-201504-I/en
		*/
		@JsonProperty("echoReturnLoss")
		public double echoReturnLoss;
		
			/**
		* similar to the echo return loss calculation
		*/
		@JsonProperty("echoReturnLossEnhancement")
		public double echoReturnLossEnhancement;
		
			/**
		* Flag indicate if the MediaTrack has been eded or not
		*/
		@JsonProperty("ended")
		public boolean ended;
		
			/**
		* The type of the payload the RTP stream carries
		*/
		@JsonProperty("payloadType")
		public int payloadType;
		
			/**
		* The type of the codec role inthe endpoint.

Possible values are: "audio", and "video"
		*/
		@JsonProperty("codecType")
		public String codecType;
		
			/**
		* The MIME type of the media codec
		*/
		@JsonProperty("mimeType")
		public String mimeType;
		
			/**
		* the clock rate of the media source generates samples or frames
		*/
		@JsonProperty("clockRate")
		public long clockRate;
		
			/**
		* The number of channels the media source has.
		*/
		@JsonProperty("channels")
		public int channels;
		
			/**
		* The corresponded SDP line in SDP negotiation
		*/
		@JsonProperty("sdpFmtpLine")
		public String sdpFmtpLine;
		
			/**
		* The identifier of the MediaTrack the client plays the audio out
		*/
		@JsonProperty("trackId")
		public String trackId;
		
			/**
		* The unique generated identifier of the peer connection the inbound audio track belongs to
		*/
		@JsonProperty("peerConnectionId")
		public String peerConnectionId;
		
	}
	/** 
	* undefined
	*/
	
	public static class OutboundVideoTrack { 
	
			/**
		* The SSRC identifier of the corresponded RTP stream
		*/
		@JsonProperty("ssrc")
		public long ssrc;
		
			/**
		* The total number of packets sent on the corresponded RTP stream
		*/
		@JsonProperty("packetsSent")
		public int packetsSent;
		
			/**
		* The total amount of payload bytes sent on the corresponded RTP stream
		*/
		@JsonProperty("bytesSent")
		public long bytesSent;
		
			/**
		* If RTX is negotiated as a separate stream, this is the SSRC of the RTX stream that is associated with this stream's ssrc.
		*/
		@JsonProperty("rtxSsrc")
		public int rtxSsrc;
		
			/**
		* The rid parameter of the corresponded RTP stream
		*/
		@JsonProperty("rid")
		public long rid;
		
			/**
		* The last RTP packet sent timestamp
		*/
		@JsonProperty("lastPacketSentTimestamp")
		public long lastPacketSentTimestamp;
		
			/**
		* The total amount of header bytes sent on the corresponded RTP stream
		*/
		@JsonProperty("headerBytesSent")
		public long headerBytesSent;
		
			/**
		* The packets discarded at sending on the corresponded RTP stream
		*/
		@JsonProperty("packetsDiscardedOnSend")
		public int packetsDiscardedOnSend;
		
			/**
		* The bytes discarded at sending on the corresponded RTP stream.
		*/
		@JsonProperty("bytesDiscardedOnSend")
		public long bytesDiscardedOnSend;
		
			/**
		* The total number of FEC packets sent on the corresponded RTP stream.
		*/
		@JsonProperty("fecPacketsSent")
		public int fecPacketsSent;
		
			/**
		* The total number of retransmitted packets sent on the corresponded RTP stream.
		*/
		@JsonProperty("retransmittedPacketsSent")
		public int retransmittedPacketsSent;
		
			/**
		* The total number of retransmitted bytes sent on the corresponded RTP stream
		*/
		@JsonProperty("retransmittedBytesSent")
		public long retransmittedBytesSent;
		
			/**
		* The media codec targeted bit rate
		*/
		@JsonProperty("targetBitrate")
		public long targetBitrate;
		
			/**
		* The total encoded bytes targeted by the media encoder. this is the sum of the encoded frames
		*/
		@JsonProperty("totalEncodedBytesTarget")
		public long totalEncodedBytesTarget;
		
			/**
		* The frame width in pixels of the frames targeted by the media encoder
		*/
		@JsonProperty("frameWidth")
		public int frameWidth;
		
			/**
		* The frame width the media encoder targeted
		*/
		@JsonProperty("frameHeight")
		public int frameHeight;
		
			/**
		* The frame depth in pixles on the corresponded RTP stream
		*/
		@JsonProperty("frameBitDepth")
		public int frameBitDepth;
		
			/**
		* The encoded number of frames in the last second on the corresponded media source
		*/
		@JsonProperty("framesPerSecond")
		public double framesPerSecond;
		
			/**
		* The total number of frames sent on the corresponded RTP stream
		*/
		@JsonProperty("framesSent")
		public int framesSent;
		
			/**
		* The total number of huge frames (avgFrameSize * 2.5) on the corresponded RTP stream
		*/
		@JsonProperty("hugeFramesSent")
		public int hugeFramesSent;
		
			/**
		* The total number of frames encoded by the media source
		*/
		@JsonProperty("framesEncoded")
		public int framesEncoded;
		
			/**
		* The total number of keyframes encoded on the corresponded RTP stream
		*/
		@JsonProperty("keyFramesEncoded")
		public int keyFramesEncoded;
		
			/**
		* The total number of frames discarded on the corresponded RTP stream.
		*/
		@JsonProperty("framesDiscardedOnSend")
		public int framesDiscardedOnSend;
		
			/**
		* The sum of the QP the media encoder provided on the corresponded RTP stream.
		*/
		@JsonProperty("qpSum")
		public long qpSum;
		
			/**
		* The total time in seconds spent in encoding media frames for the corresponded RTP stream.
		*/
		@JsonProperty("totalEncodeTime")
		public double totalEncodeTime;
		
			/**
		* The total amount of delay in seconds the packets subjected to wait before sending. This can be either because of a pace bufffer, or other enforced waiting.
		*/
		@JsonProperty("totalPacketSendDelay")
		public double totalPacketSendDelay;
		
			/**
		* The average RTCP interval for SR compound packets
		*/
		@JsonProperty("averageRtcpInterval")
		public double averageRtcpInterval;
		
			/**
		* The reason for quality limitation happeened on the corresponded RTP stream
		*/
		@JsonProperty("qualityLimitationReason")
		public String qualityLimitationReason;
		
			/**
		* the total sum of duration of the quality limitation happened on the corresponded RTP stream
		*/
		@JsonProperty("qualityLimitationDurations")
		public double qualityLimitationDurations;
		
			/**
		* The total number of resolution changes occured ont he corresponded RTP stream due to quality changes
		*/
		@JsonProperty("qualityLimitationResolutionChanges")
		public int qualityLimitationResolutionChanges;
		
			/**
		* The ratio of the DSCP packets sent on the corresponded RTP stream.
		*/
		@JsonProperty("perDscpPacketsSent")
		public double perDscpPacketsSent;
		
			/**
		* The total number of negative acknowledgement sent on the corresponded RTP stream
		*/
		@JsonProperty("nackCount")
		public int nackCount;
		
			/**
		* The total number of FIR counted on the corresponded RTP stream
		*/
		@JsonProperty("firCount")
		public int firCount;
		
			/**
		* The total number of picture loss indication happeend on teh corresaponded mRTP stream
		*/
		@JsonProperty("pliCount")
		public int pliCount;
		
			/**
		* The total number of SLI occured on the corresponded RTP stream
		*/
		@JsonProperty("sliCount")
		public int sliCount;
		
			/**
		* The libray name of the media encoder
		*/
		@JsonProperty("encoderImplementation")
		public String encoderImplementation;
		
			/**
		* The total number of packets received on the corresponded RTP stream,
		*/
		@JsonProperty("packetsReceived")
		public int packetsReceived;
		
			/**
		* The total number of packets lost on the corresponded RTP stream
		*/
		@JsonProperty("packetsLost")
		public int packetsLost;
		
			/**
		* The last RR reported jitter on the corresponded RTP stream
		*/
		@JsonProperty("jitter")
		public double jitter;
		
			/**
		* The total number of discarded packets on the corresponded RTP stream.
		*/
		@JsonProperty("packetsDiscarded")
		public int packetsDiscarded;
		
			/**
		* The total number of packets repaired by either retransmission or FEC on the corresponded RTP stream.
		*/
		@JsonProperty("packetsRepaired")
		public int packetsRepaired;
		
			/**
		* The number of packets lost in burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstPacketsLost")
		public int burstPacketsLost;
		
			/**
		* The total number of packets discarded during a burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstPacketsDiscarded")
		public int burstPacketsDiscarded;
		
			/**
		* The total number of burst lost happened on the coerresponded RTP stream
		*/
		@JsonProperty("burstLossCount")
		public int burstLossCount;
		
			/**
		* The number of burst discards happened on the corresponded RTP stream.
		*/
		@JsonProperty("burstDiscardCount")
		public int burstDiscardCount;
		
			/**
		* The loss rate during burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstLossRate")
		public double burstLossRate;
		
			/**
		* The discard rate during burst period on the corresponded RTP stream.
		*/
		@JsonProperty("burstDiscardRate")
		public int burstDiscardRate;
		
			/**
		* The loss rate during a gap period on the corresponded RTP stream.
		*/
		@JsonProperty("gapLossRate")
		public double gapLossRate;
		
			/**
		* The discard rate during a gap period on the corresponded RTP stream
		*/
		@JsonProperty("gapDiscardRate")
		public double gapDiscardRate;
		
			/**
		* The total number of frames reported to be lost by the remote endpoit on the corresponded RTP stream
		*/
		@JsonProperty("framesDropped")
		public double framesDropped;
		
			/**
		* The total number of partial frames reported to be lost by the remote endpoint on the corresponded RTP stream.
		*/
		@JsonProperty("partialFramesList")
		public int partialFramesList;
		
			/**
		* The total number of full frames lost at the remote endpoint on the corresponded RTP stream.
		*/
		@JsonProperty("fullFramesList")
		public int fullFramesList;
		
			/**
		* The last RTT measurements based on the last SR-RR
		*/
		@JsonProperty("roundTripTime")
		public double roundTripTime;
		
			/**
		* The total sum of the RTT measurements on the corresponded RTP stream
		*/
		@JsonProperty("totalRoundTripTime")
		public double totalRoundTripTime;
		
			/**
		* The last RR reported fractional lost
		*/
		@JsonProperty("fractionLost")
		public double fractionLost;
		
			/**
		* The number of RR compound report received on the corresponded RTP stream
		*/
		@JsonProperty("reportsReceived")
		public int reportsReceived;
		
			/**
		* The number of RTT measurement calculated on the corresponded RTP stream
		*/
		@JsonProperty("roundTripTimeMeasurements")
		public int roundTripTimeMeasurements;
		
			/**
		* Flag indicating if the media source is relayed or not, meaning the local endpoint is not the actual source of the media, but a proxy for that media.
		*/
		@JsonProperty("relayedSource")
		public boolean relayedSource;
		
			/**
		* The width, in pixels, of the last frame originating from the media source
		*/
		@JsonProperty("width")
		public int width;
		
			/**
		* The height, in pixels, of the last frame originating from the media source
		*/
		@JsonProperty("height")
		public int height;
		
			/**
		* The bit depth per pixels, of the last frame originating from the media source
		*/
		@JsonProperty("bitDepth")
		public int bitDepth;
		
			/**
		* The total number of frames originated from the media source
		*/
		@JsonProperty("frames")
		public int frames;
		
			/**
		* Flag indicate if the MediaTrack has been eded or not
		*/
		@JsonProperty("ended")
		public boolean ended;
		
			/**
		* The type of the payload the RTP stream carries
		*/
		@JsonProperty("payloadType")
		public int payloadType;
		
			/**
		* The type of the codec role inthe endpoint.

Possible values are: "audio", and "video"
		*/
		@JsonProperty("codecType")
		public String codecType;
		
			/**
		* The MIME type of the media codec
		*/
		@JsonProperty("mimeType")
		public String mimeType;
		
			/**
		* the clock rate of the media source generates samples or frames
		*/
		@JsonProperty("clockRate")
		public long clockRate;
		
			/**
		* The number of channels the media source has.
		*/
		@JsonProperty("channels")
		public int channels;
		
			/**
		* The corresponded SDP line in SDP negotiation
		*/
		@JsonProperty("sdpFmtpLine")
		public String sdpFmtpLine;
		
			/**
		* The identifier of the MediaTrack the client plays the audio out
		*/
		@JsonProperty("trackId")
		public String trackId;
		
			/**
		* The unique generated identifier of the peer connection the inbound audio track belongs to
		*/
		@JsonProperty("peerConnectionId")
		public String peerConnectionId;
		
	}
	/** 
	* undefined
	*/
	
	public static class ICELocalCandidate { 
	
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
		public int port;
		
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
		public String priority;
		
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
		
			/**
		* Refers to the peer connection the local candidate belongs to
		*/
		@JsonProperty("peerConnectionId")
		public String peerConnectionId;
		
	}
	/** 
	* undefined
	*/
	
	public static class ICERemoteCandidate { 
	
			/**
		* The unique identifier of the remote candidate
		*/
		@JsonProperty("id")
		public String id;
		
			/**
		* The address of the remote endpoint (Ipv4, Ipv6, FQDN)
		*/
		@JsonProperty("address")
		public String address;
		
			/**
		* The port number of the remote endpoint the ICE uses
		*/
		@JsonProperty("port")
		public int port;
		
			/**
		* The protocol for the ICE
		*/
		@JsonProperty("protocol")
		public String protocol;
		
			/**
		* The type of the remote candidate
		*/
		@JsonProperty("candidateType")
		public String candidateType;
		
			/**
		* The priority of the remote candidate
		*/
		@JsonProperty("priority")
		public String priority;
		
			/**
		* The url of the ICE server
		*/
		@JsonProperty("url")
		public String url;
		
			/**
		* The relay protocol the remote candidate uses
		*/
		@JsonProperty("relayProtocol")
		public String relayProtocol;
		
			/**
		* Refers to the peer connection the remote candidate belongs to
		*/
		@JsonProperty("peerConnectionId")
		public String peerConnectionId;
		
	}
	/** 
	* undefined
	*/
	
	public static class DataChannel { 
	
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
		* The protocol the data channel use to transfer data
		*/
		@JsonProperty("protocol")
		public String protocol;
		
			/**
		* The unique identifier of the data channel
		*/
		@JsonProperty("dataChannelIdentifier")
		public String dataChannelIdentifier;
		
			/**
		* The state of the data channel
		*/
		@JsonProperty("state")
		public String state;
		
			/**
		* The total number of messages sent on this data channel. this is not equal to the number of packets sent, as messages are chunked to packets
		*/
		@JsonProperty("messagesSent")
		public int messagesSent;
		
			/**
		* The amount of bytes sent on the corresponded data channel
		*/
		@JsonProperty("bytesSent")
		public long bytesSent;
		
			/**
		* The number of messages received on the corresponded data channel
		*/
		@JsonProperty("messagesReceived")
		public int messagesReceived;
		
			/**
		* The total amount of bytes received on the corresponded data channel
		*/
		@JsonProperty("bytesReceived")
		public long bytesReceived;
		
	}
}