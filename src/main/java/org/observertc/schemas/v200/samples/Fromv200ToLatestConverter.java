package org.observertc.schemas.v200.samples;

import io.reactivex.rxjava3.functions.Function;

public class Fromv200ToLatestConverter implements Function<Samples, org.observertc.schemas.samples.Samples> {

    @Override
    public org.observertc.schemas.samples.Samples apply(Samples source) throws Throwable {
        var result = new org.observertc.schemas.samples.Samples();
        if (source == null) {
            return result;
        }
        if (source.controls != null) {
            result.controls = new org.observertc.schemas.samples.Samples.Controls();
            result.controls.accessClaim = source.controls.accessClaim;
            result.controls.close = source.controls.close;
        }
        result.clientSamples = this.getClientSamples(source.clientSamples);
        result.sfuSamples = this.getSfuSamples(source.sfuSamples);
        result.turnSamples = this.getTurnSamples(source.turnSamples);
        return result;
    }

    private org.observertc.schemas.samples.Samples.ClientSample[] getClientSamples(Samples.ClientSample[] srcClientSamples) {
        if (srcClientSamples == null) return null;
        var dstClientSamples = new org.observertc.schemas.samples.Samples.ClientSample[srcClientSamples.length];
        for (int index = 0; index < srcClientSamples.length; ++index) {
            var srcClientSample = srcClientSamples[index];
            var dstClientSample = new org.observertc.schemas.samples.Samples.ClientSample();
            dstClientSample.callId = srcClientSample.callId;
            dstClientSample.clientId = srcClientSample.clientId;
            dstClientSample.sampleSeq = srcClientSample.sampleSeq;
            dstClientSample.roomId = srcClientSample.roomId;
            dstClientSample.userId = srcClientSample.userId;
            dstClientSample.mediaConstraints = srcClientSample.mediaConstraints;
            dstClientSample.userMediaErrors = srcClientSample.userMediaErrors;
            dstClientSample.iceServers = srcClientSample.iceServers;
            dstClientSample.localSDPs = srcClientSample.localSDPs;
            dstClientSample.timestamp = srcClientSample.timestamp;
            dstClientSample.timeZoneOffsetInHours = srcClientSample.timeZoneOffsetInHours;
            dstClientSample.marker = srcClientSample.marker;

            if (srcClientSample.engine != null) {
                var srcEngine = srcClientSample.engine;
                var dstEngine = new org.observertc.schemas.samples.Samples.ClientSample.Engine();
                dstEngine.name = srcEngine.name;
                dstEngine.version = srcEngine.version;
            }

            if (srcClientSample.platform != null) {
                var srcPlatform = srcClientSample.platform;
                var dstPlatform = new org.observertc.schemas.samples.Samples.ClientSample.Platform();
                dstPlatform.type = srcPlatform.type;
                dstPlatform.vendor = srcPlatform.vendor;
                dstPlatform.model = srcPlatform.model;
            }

            if (srcClientSample.browser != null) {
                var srcBrowser = srcClientSample.browser;
                var dstBrowser = new org.observertc.schemas.samples.Samples.ClientSample.Browser();
                dstBrowser.name = srcBrowser.name;
                dstBrowser.version = srcBrowser.version;
            }

            if (srcClientSample.os != null) {
                var srcOperationSystem = srcClientSample.os;
                var dstOperationSystem = new org.observertc.schemas.samples.Samples.ClientSample.OperationSystem();
                dstOperationSystem.name = srcOperationSystem.name;
                dstOperationSystem.version = srcOperationSystem.version;
                dstOperationSystem.versionName = srcOperationSystem.versionName;
            }

            if (srcClientSample.mediaDevices != null) {
                var srcMediaDevices = srcClientSample.mediaDevices;
                var dstMediaDevices = new org.observertc.schemas.samples.Samples.ClientSample.MediaDevice[srcMediaDevices.length];
                for (int j = 0; j < srcMediaDevices.length; ++j) {
                    var srcMediaDevice = srcMediaDevices[j];
                    var dstMediaDevice = new org.observertc.schemas.samples.Samples.ClientSample.MediaDevice();
                    dstMediaDevice.label = srcMediaDevice.label;
                    dstMediaDevice.id = srcMediaDevice.id;
                    dstMediaDevice.kind = srcMediaDevice.kind;
                    dstMediaDevices[j] = dstMediaDevice;
                }
                dstClientSample.mediaDevices = dstMediaDevices;
            }


            if (srcClientSample.extensionStats != null) {
                var srcExtensionStats = srcClientSample.extensionStats;
                var dstExtensionStats = new org.observertc.schemas.samples.Samples.ClientSample.ExtensionStat[srcExtensionStats.length];
                for (int j = 0; j < srcExtensionStats.length; ++j) {
                    var srcExtensionStat = srcExtensionStats[j];
                    var dstExtensionStat = new org.observertc.schemas.samples.Samples.ClientSample.ExtensionStat();
                    dstExtensionStat.type = srcExtensionStat.type;
                    dstExtensionStat.payload = srcExtensionStat.payload;
                    dstExtensionStats[j] = dstExtensionStat;
                }
                dstClientSample.extensionStats = dstExtensionStats;
            }

            if (srcClientSample.pcTransports != null) {
                var srcPeerConnectionTransports = srcClientSample.pcTransports;
                var dstPeerConnectionTransports = new org.observertc.schemas.samples.Samples.ClientSample.PeerConnectionTransport[srcPeerConnectionTransports.length];
                for (int j = 0; j < srcPeerConnectionTransports.length; ++j) {
                    var srcPeerConnectionTransport = srcPeerConnectionTransports[j];
                    var dstPeerConnectionTransport = new org.observertc.schemas.samples.Samples.ClientSample.PeerConnectionTransport();
                    dstPeerConnectionTransport.peerConnectionId = srcPeerConnectionTransport.peerConnectionId;
                    dstPeerConnectionTransport.label = srcPeerConnectionTransport.label;
                    dstPeerConnectionTransport.dataChannelsOpened = srcPeerConnectionTransport.dataChannelsOpened;
                    dstPeerConnectionTransport.dataChannelsClosed = srcPeerConnectionTransport.dataChannelsClosed;
                    dstPeerConnectionTransport.dataChannelsRequested = srcPeerConnectionTransport.dataChannelsRequested;
                    dstPeerConnectionTransport.dataChannelsAccepted = srcPeerConnectionTransport.dataChannelsAccepted;
                    dstPeerConnectionTransport.packetsSent = srcPeerConnectionTransport.packetsSent;
                    dstPeerConnectionTransport.packetsReceived = srcPeerConnectionTransport.packetsReceived;
                    dstPeerConnectionTransport.bytesSent = srcPeerConnectionTransport.bytesSent;
                    dstPeerConnectionTransport.bytesReceived = srcPeerConnectionTransport.bytesReceived;
                    dstPeerConnectionTransport.iceRole = srcPeerConnectionTransport.iceRole;
                    dstPeerConnectionTransport.iceLocalUsernameFragment = srcPeerConnectionTransport.iceLocalUsernameFragment;
                    dstPeerConnectionTransport.dtlsState = srcPeerConnectionTransport.dtlsState;
                    dstPeerConnectionTransport.iceState = srcPeerConnectionTransport.iceState;
                    dstPeerConnectionTransport.tlsVersion = srcPeerConnectionTransport.tlsVersion;
                    dstPeerConnectionTransport.dtlsCipher = srcPeerConnectionTransport.dtlsCipher;
                    dstPeerConnectionTransport.srtpCipher = srcPeerConnectionTransport.srtpCipher;
                    dstPeerConnectionTransport.tlsGroup = srcPeerConnectionTransport.tlsGroup;
                    dstPeerConnectionTransport.selectedCandidatePairChanges = srcPeerConnectionTransport.selectedCandidatePairChanges;
                    dstPeerConnectionTransport.localAddress = srcPeerConnectionTransport.localAddress;
                    dstPeerConnectionTransport.localPort = srcPeerConnectionTransport.localPort;
                    dstPeerConnectionTransport.localProtocol = srcPeerConnectionTransport.localProtocol;
                    dstPeerConnectionTransport.localCandidateType = srcPeerConnectionTransport.localCandidateType;
                    dstPeerConnectionTransport.localCandidateICEServerUrl = srcPeerConnectionTransport.localCandidateICEServerUrl;
                    dstPeerConnectionTransport.localCandidateRelayProtocol = srcPeerConnectionTransport.localCandidateRelayProtocol;
                    dstPeerConnectionTransport.remoteAddress = srcPeerConnectionTransport.remoteAddress;
                    dstPeerConnectionTransport.remotePort = srcPeerConnectionTransport.remotePort;
                    dstPeerConnectionTransport.remoteProtocol = srcPeerConnectionTransport.remoteProtocol;
                    dstPeerConnectionTransport.remoteCandidateType = srcPeerConnectionTransport.remoteCandidateType;
                    dstPeerConnectionTransport.remoteCandidateICEServerUrl = srcPeerConnectionTransport.remoteCandidateICEServerUrl;
                    dstPeerConnectionTransport.remoteCandidateRelayProtocol = srcPeerConnectionTransport.remoteCandidateRelayProtocol;
                    dstPeerConnectionTransport.candidatePairState = srcPeerConnectionTransport.candidatePairState;
                    dstPeerConnectionTransport.candidatePairPacketsSent = srcPeerConnectionTransport.candidatePairPacketsSent;
                    dstPeerConnectionTransport.candidatePairPacketsReceived = srcPeerConnectionTransport.candidatePairPacketsReceived;
                    dstPeerConnectionTransport.candidatePairBytesSent = srcPeerConnectionTransport.candidatePairBytesSent;
                    dstPeerConnectionTransport.candidatePairBytesReceived = srcPeerConnectionTransport.candidatePairBytesReceived;
                    dstPeerConnectionTransport.candidatePairLastPacketSentTimestamp = srcPeerConnectionTransport.candidatePairLastPacketSentTimestamp;
                    dstPeerConnectionTransport.candidatePairLastPacketReceivedTimestamp = srcPeerConnectionTransport.candidatePairLastPacketReceivedTimestamp;
                    dstPeerConnectionTransport.candidatePairFirstRequestTimestamp = srcPeerConnectionTransport.candidatePairFirstRequestTimestamp;
                    dstPeerConnectionTransport.candidatePairLastRequestTimestamp = srcPeerConnectionTransport.candidatePairLastRequestTimestamp;
                    dstPeerConnectionTransport.candidatePairLastResponseTimestamp = srcPeerConnectionTransport.candidatePairLastResponseTimestamp;
                    dstPeerConnectionTransport.candidatePairTotalRoundTripTime = srcPeerConnectionTransport.candidatePairTotalRoundTripTime;
                    dstPeerConnectionTransport.candidatePairCurrentRoundTripTime = srcPeerConnectionTransport.candidatePairCurrentRoundTripTime;
                    dstPeerConnectionTransport.candidatePairAvailableOutgoingBitrate = srcPeerConnectionTransport.candidatePairAvailableOutgoingBitrate;
                    dstPeerConnectionTransport.candidatePairAvailableIncomingBitrate = srcPeerConnectionTransport.candidatePairAvailableIncomingBitrate;
                    dstPeerConnectionTransport.candidatePairCircuitBreakerTriggerCount = srcPeerConnectionTransport.candidatePairCircuitBreakerTriggerCount;
                    dstPeerConnectionTransport.candidatePairRequestsReceived = srcPeerConnectionTransport.candidatePairRequestsReceived;
                    dstPeerConnectionTransport.candidatePairRequestsSent = srcPeerConnectionTransport.candidatePairRequestsSent;
                    dstPeerConnectionTransport.candidatePairResponsesReceived = srcPeerConnectionTransport.candidatePairResponsesReceived;
                    dstPeerConnectionTransport.candidatePairResponsesSent = srcPeerConnectionTransport.candidatePairResponsesSent;
                    dstPeerConnectionTransport.candidatePairRetransmissionReceived = srcPeerConnectionTransport.candidatePairRetransmissionReceived;
                    dstPeerConnectionTransport.candidatePairRetransmissionSent = srcPeerConnectionTransport.candidatePairRetransmissionSent;
                    dstPeerConnectionTransport.candidatePairConsentRequestsSent = srcPeerConnectionTransport.candidatePairConsentRequestsSent;
                    dstPeerConnectionTransport.candidatePairConsentExpiredTimestamp = srcPeerConnectionTransport.candidatePairConsentExpiredTimestamp;
                    dstPeerConnectionTransport.candidatePairBytesDiscardedOnSend = srcPeerConnectionTransport.candidatePairBytesDiscardedOnSend;
                    dstPeerConnectionTransport.candidatePairPacketsDiscardedOnSend = srcPeerConnectionTransport.candidatePairPacketsDiscardedOnSend;
                    dstPeerConnectionTransport.candidatePairRequestBytesSent = srcPeerConnectionTransport.candidatePairRequestBytesSent;
                    dstPeerConnectionTransport.candidatePairConsentRequestBytesSent = srcPeerConnectionTransport.candidatePairConsentRequestBytesSent;
                    dstPeerConnectionTransport.candidatePairResponseBytesSent = srcPeerConnectionTransport.candidatePairResponseBytesSent;
                    dstPeerConnectionTransport.sctpSmoothedRoundTripTime = srcPeerConnectionTransport.sctpSmoothedRoundTripTime;
                    dstPeerConnectionTransport.sctpCongestionWindow = srcPeerConnectionTransport.sctpCongestionWindow;
                    dstPeerConnectionTransport.sctpReceiverWindow = srcPeerConnectionTransport.sctpReceiverWindow;
                    dstPeerConnectionTransport.sctpMtu = srcPeerConnectionTransport.sctpMtu;
                    dstPeerConnectionTransport.sctpUnackData = srcPeerConnectionTransport.sctpUnackData;
                    dstPeerConnectionTransports[j] = dstPeerConnectionTransport;
                }
                dstClientSample.pcTransports = dstPeerConnectionTransports;
            }

            if (srcClientSample.mediaSources != null) {
                var srcMediaSourceStats = srcClientSample.mediaSources;
                var dstMediaSourceStats = new org.observertc.schemas.samples.Samples.ClientSample.MediaSourceStat[srcMediaSourceStats.length];
                for (int j = 0; j < srcMediaSourceStats.length; ++j) {
                    var srcMediaSourceStat = srcMediaSourceStats[j];
                    var dstMediaSourceStat = new org.observertc.schemas.samples.Samples.ClientSample.MediaSourceStat();
                    dstMediaSourceStat.trackIdentifier = srcMediaSourceStat.trackIdentifier;
                    dstMediaSourceStat.kind = srcMediaSourceStat.kind;
                    dstMediaSourceStat.relayedSource = srcMediaSourceStat.relayedSource;
                    dstMediaSourceStat.audioLevel = srcMediaSourceStat.audioLevel;
                    dstMediaSourceStat.totalAudioEnergy = srcMediaSourceStat.totalAudioEnergy;
                    dstMediaSourceStat.totalSamplesDuration = srcMediaSourceStat.totalSamplesDuration;
                    dstMediaSourceStat.echoReturnLoss = srcMediaSourceStat.echoReturnLoss;
                    dstMediaSourceStat.echoReturnLossEnhancement = srcMediaSourceStat.echoReturnLossEnhancement;
                    dstMediaSourceStat.width = srcMediaSourceStat.width;
                    dstMediaSourceStat.height = srcMediaSourceStat.height;
                    dstMediaSourceStat.bitDepth = srcMediaSourceStat.bitDepth;
                    dstMediaSourceStat.frames = srcMediaSourceStat.frames;
                    dstMediaSourceStat.framesPerSecond = srcMediaSourceStat.framesPerSecond;
                    dstMediaSourceStats[j] = dstMediaSourceStat;
                }
                dstClientSample.mediaSources = dstMediaSourceStats;
            }

            if (srcClientSample.codecs != null) {
                var srcMediaCodecStats = srcClientSample.codecs;
                var dstMediaCodecStats = new org.observertc.schemas.samples.Samples.ClientSample.MediaCodecStats[srcMediaCodecStats.length];
                for (int j = 0; j < srcMediaCodecStats.length; ++j) {
                    var srcMediaCodecStat = srcMediaCodecStats[j];
                    var dstMediaCodecStat = new org.observertc.schemas.samples.Samples.ClientSample.MediaCodecStats();
                    dstMediaCodecStat.payloadType = srcMediaCodecStat.payloadType;
                    dstMediaCodecStat.codecType = srcMediaCodecStat.codecType;
                    dstMediaCodecStat.mimeType = srcMediaCodecStat.mimeType;
                    dstMediaCodecStat.clockRate = srcMediaCodecStat.clockRate;
                    dstMediaCodecStat.channels = srcMediaCodecStat.channels;
                    dstMediaCodecStat.sdpFmtpLine = srcMediaCodecStat.sdpFmtpLine;
                    dstMediaCodecStats[j] = dstMediaCodecStat;
                }
                dstClientSample.codecs = dstMediaCodecStats;
            }

            if (srcClientSample.certificates != null) {
                var srcCertificates = srcClientSample.certificates;
                var dstCertificates = new org.observertc.schemas.samples.Samples.ClientSample.Certificate[srcCertificates.length];
                for (int j = 0; j < srcCertificates.length; ++j) {
                    var srcCertificate = srcCertificates[j];
                    var dstCertificate = new org.observertc.schemas.samples.Samples.ClientSample.Certificate();
                    dstCertificate.fingerprint = srcCertificate.fingerprint;
                    dstCertificate.fingerprintAlgorithm = srcCertificate.fingerprintAlgorithm;
                    dstCertificate.base64Certificate = srcCertificate.base64Certificate;
                    dstCertificate.issuerCertificateId = srcCertificate.issuerCertificateId;
                    dstCertificates[j] = dstCertificate;
                }
                dstClientSample.certificates = dstCertificates;
            }

            if (srcClientSample.inboundAudioTracks != null) {
                var srcInboundAudioTracks = srcClientSample.inboundAudioTracks;
                var dstInboundAudioTracks = new org.observertc.schemas.samples.Samples.ClientSample.InboundAudioTrack[srcInboundAudioTracks.length];
                for (int j = 0; j < srcInboundAudioTracks.length; ++j) {
                    var srcInboundAudioTrack = srcInboundAudioTracks[j];
                    var dstInboundAudioTrack = new org.observertc.schemas.samples.Samples.ClientSample.InboundAudioTrack();
                    dstInboundAudioTrack.trackId = srcInboundAudioTrack.trackId;
                    dstInboundAudioTrack.peerConnectionId = srcInboundAudioTrack.peerConnectionId;
                    dstInboundAudioTrack.remoteClientId = srcInboundAudioTrack.remoteClientId;
                    dstInboundAudioTrack.sfuSinkId = srcInboundAudioTrack.sfuSinkId;
                    dstInboundAudioTrack.ssrc = srcInboundAudioTrack.ssrc;
                    dstInboundAudioTrack.packetsReceived = srcInboundAudioTrack.packetsReceived;
                    dstInboundAudioTrack.packetsLost = srcInboundAudioTrack.packetsLost;
                    dstInboundAudioTrack.jitter = srcInboundAudioTrack.jitter;
                    dstInboundAudioTrack.packetsDiscarded = srcInboundAudioTrack.packetsDiscarded;
                    dstInboundAudioTrack.packetsRepaired = srcInboundAudioTrack.packetsRepaired;
                    dstInboundAudioTrack.burstPacketsLost = srcInboundAudioTrack.burstPacketsLost;
                    dstInboundAudioTrack.burstPacketsDiscarded = srcInboundAudioTrack.burstPacketsDiscarded;
                    dstInboundAudioTrack.burstLossCount = srcInboundAudioTrack.burstLossCount;
                    dstInboundAudioTrack.burstDiscardCount = srcInboundAudioTrack.burstDiscardCount;
                    dstInboundAudioTrack.burstLossRate = srcInboundAudioTrack.burstLossRate;
                    dstInboundAudioTrack.burstDiscardRate = srcInboundAudioTrack.burstDiscardRate;
                    dstInboundAudioTrack.gapLossRate = srcInboundAudioTrack.gapLossRate;
                    dstInboundAudioTrack.gapDiscardRate = srcInboundAudioTrack.gapDiscardRate;
                    dstInboundAudioTrack.lastPacketReceivedTimestamp = srcInboundAudioTrack.lastPacketReceivedTimestamp;
                    dstInboundAudioTrack.averageRtcpInterval = srcInboundAudioTrack.averageRtcpInterval;
                    dstInboundAudioTrack.headerBytesReceived = srcInboundAudioTrack.headerBytesReceived;
                    dstInboundAudioTrack.fecPacketsReceived = srcInboundAudioTrack.fecPacketsReceived;
                    dstInboundAudioTrack.fecPacketsDiscarded = srcInboundAudioTrack.fecPacketsDiscarded;
                    dstInboundAudioTrack.bytesReceived = srcInboundAudioTrack.bytesReceived;
                    dstInboundAudioTrack.packetsFailedDecryption = srcInboundAudioTrack.packetsFailedDecryption;
                    dstInboundAudioTrack.packetsDuplicated = srcInboundAudioTrack.packetsDuplicated;
                    dstInboundAudioTrack.perDscpPacketsReceived = srcInboundAudioTrack.perDscpPacketsReceived;
                    dstInboundAudioTrack.nackCount = srcInboundAudioTrack.nackCount;
                    dstInboundAudioTrack.totalProcessingDelay = srcInboundAudioTrack.totalProcessingDelay;
                    dstInboundAudioTrack.estimatedPlayoutTimestamp = srcInboundAudioTrack.estimatedPlayoutTimestamp;
                    dstInboundAudioTrack.jitterBufferDelay = srcInboundAudioTrack.jitterBufferDelay;
                    dstInboundAudioTrack.jitterBufferEmittedCount = srcInboundAudioTrack.jitterBufferEmittedCount;
                    dstInboundAudioTrack.decoderImplementation = srcInboundAudioTrack.decoderImplementation;
                    dstInboundAudioTrack.voiceActivityFlag = srcInboundAudioTrack.voiceActivityFlag;
                    dstInboundAudioTrack.totalSamplesReceived = srcInboundAudioTrack.totalSamplesReceived;
                    dstInboundAudioTrack.totalSamplesDecoded = srcInboundAudioTrack.totalSamplesDecoded;
                    dstInboundAudioTrack.samplesDecodedWithSilk = srcInboundAudioTrack.samplesDecodedWithSilk;
                    dstInboundAudioTrack.samplesDecodedWithCelt = srcInboundAudioTrack.samplesDecodedWithCelt;
                    dstInboundAudioTrack.concealedSamples = srcInboundAudioTrack.concealedSamples;
                    dstInboundAudioTrack.silentConcealedSamples = srcInboundAudioTrack.silentConcealedSamples;
                    dstInboundAudioTrack.concealmentEvents = srcInboundAudioTrack.concealmentEvents;
                    dstInboundAudioTrack.insertedSamplesForDeceleration = srcInboundAudioTrack.insertedSamplesForDeceleration;
                    dstInboundAudioTrack.removedSamplesForAcceleration = srcInboundAudioTrack.removedSamplesForAcceleration;
                    dstInboundAudioTrack.packetsSent = srcInboundAudioTrack.packetsSent;
                    dstInboundAudioTrack.bytesSent = srcInboundAudioTrack.bytesSent;
                    dstInboundAudioTrack.remoteTimestamp = srcInboundAudioTrack.remoteTimestamp;
                    dstInboundAudioTrack.reportsSent = srcInboundAudioTrack.reportsSent;
                    dstInboundAudioTrack.roundTripTime = srcInboundAudioTrack.roundTripTime;
                    dstInboundAudioTrack.totalRoundTripTime = srcInboundAudioTrack.totalRoundTripTime;
                    dstInboundAudioTrack.roundTripTimeMeasurements = srcInboundAudioTrack.roundTripTimeMeasurements;
                    dstInboundAudioTrack.ended = srcInboundAudioTrack.ended;
                    dstInboundAudioTrack.payloadType = srcInboundAudioTrack.payloadType;
                    dstInboundAudioTrack.mimeType = srcInboundAudioTrack.mimeType;
                    dstInboundAudioTrack.clockRate = srcInboundAudioTrack.clockRate;
                    dstInboundAudioTrack.channels = srcInboundAudioTrack.channels;
                    dstInboundAudioTrack.sdpFmtpLine = srcInboundAudioTrack.sdpFmtpLine;
                    dstInboundAudioTracks[j] = dstInboundAudioTrack;
                }
                dstClientSample.inboundAudioTracks = dstInboundAudioTracks;
            }

            if (srcClientSample.inboundVideoTracks != null) {
                var srcInboundVideoTracks = srcClientSample.inboundVideoTracks;
                var dstInboundVideoTracks = new org.observertc.schemas.samples.Samples.ClientSample.InboundVideoTrack[srcInboundVideoTracks.length];
                for (int j = 0; j < srcInboundVideoTracks.length; ++j) {
                    var srcInboundVideoTrack = srcInboundVideoTracks[j];
                    var dstInboundVideoTrack = new org.observertc.schemas.samples.Samples.ClientSample.InboundVideoTrack();
                    dstInboundVideoTrack.trackId = srcInboundVideoTrack.trackId;
                    dstInboundVideoTrack.peerConnectionId = srcInboundVideoTrack.peerConnectionId;
                    dstInboundVideoTrack.remoteClientId = srcInboundVideoTrack.remoteClientId;
                    dstInboundVideoTrack.sfuSinkId = srcInboundVideoTrack.sfuSinkId;
                    dstInboundVideoTrack.ssrc = srcInboundVideoTrack.ssrc;
                    dstInboundVideoTrack.packetsReceived = srcInboundVideoTrack.packetsReceived;
                    dstInboundVideoTrack.packetsLost = srcInboundVideoTrack.packetsLost;
                    dstInboundVideoTrack.jitter = srcInboundVideoTrack.jitter;
                    dstInboundVideoTrack.packetsDiscarded = srcInboundVideoTrack.packetsDiscarded;
                    dstInboundVideoTrack.packetsRepaired = srcInboundVideoTrack.packetsRepaired;
                    dstInboundVideoTrack.burstPacketsLost = srcInboundVideoTrack.burstPacketsLost;
                    dstInboundVideoTrack.burstPacketsDiscarded = srcInboundVideoTrack.burstPacketsDiscarded;
                    dstInboundVideoTrack.burstLossCount = srcInboundVideoTrack.burstLossCount;
                    dstInboundVideoTrack.burstDiscardCount = srcInboundVideoTrack.burstDiscardCount;
                    dstInboundVideoTrack.burstLossRate = srcInboundVideoTrack.burstLossRate;
                    dstInboundVideoTrack.burstDiscardRate = srcInboundVideoTrack.burstDiscardRate;
                    dstInboundVideoTrack.gapLossRate = srcInboundVideoTrack.gapLossRate;
                    dstInboundVideoTrack.gapDiscardRate = srcInboundVideoTrack.gapDiscardRate;
                    dstInboundVideoTrack.lastPacketReceivedTimestamp = srcInboundVideoTrack.lastPacketReceivedTimestamp;
                    dstInboundVideoTrack.averageRtcpInterval = srcInboundVideoTrack.averageRtcpInterval;
                    dstInboundVideoTrack.headerBytesReceived = srcInboundVideoTrack.headerBytesReceived;
                    dstInboundVideoTrack.fecPacketsReceived = srcInboundVideoTrack.fecPacketsReceived;
                    dstInboundVideoTrack.fecPacketsDiscarded = srcInboundVideoTrack.fecPacketsDiscarded;
                    dstInboundVideoTrack.bytesReceived = srcInboundVideoTrack.bytesReceived;
                    dstInboundVideoTrack.packetsFailedDecryption = srcInboundVideoTrack.packetsFailedDecryption;
                    dstInboundVideoTrack.packetsDuplicated = srcInboundVideoTrack.packetsDuplicated;
                    dstInboundVideoTrack.perDscpPacketsReceived = srcInboundVideoTrack.perDscpPacketsReceived;
                    dstInboundVideoTrack.nackCount = srcInboundVideoTrack.nackCount;
                    dstInboundVideoTrack.totalProcessingDelay = srcInboundVideoTrack.totalProcessingDelay;
                    dstInboundVideoTrack.estimatedPlayoutTimestamp = srcInboundVideoTrack.estimatedPlayoutTimestamp;
                    dstInboundVideoTrack.jitterBufferDelay = srcInboundVideoTrack.jitterBufferDelay;
                    dstInboundVideoTrack.jitterBufferEmittedCount = srcInboundVideoTrack.jitterBufferEmittedCount;
                    dstInboundVideoTrack.decoderImplementation = srcInboundVideoTrack.decoderImplementation;
                    dstInboundVideoTrack.framesDropped = srcInboundVideoTrack.framesDropped;
                    dstInboundVideoTrack.framesDecoded = srcInboundVideoTrack.framesDecoded;
                    dstInboundVideoTrack.partialFramesLost = srcInboundVideoTrack.partialFramesLost;
                    dstInboundVideoTrack.fullFramesLost = srcInboundVideoTrack.fullFramesLost;
                    dstInboundVideoTrack.keyFramesDecoded = srcInboundVideoTrack.keyFramesDecoded;
                    dstInboundVideoTrack.frameWidth = srcInboundVideoTrack.frameWidth;
                    dstInboundVideoTrack.frameHeight = srcInboundVideoTrack.frameHeight;
                    dstInboundVideoTrack.frameBitDepth = srcInboundVideoTrack.frameBitDepth;
                    dstInboundVideoTrack.framesPerSecond = srcInboundVideoTrack.framesPerSecond;
                    dstInboundVideoTrack.qpSum = srcInboundVideoTrack.qpSum;
                    dstInboundVideoTrack.totalDecodeTime = srcInboundVideoTrack.totalDecodeTime;
                    dstInboundVideoTrack.totalInterFrameDelay = srcInboundVideoTrack.totalInterFrameDelay;
                    dstInboundVideoTrack.totalSquaredInterFrameDelay = srcInboundVideoTrack.totalSquaredInterFrameDelay;
                    dstInboundVideoTrack.firCount = srcInboundVideoTrack.firCount;
                    dstInboundVideoTrack.pliCount = srcInboundVideoTrack.pliCount;
                    dstInboundVideoTrack.sliCount = srcInboundVideoTrack.sliCount;
                    dstInboundVideoTrack.framesReceived = srcInboundVideoTrack.framesReceived;
                    dstInboundVideoTrack.packetsSent = srcInboundVideoTrack.packetsSent;
                    dstInboundVideoTrack.bytesSent = srcInboundVideoTrack.bytesSent;
                    dstInboundVideoTrack.remoteTimestamp = srcInboundVideoTrack.remoteTimestamp;
                    dstInboundVideoTrack.reportsSent = srcInboundVideoTrack.reportsSent;
                    dstInboundVideoTrack.roundTripTime = srcInboundVideoTrack.roundTripTime;
                    dstInboundVideoTrack.totalRoundTripTime = srcInboundVideoTrack.totalRoundTripTime;
                    dstInboundVideoTrack.roundTripTimeMeasurements = srcInboundVideoTrack.roundTripTimeMeasurements;
                    dstInboundVideoTrack.ended = srcInboundVideoTrack.ended;
                    dstInboundVideoTrack.payloadType = srcInboundVideoTrack.payloadType;
                    dstInboundVideoTrack.mimeType = srcInboundVideoTrack.mimeType;
                    dstInboundVideoTrack.clockRate = srcInboundVideoTrack.clockRate;
                    dstInboundVideoTrack.channels = srcInboundVideoTrack.channels;
                    dstInboundVideoTrack.sdpFmtpLine = srcInboundVideoTrack.sdpFmtpLine;
                    dstInboundVideoTracks[j] = dstInboundVideoTrack;
                }
                dstClientSample.inboundVideoTracks = dstInboundVideoTracks;
            }

            if (srcClientSample.outboundAudioTracks != null) {
                var srcOutboundAudioTracks = srcClientSample.outboundAudioTracks;
                var dstOutboundAudioTracks = new org.observertc.schemas.samples.Samples.ClientSample.OutboundAudioTrack[srcOutboundAudioTracks.length];
                for (int j = 0; j < srcOutboundAudioTracks.length; ++j) {
                    var srcOutboundAudioTrack = srcOutboundAudioTracks[j];
                    var dstOutboundAudioTrack = new org.observertc.schemas.samples.Samples.ClientSample.OutboundAudioTrack();
                    dstOutboundAudioTrack.trackId = srcOutboundAudioTrack.trackId;
                    dstOutboundAudioTrack.peerConnectionId = srcOutboundAudioTrack.peerConnectionId;
                    dstOutboundAudioTrack.sfuStreamId = srcOutboundAudioTrack.sfuStreamId;
                    dstOutboundAudioTrack.ssrc = srcOutboundAudioTrack.ssrc;
                    dstOutboundAudioTrack.packetsSent = srcOutboundAudioTrack.packetsSent;
                    dstOutboundAudioTrack.bytesSent = srcOutboundAudioTrack.bytesSent;
                    dstOutboundAudioTrack.rtxSsrc = srcOutboundAudioTrack.rtxSsrc;
                    dstOutboundAudioTrack.rid = srcOutboundAudioTrack.rid;
                    dstOutboundAudioTrack.lastPacketSentTimestamp = srcOutboundAudioTrack.lastPacketSentTimestamp;
                    dstOutboundAudioTrack.headerBytesSent = srcOutboundAudioTrack.headerBytesSent;
                    dstOutboundAudioTrack.packetsDiscardedOnSend = srcOutboundAudioTrack.packetsDiscardedOnSend;
                    dstOutboundAudioTrack.bytesDiscardedOnSend = srcOutboundAudioTrack.bytesDiscardedOnSend;
                    dstOutboundAudioTrack.fecPacketsSent = srcOutboundAudioTrack.fecPacketsSent;
                    dstOutboundAudioTrack.retransmittedPacketsSent = srcOutboundAudioTrack.retransmittedPacketsSent;
                    dstOutboundAudioTrack.retransmittedBytesSent = srcOutboundAudioTrack.retransmittedBytesSent;
                    dstOutboundAudioTrack.targetBitrate = srcOutboundAudioTrack.targetBitrate;
                    dstOutboundAudioTrack.totalEncodedBytesTarget = srcOutboundAudioTrack.totalEncodedBytesTarget;
                    dstOutboundAudioTrack.totalPacketSendDelay = srcOutboundAudioTrack.totalPacketSendDelay;
                    dstOutboundAudioTrack.averageRtcpInterval = srcOutboundAudioTrack.averageRtcpInterval;
                    dstOutboundAudioTrack.perDscpPacketsSent = srcOutboundAudioTrack.perDscpPacketsSent;
                    dstOutboundAudioTrack.nackCount = srcOutboundAudioTrack.nackCount;
                    dstOutboundAudioTrack.encoderImplementation = srcOutboundAudioTrack.encoderImplementation;
                    dstOutboundAudioTrack.totalSamplesSent = srcOutboundAudioTrack.totalSamplesSent;
                    dstOutboundAudioTrack.samplesEncodedWithSilk = srcOutboundAudioTrack.samplesEncodedWithSilk;
                    dstOutboundAudioTrack.samplesEncodedWithCelt = srcOutboundAudioTrack.samplesEncodedWithCelt;
                    dstOutboundAudioTrack.voiceActivityFlag = srcOutboundAudioTrack.voiceActivityFlag;
                    dstOutboundAudioTrack.packetsReceived = srcOutboundAudioTrack.packetsReceived;
                    dstOutboundAudioTrack.packetsLost = srcOutboundAudioTrack.packetsLost;
                    dstOutboundAudioTrack.jitter = srcOutboundAudioTrack.jitter;
                    dstOutboundAudioTrack.packetsDiscarded = srcOutboundAudioTrack.packetsDiscarded;
                    dstOutboundAudioTrack.packetsRepaired = srcOutboundAudioTrack.packetsRepaired;
                    dstOutboundAudioTrack.burstPacketsLost = srcOutboundAudioTrack.burstPacketsLost;
                    dstOutboundAudioTrack.burstPacketsDiscarded = srcOutboundAudioTrack.burstPacketsDiscarded;
                    dstOutboundAudioTrack.burstLossCount = srcOutboundAudioTrack.burstLossCount;
                    dstOutboundAudioTrack.burstDiscardCount = srcOutboundAudioTrack.burstDiscardCount;
                    dstOutboundAudioTrack.burstLossRate = srcOutboundAudioTrack.burstLossRate;
                    dstOutboundAudioTrack.burstDiscardRate = srcOutboundAudioTrack.burstDiscardRate;
                    dstOutboundAudioTrack.gapLossRate = srcOutboundAudioTrack.gapLossRate;
                    dstOutboundAudioTrack.gapDiscardRate = srcOutboundAudioTrack.gapDiscardRate;
                    dstOutboundAudioTrack.roundTripTime = srcOutboundAudioTrack.roundTripTime;
                    dstOutboundAudioTrack.totalRoundTripTime = srcOutboundAudioTrack.totalRoundTripTime;
                    dstOutboundAudioTrack.fractionLost = srcOutboundAudioTrack.fractionLost;
                    dstOutboundAudioTrack.reportsReceived = srcOutboundAudioTrack.reportsReceived;
                    dstOutboundAudioTrack.roundTripTimeMeasurements = srcOutboundAudioTrack.roundTripTimeMeasurements;
                    dstOutboundAudioTrack.relayedSource = srcOutboundAudioTrack.relayedSource;
                    dstOutboundAudioTrack.audioLevel = srcOutboundAudioTrack.audioLevel;
                    dstOutboundAudioTrack.totalAudioEnergy = srcOutboundAudioTrack.totalAudioEnergy;
                    dstOutboundAudioTrack.totalSamplesDuration = srcOutboundAudioTrack.totalSamplesDuration;
                    dstOutboundAudioTrack.echoReturnLoss = srcOutboundAudioTrack.echoReturnLoss;
                    dstOutboundAudioTrack.echoReturnLossEnhancement = srcOutboundAudioTrack.echoReturnLossEnhancement;
                    dstOutboundAudioTrack.ended = srcOutboundAudioTrack.ended;
                    dstOutboundAudioTrack.payloadType = srcOutboundAudioTrack.payloadType;
                    dstOutboundAudioTrack.mimeType = srcOutboundAudioTrack.mimeType;
                    dstOutboundAudioTrack.clockRate = srcOutboundAudioTrack.clockRate;
                    dstOutboundAudioTrack.channels = srcOutboundAudioTrack.channels;
                    dstOutboundAudioTrack.sdpFmtpLine = srcOutboundAudioTrack.sdpFmtpLine;
                    dstOutboundAudioTracks[j] = dstOutboundAudioTrack;
                }
                dstClientSample.outboundAudioTracks = dstOutboundAudioTracks;
            }

            if (srcClientSample.outboundVideoTracks != null) {
                var srcOutboundVideoTracks = srcClientSample.outboundVideoTracks;
                var dstOutboundVideoTracks = new org.observertc.schemas.samples.Samples.ClientSample.OutboundVideoTrack[srcOutboundVideoTracks.length];
                for (int j = 0; j < srcOutboundVideoTracks.length; ++j) {
                    var srcOutboundVideoTrack = srcOutboundVideoTracks[j];
                    var dstOutboundVideoTrack = new org.observertc.schemas.samples.Samples.ClientSample.OutboundVideoTrack();
                    dstOutboundVideoTrack.trackId = srcOutboundVideoTrack.trackId;
                    dstOutboundVideoTrack.peerConnectionId = srcOutboundVideoTrack.peerConnectionId;
                    dstOutboundVideoTrack.sfuStreamId = srcOutboundVideoTrack.sfuStreamId;
                    dstOutboundVideoTrack.ssrc = srcOutboundVideoTrack.ssrc;
                    dstOutboundVideoTrack.packetsSent = srcOutboundVideoTrack.packetsSent;
                    dstOutboundVideoTrack.bytesSent = srcOutboundVideoTrack.bytesSent;
                    dstOutboundVideoTrack.rtxSsrc = srcOutboundVideoTrack.rtxSsrc;
                    dstOutboundVideoTrack.rid = srcOutboundVideoTrack.rid;
                    dstOutboundVideoTrack.lastPacketSentTimestamp = srcOutboundVideoTrack.lastPacketSentTimestamp;
                    dstOutboundVideoTrack.headerBytesSent = srcOutboundVideoTrack.headerBytesSent;
                    dstOutboundVideoTrack.packetsDiscardedOnSend = srcOutboundVideoTrack.packetsDiscardedOnSend;
                    dstOutboundVideoTrack.bytesDiscardedOnSend = srcOutboundVideoTrack.bytesDiscardedOnSend;
                    dstOutboundVideoTrack.fecPacketsSent = srcOutboundVideoTrack.fecPacketsSent;
                    dstOutboundVideoTrack.retransmittedPacketsSent = srcOutboundVideoTrack.retransmittedPacketsSent;
                    dstOutboundVideoTrack.retransmittedBytesSent = srcOutboundVideoTrack.retransmittedBytesSent;
                    dstOutboundVideoTrack.targetBitrate = srcOutboundVideoTrack.targetBitrate;
                    dstOutboundVideoTrack.totalEncodedBytesTarget = srcOutboundVideoTrack.totalEncodedBytesTarget;
                    dstOutboundVideoTrack.totalPacketSendDelay = srcOutboundVideoTrack.totalPacketSendDelay;
                    dstOutboundVideoTrack.averageRtcpInterval = srcOutboundVideoTrack.averageRtcpInterval;
                    dstOutboundVideoTrack.perDscpPacketsSent = srcOutboundVideoTrack.perDscpPacketsSent;
                    dstOutboundVideoTrack.nackCount = srcOutboundVideoTrack.nackCount;
                    dstOutboundVideoTrack.firCount = srcOutboundVideoTrack.firCount;
                    dstOutboundVideoTrack.pliCount = srcOutboundVideoTrack.pliCount;
                    dstOutboundVideoTrack.sliCount = srcOutboundVideoTrack.sliCount;
                    dstOutboundVideoTrack.encoderImplementation = srcOutboundVideoTrack.encoderImplementation;
                    dstOutboundVideoTrack.frameWidth = srcOutboundVideoTrack.frameWidth;
                    dstOutboundVideoTrack.frameHeight = srcOutboundVideoTrack.frameHeight;
                    dstOutboundVideoTrack.frameBitDepth = srcOutboundVideoTrack.frameBitDepth;
                    dstOutboundVideoTrack.framesPerSecond = srcOutboundVideoTrack.framesPerSecond;
                    dstOutboundVideoTrack.framesSent = srcOutboundVideoTrack.framesSent;
                    dstOutboundVideoTrack.hugeFramesSent = srcOutboundVideoTrack.hugeFramesSent;
                    dstOutboundVideoTrack.framesEncoded = srcOutboundVideoTrack.framesEncoded;
                    dstOutboundVideoTrack.keyFramesEncoded = srcOutboundVideoTrack.keyFramesEncoded;
                    dstOutboundVideoTrack.framesDiscardedOnSend = srcOutboundVideoTrack.framesDiscardedOnSend;
                    dstOutboundVideoTrack.qpSum = srcOutboundVideoTrack.qpSum;
                    dstOutboundVideoTrack.totalEncodeTime = srcOutboundVideoTrack.totalEncodeTime;
                    dstOutboundVideoTrack.qualityLimitationDurationNone = srcOutboundVideoTrack.qualityLimitationDurationNone;
                    dstOutboundVideoTrack.qualityLimitationDurationCPU = srcOutboundVideoTrack.qualityLimitationDurationCPU;
                    dstOutboundVideoTrack.qualityLimitationDurationBandwidth = srcOutboundVideoTrack.qualityLimitationDurationBandwidth;
                    dstOutboundVideoTrack.qualityLimitationDurationOther = srcOutboundVideoTrack.qualityLimitationDurationOther;
                    dstOutboundVideoTrack.qualityLimitationReason = srcOutboundVideoTrack.qualityLimitationReason;
                    dstOutboundVideoTrack.qualityLimitationResolutionChanges = srcOutboundVideoTrack.qualityLimitationResolutionChanges;
                    dstOutboundVideoTrack.packetsReceived = srcOutboundVideoTrack.packetsReceived;
                    dstOutboundVideoTrack.packetsLost = srcOutboundVideoTrack.packetsLost;
                    dstOutboundVideoTrack.jitter = srcOutboundVideoTrack.jitter;
                    dstOutboundVideoTrack.packetsDiscarded = srcOutboundVideoTrack.packetsDiscarded;
                    dstOutboundVideoTrack.packetsRepaired = srcOutboundVideoTrack.packetsRepaired;
                    dstOutboundVideoTrack.burstPacketsLost = srcOutboundVideoTrack.burstPacketsLost;
                    dstOutboundVideoTrack.burstPacketsDiscarded = srcOutboundVideoTrack.burstPacketsDiscarded;
                    dstOutboundVideoTrack.burstLossCount = srcOutboundVideoTrack.burstLossCount;
                    dstOutboundVideoTrack.burstDiscardCount = srcOutboundVideoTrack.burstDiscardCount;
                    dstOutboundVideoTrack.burstLossRate = srcOutboundVideoTrack.burstLossRate;
                    dstOutboundVideoTrack.burstDiscardRate = srcOutboundVideoTrack.burstDiscardRate;
                    dstOutboundVideoTrack.gapLossRate = srcOutboundVideoTrack.gapLossRate;
                    dstOutboundVideoTrack.gapDiscardRate = srcOutboundVideoTrack.gapDiscardRate;
                    dstOutboundVideoTrack.roundTripTime = srcOutboundVideoTrack.roundTripTime;
                    dstOutboundVideoTrack.totalRoundTripTime = srcOutboundVideoTrack.totalRoundTripTime;
                    dstOutboundVideoTrack.fractionLost = srcOutboundVideoTrack.fractionLost;
                    dstOutboundVideoTrack.reportsReceived = srcOutboundVideoTrack.reportsReceived;
                    dstOutboundVideoTrack.roundTripTimeMeasurements = srcOutboundVideoTrack.roundTripTimeMeasurements;
                    dstOutboundVideoTrack.framesDropped = srcOutboundVideoTrack.framesDropped;
                    dstOutboundVideoTrack.partialFramesLost = srcOutboundVideoTrack.partialFramesLost;
                    dstOutboundVideoTrack.fullFramesLost = srcOutboundVideoTrack.fullFramesLost;
                    dstOutboundVideoTrack.relayedSource = srcOutboundVideoTrack.relayedSource;
                    dstOutboundVideoTrack.width = srcOutboundVideoTrack.width;
                    dstOutboundVideoTrack.height = srcOutboundVideoTrack.height;
                    dstOutboundVideoTrack.bitDepth = srcOutboundVideoTrack.bitDepth;
                    dstOutboundVideoTrack.frames = srcOutboundVideoTrack.frames;
                    dstOutboundVideoTrack.ended = srcOutboundVideoTrack.ended;
                    dstOutboundVideoTrack.payloadType = srcOutboundVideoTrack.payloadType;
                    dstOutboundVideoTrack.mimeType = srcOutboundVideoTrack.mimeType;
                    dstOutboundVideoTrack.clockRate = srcOutboundVideoTrack.clockRate;
                    dstOutboundVideoTrack.channels = srcOutboundVideoTrack.channels;
                    dstOutboundVideoTrack.sdpFmtpLine = srcOutboundVideoTrack.sdpFmtpLine;
                    dstOutboundVideoTracks[j] = dstOutboundVideoTrack;
                }
                dstClientSample.outboundVideoTracks = dstOutboundVideoTracks;
            }

            if (srcClientSample.iceLocalCandidates != null) {
                var srcIceLocalCandidates = srcClientSample.iceLocalCandidates;
                var dstIceLocalCandidates = new org.observertc.schemas.samples.Samples.ClientSample.IceLocalCandidate[srcIceLocalCandidates.length];
                for (int j = 0; j < srcIceLocalCandidates.length; ++j) {
                    var srcIceLocalCandidate = srcIceLocalCandidates[j];
                    var dstIceLocalCandidate = new org.observertc.schemas.samples.Samples.ClientSample.IceLocalCandidate();
                    dstIceLocalCandidate.peerConnectionId = srcIceLocalCandidate.peerConnectionId;
                    dstIceLocalCandidate.id = srcIceLocalCandidate.id;
                    dstIceLocalCandidate.address = srcIceLocalCandidate.address;
                    dstIceLocalCandidate.port = srcIceLocalCandidate.port;
                    dstIceLocalCandidate.protocol = srcIceLocalCandidate.protocol;
                    dstIceLocalCandidate.candidateType = srcIceLocalCandidate.candidateType;
                    dstIceLocalCandidate.priority = srcIceLocalCandidate.priority;
                    dstIceLocalCandidate.url = srcIceLocalCandidate.url;
                    dstIceLocalCandidate.relayProtocol = srcIceLocalCandidate.relayProtocol;
                    dstIceLocalCandidates[j] = dstIceLocalCandidate;
                }
                dstClientSample.iceLocalCandidates = dstIceLocalCandidates;
            }

            if (srcClientSample.iceRemoteCandidates != null) {
                var srcIceRemoteCandidates = srcClientSample.iceRemoteCandidates;
                var dstIceRemoteCandidates = new org.observertc.schemas.samples.Samples.ClientSample.IceRemoteCandidate[srcIceRemoteCandidates.length];
                for (int j = 0; j < srcIceRemoteCandidates.length; ++j) {
                    var srcIceRemoteCandidate = srcIceRemoteCandidates[j];
                    var dstIceRemoteCandidate = new org.observertc.schemas.samples.Samples.ClientSample.IceRemoteCandidate();
                    dstIceRemoteCandidate.peerConnectionId = srcIceRemoteCandidate.peerConnectionId;
                    dstIceRemoteCandidate.id = srcIceRemoteCandidate.id;
                    dstIceRemoteCandidate.address = srcIceRemoteCandidate.address;
                    dstIceRemoteCandidate.port = srcIceRemoteCandidate.port;
                    dstIceRemoteCandidate.protocol = srcIceRemoteCandidate.protocol;
                    dstIceRemoteCandidate.candidateType = srcIceRemoteCandidate.candidateType;
                    dstIceRemoteCandidate.priority = srcIceRemoteCandidate.priority;
                    dstIceRemoteCandidate.url = srcIceRemoteCandidate.url;
                    dstIceRemoteCandidate.relayProtocol = srcIceRemoteCandidate.relayProtocol;
                    dstIceRemoteCandidates[j] = dstIceRemoteCandidate;
                }
                dstClientSample.iceRemoteCandidates = dstIceRemoteCandidates;
            }

            if (srcClientSample.dataChannels != null) {
                var srcDataChannels = srcClientSample.dataChannels;
                var dstDataChannels = new org.observertc.schemas.samples.Samples.ClientSample.DataChannel[srcDataChannels.length];
                for (int j = 0; j < srcDataChannels.length; ++j) {
                    var srcDataChannel = srcDataChannels[j];
                    var dstDataChannel = new org.observertc.schemas.samples.Samples.ClientSample.DataChannel();
                    dstDataChannel.peerConnectionId = srcDataChannel.peerConnectionId;
                    dstDataChannel.id = srcDataChannel.id;
                    dstDataChannel.label = srcDataChannel.label;
                    dstDataChannel.address = srcDataChannel.address;
                    dstDataChannel.port = srcDataChannel.port;
                    dstDataChannel.protocol = srcDataChannel.protocol;
                    dstDataChannel.dataChannelIdentifier = srcDataChannel.dataChannelIdentifier;
                    dstDataChannel.state = srcDataChannel.state;
                    dstDataChannel.messagesSent = srcDataChannel.messagesSent;
                    dstDataChannel.bytesSent = srcDataChannel.bytesSent;
                    dstDataChannel.messagesReceived = srcDataChannel.messagesReceived;
                    dstDataChannel.bytesReceived = srcDataChannel.bytesReceived;
                    dstDataChannels[j] = dstDataChannel;
                }
                dstClientSample.dataChannels = dstDataChannels;
            }

            dstClientSamples[index] = dstClientSample;

        }
        return dstClientSamples;
    }

