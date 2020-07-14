CREATE TABLE WebRTC.InitiatedCalls 
(
observerUUID STRING, 
callUUID STRING,
initiated TIMESTAMP,
);

CREATE TABLE WebRTC.FinishedCalls 
(
observerUUID STRING, 
callUUID STRING,
finished TIMESTAMP,
);

CREATE TABLE WebRTC.JoinedPeerConnections 
(
observerUUID STRING, 
callUUID STRING,
peerConnectionUUID STRING,
joined TIMESTAMP
);

CREATE TABLE WebRTC.DetachedPeerConnections 
(
observerUUID STRING, 
callUUID STRING,
peerConnectionUUID STRING,
detached TIMESTAMP
);


CREATE TABLE WebRTC.InboundStreamSamples
(
    observerUUID STRING,
    peerConnectionUUID STRING,
    SSRC INT64,
    packetsReceived STRUCT<
        minimum INT64,
        maximum INT64,
        sum INT64,
        count INT64
    >,
    bytesReceived STRUCT<
        minimum INT64,
        maximum INT64,
        sum INT64,
        count INT64
    >,
    packetsLost STRUCT<
        minimum INT64,
        maximum INT64,
        sum INT64,
        count INT64
    >,
    firstSample TIMESTAMP,
    lastSample TIMESTAMP
);

CREATE TABLE WebRTC.RemoteInboundStreamSamples
(
    observerUUID STRING,
    peerConnectionUUID STRING,
    SSRC INT64,
    RTTInMs STRUCT<
        minimum INT64,
        maximum INT64,
        sum INT64,
        count INT64
    >,
    firstSample TIMESTAMP,
    lastSample TIMESTAMP
);

CREATE TABLE WebRTC.OutboundStreamSamples
(
    observerUUID STRING,
    peerConnectionUUID STRING,
    SSRC INT64,
    packetsSent STRUCT<
        minimum INT64,
        maximum INT64,
        sum INT64,
        count INT64
    >,
    bytesSent STRUCT<
        minimum INT64,
        maximum INT64,
        sum INT64,
        count INT64
    >,
    firstSample TIMESTAMP,
    lastSample TIMESTAMP
);