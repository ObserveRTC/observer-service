Schema Description for Peer Connection Sample Version 20200114
=== 

#### PeerConnectionSample

The Peer Connection Sample provided by the WebExtrApp, version 20200114

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| browserId | string | fingerprint of the browser the sample is originated from. Use to identify unique number of browsers in a call. | No |
| iceStats | object | Measurement to describe the state of the Interactive Connectivity Establishment (ICE) | No |
| peerConnectionId | string | The UUID of the peer connection the sample belongs to | No |
| receiverStats | object | Describe a Real Time Communication endpoint. A direct mapping of: <https://www.w3.org/TR/webrtc/#dom-rtcstats> | No |
| senderStats | object | Describe a Real Time Communication endpoint. A direct mapping of: <https://www.w3.org/TR/webrtc/#dom-rtcstats> | No |
| timeZoneOffsetInMinute | long | The offset in minutes from GMT the browser reports the sample may have. | No |
| timestamp | long | The timestamp in epoch of the browser reports the sample | No |
| callId | string | The application provided identifier of the call the peer connection joined to | No |
| userId | string | The application provided Id of the user joined to the call | No |
| extensions | [ object ] | Custom defined measurements attached to peer connection sample | No |
| userMediaErrors | [ object ] | Custom defined measurements attached to peer connection sample | No |
| marker | string | A custom defined string all report will be marked with. | No |

#### PeerConnectionSampleV20200114.ICEState

Map the value of <https://tools.ietf.org/html/rfc8445#section-6.1.2.6>.

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| PeerConnectionSampleV20200114.ICEState | string | Map the value of <https://tools.ietf.org/html/rfc8445#section-6.1.2.6>. |  |

#### PeerConnectionSampleV20200114.ICECandidatePair

Represents the <https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-candidate-pair>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| availableOutgoingBitrate | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-availableoutgoingbitrate> | No |
| bytesReceived | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-bytesreceived> | No |
| bytesSent | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-bytessent> | No |
| consentRequestsSent | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-consentrequestssent> | No |
| currentRoundTripTime | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-currentroundtriptime> | No |
| id | string | Identifying the candidate pair in a map | No |
| localCandidateId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-localcandidateid> | No |
| nominated | boolean | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-nominated> | No |
| priority | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-remotecandidateid> | No |
| remoteCandidateId | string | The priority of the candidate pair | No |
| requestsReceived | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-requestsreceived> | No |
| requestsSent | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-requestssent> | No |
| responsesReceived | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-responsesreceived> | No |
| responsesSent | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-responsessent> | No |
| state | string | Map the value of <https://tools.ietf.org/html/rfc8445#section-6.1.2.6>.<br>_Enum:_ `"FAILED"`, `"FROZEN"`, `"IN_PROGRESS"`, `"SUCCEEDED"`, `"WAITING"`, `"UNKNOWN"` | No |
| totalRoundTripTime | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatepairstats-transportId> | No |
| transportId | string | The identifier of the transport stats belongs to this ICE candidate pair | No |
| writable | boolean |  | No |

#### PeerConnectionSampleV20200114.CandidateType

Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| PeerConnectionSampleV20200114.CandidateType | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype> |  |

#### PeerConnectionSampleV20200114.NetworkType

Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| PeerConnectionSampleV20200114.NetworkType | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype> |  |

#### PeerConnectionSampleV20200114.InternetProtocol

Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-protocol> and extended by the value of UNKNOWN, for unrecognized or null values from client side

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| PeerConnectionSampleV20200114.InternetProtocol | string | Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-protocol> and extended by the value of UNKNOWN, for unrecognized or null values from client side |  |

#### PeerConnectionSampleV20200114.ICELocalCandidate

Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-remote-candidate>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| candidateType | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype><br>_Enum:_ `"HOST"`, `"PRFLX"`, `"RELAY"`, `"SRFLX"`, `"UNKNOWN"` | No |
| deleted | boolean | Indicates if the candidate has been deleted locally or not | No |
| id | string | The id of the candidate used inside of the client | No |
| ip | string | The IP address of the candidate | No |
| isRemote | boolean | Indicates if the candidate is a remote candidate or not | No |
| networkType | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype><br>_Enum:_ `"BLUETOOTH"`, `"CELLULAR"`, `"ETHERNET"`, `"UNKNOWN"`, `"VPN"`, `"WIFI"`, `"WIMAX"` | No |
| port | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-port> | No |
| priority | long | The Priority of the candidate | No |
| protocol | string | Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-protocol> and extended by the value of UNKNOWN, for unrecognized or null values from client side<br>_Enum:_ `"TCP"`, `"UDP"`, `"UNKNOWN"` | No |
| transportId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid> | No |