    private org.observertc.schemas.samples.Samples.SfuSample[] getSfuSamples(Samples.SfuSample[] srcSfuSamples) {
        if (srcSfuSamples == null) return null;
        var dstSfuSamples = new org.observertc.schemas.samples.Samples.SfuSample[srcSfuSamples.length];
        for (int index = 0; index < srcSfuSamples.length; ++index) {
            var srcSfuSample = srcSfuSamples[index];
            var dstSfuSample = new org.observertc.schemas.samples.Samples.SfuSample();

            dstSfuSample.sfuId = srcSfuSample.sfuId;
            dstSfuSample.timestamp = srcSfuSample.timestamp;
            dstSfuSample.timeZoneOffsetInHours = srcSfuSample.timeZoneOffsetInHours;
            dstSfuSample.marker = srcSfuSample.marker;

            if (srcSfuSample.transports != null) {
                var srcSfuTransports = srcSfuSample.transports;
                var dstSfuTransports = new org.observertc.schemas.samples.Samples.SfuSample.SfuTransport[srcSfuTransports.length];
                for (int j = 0; j < srcSfuTransports.length; ++j) {
                    var srcSfuTransport = srcSfuTransports[j];
                    var dstSfuTransport = new org.observertc.schemas.samples.Samples.SfuSample.SfuTransport();
                    dstSfuTransport.noReport = srcSfuTransport.noReport;
                    dstSfuTransport.transportId = srcSfuTransport.transportId;
                    dstSfuTransport.internal = srcSfuTransport.internal;
                    dstSfuTransport.dtlsState = srcSfuTransport.dtlsState;
                    dstSfuTransport.iceState = srcSfuTransport.iceState;
                    dstSfuTransport.sctpState = srcSfuTransport.sctpState;
                    dstSfuTransport.iceRole = srcSfuTransport.iceRole;
                    dstSfuTransport.localAddress = srcSfuTransport.localAddress;
                    dstSfuTransport.localPort = srcSfuTransport.localPort;
                    dstSfuTransport.protocol = srcSfuTransport.protocol;
                    dstSfuTransport.remoteAddress = srcSfuTransport.remoteAddress;
                    dstSfuTransport.remotePort = srcSfuTransport.remotePort;
                    dstSfuTransport.rtpBytesReceived = srcSfuTransport.rtpBytesReceived;
                    dstSfuTransport.rtpBytesSent = srcSfuTransport.rtpBytesSent;
                    dstSfuTransport.rtpPacketsReceived = srcSfuTransport.rtpPacketsReceived;
                    dstSfuTransport.rtpPacketsSent = srcSfuTransport.rtpPacketsSent;
                    dstSfuTransport.rtpPacketsLost = srcSfuTransport.rtpPacketsLost;
                    dstSfuTransport.rtxBytesReceived = srcSfuTransport.rtxBytesReceived;
                    dstSfuTransport.rtxBytesSent = srcSfuTransport.rtxBytesSent;
                    dstSfuTransport.rtxPacketsReceived = srcSfuTransport.rtxPacketsReceived;
                    dstSfuTransport.rtxPacketsSent = srcSfuTransport.rtxPacketsSent;
                    dstSfuTransport.rtxPacketsLost = srcSfuTransport.rtxPacketsLost;
                    dstSfuTransport.rtxPacketsDiscarded = srcSfuTransport.rtxPacketsDiscarded;
                    dstSfuTransport.sctpBytesReceived = srcSfuTransport.sctpBytesReceived;
                    dstSfuTransport.sctpBytesSent = srcSfuTransport.sctpBytesSent;
                    dstSfuTransport.sctpPacketsReceived = srcSfuTransport.sctpPacketsReceived;
                    dstSfuTransport.sctpPacketsSent = srcSfuTransport.sctpPacketsSent;
                    dstSfuTransports[j] = dstSfuTransport;
                }
                dstSfuSample.transports = dstSfuTransports;
            }

            if (srcSfuSample.inboundRtpPads != null) {
                var srcSfuInboundRtpPads = srcSfuSample.inboundRtpPads;
                var dstSfuInboundRtpPads = new org.observertc.schemas.samples.Samples.SfuSample.SfuInboundRtpPad[srcSfuInboundRtpPads.length];
                for (int j = 0; j < srcSfuInboundRtpPads.length; ++j) {
                    var srcSfuInboundRtpPad = srcSfuInboundRtpPads[j];
                    var dstSfuInboundRtpPad = new org.observertc.schemas.samples.Samples.SfuSample.SfuInboundRtpPad();
                    dstSfuInboundRtpPad.noReport = srcSfuInboundRtpPad.noReport;
                    dstSfuInboundRtpPad.transportId = srcSfuInboundRtpPad.transportId;
                    dstSfuInboundRtpPad.internal = srcSfuInboundRtpPad.internal;
                    dstSfuInboundRtpPad.streamId = srcSfuInboundRtpPad.streamId;
                    dstSfuInboundRtpPad.padId = srcSfuInboundRtpPad.padId;
                    dstSfuInboundRtpPad.ssrc = srcSfuInboundRtpPad.ssrc;
                    dstSfuInboundRtpPad.mediaType = srcSfuInboundRtpPad.mediaType;
                    dstSfuInboundRtpPad.payloadType = srcSfuInboundRtpPad.payloadType;
                    dstSfuInboundRtpPad.mimeType = srcSfuInboundRtpPad.mimeType;
                    dstSfuInboundRtpPad.clockRate = srcSfuInboundRtpPad.clockRate;
                    dstSfuInboundRtpPad.sdpFmtpLine = srcSfuInboundRtpPad.sdpFmtpLine;
                    dstSfuInboundRtpPad.rid = srcSfuInboundRtpPad.rid;
                    dstSfuInboundRtpPad.rtxSsrc = srcSfuInboundRtpPad.rtxSsrc;
                    dstSfuInboundRtpPad.targetBitrate = srcSfuInboundRtpPad.targetBitrate;
                    dstSfuInboundRtpPad.voiceActivityFlag = srcSfuInboundRtpPad.voiceActivityFlag;
                    dstSfuInboundRtpPad.firCount = srcSfuInboundRtpPad.firCount;
                    dstSfuInboundRtpPad.pliCount = srcSfuInboundRtpPad.pliCount;
                    dstSfuInboundRtpPad.nackCount = srcSfuInboundRtpPad.nackCount;
                    dstSfuInboundRtpPad.sliCount = srcSfuInboundRtpPad.sliCount;
                    dstSfuInboundRtpPad.packetsLost = srcSfuInboundRtpPad.packetsLost;
                    dstSfuInboundRtpPad.packetsReceived = srcSfuInboundRtpPad.packetsReceived;
                    dstSfuInboundRtpPad.packetsDiscarded = srcSfuInboundRtpPad.packetsDiscarded;
                    dstSfuInboundRtpPad.packetsRepaired = srcSfuInboundRtpPad.packetsRepaired;
                    dstSfuInboundRtpPad.packetsFailedDecryption = srcSfuInboundRtpPad.packetsFailedDecryption;
                    dstSfuInboundRtpPad.packetsDuplicated = srcSfuInboundRtpPad.packetsDuplicated;
                    dstSfuInboundRtpPad.fecPacketsReceived = srcSfuInboundRtpPad.fecPacketsReceived;
                    dstSfuInboundRtpPad.fecPacketsDiscarded = srcSfuInboundRtpPad.fecPacketsDiscarded;
                    dstSfuInboundRtpPad.bytesReceived = srcSfuInboundRtpPad.bytesReceived;
                    dstSfuInboundRtpPad.rtcpSrReceived = srcSfuInboundRtpPad.rtcpSrReceived;
                    dstSfuInboundRtpPad.rtcpRrSent = srcSfuInboundRtpPad.rtcpRrSent;
                    dstSfuInboundRtpPad.rtxPacketsReceived = srcSfuInboundRtpPad.rtxPacketsReceived;
                    dstSfuInboundRtpPad.rtxPacketsDiscarded = srcSfuInboundRtpPad.rtxPacketsDiscarded;
                    dstSfuInboundRtpPad.framesReceived = srcSfuInboundRtpPad.framesReceived;
                    dstSfuInboundRtpPad.framesDecoded = srcSfuInboundRtpPad.framesDecoded;
                    dstSfuInboundRtpPad.keyFramesDecoded = srcSfuInboundRtpPad.keyFramesDecoded;
                    dstSfuInboundRtpPad.fractionLost = srcSfuInboundRtpPad.fractionLost;
                    dstSfuInboundRtpPad.jitter = srcSfuInboundRtpPad.jitter;
                    dstSfuInboundRtpPad.roundTripTime = srcSfuInboundRtpPad.roundTripTime;
                    dstSfuInboundRtpPads[j] = dstSfuInboundRtpPad;
                }
                dstSfuSample.inboundRtpPads = dstSfuInboundRtpPads;
            }

            if (srcSfuSample.outboundRtpPads != null) {
                var srcSfuOutboundRtpPads = srcSfuSample.outboundRtpPads;
                var dstSfuOutboundRtpPads = new org.observertc.schemas.samples.Samples.SfuSample.SfuOutboundRtpPad[srcSfuOutboundRtpPads.length];
                for (int j = 0; j < srcSfuOutboundRtpPads.length; ++j) {
                    var srcSfuOutboundRtpPad = srcSfuOutboundRtpPads[j];
                    var dstSfuOutboundRtpPad = new org.observertc.schemas.samples.Samples.SfuSample.SfuOutboundRtpPad();
                    dstSfuOutboundRtpPad.noReport = srcSfuOutboundRtpPad.noReport;
                    dstSfuOutboundRtpPad.transportId = srcSfuOutboundRtpPad.transportId;
                    dstSfuOutboundRtpPad.internal = srcSfuOutboundRtpPad.internal;
                    dstSfuOutboundRtpPad.streamId = srcSfuOutboundRtpPad.streamId;
                    dstSfuOutboundRtpPad.sinkId = srcSfuOutboundRtpPad.sinkId;
                    dstSfuOutboundRtpPad.padId = srcSfuOutboundRtpPad.padId;
                    dstSfuOutboundRtpPad.ssrc = srcSfuOutboundRtpPad.ssrc;
                    dstSfuOutboundRtpPad.callId = srcSfuOutboundRtpPad.callId;
                    dstSfuOutboundRtpPad.clientId = srcSfuOutboundRtpPad.clientId;
                    dstSfuOutboundRtpPad.trackId = srcSfuOutboundRtpPad.trackId;
                    dstSfuOutboundRtpPad.mediaType = srcSfuOutboundRtpPad.mediaType;
                    dstSfuOutboundRtpPad.payloadType = srcSfuOutboundRtpPad.payloadType;
                    dstSfuOutboundRtpPad.mimeType = srcSfuOutboundRtpPad.mimeType;
                    dstSfuOutboundRtpPad.clockRate = srcSfuOutboundRtpPad.clockRate;
                    dstSfuOutboundRtpPad.sdpFmtpLine = srcSfuOutboundRtpPad.sdpFmtpLine;
                    dstSfuOutboundRtpPad.rid = srcSfuOutboundRtpPad.rid;
                    dstSfuOutboundRtpPad.rtxSsrc = srcSfuOutboundRtpPad.rtxSsrc;
                    dstSfuOutboundRtpPad.targetBitrate = srcSfuOutboundRtpPad.targetBitrate;
                    dstSfuOutboundRtpPad.voiceActivityFlag = srcSfuOutboundRtpPad.voiceActivityFlag;
                    dstSfuOutboundRtpPad.firCount = srcSfuOutboundRtpPad.firCount;
                    dstSfuOutboundRtpPad.pliCount = srcSfuOutboundRtpPad.pliCount;
                    dstSfuOutboundRtpPad.nackCount = srcSfuOutboundRtpPad.nackCount;
                    dstSfuOutboundRtpPad.sliCount = srcSfuOutboundRtpPad.sliCount;
                    dstSfuOutboundRtpPad.packetsLost = srcSfuOutboundRtpPad.packetsLost;
                    dstSfuOutboundRtpPad.packetsSent = srcSfuOutboundRtpPad.packetsSent;
                    dstSfuOutboundRtpPad.packetsDiscarded = srcSfuOutboundRtpPad.packetsDiscarded;
                    dstSfuOutboundRtpPad.packetsRetransmitted = srcSfuOutboundRtpPad.packetsRetransmitted;
                    dstSfuOutboundRtpPad.packetsFailedEncryption = srcSfuOutboundRtpPad.packetsFailedEncryption;
                    dstSfuOutboundRtpPad.packetsDuplicated = srcSfuOutboundRtpPad.packetsDuplicated;
                    dstSfuOutboundRtpPad.fecPacketsSent = srcSfuOutboundRtpPad.fecPacketsSent;
                    dstSfuOutboundRtpPad.fecPacketsDiscarded = srcSfuOutboundRtpPad.fecPacketsDiscarded;
                    dstSfuOutboundRtpPad.bytesSent = srcSfuOutboundRtpPad.bytesSent;
                    dstSfuOutboundRtpPad.rtcpSrSent = srcSfuOutboundRtpPad.rtcpSrSent;
                    dstSfuOutboundRtpPad.rtcpRrReceived = srcSfuOutboundRtpPad.rtcpRrReceived;
                    dstSfuOutboundRtpPad.rtxPacketsSent = srcSfuOutboundRtpPad.rtxPacketsSent;
                    dstSfuOutboundRtpPad.rtxPacketsDiscarded = srcSfuOutboundRtpPad.rtxPacketsDiscarded;
                    dstSfuOutboundRtpPad.framesSent = srcSfuOutboundRtpPad.framesSent;
                    dstSfuOutboundRtpPad.framesEncoded = srcSfuOutboundRtpPad.framesEncoded;
                    dstSfuOutboundRtpPad.keyFramesEncoded = srcSfuOutboundRtpPad.keyFramesEncoded;
                    dstSfuOutboundRtpPad.fractionLost = srcSfuOutboundRtpPad.fractionLost;
                    dstSfuOutboundRtpPad.jitter = srcSfuOutboundRtpPad.jitter;
                    dstSfuOutboundRtpPad.roundTripTime = srcSfuOutboundRtpPad.roundTripTime;
                    dstSfuOutboundRtpPads[j] = dstSfuOutboundRtpPad;
                }
                dstSfuSample.outboundRtpPads = dstSfuOutboundRtpPads;
            }

            if (srcSfuSample.sctpChannels != null) {
                var srcSfuSctpChannels = srcSfuSample.sctpChannels;
                var dstSfuSctpChannels = new org.observertc.schemas.samples.Samples.SfuSample.SfuSctpChannel[srcSfuSctpChannels.length];
                for (int j = 0; j < srcSfuSctpChannels.length; ++j) {
                    var srcSfuSctpChannel = srcSfuSctpChannels[j];
                    var dstSfuSctpChannel = new org.observertc.schemas.samples.Samples.SfuSample.SfuSctpChannel();
                    dstSfuSctpChannel.noReport = srcSfuSctpChannel.noReport;
                    dstSfuSctpChannel.transportId = srcSfuSctpChannel.transportId;
                    dstSfuSctpChannel.streamId = srcSfuSctpChannel.streamId;
                    dstSfuSctpChannel.channelId = srcSfuSctpChannel.channelId;
                    dstSfuSctpChannel.label = srcSfuSctpChannel.label;
                    dstSfuSctpChannel.protocol = srcSfuSctpChannel.protocol;
                    dstSfuSctpChannel.sctpSmoothedRoundTripTime = srcSfuSctpChannel.sctpSmoothedRoundTripTime;
                    dstSfuSctpChannel.sctpCongestionWindow = srcSfuSctpChannel.sctpCongestionWindow;
                    dstSfuSctpChannel.sctpReceiverWindow = srcSfuSctpChannel.sctpReceiverWindow;
                    dstSfuSctpChannel.sctpMtu = srcSfuSctpChannel.sctpMtu;
                    dstSfuSctpChannel.sctpUnackData = srcSfuSctpChannel.sctpUnackData;
                    dstSfuSctpChannel.messageReceived = srcSfuSctpChannel.messageReceived;
                    dstSfuSctpChannel.messageSent = srcSfuSctpChannel.messageSent;
                    dstSfuSctpChannel.bytesReceived = srcSfuSctpChannel.bytesReceived;
                    dstSfuSctpChannel.bytesSent = srcSfuSctpChannel.bytesSent;
                    dstSfuSctpChannels[j] = dstSfuSctpChannel;
                }
                dstSfuSample.sctpChannels = dstSfuSctpChannels;
            }

            if (srcSfuSample.extensionStats != null) {
                var srcSfuExtensionStatsArray = srcSfuSample.extensionStats;
                var dstSfuExtensionStatsArray = new org.observertc.schemas.samples.Samples.SfuSample.SfuExtensionStats[srcSfuExtensionStatsArray.length];
                for (int j = 0; j < srcSfuExtensionStatsArray.length; ++j) {
                    var srcSfuExtensionStats = srcSfuExtensionStatsArray[j];
                    var dstSfuExtensionStats = new org.observertc.schemas.samples.Samples.SfuSample.SfuExtensionStats();
                    dstSfuExtensionStats.type = srcSfuExtensionStats.type;
                    dstSfuExtensionStats.payload = srcSfuExtensionStats.payload;
                    dstSfuExtensionStatsArray[j] = dstSfuExtensionStats;
                }
                dstSfuSample.extensionStats = dstSfuExtensionStatsArray;
            }

            dstSfuSamples[index] = dstSfuSample;
        }
        return dstSfuSamples;
    }

