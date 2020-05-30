export interface MediaSource {
    audioLevel: number
    id: string
    kind: 'audio' | 'video'
    framesPerSecond: number
    height: number
    width: number
    timestamp: number
    totalAudioEnergy: number
    totalSamplesDuration: number
    trackIdentifier: string
    type: 'media-source'
}

export interface CandidatePair {
    availableOutgoingBitrate: number
    bytesReceived: number
    bytesSent: number
    consentRequestsSent: number
    currentRoundTripTime: number
    id: string
    localCandidateId: string
    nominated: boolean
    priority: number
    remoteCandidateId: string
    requestsReceived: number
    requestsSent: number
    responsesReceived: number
    responsesSent: number
    state: 'frozen' | 'waiting' | 'in-progress' | 'failed' | 'succeeded'
    timestamp: number
    totalRoundTripTime: number
    transportId: string
    type: 'candidate-pair'
    writable: boolean
}

export interface RemoteCandidate {
    candidateType:   'host' | 'srflx' | 'prflx' | 'relay'
    deleted: boolean
    id: string
    ip: string
    isRemote: true
    port: number
    priority: number
    protocol: 'udp' | 'tcp'
    timestamp: number
    transportId: 'string'
    type: 'remote-candidate'
}

export interface LocalCandidate {
    candidateType: 'host' | 'srflx' | 'prflx' | 'relay'
    deleted: boolean
    id: string
    ip: string
    isRemote: false
    networkType:  'bluetooth' | 'cellular' | 'ethernet' | 'wifi' | 'wimax' | 'vpn' | 'unknown'
    port: number
    priority: number
    protocol: 'udp' | 'tcp'
    timestamp: number
    transportId: string
    type: 'local-candidate'
}

export interface Track {
    detached: boolean
    ended: boolean
    id: string
    kind: 'audio' | 'video'
    mediaSourceId: string
    remoteSource: boolean
    timestamp: string
    trackIdentifier: string
    frameHeight: number
    frameWidth: number
    framesSent: number
    hugeFramesSent: number
    audioLevel: number
    concealedSamples: number
    concealmentEvents: number
    insertedSamplesForDeceleration: number
    jitterBufferDelay: number
    jitterBufferEmittedCount: number
    removedSamplesForAcceleration: number
    silentConcealedSamples: number
    totalAudioEnergy: number
    totalSamplesDuration: number
    totalSamplesReceived: number
    framesDecoded: number
    framesDropped: number
    framesReceived: number
    type: 'track'
}

export interface OutboundRTP {
    bytesSent: number
    codecId: string
    headerBytesSent: number
    id: string
    isRemote: boolean
    kind: 'audio' | 'video'
    mediaSourceId: string
    mediaType: 'audio' | 'video'
    packetsSent: number
    remoteId: string
    retransmittedBytesSent: number
    retransmittedPacketsSent: number
    ssrc: number
    timestamp: number
    trackId: number
    transportId: string
    encoderImplementation: string
    firCount: number
    framesEncoded: number
    keyFramesEncoded: number
    nackCount: number
    pliCount: number
    qpSum: number
    qualityLimitationReason: 'none' | 'cpu' | 'bandwidth' | 'other'
    qualityLimitationResolutionChanges: number
    totalEncodeTime: number
    totalEncodedBytesTarget: number
    totalPacketSendDelay: number
    type: 'outbound-rtp'
}

export interface InboundRTP {
    bytesReceived: number
    codecId: string
    estimatedPlayoutTimestamp: number
    fecPacketsDiscarded: number
    fecPacketsReceived: number
    headerBytesReceived: number
    id: string
    isRemote: boolean
    jitter: number
    kind: 'audio' | 'video'
    lastPacketReceivedTimestamp: number
    mediaType: 'audio' | 'video'
    packetsLost: number
    packetsReceived: number
    ssrc: number
    timestamp: number
    trackId: string
    transportId: string
    decoderImplementation: string
    firCount: number
    framesDecoded: number
    keyFramesDecoded: number
    nackCount: number
    pliCount: number
    qpSum: number
    totalDecodeTime: number
    totalInterFrameDelay: number
    totalSquaredInterFrameDelay: number
    type: 'inbound-rtp'
}

export interface RemoteInboundRTP {
    codecId: string
    id: string
    jitter: number
    kind: 'audio' | 'video'
    localId: string
    packetsLost: number
    roundTripTime: number
    ssrc: number
    timestamp: number
    transportId: string
    type: 'remote-inbound-rtp'
}

export interface StatsPayload {
    peerConnectionId: string
    receiverStats: [
        CandidatePair |
        RemoteCandidate |
        LocalCandidate |
        Track |
        InboundRTP
    ],
    senderStats: [
        MediaSource |
        CandidatePair |
        RemoteCandidate |
        LocalCandidate |
        Track |
        OutboundRTP |
        RemoteInboundRTP
    ]
}