#### PeerConnectionSampleV20200114.ICERemoteCandidate

Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-remote-candidate>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| candidateType | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-candidatetype><br>_Enum:_ `"HOST"`, `"PRFLX"`, `"RELAY"`, `"SRFLX"`, `"UNKNOWN"` | No |
| deleted | boolean |  | No |
| id | string | The id of the candidate used inside of the client | No |
| ip | string | The IP address of the candidate | No |
| isRemote | boolean | Indicates if the candidate is a remote candidate or not | No |
| port | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-port> | No |
| priority | long | The priority of the candidate | No |
| protocol | string | Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-protocol> and extended by the value of UNKNOWN, for unrecognized or null values from client side<br>_Enum:_ `"TCP"`, `"UDP"`, `"UNKNOWN"` | No |
| transportId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid> | No |

#### PeerConnectionSampleV20200114.ICEStats

Measurement to describe the state of the Interactive Connectivity Establishment (ICE)

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| candidatePairs | [ object ] | An array of ICE candidate pairs | No |
| localCandidates | [ object ] | An array of Local ICE candidates | No |
| remoteCandidates | [ object ] | An array of remote ICE candidates | No |

#### PeerConnectionSampleV20200114.RTCStreamMediaType

Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-kind> and extended by the value of UNKNOWN, for unrecognized or null values from client side

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| PeerConnectionSampleV20200114.RTCStreamMediaType | string | Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-kind> and extended by the value of UNKNOWN, for unrecognized or null values from client side |  |

#### PeerConnectionSampleV20200114.InboundRTPStreamStats

Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| bytesReceived | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-bytesreceived> | No |
| codecId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-codecid> | No |
| decoderImplementation | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-decoderimplementation> | No |
| estimatedPlayoutTimestamp | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-estimatedplayouttimestamp> | No |
| fecPacketsDiscarded | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-fecpacketsdiscarded> | No |
| fecPacketsReceived | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-fecpacketsreceived> | No |
| firCount | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-fircount> | No |
| framesDecoded | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-framesdecoded> | No |
| headerBytesReceived | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-headerbytesreceived> | No |
| id | string | The id of the InboundRTP the client use in its dictionary | No |
| isRemote | boolean | Indicates if the report belongs to a remote participant | No |
| jitter | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-jitter> | No |
| keyFramesDecoded | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-framesdecoded> | No |
| lastPacketReceivedTimestamp | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-lastpacketreceivedtimestamp> | No |
| mediaType | string | Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-kind> and extended by the value of UNKNOWN, for unrecognized or null values from client side<br>_Enum:_ `"AUDIO"`, `"VIDEO"`, `"UNKNOWN"` | No |
| nackCount | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-nackcount> | No |
| packetsLost | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-packetslost> | No |
| packetsReceived | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-packetsreceived> | No |
| pliCount | integer | Map the value of  <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-plicount> | No |
| qpSum | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-qpsum> | No |
| ssrc | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-ssrc> | No |
| totalDecodeTime | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-totaldecodetime> | No |
| totalInterFrameDelay | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-totalinterframedelay> | No |
| totalSquaredInterFrameDelay | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-totalsquaredinterframedelay> | No |
| trackId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-trackidentifier> | No |
| transportId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid> | No |

#### PeerConnectionSampleV20200114.MediaSourceStats

Represent the class described at <https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-media-source>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| audioLevel | float | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcaudiosourcestats-audiolevel> | No |
| framesPerSecond | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcvideosourcestats-framespersecond> | No |
| height | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcvideosourcestats-height> | No |
| id | string | The id of the media source used in the dictionary | No |
| mediaType | string | Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-kind> and extended by the value of UNKNOWN, for unrecognized or null values from client side<br>_Enum:_ `"AUDIO"`, `"VIDEO"`, `"UNKNOWN"` | No |
| totalAudioEnergy | float | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcaudiosourcestats-totalaudioenergy> | No |
| totalSamplesDuration | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcaudiosourcestats-totalsamplesduration> | No |
| trackId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-trackidentifier> | No |
| width | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcvideosourcestats-width> | No |

#### PeerConnectionSampleV20200114.RTCQualityLimitationReason

Indicates the reason of the limitation

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| PeerConnectionSampleV20200114.RTCQualityLimitationReason | string | Indicates the reason of the limitation |  |

