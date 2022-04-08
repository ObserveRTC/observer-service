package org.observertc.schemas.samples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
* Observer created reports related to events (call started, call ended, client joined, etc...) indicated by the incoming samples.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class Samples {
	public static final String VERSION="2.0.0-beta.55";
	public static Builder newBuilder() {
		return new Builder();
	}
	/**
	* undefined
	*/
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SamplesMeta {
		public static Builder newBuilder() {
			return new Builder();
		}
		/**
		* Indicate the version of the schema for compatibility measures.
		*/
		@JsonProperty("schemaVersion")
		public String schemaVersion;
	

		public static class Builder {
	
			private SamplesMeta result = new SamplesMeta();
	
			public Builder setSchemaVersion(String value) { this.result.schemaVersion = value; return this; }
			public SamplesMeta build() {
				return this.result;
			}
		}
	}
	/**
	* undefined
	*/
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ControlFlags {
		public static Builder newBuilder() {
			return new Builder();
		}
		/**
		* Indicate that the server should close the connection
		*/
		@JsonProperty("close")
		public Boolean close;
	

		public static class Builder {
	
			private ControlFlags result = new ControlFlags();
	
			public Builder setClose(Boolean value) { this.result.close = value; return this; }
			public ControlFlags build() {
				return this.result;
			}
		}
	}
	/**
	* docs
	*/
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ClientSample {
		public static Builder newBuilder() {
			return new Builder();
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Engine {
			public static Builder newBuilder() {
				return new Builder();
			}
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
		

			public static class Builder {
		
				private Engine result = new Engine();
		
				public Builder setName(String value) { this.result.name = value; return this; }
				public Builder setVersion(String value) { this.result.version = value; return this; }
				public Engine build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Platform {
			public static Builder newBuilder() {
				return new Builder();
			}
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
		

			public static class Builder {
		
				private Platform result = new Platform();
		
				public Builder setType(String value) { this.result.type = value; return this; }
				public Builder setVendor(String value) { this.result.vendor = value; return this; }
				public Builder setModel(String value) { this.result.model = value; return this; }
				public Platform build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Browser {
			public static Builder newBuilder() {
				return new Builder();
			}
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
		

			public static class Builder {
		
				private Browser result = new Browser();
		
				public Builder setName(String value) { this.result.name = value; return this; }
				public Builder setVersion(String value) { this.result.version = value; return this; }
				public Browser build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class OperationSystem {
			public static Builder newBuilder() {
				return new Builder();
			}
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
		

			public static class Builder {
		
				private OperationSystem result = new OperationSystem();
		
				public Builder setName(String value) { this.result.name = value; return this; }
				public Builder setVersion(String value) { this.result.version = value; return this; }
				public Builder setVersionName(String value) { this.result.versionName = value; return this; }
				public OperationSystem build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class MediaDevice {
			public static Builder newBuilder() {
				return new Builder();
			}
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
		

			public static class Builder {
		
				private MediaDevice result = new MediaDevice();
		
				public Builder setId(String value) { this.result.id = value; return this; }
				public Builder setKind(String value) { this.result.kind = value; return this; }
				public Builder setLabel(String value) { this.result.label = value; return this; }
				public MediaDevice build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class ExtensionStat {
			public static Builder newBuilder() {
				return new Builder();
			}
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
		

			public static class Builder {
		
				private ExtensionStat result = new ExtensionStat();
		
				public Builder setType(String value) { this.result.type = value; return this; }
				public Builder setPayload(String value) { this.result.payload = value; return this; }
				public ExtensionStat build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class PeerConnectionTransport {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* The unique identifier of the peer connection
			*/
			@JsonProperty("peerConnectionId")
			public UUID peerConnectionId;
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
		
				private PeerConnectionTransport result = new PeerConnectionTransport();
		
				public Builder setPeerConnectionId(UUID value) { this.result.peerConnectionId = value; return this; }
				public Builder setLabel(String value) { this.result.label = value; return this; }
				public Builder setDataChannelsOpened(Integer value) { this.result.dataChannelsOpened = value; return this; }
				public Builder setDataChannelsClosed(Integer value) { this.result.dataChannelsClosed = value; return this; }
				public Builder setDataChannelsRequested(Integer value) { this.result.dataChannelsRequested = value; return this; }
				public Builder setDataChannelsAccepted(Integer value) { this.result.dataChannelsAccepted = value; return this; }
				public Builder setPacketsSent(Integer value) { this.result.packetsSent = value; return this; }
				public Builder setPacketsReceived(Integer value) { this.result.packetsReceived = value; return this; }
				public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
				public Builder setBytesReceived(Long value) { this.result.bytesReceived = value; return this; }
				public Builder setIceRole(String value) { this.result.iceRole = value; return this; }
				public Builder setIceLocalUsernameFragment(String value) { this.result.iceLocalUsernameFragment = value; return this; }
				public Builder setDtlsState(String value) { this.result.dtlsState = value; return this; }
				public Builder setIceState(String value) { this.result.iceState = value; return this; }
				public Builder setTlsVersion(String value) { this.result.tlsVersion = value; return this; }
				public Builder setDtlsCipher(String value) { this.result.dtlsCipher = value; return this; }
				public Builder setSrtpCipher(String value) { this.result.srtpCipher = value; return this; }
				public Builder setTlsGroup(String value) { this.result.tlsGroup = value; return this; }
				public Builder setSelectedCandidatePairChanges(Integer value) { this.result.selectedCandidatePairChanges = value; return this; }
				public Builder setLocalAddress(String value) { this.result.localAddress = value; return this; }
				public Builder setLocalPort(Integer value) { this.result.localPort = value; return this; }
				public Builder setLocalProtocol(String value) { this.result.localProtocol = value; return this; }
				public Builder setLocalCandidateType(String value) { this.result.localCandidateType = value; return this; }
				public Builder setLocalCandidateICEServerUrl(String value) { this.result.localCandidateICEServerUrl = value; return this; }
				public Builder setLocalCandidateRelayProtocol(String value) { this.result.localCandidateRelayProtocol = value; return this; }
				public Builder setRemoteAddress(String value) { this.result.remoteAddress = value; return this; }
				public Builder setRemotePort(Integer value) { this.result.remotePort = value; return this; }
				public Builder setRemoteProtocol(String value) { this.result.remoteProtocol = value; return this; }
				public Builder setRemoteCandidateType(String value) { this.result.remoteCandidateType = value; return this; }
				public Builder setRemoteCandidateICEServerUrl(String value) { this.result.remoteCandidateICEServerUrl = value; return this; }
				public Builder setRemoteCandidateRelayProtocol(String value) { this.result.remoteCandidateRelayProtocol = value; return this; }
				public Builder setCandidatePairState(String value) { this.result.candidatePairState = value; return this; }
				public Builder setCandidatePairPacketsSent(Integer value) { this.result.candidatePairPacketsSent = value; return this; }
				public Builder setCandidatePairPacketsReceived(Integer value) { this.result.candidatePairPacketsReceived = value; return this; }
				public Builder setCandidatePairBytesSent(Long value) { this.result.candidatePairBytesSent = value; return this; }
				public Builder setCandidatePairBytesReceived(Long value) { this.result.candidatePairBytesReceived = value; return this; }
				public Builder setCandidatePairLastPacketSentTimestamp(Long value) { this.result.candidatePairLastPacketSentTimestamp = value; return this; }
				public Builder setCandidatePairLastPacketReceivedTimestamp(Long value) { this.result.candidatePairLastPacketReceivedTimestamp = value; return this; }
				public Builder setCandidatePairFirstRequestTimestamp(Long value) { this.result.candidatePairFirstRequestTimestamp = value; return this; }
				public Builder setCandidatePairLastRequestTimestamp(Long value) { this.result.candidatePairLastRequestTimestamp = value; return this; }
				public Builder setCandidatePairLastResponseTimestamp(Long value) { this.result.candidatePairLastResponseTimestamp = value; return this; }
				public Builder setCandidatePairTotalRoundTripTime(Double value) { this.result.candidatePairTotalRoundTripTime = value; return this; }
				public Builder setCandidatePairCurrentRoundTripTime(Double value) { this.result.candidatePairCurrentRoundTripTime = value; return this; }
				public Builder setCandidatePairAvailableOutgoingBitrate(Double value) { this.result.candidatePairAvailableOutgoingBitrate = value; return this; }
				public Builder setCandidatePairAvailableIncomingBitrate(Double value) { this.result.candidatePairAvailableIncomingBitrate = value; return this; }
				public Builder setCandidatePairCircuitBreakerTriggerCount(Integer value) { this.result.candidatePairCircuitBreakerTriggerCount = value; return this; }
				public Builder setCandidatePairRequestsReceived(Integer value) { this.result.candidatePairRequestsReceived = value; return this; }
				public Builder setCandidatePairRequestsSent(Integer value) { this.result.candidatePairRequestsSent = value; return this; }
				public Builder setCandidatePairResponsesReceived(Integer value) { this.result.candidatePairResponsesReceived = value; return this; }
				public Builder setCandidatePairResponsesSent(Integer value) { this.result.candidatePairResponsesSent = value; return this; }
				public Builder setCandidatePairRetransmissionReceived(Integer value) { this.result.candidatePairRetransmissionReceived = value; return this; }
				public Builder setCandidatePairRetransmissionSent(Integer value) { this.result.candidatePairRetransmissionSent = value; return this; }
				public Builder setCandidatePairConsentRequestsSent(Integer value) { this.result.candidatePairConsentRequestsSent = value; return this; }
				public Builder setCandidatePairConsentExpiredTimestamp(Long value) { this.result.candidatePairConsentExpiredTimestamp = value; return this; }
				public Builder setCandidatePairBytesDiscardedOnSend(Long value) { this.result.candidatePairBytesDiscardedOnSend = value; return this; }
				public Builder setCandidatePairPacketsDiscardedOnSend(Integer value) { this.result.candidatePairPacketsDiscardedOnSend = value; return this; }
				public Builder setCandidatePairRequestBytesSent(Long value) { this.result.candidatePairRequestBytesSent = value; return this; }
				public Builder setCandidatePairConsentRequestBytesSent(Long value) { this.result.candidatePairConsentRequestBytesSent = value; return this; }
				public Builder setCandidatePairResponseBytesSent(Long value) { this.result.candidatePairResponseBytesSent = value; return this; }
				public Builder setSctpSmoothedRoundTripTime(Double value) { this.result.sctpSmoothedRoundTripTime = value; return this; }
				public Builder setSctpCongestionWindow(Double value) { this.result.sctpCongestionWindow = value; return this; }
				public Builder setSctpReceiverWindow(Double value) { this.result.sctpReceiverWindow = value; return this; }
				public Builder setSctpMtu(Integer value) { this.result.sctpMtu = value; return this; }
				public Builder setSctpUnackData(Integer value) { this.result.sctpUnackData = value; return this; }
				public PeerConnectionTransport build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class MediaSourceStat {
			public static Builder newBuilder() {
				return new Builder();
			}
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
			public Integer frames;
			/**
			*  The number of frames origianted from the media source in the last second
			*/
			@JsonProperty("framesPerSecond")
			public Double framesPerSecond;
		

			public static class Builder {
		
				private MediaSourceStat result = new MediaSourceStat();
		
				public Builder setTrackIdentifier(String value) { this.result.trackIdentifier = value; return this; }
				public Builder setKind(String value) { this.result.kind = value; return this; }
				public Builder setRelayedSource(Boolean value) { this.result.relayedSource = value; return this; }
				public Builder setAudioLevel(Double value) { this.result.audioLevel = value; return this; }
				public Builder setTotalAudioEnergy(Double value) { this.result.totalAudioEnergy = value; return this; }
				public Builder setTotalSamplesDuration(Double value) { this.result.totalSamplesDuration = value; return this; }
				public Builder setEchoReturnLoss(Double value) { this.result.echoReturnLoss = value; return this; }
				public Builder setEchoReturnLossEnhancement(Double value) { this.result.echoReturnLossEnhancement = value; return this; }
				public Builder setWidth(Integer value) { this.result.width = value; return this; }
				public Builder setHeight(Integer value) { this.result.height = value; return this; }
				public Builder setBitDepth(Integer value) { this.result.bitDepth = value; return this; }
				public Builder setFrames(Integer value) { this.result.frames = value; return this; }
				public Builder setFramesPerSecond(Double value) { this.result.framesPerSecond = value; return this; }
				public MediaSourceStat build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class MediaCodecStats {
			public static Builder newBuilder() {
				return new Builder();
			}
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
		

			public static class Builder {
		
				private MediaCodecStats result = new MediaCodecStats();
		
				public Builder setPayloadType(String value) { this.result.payloadType = value; return this; }
				public Builder setCodecType(String value) { this.result.codecType = value; return this; }
				public Builder setMimeType(String value) { this.result.mimeType = value; return this; }
				public Builder setClockRate(Integer value) { this.result.clockRate = value; return this; }
				public Builder setChannels(Integer value) { this.result.channels = value; return this; }
				public Builder setSdpFmtpLine(String value) { this.result.sdpFmtpLine = value; return this; }
				public MediaCodecStats build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Certificate {
			public static Builder newBuilder() {
				return new Builder();
			}
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
		

			public static class Builder {
		
				private Certificate result = new Certificate();
		
				public Builder setFingerprint(String value) { this.result.fingerprint = value; return this; }
				public Builder setFingerprintAlgorithm(String value) { this.result.fingerprintAlgorithm = value; return this; }
				public Builder setBase64Certificate(String value) { this.result.base64Certificate = value; return this; }
				public Builder setIssuerCertificateId(String value) { this.result.issuerCertificateId = value; return this; }
				public Certificate build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class InboundAudioTrack {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* The id of the track
			*/
			@JsonProperty("trackId")
			public UUID trackId;
			/**
			*  The unique generated identifier of the peer connection the inbound audio track belongs to
			*/
			@JsonProperty("peerConnectionId")
			public UUID peerConnectionId;
			/**
			* The remote clientId the source outbound track belongs to
			*/
			@JsonProperty("remoteClientId")
			public String remoteClientId;
			/**
			* The id of the sink this track belongs to in the SFU
			*/
			@JsonProperty("sfuSinkId")
			public UUID sfuSinkId;
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
		
				private InboundAudioTrack result = new InboundAudioTrack();
		
				public Builder setTrackId(UUID value) { this.result.trackId = value; return this; }
				public Builder setPeerConnectionId(UUID value) { this.result.peerConnectionId = value; return this; }
				public Builder setRemoteClientId(String value) { this.result.remoteClientId = value; return this; }
				public Builder setSfuSinkId(UUID value) { this.result.sfuSinkId = value; return this; }
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
				public Builder setVoiceActivityFlag(Boolean value) { this.result.voiceActivityFlag = value; return this; }
				public Builder setTotalSamplesReceived(Integer value) { this.result.totalSamplesReceived = value; return this; }
				public Builder setTotalSamplesDecoded(Integer value) { this.result.totalSamplesDecoded = value; return this; }
				public Builder setSamplesDecodedWithSilk(Integer value) { this.result.samplesDecodedWithSilk = value; return this; }
				public Builder setSamplesDecodedWithCelt(Integer value) { this.result.samplesDecodedWithCelt = value; return this; }
				public Builder setConcealedSamples(Integer value) { this.result.concealedSamples = value; return this; }
				public Builder setSilentConcealedSamples(Integer value) { this.result.silentConcealedSamples = value; return this; }
				public Builder setConcealmentEvents(Integer value) { this.result.concealmentEvents = value; return this; }
				public Builder setInsertedSamplesForDeceleration(Integer value) { this.result.insertedSamplesForDeceleration = value; return this; }
				public Builder setRemovedSamplesForAcceleration(Integer value) { this.result.removedSamplesForAcceleration = value; return this; }
				public Builder setPacketsSent(Integer value) { this.result.packetsSent = value; return this; }
				public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
				public Builder setRemoteTimestamp(Long value) { this.result.remoteTimestamp = value; return this; }
				public Builder setReportsSent(Integer value) { this.result.reportsSent = value; return this; }
				public Builder setRoundTripTime(Double value) { this.result.roundTripTime = value; return this; }
				public Builder setTotalRoundTripTime(Double value) { this.result.totalRoundTripTime = value; return this; }
				public Builder setRoundTripTimeMeasurements(Integer value) { this.result.roundTripTimeMeasurements = value; return this; }
				public Builder setEnded(Boolean value) { this.result.ended = value; return this; }
				public Builder setPayloadType(Integer value) { this.result.payloadType = value; return this; }
				public Builder setMimeType(String value) { this.result.mimeType = value; return this; }
				public Builder setClockRate(Integer value) { this.result.clockRate = value; return this; }
				public Builder setChannels(Integer value) { this.result.channels = value; return this; }
				public Builder setSdpFmtpLine(String value) { this.result.sdpFmtpLine = value; return this; }
				public InboundAudioTrack build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class InboundVideoTrack {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* The id of the track
			*/
			@JsonProperty("trackId")
			public UUID trackId;
			/**
			*  The unique generated identifier of the peer connection the inbound audio track belongs to
			*/
			@JsonProperty("peerConnectionId")
			public UUID peerConnectionId;
			/**
			* The remote clientId the source outbound track belongs to
			*/
			@JsonProperty("remoteClientId")
			public String remoteClientId;
			/**
			* The id of the sink this track belongs to in the SFU
			*/
			@JsonProperty("sfuSinkId")
			public UUID sfuSinkId;
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
			* The total number of frames dropped on the corresponded RTP stream
			*/
			@JsonProperty("framesDropped")
			public Integer framesDropped;
			/**
			* The total number of frames decoded on the corresponded RTP stream
			*/
			@JsonProperty("framesDecoded")
			public Integer framesDecoded;
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
			* The total number of inter frame delay squere on the corresponded synchronization source (ssrc) Useful for variance calculation for interframe delays
			*/
			@JsonProperty("totalSquaredInterFrameDelay")
			public Double totalSquaredInterFrameDelay;
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
		
				private InboundVideoTrack result = new InboundVideoTrack();
		
				public Builder setTrackId(UUID value) { this.result.trackId = value; return this; }
				public Builder setPeerConnectionId(UUID value) { this.result.peerConnectionId = value; return this; }
				public Builder setRemoteClientId(String value) { this.result.remoteClientId = value; return this; }
				public Builder setSfuSinkId(UUID value) { this.result.sfuSinkId = value; return this; }
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
				public Builder setFramesDropped(Integer value) { this.result.framesDropped = value; return this; }
				public Builder setFramesDecoded(Integer value) { this.result.framesDecoded = value; return this; }
				public Builder setPartialFramesLost(Integer value) { this.result.partialFramesLost = value; return this; }
				public Builder setFullFramesLost(Integer value) { this.result.fullFramesLost = value; return this; }
				public Builder setKeyFramesDecoded(Integer value) { this.result.keyFramesDecoded = value; return this; }
				public Builder setFrameWidth(Integer value) { this.result.frameWidth = value; return this; }
				public Builder setFrameHeight(Integer value) { this.result.frameHeight = value; return this; }
				public Builder setFrameBitDepth(Integer value) { this.result.frameBitDepth = value; return this; }
				public Builder setFramesPerSecond(Double value) { this.result.framesPerSecond = value; return this; }
				public Builder setQpSum(Long value) { this.result.qpSum = value; return this; }
				public Builder setTotalDecodeTime(Double value) { this.result.totalDecodeTime = value; return this; }
				public Builder setTotalInterFrameDelay(Double value) { this.result.totalInterFrameDelay = value; return this; }
				public Builder setTotalSquaredInterFrameDelay(Double value) { this.result.totalSquaredInterFrameDelay = value; return this; }
				public Builder setFirCount(Integer value) { this.result.firCount = value; return this; }
				public Builder setPliCount(Integer value) { this.result.pliCount = value; return this; }
				public Builder setSliCount(Integer value) { this.result.sliCount = value; return this; }
				public Builder setFramesReceived(Integer value) { this.result.framesReceived = value; return this; }
				public Builder setPacketsSent(Integer value) { this.result.packetsSent = value; return this; }
				public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
				public Builder setRemoteTimestamp(Long value) { this.result.remoteTimestamp = value; return this; }
				public Builder setReportsSent(Integer value) { this.result.reportsSent = value; return this; }
				public Builder setRoundTripTime(Double value) { this.result.roundTripTime = value; return this; }
				public Builder setTotalRoundTripTime(Double value) { this.result.totalRoundTripTime = value; return this; }
				public Builder setRoundTripTimeMeasurements(Integer value) { this.result.roundTripTimeMeasurements = value; return this; }
				public Builder setEnded(Boolean value) { this.result.ended = value; return this; }
				public Builder setPayloadType(Integer value) { this.result.payloadType = value; return this; }
				public Builder setMimeType(String value) { this.result.mimeType = value; return this; }
				public Builder setClockRate(Integer value) { this.result.clockRate = value; return this; }
				public Builder setChannels(Integer value) { this.result.channels = value; return this; }
				public Builder setSdpFmtpLine(String value) { this.result.sdpFmtpLine = value; return this; }
				public InboundVideoTrack build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class OutboundAudioTrack {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* The id of the track
			*/
			@JsonProperty("trackId")
			public UUID trackId;
			/**
			*  The unique generated identifier of the peer connection the inbound audio track belongs to
			*/
			@JsonProperty("peerConnectionId")
			public UUID peerConnectionId;
			/**
			* The id of the SFU stream this track is related to
			*/
			@JsonProperty("sfuStreamId")
			public UUID sfuStreamId;
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
			public Integer targetBitrate;
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
			public Integer perDscpPacketsSent;
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
		
				private OutboundAudioTrack result = new OutboundAudioTrack();
		
				public Builder setTrackId(UUID value) { this.result.trackId = value; return this; }
				public Builder setPeerConnectionId(UUID value) { this.result.peerConnectionId = value; return this; }
				public Builder setSfuStreamId(UUID value) { this.result.sfuStreamId = value; return this; }
				public Builder setSsrc(Long value) { this.result.ssrc = value; return this; }
				public Builder setPacketsSent(Integer value) { this.result.packetsSent = value; return this; }
				public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
				public Builder setRtxSsrc(Long value) { this.result.rtxSsrc = value; return this; }
				public Builder setRid(String value) { this.result.rid = value; return this; }
				public Builder setLastPacketSentTimestamp(Long value) { this.result.lastPacketSentTimestamp = value; return this; }
				public Builder setHeaderBytesSent(Long value) { this.result.headerBytesSent = value; return this; }
				public Builder setPacketsDiscardedOnSend(Integer value) { this.result.packetsDiscardedOnSend = value; return this; }
				public Builder setBytesDiscardedOnSend(Long value) { this.result.bytesDiscardedOnSend = value; return this; }
				public Builder setFecPacketsSent(Integer value) { this.result.fecPacketsSent = value; return this; }
				public Builder setRetransmittedPacketsSent(Integer value) { this.result.retransmittedPacketsSent = value; return this; }
				public Builder setRetransmittedBytesSent(Long value) { this.result.retransmittedBytesSent = value; return this; }
				public Builder setTargetBitrate(Integer value) { this.result.targetBitrate = value; return this; }
				public Builder setTotalEncodedBytesTarget(Long value) { this.result.totalEncodedBytesTarget = value; return this; }
				public Builder setTotalPacketSendDelay(Double value) { this.result.totalPacketSendDelay = value; return this; }
				public Builder setAverageRtcpInterval(Double value) { this.result.averageRtcpInterval = value; return this; }
				public Builder setPerDscpPacketsSent(Integer value) { this.result.perDscpPacketsSent = value; return this; }
				public Builder setNackCount(Integer value) { this.result.nackCount = value; return this; }
				public Builder setEncoderImplementation(String value) { this.result.encoderImplementation = value; return this; }
				public Builder setTotalSamplesSent(Integer value) { this.result.totalSamplesSent = value; return this; }
				public Builder setSamplesEncodedWithSilk(Integer value) { this.result.samplesEncodedWithSilk = value; return this; }
				public Builder setSamplesEncodedWithCelt(Integer value) { this.result.samplesEncodedWithCelt = value; return this; }
				public Builder setVoiceActivityFlag(Boolean value) { this.result.voiceActivityFlag = value; return this; }
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
				public Builder setRoundTripTime(Double value) { this.result.roundTripTime = value; return this; }
				public Builder setTotalRoundTripTime(Double value) { this.result.totalRoundTripTime = value; return this; }
				public Builder setFractionLost(Double value) { this.result.fractionLost = value; return this; }
				public Builder setReportsReceived(Integer value) { this.result.reportsReceived = value; return this; }
				public Builder setRoundTripTimeMeasurements(Integer value) { this.result.roundTripTimeMeasurements = value; return this; }
				public Builder setRelayedSource(Boolean value) { this.result.relayedSource = value; return this; }
				public Builder setAudioLevel(Double value) { this.result.audioLevel = value; return this; }
				public Builder setTotalAudioEnergy(Double value) { this.result.totalAudioEnergy = value; return this; }
				public Builder setTotalSamplesDuration(Double value) { this.result.totalSamplesDuration = value; return this; }
				public Builder setEchoReturnLoss(Double value) { this.result.echoReturnLoss = value; return this; }
				public Builder setEchoReturnLossEnhancement(Double value) { this.result.echoReturnLossEnhancement = value; return this; }
				public Builder setEnded(Boolean value) { this.result.ended = value; return this; }
				public Builder setPayloadType(Integer value) { this.result.payloadType = value; return this; }
				public Builder setMimeType(String value) { this.result.mimeType = value; return this; }
				public Builder setClockRate(Integer value) { this.result.clockRate = value; return this; }
				public Builder setChannels(Integer value) { this.result.channels = value; return this; }
				public Builder setSdpFmtpLine(String value) { this.result.sdpFmtpLine = value; return this; }
				public OutboundAudioTrack build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class OutboundVideoTrack {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* The id of the track
			*/
			@JsonProperty("trackId")
			public UUID trackId;
			/**
			*  The unique generated identifier of the peer connection the inbound audio track belongs to
			*/
			@JsonProperty("peerConnectionId")
			public UUID peerConnectionId;
			/**
			* The id of the SFU stream this track is related to
			*/
			@JsonProperty("sfuStreamId")
			public UUID sfuStreamId;
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
			public Integer targetBitrate;
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
			public Integer perDscpPacketsSent;
			/**
			* Count the total number of Negative ACKnowledgement (NACK) packets received over the corresponding synchronization source (ssrc)
			*/
			@JsonProperty("nackCount")
			public Integer nackCount;
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
			public Long qpSum;
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
			@JsonProperty("fullFramesLost")
			public Integer fullFramesLost;
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
		
				private OutboundVideoTrack result = new OutboundVideoTrack();
		
				public Builder setTrackId(UUID value) { this.result.trackId = value; return this; }
				public Builder setPeerConnectionId(UUID value) { this.result.peerConnectionId = value; return this; }
				public Builder setSfuStreamId(UUID value) { this.result.sfuStreamId = value; return this; }
				public Builder setSsrc(Long value) { this.result.ssrc = value; return this; }
				public Builder setPacketsSent(Integer value) { this.result.packetsSent = value; return this; }
				public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
				public Builder setRtxSsrc(Long value) { this.result.rtxSsrc = value; return this; }
				public Builder setRid(String value) { this.result.rid = value; return this; }
				public Builder setLastPacketSentTimestamp(Long value) { this.result.lastPacketSentTimestamp = value; return this; }
				public Builder setHeaderBytesSent(Long value) { this.result.headerBytesSent = value; return this; }
				public Builder setPacketsDiscardedOnSend(Integer value) { this.result.packetsDiscardedOnSend = value; return this; }
				public Builder setBytesDiscardedOnSend(Long value) { this.result.bytesDiscardedOnSend = value; return this; }
				public Builder setFecPacketsSent(Integer value) { this.result.fecPacketsSent = value; return this; }
				public Builder setRetransmittedPacketsSent(Integer value) { this.result.retransmittedPacketsSent = value; return this; }
				public Builder setRetransmittedBytesSent(Long value) { this.result.retransmittedBytesSent = value; return this; }
				public Builder setTargetBitrate(Integer value) { this.result.targetBitrate = value; return this; }
				public Builder setTotalEncodedBytesTarget(Long value) { this.result.totalEncodedBytesTarget = value; return this; }
				public Builder setTotalPacketSendDelay(Double value) { this.result.totalPacketSendDelay = value; return this; }
				public Builder setAverageRtcpInterval(Double value) { this.result.averageRtcpInterval = value; return this; }
				public Builder setPerDscpPacketsSent(Integer value) { this.result.perDscpPacketsSent = value; return this; }
				public Builder setNackCount(Integer value) { this.result.nackCount = value; return this; }
				public Builder setFirCount(Integer value) { this.result.firCount = value; return this; }
				public Builder setPliCount(Integer value) { this.result.pliCount = value; return this; }
				public Builder setSliCount(Integer value) { this.result.sliCount = value; return this; }
				public Builder setEncoderImplementation(String value) { this.result.encoderImplementation = value; return this; }
				public Builder setFrameWidth(Integer value) { this.result.frameWidth = value; return this; }
				public Builder setFrameHeight(Integer value) { this.result.frameHeight = value; return this; }
				public Builder setFrameBitDepth(Integer value) { this.result.frameBitDepth = value; return this; }
				public Builder setFramesPerSecond(Double value) { this.result.framesPerSecond = value; return this; }
				public Builder setFramesSent(Integer value) { this.result.framesSent = value; return this; }
				public Builder setHugeFramesSent(Integer value) { this.result.hugeFramesSent = value; return this; }
				public Builder setFramesEncoded(Integer value) { this.result.framesEncoded = value; return this; }
				public Builder setKeyFramesEncoded(Integer value) { this.result.keyFramesEncoded = value; return this; }
				public Builder setFramesDiscardedOnSend(Integer value) { this.result.framesDiscardedOnSend = value; return this; }
				public Builder setQpSum(Long value) { this.result.qpSum = value; return this; }
				public Builder setTotalEncodeTime(Double value) { this.result.totalEncodeTime = value; return this; }
				public Builder setQualityLimitationDurationNone(Double value) { this.result.qualityLimitationDurationNone = value; return this; }
				public Builder setQualityLimitationDurationCPU(Double value) { this.result.qualityLimitationDurationCPU = value; return this; }
				public Builder setQualityLimitationDurationBandwidth(Double value) { this.result.qualityLimitationDurationBandwidth = value; return this; }
				public Builder setQualityLimitationDurationOther(Double value) { this.result.qualityLimitationDurationOther = value; return this; }
				public Builder setQualityLimitationReason(String value) { this.result.qualityLimitationReason = value; return this; }
				public Builder setQualityLimitationResolutionChanges(Integer value) { this.result.qualityLimitationResolutionChanges = value; return this; }
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
				public Builder setRoundTripTime(Double value) { this.result.roundTripTime = value; return this; }
				public Builder setTotalRoundTripTime(Double value) { this.result.totalRoundTripTime = value; return this; }
				public Builder setFractionLost(Double value) { this.result.fractionLost = value; return this; }
				public Builder setReportsReceived(Integer value) { this.result.reportsReceived = value; return this; }
				public Builder setRoundTripTimeMeasurements(Integer value) { this.result.roundTripTimeMeasurements = value; return this; }
				public Builder setFramesDropped(Integer value) { this.result.framesDropped = value; return this; }
				public Builder setPartialFramesLost(Integer value) { this.result.partialFramesLost = value; return this; }
				public Builder setFullFramesLost(Integer value) { this.result.fullFramesLost = value; return this; }
				public Builder setRelayedSource(Boolean value) { this.result.relayedSource = value; return this; }
				public Builder setWidth(Integer value) { this.result.width = value; return this; }
				public Builder setHeight(Integer value) { this.result.height = value; return this; }
				public Builder setBitDepth(Integer value) { this.result.bitDepth = value; return this; }
				public Builder setFrames(Integer value) { this.result.frames = value; return this; }
				public Builder setEnded(Boolean value) { this.result.ended = value; return this; }
				public Builder setPayloadType(Integer value) { this.result.payloadType = value; return this; }
				public Builder setMimeType(String value) { this.result.mimeType = value; return this; }
				public Builder setClockRate(Integer value) { this.result.clockRate = value; return this; }
				public Builder setChannels(Integer value) { this.result.channels = value; return this; }
				public Builder setSdpFmtpLine(String value) { this.result.sdpFmtpLine = value; return this; }
				public OutboundVideoTrack build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class IceLocalCandidate {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* Refers to the peer connection the local candidate belongs to
			*/
			@JsonProperty("peerConnectionId")
			public UUID peerConnectionId;
			/**
			* The unique identifier of the local candidate
			*/
			@JsonProperty("id")
			public String id;
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
		

			public static class Builder {
		
				private IceLocalCandidate result = new IceLocalCandidate();
		
				public Builder setPeerConnectionId(UUID value) { this.result.peerConnectionId = value; return this; }
				public Builder setId(String value) { this.result.id = value; return this; }
				public Builder setAddress(String value) { this.result.address = value; return this; }
				public Builder setPort(Integer value) { this.result.port = value; return this; }
				public Builder setProtocol(String value) { this.result.protocol = value; return this; }
				public Builder setCandidateType(String value) { this.result.candidateType = value; return this; }
				public Builder setPriority(Long value) { this.result.priority = value; return this; }
				public Builder setUrl(String value) { this.result.url = value; return this; }
				public Builder setRelayProtocol(String value) { this.result.relayProtocol = value; return this; }
				public IceLocalCandidate build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class IceRemoteCandidate {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* Refers to the peer connection the local candidate belongs to
			*/
			@JsonProperty("peerConnectionId")
			public UUID peerConnectionId;
			/**
			* The unique identifier of the local candidate
			*/
			@JsonProperty("id")
			public String id;
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
		

			public static class Builder {
		
				private IceRemoteCandidate result = new IceRemoteCandidate();
		
				public Builder setPeerConnectionId(UUID value) { this.result.peerConnectionId = value; return this; }
				public Builder setId(String value) { this.result.id = value; return this; }
				public Builder setAddress(String value) { this.result.address = value; return this; }
				public Builder setPort(Integer value) { this.result.port = value; return this; }
				public Builder setProtocol(String value) { this.result.protocol = value; return this; }
				public Builder setCandidateType(String value) { this.result.candidateType = value; return this; }
				public Builder setPriority(Long value) { this.result.priority = value; return this; }
				public Builder setUrl(String value) { this.result.url = value; return this; }
				public Builder setRelayProtocol(String value) { this.result.relayProtocol = value; return this; }
				public IceRemoteCandidate build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class DataChannel {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* Refers to the peer connection the local candidate belongs to
			*/
			@JsonProperty("peerConnectionId")
			public UUID peerConnectionId;
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
		

			public static class Builder {
		
				private DataChannel result = new DataChannel();
		
				public Builder setPeerConnectionId(UUID value) { this.result.peerConnectionId = value; return this; }
				public Builder setId(String value) { this.result.id = value; return this; }
				public Builder setLabel(String value) { this.result.label = value; return this; }
				public Builder setAddress(String value) { this.result.address = value; return this; }
				public Builder setPort(Integer value) { this.result.port = value; return this; }
				public Builder setProtocol(String value) { this.result.protocol = value; return this; }
				public Builder setDataChannelIdentifier(Integer value) { this.result.dataChannelIdentifier = value; return this; }
				public Builder setState(String value) { this.result.state = value; return this; }
				public Builder setMessagesSent(Integer value) { this.result.messagesSent = value; return this; }
				public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
				public Builder setMessagesReceived(Integer value) { this.result.messagesReceived = value; return this; }
				public Builder setBytesReceived(Long value) { this.result.bytesReceived = value; return this; }
				public DataChannel build() {
					return this.result;
				}
			}
		}
		/**
		* If it is provided the server uses the given id to match clients in the same call. Must be a valid UUID. 
		*/
		@JsonProperty("callId")
		public UUID callId;
		/**
		* Unique id of the client providing samples. Must be a valid UUID
		*/
		@JsonProperty("clientId")
		public UUID clientId;
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
	

		public static class Builder {
	
			private ClientSample result = new ClientSample();
	
			public Builder setCallId(UUID value) { this.result.callId = value; return this; }
			public Builder setClientId(UUID value) { this.result.clientId = value; return this; }
			public Builder setSampleSeq(Integer value) { this.result.sampleSeq = value; return this; }
			public Builder setRoomId(String value) { this.result.roomId = value; return this; }
			public Builder setUserId(String value) { this.result.userId = value; return this; }
			public Builder setEngine(Engine value) { this.result.engine = value; return this; }
			public Builder setPlatform(Platform value) { this.result.platform = value; return this; }
			public Builder setBrowser(Browser value) { this.result.browser = value; return this; }
			public Builder setOs(OperationSystem value) { this.result.os = value; return this; }
			public Builder setMediaConstraints(String[] value) { this.result.mediaConstraints = value; return this; }
			public Builder setMediaDevices(MediaDevice[] value) { this.result.mediaDevices = value; return this; }
			public Builder setUserMediaErrors(String[] value) { this.result.userMediaErrors = value; return this; }
			public Builder setExtensionStats(ExtensionStat[] value) { this.result.extensionStats = value; return this; }
			public Builder setIceServers(String[] value) { this.result.iceServers = value; return this; }
			public Builder setPcTransports(PeerConnectionTransport[] value) { this.result.pcTransports = value; return this; }
			public Builder setMediaSources(MediaSourceStat[] value) { this.result.mediaSources = value; return this; }
			public Builder setCodecs(MediaCodecStats[] value) { this.result.codecs = value; return this; }
			public Builder setCertificates(Certificate[] value) { this.result.certificates = value; return this; }
			public Builder setInboundAudioTracks(InboundAudioTrack[] value) { this.result.inboundAudioTracks = value; return this; }
			public Builder setInboundVideoTracks(InboundVideoTrack[] value) { this.result.inboundVideoTracks = value; return this; }
			public Builder setOutboundAudioTracks(OutboundAudioTrack[] value) { this.result.outboundAudioTracks = value; return this; }
			public Builder setOutboundVideoTracks(OutboundVideoTrack[] value) { this.result.outboundVideoTracks = value; return this; }
			public Builder setIceLocalCandidates(IceLocalCandidate[] value) { this.result.iceLocalCandidates = value; return this; }
			public Builder setIceRemoteCandidates(IceRemoteCandidate[] value) { this.result.iceRemoteCandidates = value; return this; }
			public Builder setDataChannels(DataChannel[] value) { this.result.dataChannels = value; return this; }
			public Builder setTimestamp(Long value) { this.result.timestamp = value; return this; }
			public Builder setTimeZoneOffsetInHours(Integer value) { this.result.timeZoneOffsetInHours = value; return this; }
			public Builder setMarker(String value) { this.result.marker = value; return this; }
			public ClientSample build() {
				return this.result;
			}
		}
	}
	/**
	* docs
	*/
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SfuSample {
		public static Builder newBuilder() {
			return new Builder();
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuTransport {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* Flag indicate to not generate report from this sample
			*/
			@JsonProperty("noReport")
			public Boolean noReport;
			/**
			* The generated unique identifier of the transport
			*/
			@JsonProperty("transportId")
			public UUID transportId;
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
		

			public static class Builder {
		
				private SfuTransport result = new SfuTransport();
		
				public Builder setNoReport(Boolean value) { this.result.noReport = value; return this; }
				public Builder setTransportId(UUID value) { this.result.transportId = value; return this; }
				public Builder setInternal(Boolean value) { this.result.internal = value; return this; }
				public Builder setDtlsState(String value) { this.result.dtlsState = value; return this; }
				public Builder setIceState(String value) { this.result.iceState = value; return this; }
				public Builder setSctpState(String value) { this.result.sctpState = value; return this; }
				public Builder setIceRole(String value) { this.result.iceRole = value; return this; }
				public Builder setLocalAddress(String value) { this.result.localAddress = value; return this; }
				public Builder setLocalPort(Integer value) { this.result.localPort = value; return this; }
				public Builder setProtocol(String value) { this.result.protocol = value; return this; }
				public Builder setRemoteAddress(String value) { this.result.remoteAddress = value; return this; }
				public Builder setRemotePort(Integer value) { this.result.remotePort = value; return this; }
				public Builder setRtpBytesReceived(Long value) { this.result.rtpBytesReceived = value; return this; }
				public Builder setRtpBytesSent(Long value) { this.result.rtpBytesSent = value; return this; }
				public Builder setRtpPacketsReceived(Integer value) { this.result.rtpPacketsReceived = value; return this; }
				public Builder setRtpPacketsSent(Integer value) { this.result.rtpPacketsSent = value; return this; }
				public Builder setRtpPacketsLost(Integer value) { this.result.rtpPacketsLost = value; return this; }
				public Builder setRtxBytesReceived(Long value) { this.result.rtxBytesReceived = value; return this; }
				public Builder setRtxBytesSent(Long value) { this.result.rtxBytesSent = value; return this; }
				public Builder setRtxPacketsReceived(Integer value) { this.result.rtxPacketsReceived = value; return this; }
				public Builder setRtxPacketsSent(Integer value) { this.result.rtxPacketsSent = value; return this; }
				public Builder setRtxPacketsLost(Integer value) { this.result.rtxPacketsLost = value; return this; }
				public Builder setRtxPacketsDiscarded(Integer value) { this.result.rtxPacketsDiscarded = value; return this; }
				public Builder setSctpBytesReceived(Long value) { this.result.sctpBytesReceived = value; return this; }
				public Builder setSctpBytesSent(Long value) { this.result.sctpBytesSent = value; return this; }
				public Builder setSctpPacketsReceived(Integer value) { this.result.sctpPacketsReceived = value; return this; }
				public Builder setSctpPacketsSent(Integer value) { this.result.sctpPacketsSent = value; return this; }
				public SfuTransport build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuInboundRtpPad {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* Flag indicate to not generate report from this sample
			*/
			@JsonProperty("noReport")
			public Boolean noReport;
			/**
			* The id of the transport the RTP Pad uses.
			*/
			@JsonProperty("transportId")
			public UUID transportId;
			/**
			* Flag to indicate that the rtp pad is used as an internal communication between SFU instances
			*/
			@JsonProperty("internal")
			public Boolean internal;
			/**
			* The id of the media stream the RTP pad belongs to. This id is to group rtp pads (e.g.: simulcast) carrying payloads to the same media. 
			*/
			@JsonProperty("streamId")
			public UUID streamId;
			/**
			* The id of Sfu pad.
			*/
			@JsonProperty("padId")
			public UUID padId;
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
		
				private SfuInboundRtpPad result = new SfuInboundRtpPad();
		
				public Builder setNoReport(Boolean value) { this.result.noReport = value; return this; }
				public Builder setTransportId(UUID value) { this.result.transportId = value; return this; }
				public Builder setInternal(Boolean value) { this.result.internal = value; return this; }
				public Builder setStreamId(UUID value) { this.result.streamId = value; return this; }
				public Builder setPadId(UUID value) { this.result.padId = value; return this; }
				public Builder setSsrc(Long value) { this.result.ssrc = value; return this; }
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
				public Builder setPacketsReceived(Integer value) { this.result.packetsReceived = value; return this; }
				public Builder setPacketsDiscarded(Integer value) { this.result.packetsDiscarded = value; return this; }
				public Builder setPacketsRepaired(Integer value) { this.result.packetsRepaired = value; return this; }
				public Builder setPacketsFailedDecryption(Integer value) { this.result.packetsFailedDecryption = value; return this; }
				public Builder setPacketsDuplicated(Integer value) { this.result.packetsDuplicated = value; return this; }
				public Builder setFecPacketsReceived(Integer value) { this.result.fecPacketsReceived = value; return this; }
				public Builder setFecPacketsDiscarded(Integer value) { this.result.fecPacketsDiscarded = value; return this; }
				public Builder setBytesReceived(Long value) { this.result.bytesReceived = value; return this; }
				public Builder setRtcpSrReceived(Integer value) { this.result.rtcpSrReceived = value; return this; }
				public Builder setRtcpRrSent(Integer value) { this.result.rtcpRrSent = value; return this; }
				public Builder setRtxPacketsReceived(Integer value) { this.result.rtxPacketsReceived = value; return this; }
				public Builder setRtxPacketsDiscarded(Integer value) { this.result.rtxPacketsDiscarded = value; return this; }
				public Builder setFramesReceived(Integer value) { this.result.framesReceived = value; return this; }
				public Builder setFramesDecoded(Integer value) { this.result.framesDecoded = value; return this; }
				public Builder setKeyFramesDecoded(Integer value) { this.result.keyFramesDecoded = value; return this; }
				public Builder setFractionLost(Double value) { this.result.fractionLost = value; return this; }
				public Builder setJitter(Double value) { this.result.jitter = value; return this; }
				public Builder setRoundTripTime(Double value) { this.result.roundTripTime = value; return this; }
				public SfuInboundRtpPad build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuOutboundRtpPad {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* Flag indicate to not generate report from this sample
			*/
			@JsonProperty("noReport")
			public Boolean noReport;
			/**
			* The id of the transport the RTP stream uses.
			*/
			@JsonProperty("transportId")
			public UUID transportId;
			/**
			* Flag to indicate that the rtp pad is used as an internal communication between SFU instances
			*/
			@JsonProperty("internal")
			public Boolean internal;
			/**
			* The id of the stream this outbound RTP pad sinks the media from
			*/
			@JsonProperty("streamId")
			public UUID streamId;
			/**
			* The id of a group of RTP pad sinks the media stream out from the SFU.
			*/
			@JsonProperty("sinkId")
			public UUID sinkId;
			/**
			* The id of Sfu pad.
			*/
			@JsonProperty("padId")
			public UUID padId;
			/**
			* The synchronization source id of the RTP stream
			*/
			@JsonProperty("ssrc")
			public Long ssrc;
			/**
			* The callId the event belongs to
			*/
			@JsonProperty("callId")
			public UUID callId;
			/**
			* If the track id was provided by the Sfu, the observer can fill up the information of which client it belongs to
			*/
			@JsonProperty("clientId")
			public UUID clientId;
			/**
			* The id of the track the RTP stream related to at the client side
			*/
			@JsonProperty("trackId")
			public UUID trackId;
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
		
				private SfuOutboundRtpPad result = new SfuOutboundRtpPad();
		
				public Builder setNoReport(Boolean value) { this.result.noReport = value; return this; }
				public Builder setTransportId(UUID value) { this.result.transportId = value; return this; }
				public Builder setInternal(Boolean value) { this.result.internal = value; return this; }
				public Builder setStreamId(UUID value) { this.result.streamId = value; return this; }
				public Builder setSinkId(UUID value) { this.result.sinkId = value; return this; }
				public Builder setPadId(UUID value) { this.result.padId = value; return this; }
				public Builder setSsrc(Long value) { this.result.ssrc = value; return this; }
				public Builder setCallId(UUID value) { this.result.callId = value; return this; }
				public Builder setClientId(UUID value) { this.result.clientId = value; return this; }
				public Builder setTrackId(UUID value) { this.result.trackId = value; return this; }
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
				public Builder setFractionLost(Double value) { this.result.fractionLost = value; return this; }
				public Builder setJitter(Double value) { this.result.jitter = value; return this; }
				public Builder setRoundTripTime(Double value) { this.result.roundTripTime = value; return this; }
				public SfuOutboundRtpPad build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuSctpChannel {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* Flag indicate to not generate report from this sample
			*/
			@JsonProperty("noReport")
			public Boolean noReport;
			/**
			* The id of the transport the RTP stream uses.
			*/
			@JsonProperty("transportId")
			public UUID transportId;
			/**
			* The id of the sctp stream
			*/
			@JsonProperty("streamId")
			public UUID streamId;
			/**
			* The id of the sctp stream
			*/
			@JsonProperty("channelId")
			public UUID channelId;
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
		
				private SfuSctpChannel result = new SfuSctpChannel();
		
				public Builder setNoReport(Boolean value) { this.result.noReport = value; return this; }
				public Builder setTransportId(UUID value) { this.result.transportId = value; return this; }
				public Builder setStreamId(UUID value) { this.result.streamId = value; return this; }
				public Builder setChannelId(UUID value) { this.result.channelId = value; return this; }
				public Builder setLabel(String value) { this.result.label = value; return this; }
				public Builder setProtocol(String value) { this.result.protocol = value; return this; }
				public Builder setSctpSmoothedRoundTripTime(Double value) { this.result.sctpSmoothedRoundTripTime = value; return this; }
				public Builder setSctpCongestionWindow(Double value) { this.result.sctpCongestionWindow = value; return this; }
				public Builder setSctpReceiverWindow(Double value) { this.result.sctpReceiverWindow = value; return this; }
				public Builder setSctpMtu(Integer value) { this.result.sctpMtu = value; return this; }
				public Builder setSctpUnackData(Integer value) { this.result.sctpUnackData = value; return this; }
				public Builder setMessageReceived(Integer value) { this.result.messageReceived = value; return this; }
				public Builder setMessageSent(Integer value) { this.result.messageSent = value; return this; }
				public Builder setBytesReceived(Long value) { this.result.bytesReceived = value; return this; }
				public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
				public SfuSctpChannel build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class SfuExtensionStats {
			public static Builder newBuilder() {
				return new Builder();
			}
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
		

			public static class Builder {
		
				private SfuExtensionStats result = new SfuExtensionStats();
		
				public Builder setType(String value) { this.result.type = value; return this; }
				public Builder setPayload(String value) { this.result.payload = value; return this; }
				public SfuExtensionStats build() {
					return this.result;
				}
			}
		}
		/**
		* Unique generated id for the sfu samples are originated from
		*/
		@JsonProperty("sfuId")
		public UUID sfuId;
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
	

		public static class Builder {
	
			private SfuSample result = new SfuSample();
	
			public Builder setSfuId(UUID value) { this.result.sfuId = value; return this; }
			public Builder setTimestamp(Long value) { this.result.timestamp = value; return this; }
			public Builder setTimeZoneOffsetInHours(Integer value) { this.result.timeZoneOffsetInHours = value; return this; }
			public Builder setMarker(String value) { this.result.marker = value; return this; }
			public Builder setTransports(SfuTransport[] value) { this.result.transports = value; return this; }
			public Builder setInboundRtpPads(SfuInboundRtpPad[] value) { this.result.inboundRtpPads = value; return this; }
			public Builder setOutboundRtpPads(SfuOutboundRtpPad[] value) { this.result.outboundRtpPads = value; return this; }
			public Builder setSctpChannels(SfuSctpChannel[] value) { this.result.sctpChannels = value; return this; }
			public Builder setExtensionStats(SfuExtensionStats[] value) { this.result.extensionStats = value; return this; }
			public SfuSample build() {
				return this.result;
			}
		}
	}
	/**
	* docs
	*/
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TurnSample {
		public static Builder newBuilder() {
			return new Builder();
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class TurnPeerAllocation {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* a unique id for the allocation
			*/
			@JsonProperty("peerId")
			public String peerId;
			/**
			* The corresponded session the allocation belongs to
			*/
			@JsonProperty("sessionId")
			public String sessionId;
			/**
			* The allocated address
			*/
			@JsonProperty("relayedAddress")
			public String relayedAddress;
			/**
			* The allocated port
			*/
			@JsonProperty("relayedPort")
			public Integer relayedPort;
			/**
			* protocol (TCP, UDP)
			*/
			@JsonProperty("transportProtocol")
			public String transportProtocol;
			/**
			* The address of the address the serever connect to
			*/
			@JsonProperty("peerAddress")
			public String peerAddress;
			/**
			* The portnumber the server connects to
			*/
			@JsonProperty("peerPort")
			public Integer peerPort;
			/**
			* the bitrate the TURN server sending to the peer
			*/
			@JsonProperty("sendingBitrate")
			public Integer sendingBitrate;
			/**
			* the bitrate the TURN server receiving from the peer
			*/
			@JsonProperty("receivingBitrate")
			public Integer receivingBitrate;
			/**
			* the amount of bytes sent to the peer
			*/
			@JsonProperty("sentBytes")
			public Long sentBytes;
			/**
			* the amount of bytes received from the peer
			*/
			@JsonProperty("receivedBytes")
			public Long receivedBytes;
			/**
			* the amount of packets sent to the peer
			*/
			@JsonProperty("sentPackets")
			public Integer sentPackets;
			/**
			* the amount of packets received from the peer
			*/
			@JsonProperty("receivedPackets")
			public Integer receivedPackets;
		

			public static class Builder {
		
				private TurnPeerAllocation result = new TurnPeerAllocation();
		
				public Builder setPeerId(String value) { this.result.peerId = value; return this; }
				public Builder setSessionId(String value) { this.result.sessionId = value; return this; }
				public Builder setRelayedAddress(String value) { this.result.relayedAddress = value; return this; }
				public Builder setRelayedPort(Integer value) { this.result.relayedPort = value; return this; }
				public Builder setTransportProtocol(String value) { this.result.transportProtocol = value; return this; }
				public Builder setPeerAddress(String value) { this.result.peerAddress = value; return this; }
				public Builder setPeerPort(Integer value) { this.result.peerPort = value; return this; }
				public Builder setSendingBitrate(Integer value) { this.result.sendingBitrate = value; return this; }
				public Builder setReceivingBitrate(Integer value) { this.result.receivingBitrate = value; return this; }
				public Builder setSentBytes(Long value) { this.result.sentBytes = value; return this; }
				public Builder setReceivedBytes(Long value) { this.result.receivedBytes = value; return this; }
				public Builder setSentPackets(Integer value) { this.result.sentPackets = value; return this; }
				public Builder setReceivedPackets(Integer value) { this.result.receivedPackets = value; return this; }
				public TurnPeerAllocation build() {
					return this.result;
				}
			}
		}
		/**
		* undefined
		*/
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class TurnSession {
			public static Builder newBuilder() {
				return new Builder();
			}
			/**
			* Flag indicate to not generate report from this sample
			*/
			@JsonProperty("sessionId")
			public String sessionId;
			/**
			* The Authentication Realm (RFC 8656)
			*/
			@JsonProperty("realm")
			public String realm;
			/**
			* The username of the used in authentication
			*/
			@JsonProperty("username")
			public String username;
			/**
			* The id of the client the TURN session belongs to (ClientSample)
			*/
			@JsonProperty("clientId")
			public UUID clientId;
			/**
			* The timestamp when the session has been started. Epoch in milliseconds, GMT
			*/
			@JsonProperty("started")
			public Long started;
			/**
			* For each Allocate request, the server SHOULD generate a new random nonce when the allocation is first attempted following the randomness recommendations in [RFC4086] and SHOULD expire the nonce at least once every hour during the lifetime of the allocation.  Epoch in millis GMT
			*/
			@JsonProperty("nonceExpirationTime")
			public Long nonceExpirationTime;
			/**
			* The address of the server the client connected to
			*/
			@JsonProperty("serverAddress")
			public String serverAddress;
			/**
			* The portnumber the server listens the client requests
			*/
			@JsonProperty("serverPort")
			public Integer serverPort;
			/**
			* the transport protocol betwwen the client and the server (TCP, UDP, TCPTLS, UDPTLS, SCTP, SCTPTLS)
			*/
			@JsonProperty("transportProtocol")
			public String transportProtocol;
			/**
			* The address of the client connected from
			*/
			@JsonProperty("clientAddress")
			public String clientAddress;
			/**
			* The portnumber the client requested from
			*/
			@JsonProperty("clientPort")
			public Integer clientPort;
			/**
			* the bitrate the TURN server sending to the client
			*/
			@JsonProperty("sendingBitrate")
			public Integer sendingBitrate;
			/**
			* the bitrate the TURN server receiving from the client
			*/
			@JsonProperty("receivingBitrate")
			public Integer receivingBitrate;
			/**
			* the amount of bytes sent to the client
			*/
			@JsonProperty("sentBytes")
			public Long sentBytes;
			/**
			* the amount of bytes received from the client
			*/
			@JsonProperty("receivedBytes")
			public Long receivedBytes;
			/**
			* the amount of packets sent to the client
			*/
			@JsonProperty("sentPackets")
			public Integer sentPackets;
			/**
			* the amount of packets received from the client
			*/
			@JsonProperty("receivedPackets")
			public Integer receivedPackets;
		

			public static class Builder {
		
				private TurnSession result = new TurnSession();
		
				public Builder setSessionId(String value) { this.result.sessionId = value; return this; }
				public Builder setRealm(String value) { this.result.realm = value; return this; }
				public Builder setUsername(String value) { this.result.username = value; return this; }
				public Builder setClientId(UUID value) { this.result.clientId = value; return this; }
				public Builder setStarted(Long value) { this.result.started = value; return this; }
				public Builder setNonceExpirationTime(Long value) { this.result.nonceExpirationTime = value; return this; }
				public Builder setServerAddress(String value) { this.result.serverAddress = value; return this; }
				public Builder setServerPort(Integer value) { this.result.serverPort = value; return this; }
				public Builder setTransportProtocol(String value) { this.result.transportProtocol = value; return this; }
				public Builder setClientAddress(String value) { this.result.clientAddress = value; return this; }
				public Builder setClientPort(Integer value) { this.result.clientPort = value; return this; }
				public Builder setSendingBitrate(Integer value) { this.result.sendingBitrate = value; return this; }
				public Builder setReceivingBitrate(Integer value) { this.result.receivingBitrate = value; return this; }
				public Builder setSentBytes(Long value) { this.result.sentBytes = value; return this; }
				public Builder setReceivedBytes(Long value) { this.result.receivedBytes = value; return this; }
				public Builder setSentPackets(Integer value) { this.result.sentPackets = value; return this; }
				public Builder setReceivedPackets(Integer value) { this.result.receivedPackets = value; return this; }
				public TurnSession build() {
					return this.result;
				}
			}
		}
		/**
		* A unique id of the turn server
		*/
		@JsonProperty("serverId")
		public String serverId;
		/**
		* Peer Alloocation data
		*/
		@JsonProperty("allocations")
		public TurnPeerAllocation[] allocations;
		/**
		* Session data
		*/
		@JsonProperty("sessions")
		public TurnSession[] sessions;
	

		public static class Builder {
	
			private TurnSample result = new TurnSample();
	
			public Builder setServerId(String value) { this.result.serverId = value; return this; }
			public Builder setAllocations(TurnPeerAllocation[] value) { this.result.allocations = value; return this; }
			public Builder setSessions(TurnSession[] value) { this.result.sessions = value; return this; }
			public TurnSample build() {
				return this.result;
			}
		}
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
	/**
	* Samples taken from the TURN server
	*/
	@JsonProperty("turnSamples")
	public TurnSample[] turnSamples;


	public static class Builder {

		private Samples result = new Samples();

		public Builder setMeta(SamplesMeta value) { this.result.meta = value; return this; }
		public Builder setControlFlags(ControlFlags value) { this.result.controlFlags = value; return this; }
		public Builder setClientSamples(ClientSample[] value) { this.result.clientSamples = value; return this; }
		public Builder setSfuSamples(SfuSample[] value) { this.result.sfuSamples = value; return this; }
		public Builder setTurnSamples(TurnSample[] value) { this.result.turnSamples = value; return this; }
		public Samples build() {
			return this.result;
		}
	}
}