    private org.observertc.schemas.samples.Samples.TurnSample[] getTurnSamples(Samples.TurnSample[] srcTurnSamples) {
        if (srcTurnSamples == null) return null;
        var dstTurnSamples = new org.observertc.schemas.samples.Samples.TurnSample[srcTurnSamples.length];
        for (int index = 0; index < dstTurnSamples.length; ++index) {
            var srcTurnSample = srcTurnSamples[index];
            var dstTurnSample = new org.observertc.schemas.samples.Samples.TurnSample();
            dstTurnSample.serverId = srcTurnSample.serverId;

            if (srcTurnSample.allocations != null) {
                var srcTurnPeerAllocations = srcTurnSample.allocations;
                var dstTurnPeerAllocations = new org.observertc.schemas.samples.Samples.TurnSample.TurnPeerAllocation[srcTurnPeerAllocations.length];
                for (int j = 0; j < srcTurnPeerAllocations.length; ++j) {
                    var srcTurnPeerAllocation = srcTurnPeerAllocations[j];
                    var dstTurnPeerAllocation = new org.observertc.schemas.samples.Samples.TurnSample.TurnPeerAllocation();
                    dstTurnPeerAllocation.peerId = srcTurnPeerAllocation.peerId;
                    dstTurnPeerAllocation.sessionId = srcTurnPeerAllocation.sessionId;
                    dstTurnPeerAllocation.relayedAddress = srcTurnPeerAllocation.relayedAddress;
                    dstTurnPeerAllocation.relayedPort = srcTurnPeerAllocation.relayedPort;
                    dstTurnPeerAllocation.transportProtocol = srcTurnPeerAllocation.transportProtocol;
                    dstTurnPeerAllocation.peerAddress = srcTurnPeerAllocation.peerAddress;
                    dstTurnPeerAllocation.peerPort = srcTurnPeerAllocation.peerPort;
                    dstTurnPeerAllocation.sendingBitrate = srcTurnPeerAllocation.sendingBitrate;
                    dstTurnPeerAllocation.receivingBitrate = srcTurnPeerAllocation.receivingBitrate;
                    dstTurnPeerAllocation.sentBytes = srcTurnPeerAllocation.sentBytes;
                    dstTurnPeerAllocation.receivedBytes = srcTurnPeerAllocation.receivedBytes;
                    dstTurnPeerAllocation.sentPackets = srcTurnPeerAllocation.sentPackets;
                    dstTurnPeerAllocation.receivedPackets = srcTurnPeerAllocation.receivedPackets;
                    dstTurnPeerAllocations[j] = dstTurnPeerAllocation;
                }
                dstTurnSample.allocations = dstTurnPeerAllocations;
            }

            if (srcTurnSample.sessions != null) {
                var srcTurnSessions = srcTurnSample.sessions;
                var dstTurnSessions = new org.observertc.schemas.samples.Samples.TurnSample.TurnSession[srcTurnSessions.length];
                for (int j = 0; j < srcTurnSessions.length; ++j) {
                    var srcTurnSession = srcTurnSessions[j];
                    var dstTurnSession = new org.observertc.schemas.samples.Samples.TurnSample.TurnSession();
                    dstTurnSession.sessionId = srcTurnSession.sessionId;
                    dstTurnSession.realm = srcTurnSession.realm;
                    dstTurnSession.username = srcTurnSession.username;
                    dstTurnSession.clientId = srcTurnSession.clientId;
                    dstTurnSession.started = srcTurnSession.started;
                    dstTurnSession.nonceExpirationTime = srcTurnSession.nonceExpirationTime;
                    dstTurnSession.serverAddress = srcTurnSession.serverAddress;
                    dstTurnSession.serverPort = srcTurnSession.serverPort;
                    dstTurnSession.transportProtocol = srcTurnSession.transportProtocol;
                    dstTurnSession.clientAddress = srcTurnSession.clientAddress;
                    dstTurnSession.clientPort = srcTurnSession.clientPort;
                    dstTurnSession.sendingBitrate = srcTurnSession.sendingBitrate;
                    dstTurnSession.receivingBitrate = srcTurnSession.receivingBitrate;
                    dstTurnSession.sentBytes = srcTurnSession.sentBytes;
                    dstTurnSession.receivedBytes = srcTurnSession.receivedBytes;
                    dstTurnSession.sentPackets = srcTurnSession.sentPackets;
                    dstTurnSession.receivedPackets = srcTurnSession.receivedPackets;
                    dstTurnSessions[j] = dstTurnSession;
                }
                dstTurnSample.sessions = dstTurnSessions;
            }

            dstTurnSamples[index] = dstTurnSample;
        }
        return dstTurnSamples;
    }
}
