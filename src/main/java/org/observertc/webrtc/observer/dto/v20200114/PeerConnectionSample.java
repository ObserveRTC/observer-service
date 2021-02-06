/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.observer.dto.v20200114;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.IOException;

/**
 * A full sample a peer connection provides to the Observer
 */
//@Schema(requiredProperties = {"peerConnectionId"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PeerConnectionSample {

	@JsonProperty("browserId")
	public String browserId;

	@JsonProperty("iceStats")
	public ICEStats iceStats;

	@JsonProperty("peerConnectionId")
	public String peerConnectionId;

	@JsonProperty("receiverStats")
	public RTCStats receiverStats;

	@JsonProperty("senderStats")
	public RTCStats senderStats;

	@JsonProperty("timeZoneOffsetInMinute")
	public Long timeZoneOffsetInMinute;

	@JsonProperty("timestamp")
	public Long timestamp;

	@JsonProperty("callId")
	public String callId;

	@JsonProperty("userId")
	public String userId;

	@JsonProperty("extensions")
	public ExtensionStat[] extensionStats;

	@JsonProperty("userMediaErrors")
	public UserMediaError[] userMediaErrors;

	@JsonProperty("deviceList")
	public MediaDeviceInfo[] deviceList;

	@JsonProperty("clientDetails")
	public ClientDetails clientDetails;

	public static class UserMediaError {
		@JsonProperty("message")
		public String message;
	}

	public static class ExtensionStat {

		@JsonProperty("extensionType")
		public String extensionType;

		@JsonProperty("payload")
		public String payload;
	}

	public static class MediaDeviceInfo {
		@JsonProperty("deviceId")
		public String deviceId;

		@JsonProperty("groupId")
		public String groupId;

		@JsonProperty("kind")
		public MediaDeviceKind kind;

		@JsonProperty("label")
		public String label;
	}


	/**
	 * https://www.w3.org/TR/webrtc/#dom-rtcstats
	 */
	public static class RTCStats {
		@JsonProperty("inboundRTPStats")
		public InboundRTPStreamStats[] inboundRTPStats;

		@JsonProperty("mediaSources")
		public MediaSourceStats[] mediaSources;

		@JsonProperty("outboundRTPStats")
		public OutboundRTPStreamStats[] outboundRTPStats;

		@JsonProperty("remoteInboundRTPStats")
		public RemoteInboundRTPStreamStats[] remoteInboundRTPStats;

		@JsonProperty("tracks")
		public RTCTrackStats[] tracks;
	}

	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-track, This is absolutely confusing
	 * what should the track contains.
	 */
	public static class RTCTrackStats {
		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-concealedsamples
		 */
		@JsonProperty("concealedSamples")
		public Integer concealedSamples;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-concealmentevents
		 */
		@JsonProperty("concealmentEvents")
		public Integer concealmentEvents;

		/**
		 * Indicates if the track is detached from the source or not
		 */
		@JsonProperty("detached")
		public Boolean detached;

		/**
		 * Indicates if the source ended the transmission for this track
		 */
		@JsonProperty("ended")
		public Boolean ended;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-framesdecoded
		 */
		@JsonProperty("framesDecoded")
		public Integer framesDecoded;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-framesdropped
		 */
		@JsonProperty("framesDropped")
		public Integer framesDropped;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-framesreceived
		 */
		@JsonProperty("framesReceived")
		public Integer framesReceived;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-hugeframessent
		 */
		@JsonProperty("hugeFramesSent")
		public Integer hugeFramesSent;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-trackidentifier
		 */
		@JsonProperty("id")
		public String id;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-insertedsamplesfordeceleration
		 */
		@JsonProperty("insertedSamplesForDeceleration")
		public Integer insertedSamplesForDeceleration;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcvideoreceiverstats-jitterbufferdelay
		 */
		@JsonProperty("jitterBufferDelay")
		public Double jitterBufferDelay;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcvideoreceiverstats-jitterbufferemittedcount
		 */
		@JsonProperty("jitterBufferEmittedCount")
		public Integer jitterBufferEmittedCount;

		/**
		 * The id of the media source the track is attached to
		 */
		@JsonProperty("mediaSourceId")
		public String mediaSourceId;

		@JsonProperty("mediaType")
		public RTCStreamMediaType mediaType;

		/**
		 * Indicates if the track attached to a remote source or not
		 */
		@JsonProperty("remoteSource")
		public Boolean remoteSource;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-removedsamplesforacceleration
		 */
		@JsonProperty("removedSamplesForAcceleration")
		public Integer removedSamplesForAcceleration;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-totalsamplesduration
		 */
		@JsonProperty("samplesDuration")
		public Double samplesDuration;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-silentconcealedsamples
		 */
		@JsonProperty("silentConcealedSamples")
		public Integer silentConcealedSamples;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-totalsamplesreceived
		 */
		@JsonProperty("totalSamplesReceived")
		public Integer totalSamplesReceived;

	}

	public static class RemoteInboundRTPStreamStats {

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-codecid
		 */
		@JsonProperty("codecId")
		public String codecId;

		/**
		 * The id of the InboundRTP the client use in its dictionary
		 */
		@JsonProperty("id")
		public String id;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-jitter
		 */
		@JsonProperty("jitter")
		public Double jitter;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcremoteinboundrtpstreamstats-localid
		 */
		@JsonProperty("localId")
		public String localId;


		@JsonProperty("mediaType")
		public RTCStreamMediaType mediaType;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-packetslost
		 */
		@JsonProperty("packetsLost")
		public Integer packetsLost;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcremoteinboundrtpstreamstats-roundtriptime
		 */
		@JsonProperty("roundTripTime")
		public Double roundTripTime;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-ssrc
		 */
		@JsonProperty("ssrc")
		public Long ssrc;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid
		 */
		@JsonProperty("transportId")
		public String transportId;
	}

	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcremoteoutboundrtpstreamstats
	 */
	public static class OutboundRTPStreamStats {

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtctransportstats-bytessent
		 */
		@JsonProperty("bytesSent")
		public Long bytesSent;

		@JsonProperty("codecId")
		public String codecId;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-encoderimplementation
		 */
		@JsonProperty("encoderImplementation")
		public String encoderImplementation;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-fircount
		 */
		@JsonProperty("firCount")
		public Integer firCount;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-framesencoded
		 */
		@JsonProperty("framesEncoded")
		public Integer framesEncoded;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-headerbytessent
		 */
		@JsonProperty("headerBytesSent")
		public Long headerBytesSent;

		/**
		 * Indicates if the report belongs to a remote participant
		 */
		@JsonProperty("id")
		public String id;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-keyframesencoded
		 */
		@JsonProperty("isRemote")
		public Boolean isRemote;

		@JsonProperty("keyFramesEncoded")
		public Long keyFramesEncoded;

		@JsonProperty("mediaSourceId")
		public String mediaSourceId;

		@JsonProperty("mediaType")
		public RTCStreamMediaType mediaType;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-nackcount
		 */
		@JsonProperty("nackCount")
		public Integer nackCount;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtctransportstats-packetssent
		 */
		@JsonProperty("packetsSent")
		public Integer packetsSent;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-plicount
		 */
		@JsonProperty("pliCount")
		public Integer pliCount;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-qpsum
		 */
		@JsonProperty("qpSum")
		public Double qpSum;

		@JsonProperty("qualityLimitationReason")
		public RTCQualityLimitationReason qualityLimitationReason;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-qualitylimitationresolutionchanges
		 */
		@JsonProperty("qualityLimitationResolutionChanges")
		public Long qualityLimitationResolutionChanges;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-remoteid
		 */
		@JsonProperty("remoteId")
		public String remoteId;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-retransmittedbytessent
		 */
		@JsonProperty("retransmittedBytesSent")
		public Long retransmittedBytesSent;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-retransmittedpacketssent
		 */
		@JsonProperty("retransmittedPacketsSent")
		public Integer retransmittedPacketsSent;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-ssrc
		 */
		@JsonProperty("ssrc")
		public long ssrc;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-totalencodedbytestarget
		 */
		@JsonProperty("totalEncodedBytesTarget")
		public Long totalEncodedBytesTarget;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-totalencodetime
		 */
		@JsonProperty("totalEncodeTime")
		public Double totalEncodeTime;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-totalpacketsenddelay
		 */
		@JsonProperty("totalPacketSendDelay")
		public Double totalPacketSendDelay;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-trackidentifier
		 */
		@JsonProperty("trackId")
		public String trackId;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid
		 */
		@JsonProperty("transportId")
		public String transportId;
	}


	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-media-source
	 */
	public static class MediaSourceStats {
		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcaudiosourcestats-audiolevel
		 */
		@JsonProperty("audioLevel")
		public Float audioLevel;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcvideosourcestats-framespersecond
		 */
		@JsonProperty("framesPerSecond")
		public Double framesPerSecond;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcvideosourcestats-height
		 */
		@JsonProperty("height")
		public Double height;

		/**
		 * The id of the media source used in the dictionary
		 */
		@JsonProperty("id")
		public String id;

		@JsonProperty("mediaType")
		public RTCStreamMediaType mediaType;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcaudiosourcestats-totalaudioenergy
		 */
		@JsonProperty("totalAudioEnergy")
		public Float totalAudioEnergy;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcaudiosourcestats-totalsamplesduration
		 */
		@JsonProperty("totalSamplesDuration")
		public Double totalSamplesDuration;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-trackidentifier
		 */
		@JsonProperty("trackId")
		public String trackId;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcvideosourcestats-width
		 */
		@JsonProperty("width")
		public Double width;
	}

	/**
	 * Measurements regarding to Internet Connectivity Establishments
	 */
	public static class ICEStats {
		@JsonProperty("candidatePairs")
		public ICECandidatePair[] candidatePairs;

		@JsonProperty("localCandidates")
		public ICELocalCandidate[] localCandidates;

		@JsonProperty("remoteCandidates")
		public ICERemoteCandidate[] remoteCandidates;
	}

	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-candidate-pair
	 */
	public static class ICECandidatePair {

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-availableoutgoingbitrate
		 */
		@JsonProperty("availableOutgoingBitrate")
		public Integer availableOutgoingBitrate;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-bytesreceived
		 */
		@JsonProperty("bytesReceived")
		public Long bytesReceived;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-bytessent
		 */
		@JsonProperty("bytesSent")
		public Long bytesSent;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-consentrequestssent
		 */
		@JsonProperty("consentRequestsSent")
		public Integer consentRequestsSent;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-currentroundtriptime
		 */
		@JsonProperty("currentRoundTripTime")
		public Double currentRoundTripTime;

		/**
		 * Identifying the candidate pair in a map
		 */
		@JsonProperty("id")
		public String id;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-localcandidateid
		 */
		@JsonProperty("localCandidateId")
		public String localCandidateId;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-nominated
		 */
		@JsonProperty("nominated")
		public Boolean nominated;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-remotecandidateid
		 */
		@JsonProperty("priority")
		public Long priority;

		/**
		 * The priority of the candidate pair
		 */
		@JsonProperty("remoteCandidateId")
		public String remoteCandidateId;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-requestsreceived
		 */
		@JsonProperty("requestsReceived")
		public Integer requestsReceived;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-requestssent
		 */
		@JsonProperty("requestsSent")
		public Integer requestsSent;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-responsesreceived
		 */
		@JsonProperty("responsesReceived")
		public Integer responsesReceived;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-responsessent
		 */
		@JsonProperty("responsesSent")
		public Integer responsesSent;

		/**
		 * https://tools.ietf.org/html/rfc8445#section-6.1.2.6. The unknown state indicates value
		 * cannot be parsed by the server, not part of the specification
		 */
		@JsonProperty("state")
		public ICEState state;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-transportId
		 */
		@JsonProperty("totalRoundTripTime")
		public Double totalRoundTripTime;

		@JsonProperty("transportId")
		public String transportId;

		@JsonProperty("writable")
		public Boolean writable;

	}

	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-remote-candidate
	 */
	public static class ICELocalCandidate {

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype
		 */
		@JsonProperty("candidateType")
		public CandidateType candidateType;

		@JsonProperty("deleted")
		public Boolean deleted;

		/**
		 * the id of the candidate used inside of the client
		 */
		@JsonProperty("id")
		public String id;

		/**
		 * The IP address of the candidate
		 */
		@JsonProperty("ip")
		public String ip;

		/**
		 * Indicates if the candidate is a remote candidate or not
		 */
		@JsonProperty("isRemote")
		public Boolean isRemote;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype
		 */
		@JsonProperty("networkType")
		public NetworkType networkType;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-port
		 */
		@JsonProperty("port")
		public Integer port;

		/**
		 * The Priority of the candidate
		 */
		@JsonProperty("priority")
		public Long priority;

		@JsonProperty("protocol")
		public InternetProtocol protocol;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid
		 */
		@JsonProperty("transportId")
		public String transportId;
	}

	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-remote-candidate
	 */
	public static class ICERemoteCandidate {
		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype
		 */
		@JsonProperty("candidateType")
		public CandidateType candidateType;

		@JsonProperty("deleted")
		public Boolean deleted;

		/**
		 * The id of the candidate used inside of the client
		 */
		@JsonProperty("id")
		public String id;

		/**
		 * The IP address of the candidate
		 */
		@JsonProperty("ip")
		public String ip;

		/**
		 * Indicates if the candidate is a remote candidate or not
		 */
		@JsonProperty("isRemote")
		public Boolean isRemote;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-port
		 */
		@JsonProperty("port")
		public Integer port;

		/**
		 * The Priority of the candidate
		 */
		@JsonProperty("priority")
		public Long priority;

		@JsonProperty("protocol")
		public InternetProtocol protocol;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid
		 */
		@JsonProperty("transportId")
		public String transportId;
	}

	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats
	 */
	public static class InboundRTPStreamStats {

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-bytesreceived
		 */
		@JsonProperty("bytesReceived")
		public Long bytesReceived;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-codecid
		 */
		@JsonProperty("codecId")
		public String codecId;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-decoderimplementation
		 */
		@JsonProperty("decoderImplementation")
		public String decoderImplementation;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-estimatedplayouttimestamp
		 */
		@JsonProperty("estimatedPlayoutTimestamp")
		public Double estimatedPlayoutTimestamp;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-fecpacketsdiscarded
		 */
		@JsonProperty("fecPacketsDiscarded")
		public Integer fecPacketsDiscarded;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-fecpacketsreceived
		 */
		@JsonProperty("fecPacketsReceived")
		public Integer fecPacketsReceived;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-fircount
		 */
		@JsonProperty("firCount")
		public Integer firCount;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-framesdecoded
		 */
		@JsonProperty("framesDecoded")
		public Integer framesDecoded;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-headerbytesreceived
		 */
		@JsonProperty("headerBytesReceived")
		public Long headerBytesReceived;

		/**
		 * The id of the InboundRTP the client use in its dictionary
		 */
		@JsonProperty("id")
		public String id;

		/**
		 * Indicates if the report belongs to a remote participant
		 */
		@JsonProperty("isRemote")
		public Boolean isRemote;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-jitter
		 */
		@JsonProperty("jitter")
		public Double jitter;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-framesdecoded
		 */
		@JsonProperty("keyFramesDecoded")
		public Integer keyFramesDecoded;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-lastpacketreceivedtimestamp
		 */
		@JsonProperty("lastPacketReceivedTimestamp")
		public Double lastPacketReceivedTimestamp;

		@JsonProperty("mediaType")
		public RTCStreamMediaType mediaType;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-nackcount
		 */
		@JsonProperty("nackCount")
		public Integer nackCount;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-packetslost
		 */
		@JsonProperty("packetsLost")
		public Integer packetsLost;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-packetsreceived
		 */
		@JsonProperty("packetsReceived")
		public Integer packetsReceived;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-plicount
		 */
		@JsonProperty("pliCount")
		public Integer pliCount;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-qpsum
		 */
		@JsonProperty("qpSum")
		public Double qpSum;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-ssrc
		 */
		@JsonProperty("ssrc")
		public Long ssrc;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-totaldecodetime
		 */
		@JsonProperty("totalDecodeTime")
		public Double totalDecodeTime;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-totalinterframedelay
		 */
		@JsonProperty("totalInterFrameDelay")
		public Double totalInterFrameDelay;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-totalsquaredinterframedelay
		 */
		@JsonProperty("totalSquaredInterFrameDelay")
		public Double totalSquaredInterFrameDelay;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-trackidentifier
		 */
		@JsonProperty("trackId")
		public String trackId;

		/**
		 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid
		 */
		@JsonProperty("transportId")
		public String transportId;

	}

	public static class ClientDetails {
		@JsonProperty("browser")
		public BrowserDetails browser;
		@JsonProperty("os")
		public OSDetails os;
		@JsonProperty("platform")
		public PlatformDetails platform;
		@JsonProperty("engine")
		public EngineDetails engine;
	}

	public static class BaseBrowserDetails {
		@JsonProperty("name")
		public String name;
		@JsonProperty("version")
		public String version;
	}

	public static class PlatformDetails {
		@JsonProperty("type")
		public String type;
		@JsonProperty("vendor")
		public String vendor;
		@JsonProperty("model")
		public String model;
	}

	public static class OSDetails extends BaseBrowserDetails {
		@JsonProperty("versionName")
		public String versionName;
	}

	public static class BrowserDetails extends BaseBrowserDetails {
	}

	@Schema(description = "Browser engine detail specific details of a client")
	public static class EngineDetails extends BaseBrowserDetails {
	}

	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcqualitylimitationreason
	 */
	public enum RTCQualityLimitationReason {
		BANDWIDTH, CPU, NONE, OTHER, UNKNOWN;

		@JsonValue
		public String toValue() {
			switch (this) {
				case BANDWIDTH:
					return "bandwidth";
				case CPU:
					return "cpu";
				case NONE:
					return "none";
				case OTHER:
					return "other";
				default:
					return "unknown";
			}
		}

		@JsonCreator
		public static RTCQualityLimitationReason forValue(String value) throws IOException {
			if (value == null) return UNKNOWN;
			if (value.equals("bandwidth")) return BANDWIDTH;
			if (value.equals("cpu")) return CPU;
			if (value.equals("none")) return NONE;
			if (value.equals("other")) return OTHER;
			return UNKNOWN;
		}
	}

	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-kind
	 */
	public enum RTCStreamMediaType {
		AUDIO, VIDEO, UNKNOWN;

		@JsonValue
		public String toValue() {
			switch (this) {
				case AUDIO:
					return "audio";
				case VIDEO:
					return "video";
				default:
					return "unknown";
			}
		}

		@JsonCreator
		public static RTCStreamMediaType forValue(String value) throws IOException {
			if (value == null) return UNKNOWN;
			if (value.equals("audio")) return AUDIO;
			if (value.equals("video")) return VIDEO;
			return UNKNOWN;
		}
	}


	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype
	 */
	public enum NetworkType {
		BLUETOOTH, CELLULAR, ETHERNET, UNKNOWN, VPN, WIFI, WIMAX;

		@JsonValue
		public String toValue() {
			switch (this) {
				case BLUETOOTH:
					return "bluetooth";
				case CELLULAR:
					return "cellular";
				case ETHERNET:
					return "ethernet";
				case UNKNOWN:
					return "unknown";
				case VPN:
					return "vpn";
				case WIFI:
					return "wifi";
				case WIMAX:
					return "wimax";
				default:
					return "unknown";
			}
		}

		@JsonCreator
		public static NetworkType forValue(String value) throws IOException {
			if (value == null) return UNKNOWN;
			if (value.equals("bluetooth")) return BLUETOOTH;
			if (value.equals("cellular")) return CELLULAR;
			if (value.equals("ethernet")) return ETHERNET;
			if (value.equals("unknown")) return UNKNOWN;
			if (value.equals("vpn")) return VPN;
			if (value.equals("wifi")) return WIFI;
			if (value.equals("wimax")) return WIMAX;
			return UNKNOWN;
		}
	}


	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-protocol
	 */
	public enum InternetProtocol {
		TCP, UDP, UNKNOWN;

		@JsonValue
		public String toValue() {
			switch (this) {
				case TCP:
					return "TCP";
				case UDP:
					return "UDP";
				default:
					return "unknown";
			}
		}

		@JsonCreator
		public static InternetProtocol forValue(String value) throws IOException {
			if (value == null) return UNKNOWN;
			if (value.equals("TCP")) return TCP;
			if (value.equals("UDP")) return UDP;
			return UNKNOWN;
		}
	}


	/**
	 * https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype
	 */
	public enum CandidateType {
		HOST, PRFLX, RELAY, SRFLX, UNKNOWN;

		@JsonValue
		public String toValue() {
			switch (this) {
				case HOST:
					return "host";
				case PRFLX:
					return "prflx";
				case RELAY:
					return "relay";
				case SRFLX:
					return "srflx";
				default:
					return "unknown";
			}
		}

		@JsonCreator
		public static CandidateType forValue(String value) throws IOException {
			if (value == null) return UNKNOWN;
			if (value.equals("host")) return HOST;
			if (value.equals("prflx")) return PRFLX;
			if (value.equals("relay")) return RELAY;
			if (value.equals("srflx")) return SRFLX;
			return UNKNOWN;
		}
	}


	/**
	 * https://tools.ietf.org/html/rfc8445#section-6.1.2.6.
	 */
	public enum ICEState {
		FAILED, FROZEN, IN_PROGRESS, SUCCEEDED, WAITING, UNKNOWN;

		@JsonValue
		public String toValue() {
			switch (this) {
				case FAILED:
					return "failed";
				case FROZEN:
					return "frozen";
				case IN_PROGRESS:
					return "inprogress";
				case SUCCEEDED:
					return "succeeded";
				case WAITING:
					return "waiting";
				default:
					return "unknown";
			}
		}

		@JsonCreator
		public static ICEState forValue(String value) throws IOException {
			if (value == null) return UNKNOWN;
			if (value.equals("failed")) return FAILED;
			if (value.equals("frozen")) return FROZEN;
			if (value.equals("inprogress")) return IN_PROGRESS;
			if (value.equals("succeeded")) return SUCCEEDED;
			if (value.equals("waiting")) return WAITING;
			return UNKNOWN;
		}
	}

	@Schema(description = "Media device kind https://developer.mozilla.org/en-US/docs/Web/API/MediaDeviceInfo/kind")
	public enum MediaDeviceKind {
		VIDEO_INPUT, AUDIO_INPUT, AUDIO_OUTPUT, UNKNOWN;

		@JsonValue
		public String toValue() {
			switch (this) {
				case VIDEO_INPUT:
					return "videoinput";
				case AUDIO_INPUT:
					return "audioinput";
				case AUDIO_OUTPUT:
					return "audiooutput";
				default:
					return "unknown";
			}
		}

		@JsonCreator
		public static MediaDeviceKind forValue(String value) throws IOException {
			if (value == null) return UNKNOWN;
			if (value.equals("videoinput")) return VIDEO_INPUT;
			if (value.equals("audioinput")) return AUDIO_INPUT;
			if (value.equals("audiooutput")) return AUDIO_OUTPUT;
			return UNKNOWN;
		}
	}
}