#### PeerConnectionSampleV20200114.OutboundRTPStreamStats

Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcremoteoutboundrtpstreamstats>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| bytesSent | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtctransportstats-bytessent> | No |
| codecId | string |  | No |
| encoderImplementation | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-encoderimplementation> | No |
| firCount | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-fircount> | No |
| framesEncoded | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-framesencoded> | No |
| headerBytesSent | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-headerbytessent> | No |
| id | string | Indicates if the report belongs to a remote participant | No |
| isRemote | boolean | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-keyframesencoded> | No |
| keyFramesEncoded | long | The number of keyframes encoded | No |
| mediaSourceId | string | The id of the media source | No |
| mediaType | string | Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-kind> and extended by the value of UNKNOWN, for unrecognized or null values from client side<br>_Enum:_ `"AUDIO"`, `"VIDEO"`, `"UNKNOWN"` | No |
| nackCount | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-nackcount> | No |
| packetsSent | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtctransportstats-packetssent> | No |
| pliCount | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-plicount> | No |
| qpSum | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-qpsum> | No |
| qualityLimitationReason | string | Indicates the reason of the limitation<br>_Enum:_ `"BANDWIDTH"`, `"CPU"`, `"NONE"`, `"OTHER"`, `"UNKNOWN"` | No |
| qualityLimitationResolutionChanges | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-qualitylimitationresolutionchanges> | No |
| remoteId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-remoteid> | No |
| retransmittedBytesSent | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-retransmittedbytessent> | No |
| retransmittedPacketsSent | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-retransmittedpacketssent> | No |
| ssrc | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-ssrc> | No |
| totalEncodedBytesTarget | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-totalencodedbytestarget> | No |
| totalEncodeTime | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-totalencodetime> | No |
| totalPacketSendDelay | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-totalpacketsenddelay> | No |
| trackId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-trackidentifier> | No |
| transportId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid> | No |

#### PeerConnectionSampleV20200114.RemoteInboundRTPStreamStats

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| codecId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-codecid> | No |
| id | string | The id of the InboundRTP the client use in its dictionary | No |
| jitter | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-jitter> | No |
| localId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcremoteinboundrtpstreamstats-localid> | No |
| mediaType | string | Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-kind> and extended by the value of UNKNOWN, for unrecognized or null values from client side<br>_Enum:_ `"AUDIO"`, `"VIDEO"`, `"UNKNOWN"` | No |
| packetsLost | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-packetslost> | No |
| roundTripTime | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcremoteinboundrtpstreamstats-roundtriptime> | No |
| ssrc | long | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcrtpstreamstats-ssrc> | No |
| transportId | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcicecandidatestats-transportid> | No |

#### PeerConnectionSampleV20200114.RTCTrackStats

A direct mapping of the <https://www.w3.org/TR/webrtc-stats/#dom-rtcstatstype-track>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| concealedSamples | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-concealedsamples> | No |
| concealmentEvents | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-concealmentevents> | No |
| detached | boolean | Indicates if the track is detached from the source or not | No |
| ended | boolean | Indicates if the source ended the transmission for this track | No |
| framesDecoded | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-framesdecoded> | No |
| framesDropped | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcreceivedrtpstreamstats-framesdropped> | No |
| framesReceived | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-framesreceived> | No |
| hugeFramesSent | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcoutboundrtpstreamstats-hugeframessent> | No |
| id | string | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-trackidentifier> | No |
| insertedSamplesForDeceleration | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-insertedsamplesfordeceleration> | No |
| jitterBufferDelay | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcvideoreceiverstats-jitterbufferdelay> | No |
| jitterBufferEmittedCount | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcvideoreceiverstats-jitterbufferemittedcount> | No |
| mediaSourceId | string | The id of the media source the track is attached to | No |
| mediaType | string | Map the object of <https://www.w3.org/TR/webrtc-stats/#dom-rtcmediasourcestats-kind> and extended by the value of UNKNOWN, for unrecognized or null values from client side<br>_Enum:_ `"AUDIO"`, `"VIDEO"`, `"UNKNOWN"` | No |
| remoteSource | boolean | Indicates if the track attached to a remote source or not | No |
| removedSamplesForAcceleration | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-removedsamplesforacceleration> | No |
| samplesDuration | double | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-totalsamplesduration> | No |
| silentConcealedSamples | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcaudioreceiverstats-silentconcealedsamples> | No |
| totalSamplesReceived | integer | Map the value of <https://www.w3.org/TR/webrtc-stats/#dom-rtcinboundrtpstreamstats-totalsamplesreceived> | No |

#### PeerConnectionSampleV20200114.RTCStats

Describe a Real Time Communication endpoint. A direct mapping of: <https://www.w3.org/TR/webrtc/#dom-rtcstats>

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| inboundRTPStats | [ object ] |  | No |
| mediaSources | [ object ] |  | No |
| outboundRTPStats | [ object ] |  | No |
| remoteInboundRTPStats | [ object ] |  | No |
| tracks | [ object ] |  | No |

#### PeerConnectionSampleV20200114.ExtensionStat

A custom defined extension for the sample

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| extensionType | string |  | No |
| payload | string | The payload of the customized stat | No |

#### PeerConnectionSampleV20200114.UserMediaError

The reported user media error messages appeared to the user. (If this is not null, then peer connection uuid can be null.)

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| message | string |  | No |
