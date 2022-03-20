package org.observertc.observer.evaluators;

import io.micrometer.core.annotation.Timed;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.reports.Report;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.repositories.tasks.FetchTracksRelationsTask;
import org.observertc.observer.samples.*;
import org.observertc.schemas.reports.*;
import org.observertc.schemas.samples.Samples.ClientSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

import static org.observertc.observer.micrometer.ExposedMetrics.OBSERVERTC_EVALUATORS_DEMUX_COLLECTED_CALL_SAMPLES_TIME;

@Prototype
public class DemuxCollectedCallSamples implements Consumer<CollectedCallSamples> {
    private static final Logger logger = LoggerFactory.getLogger(DemuxCollectedCallSamples.class);
//    private static final String _____missingFromSample____ = "I did not find it in the ClientSample";
//    private static final String _____missingByBound____ = "I did not find it in to be bound from the observed sample?";
    private Subject<List<Report>> reportSubject = PublishSubject.create();

    public Observable<List<Report>> getObservableReport() {
        return this.reportSubject;
    }

    @Inject
    ObserverConfig observerConfig;

    @Inject
    Provider<FetchTracksRelationsTask> matchCallTracksTaskProvider;

    @PostConstruct
    void setup() {

    }

    @Override
    @Timed(value = OBSERVERTC_EVALUATORS_DEMUX_COLLECTED_CALL_SAMPLES_TIME)
    public void accept(CollectedCallSamples collectedCallSamples) throws Throwable {
        try {
            this.doAccept(collectedCallSamples);
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while demultiplexing collected samples", ex);
        }
    }

    public void doAccept(CollectedCallSamples collectedCallSamples) throws Throwable {
        Map<UUID, FetchTracksRelationsTask.MatchedIds> inboundTrackMatchIds = this.makeInboundTrackMatchIds(collectedCallSamples);

        List<Report> reports = new LinkedList<>();
        Map<String, String> peerConnectionLabels = new HashMap<>();
        for (CallSamples callSamples : collectedCallSamples) {
            var callId = callSamples.getCallId();
            for (ClientSamples clientSamples: callSamples) {
                ObservedClientSample observedClientSample = clientSamples;
                for (ClientSample clientSample : clientSamples) {
                        ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
                                .map(peerConnectionTransport -> {
                                    peerConnectionLabels.put(peerConnectionTransport.peerConnectionId, peerConnectionTransport.label);
                                    return this.createPeerConnectionTransportReport(
                                            callId,
                                            observedClientSample,
                                            peerConnectionTransport
                                    );

                                })
                                .filter(Objects::nonNull)
                                .map(Report::fromClientTransportReport)
                                .forEach(reports::add);

                    ClientSampleVisitor.streamDataChannels(clientSample)
                            .map(dataChannel -> {
                                String peerConnectionLabel = peerConnectionLabels.get(dataChannel.peerConnectionId);
                                return this.createClientDataChannelReport(
                                        callId,
                                        observedClientSample,
                                        peerConnectionLabel,
                                        dataChannel
                                );
                            })
                            .filter(Objects::nonNull)
                            .map(Report::fromClientDataChannelReport)
                            .forEach(reports::add);

                    ClientSampleVisitor.streamExtensionStats(clientSample)
                            .map(extensionStat -> {
                                return this.createClientExtensionReport(
                                        callId,
                                        observedClientSample,
                                        extensionStat
                                );
                            })
                            .filter(Objects::nonNull)
                            .map(Report::fromClientExtensionReport)
                            .forEach(reports::add);

                    ClientSampleVisitor.streamInboundAudioTracks(clientSample)
                                .map(inboundAudioTrack -> {
                                    UUID trackId = UUIDAdapter.tryParseOrNull(inboundAudioTrack.trackId);
                                    FetchTracksRelationsTask.MatchedIds matchedIds = Objects.nonNull(trackId) ? inboundTrackMatchIds.get(trackId) : null;
                                    String peerConnectionLabel = peerConnectionLabels.get(inboundAudioTrack.peerConnectionId);
                                    return this.createInboundAudioTrackReport(
                                            callId,
                                            observedClientSample,
                                            matchedIds,
                                            peerConnectionLabel,
                                            inboundAudioTrack
                                    );
                                })
                                .filter(Objects::nonNull)
                                .map(Report::fromInboundAudioTrackReport)
                                .forEach(reports::add);

                        ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                            .map(inboundVideoTrack -> {
                                UUID trackId = UUIDAdapter.tryParseOrNull(inboundVideoTrack.trackId);
                                FetchTracksRelationsTask.MatchedIds matchedIds = Objects.nonNull(trackId) ? inboundTrackMatchIds.get(trackId) : null;
                                String peerConnectionLabel = peerConnectionLabels.get(inboundVideoTrack.peerConnectionId);
                                return this.createInboundVideoTrackReport(
                                        callId,
                                        observedClientSample,
                                        matchedIds,
                                        peerConnectionLabel,
                                        inboundVideoTrack
                                );
                            })
                            .filter(Objects::nonNull)
                            .map(Report::fromInboundVideoTrackReport)
                            .forEach(reports::add);


                        ClientSampleVisitor.streamOutboundAudioTracks(clientSample)
                            .map(outboundAudioTrack -> {
                                String peerConnectionLabel = peerConnectionLabels.get(outboundAudioTrack.peerConnectionId);
                                return this.createOutboundAudioTrackReport(
                                        callId,
                                        observedClientSample,
                                        peerConnectionLabel,
                                        outboundAudioTrack
                                );
                            })
                            .filter(Objects::nonNull)
                            .map(Report::fromOutboundAudioTrackReport)
                            .forEach(reports::add);

                    ClientSampleVisitor.streamOutboundVideoTracks(clientSample)
                            .map(outboundVideoTrack -> {
                                String peerConnectionLabel = peerConnectionLabels.get(outboundVideoTrack.peerConnectionId);
                                return this.createOutboundVideoTrackReport(
                                        callId,
                                        observedClientSample,
                                        peerConnectionLabel,
                                        outboundVideoTrack
                                );
                            })
                            .filter(Objects::nonNull)
                            .map(Report::fromOutboundVideoTrackReport)
                            .forEach(reports::add);

                }
            }
        }
        synchronized (this) {
            this.reportSubject.onNext(reports);
        }
    }

    private InboundAudioTrackReport createInboundAudioTrackReport(
            UUID callId,
            ObservedClientSample observedClientSample,
            FetchTracksRelationsTask.MatchedIds matchedIds,
            String peerConnectionLabel,
            ClientSample.InboundAudioTrack inboundAudioTrack
    ) {
        try {
            String remoteClientId = null;
            String remoteUserId = null;
            String remotePeerConnectionId = null;
            String remoteTrackId = null;

            if (Objects.nonNull(matchedIds)) {
                remoteClientId = UUIDAdapter.toStringOrNull(matchedIds.outboundClientId);
                remoteUserId = matchedIds.outboundUserId;
                remotePeerConnectionId = UUIDAdapter.toStringOrNull(matchedIds.outboundPeerConnectionId);
                remoteTrackId = UUIDAdapter.toStringOrNull(matchedIds.inboundTrackId);
            }
            var result = InboundAudioTrackReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(observedClientSample.getMarker())
                    .setTimestamp(observedClientSample.getTimestamp())

                    /* Peer Connection Report Fields */
                    .setCallId(callId.toString())
                    .setRoomId(observedClientSample.getRoomId())
                    .setClientId(observedClientSample.getClientId().toString())
                    .setUserId(observedClientSample.getUserId())
                    .setPeerConnectionId(inboundAudioTrack.peerConnectionId)
                    .setLabel(peerConnectionLabel)
                    .setTrackId(inboundAudioTrack.trackId)

                    /* Remote Identifier */
                    .setRemoteClientId(remoteClientId)
                    .setRemoteUserId(remoteUserId)
                    .setRemotePeerConnectionId(remotePeerConnectionId)
                    .setRemoteTrackId(remoteTrackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(observedClientSample.getSampleSeq())


                    /* Inbound RTP Audio specific fields */
                    .setSsrc(inboundAudioTrack.ssrc)
                    .setPacketsReceived(inboundAudioTrack.packetsReceived)
                    .setPacketsSent(inboundAudioTrack.packetsSent)
                    .setPacketsLost(inboundAudioTrack.packetsLost)
                    .setJitter(inboundAudioTrack.jitter)
                    .setPacketsDiscarded(inboundAudioTrack.packetsDiscarded)
                    .setPacketsRepaired(inboundAudioTrack.packetsRepaired)
                    .setBurstPacketsLost(inboundAudioTrack.burstPacketsLost)
                    .setBurstPacketsDiscarded(inboundAudioTrack.burstPacketsDiscarded)
                    .setBurstLossCount(inboundAudioTrack.burstLossCount)
                    .setBurstDiscardCount(inboundAudioTrack.burstDiscardCount)
                    .setBurstLossRate(inboundAudioTrack.burstLossRate)
                    .setBurstDiscardRate(inboundAudioTrack.burstDiscardRate)
                    .setGapLossRate(inboundAudioTrack.gapLossRate)
                    .setGapDiscardRate(inboundAudioTrack.gapLossRate)
                    .setGapDiscardRate(inboundAudioTrack.gapDiscardRate)
                    .setVoiceActivityFlag(inboundAudioTrack.voiceActivityFlag)
                    .setLastPacketReceivedTimestamp(inboundAudioTrack.lastPacketReceivedTimestamp)
                    .setAverageRtcpInterval(inboundAudioTrack.averageRtcpInterval)
                    .setHeaderBytesReceived(inboundAudioTrack.headerBytesReceived)
                    .setFecPacketsReceived(inboundAudioTrack.fecPacketsReceived)
                    .setFecPacketsDiscarded(inboundAudioTrack.fecPacketsDiscarded)
                    .setBytesReceived(inboundAudioTrack.bytesReceived)
                    .setPacketsFailedDecryption(inboundAudioTrack.packetsFailedDecryption)
                    .setPacketsDuplicated(inboundAudioTrack.packetsDuplicated)
                    .setPerDscpPacketsReceived(inboundAudioTrack.perDscpPacketsReceived)
                    .setNackCount(inboundAudioTrack.nackCount)
                    .setTotalProcessingDelay(inboundAudioTrack.totalProcessingDelay)
                    .setEstimatedPlayoutTimestamp(inboundAudioTrack.estimatedPlayoutTimestamp)
                    .setJitterBufferDelay(inboundAudioTrack.jitterBufferDelay)
                    .setJitterBufferEmittedCount(inboundAudioTrack.jitterBufferEmittedCount)
                    .setDecoderImplementation(inboundAudioTrack.decoderImplementation)


                    /* Remote Outbound RTP Audio specific fields */
                    .setPacketsSent(inboundAudioTrack.packetsSent)
                    .setBytesSent(inboundAudioTrack.bytesSent)
                    .setRemoteTimestamp(inboundAudioTrack.remoteTimestamp)
                    .setReportsSent(inboundAudioTrack.reportsSent)

                    /* Receiver related stats */
                    .setEnded(inboundAudioTrack.ended)

                    /* Codec Specific fields  */
                    .setPayloadType(inboundAudioTrack.payloadType)
                    .setMimeType(inboundAudioTrack.mimeType)
                    .setClockRate(inboundAudioTrack.clockRate)
                    .setChannels(inboundAudioTrack.channels)
                    .setSdpFmtpLine(inboundAudioTrack.sdpFmtpLine)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building InboundAudioTrackReport", ex);
            return null;
        }
    }

    private InboundVideoTrackReport createInboundVideoTrackReport(
            UUID callId,
            ObservedClientSample observedClientSample,
            FetchTracksRelationsTask.MatchedIds matchedIds,
            String peerConnectionLabel,
            ClientSample.InboundVideoTrack inboundVideoTrack
    ) {
        try {
            String remoteClientId = null;
            String remoteUserId = null;
            String remotePeerConnectionId = null;
            String remoteTrackId = null;

            if (Objects.nonNull(matchedIds)) {
                remoteClientId = UUIDAdapter.toStringOrNull(matchedIds.outboundClientId);
                remoteUserId = matchedIds.outboundUserId;
                remotePeerConnectionId = UUIDAdapter.toStringOrNull(matchedIds.outboundPeerConnectionId);
                remoteTrackId = UUIDAdapter.toStringOrNull(matchedIds.inboundTrackId);
            }
            var result = InboundVideoTrackReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(observedClientSample.getMarker())
                    .setTimestamp(observedClientSample.getTimestamp())

                    /* Peer Connection Report Fields */
                    .setCallId(callId.toString())
                    .setRoomId(observedClientSample.getRoomId())
                    .setClientId(observedClientSample.getClientId().toString())
                    .setUserId(observedClientSample.getUserId())
                    .setPeerConnectionId(inboundVideoTrack.peerConnectionId)
                    .setLabel(peerConnectionLabel)
                    .setTrackId(inboundVideoTrack.trackId)

                    /* Remote Identifier */
                    .setRemoteClientId(remoteClientId)
                    .setRemoteUserId(remoteUserId)
                    .setRemotePeerConnectionId(remotePeerConnectionId)
                    .setRemoteTrackId(remoteTrackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(observedClientSample.getSampleSeq())


                    /* Inbound RTP Video specific fields */
                    .setSsrc(inboundVideoTrack.ssrc)
                    .setPacketsReceived(inboundVideoTrack.packetsReceived)
                    .setPacketsLost(inboundVideoTrack.packetsLost)
                    .setJitter(inboundVideoTrack.jitter)
                    .setPacketsDiscarded(inboundVideoTrack.packetsDiscarded)
                    .setPacketsRepaired(inboundVideoTrack.packetsRepaired)
                    .setBurstPacketsLost(inboundVideoTrack.burstPacketsLost)
                    .setBurstPacketsDiscarded(inboundVideoTrack.burstPacketsDiscarded)
                    .setBurstLossCount(inboundVideoTrack.burstLossCount)
                    .setBurstDiscardCount(inboundVideoTrack.burstDiscardCount)
                    .setBurstLossRate(inboundVideoTrack.burstLossRate)
                    .setBurstDiscardRate(inboundVideoTrack.burstDiscardRate)
                    .setGapLossRate(inboundVideoTrack.gapLossRate)
                    .setGapDiscardRate(inboundVideoTrack.gapDiscardRate)
                    .setFramesDropped(inboundVideoTrack.framesDropped)
                    .setPartialFramesLost(inboundVideoTrack.partialFramesLost)
                    .setFullFramesLost(inboundVideoTrack.fullFramesLost)
                    .setFramesDecoded(inboundVideoTrack.framesDecoded)
                    .setKeyFramesDecoded(inboundVideoTrack.keyFramesDecoded)
                    .setFrameWidth(inboundVideoTrack.frameWidth)
                    .setFrameHeight(inboundVideoTrack.frameHeight)
                    .setFrameBitDepth(inboundVideoTrack.frameBitDepth)
                    .setFramesPerSecond(inboundVideoTrack.framesPerSecond)
                    .setQpSum(inboundVideoTrack.qpSum)
                    .setTotalDecodeTime(inboundVideoTrack.totalDecodeTime)
                    .setTotalInterFrameDelay(inboundVideoTrack.totalInterFrameDelay)
                    .setTotalSquaredInterFrameDelay(inboundVideoTrack.totalSquaredInterFrameDelay)
                    .setLastPacketReceivedTimestamp(inboundVideoTrack.lastPacketReceivedTimestamp)
                    .setAverageRtcpInterval(inboundVideoTrack.averageRtcpInterval)
                    .setHeaderBytesReceived(inboundVideoTrack.headerBytesReceived)
                    .setFecPacketsReceived(inboundVideoTrack.fecPacketsReceived)
                    .setFecPacketsDiscarded(inboundVideoTrack.fecPacketsDiscarded)
                    .setBytesReceived(inboundVideoTrack.bytesReceived)
                    .setPacketsFailedDecryption(inboundVideoTrack.packetsFailedDecryption)
                    .setPacketsDuplicated(inboundVideoTrack.packetsDuplicated)
                    .setPerDscpPacketsReceived(inboundVideoTrack.perDscpPacketsReceived)
                    .setFirCount(inboundVideoTrack.firCount)
                    .setPliCount(inboundVideoTrack.pliCount)
                    .setNackCount(inboundVideoTrack.nackCount)
                    .setSliCount(inboundVideoTrack.sliCount)
                    .setTotalProcessingDelay(inboundVideoTrack.totalProcessingDelay)
                    .setEstimatedPlayoutTimestamp(inboundVideoTrack.estimatedPlayoutTimestamp)
                    .setJitterBufferDelay(inboundVideoTrack.jitterBufferDelay)
                    .setJitterBufferEmittedCount(inboundVideoTrack.jitterBufferEmittedCount)
                    .setFramesReceived(inboundVideoTrack.framesReceived)
                    .setDecoderImplementation(inboundVideoTrack.decoderImplementation)


                    /* Remote Outbound RTP Video specific fields */
                    .setPacketsSent(inboundVideoTrack.packetsSent)
                    .setBytesSent(inboundVideoTrack.bytesSent)
                    .setRemoteTimestamp(inboundVideoTrack.remoteTimestamp)
                    .setReportsSent(inboundVideoTrack.reportsSent)

                    /* Receiver related stats */
                    .setEnded(inboundVideoTrack.ended)

                    /* Codec Specific fields  */
                    .setPayloadType(inboundVideoTrack.payloadType)
                    .setMimeType(inboundVideoTrack.mimeType)
                    .setClockRate(inboundVideoTrack.clockRate)
                    .setSdpFmtpLine(inboundVideoTrack.sdpFmtpLine)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building InboundVideoTrackReport", ex);
            return null;
        }
    }


    private OutboundAudioTrackReport createOutboundAudioTrackReport(
            UUID callId,
            ObservedClientSample observedClientSample,
            String peerConnectionLabel,
            ClientSample.OutboundAudioTrack outboundAudioTrack
    ) {
        try {
            var result = OutboundAudioTrackReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(observedClientSample.getMarker())
                    .setTimestamp(observedClientSample.getTimestamp())

                    /* Peer Connection Report Fields */
                    .setCallId(callId.toString())
                    .setRoomId(observedClientSample.getRoomId())
                    .setClientId(observedClientSample.getClientId().toString())
                    .setUserId(observedClientSample.getUserId())
                    .setPeerConnectionId(outboundAudioTrack.peerConnectionId)
                    .setLabel(peerConnectionLabel)
                    .setTrackId(outboundAudioTrack.trackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(observedClientSample.getSampleSeq())

                    /* OutboundRTP related fields specific for Audio*/
                    .setSsrc(outboundAudioTrack.ssrc)
                    .setPacketsSent(outboundAudioTrack.packetsSent)
                    .setBytesSent(outboundAudioTrack.bytesSent)
                    .setRid(outboundAudioTrack.rid)
                    .setLastPacketSentTimestamp(outboundAudioTrack.lastPacketSentTimestamp)
                    .setHeaderBytesSent(outboundAudioTrack.headerBytesSent)
                    .setPacketsDiscarded(outboundAudioTrack.packetsDiscarded)
                    .setBytesDiscardedOnSend(outboundAudioTrack.bytesDiscardedOnSend)
                    .setPacketsSent(outboundAudioTrack.fecPacketsSent)
                    .setRetransmittedPacketsSent(outboundAudioTrack.retransmittedPacketsSent)
                    .setRetransmittedBytesSent(outboundAudioTrack.retransmittedBytesSent)
                    .setTargetBitrate(outboundAudioTrack.targetBitrate)
                    .setTotalEncodedBytesTarget(outboundAudioTrack.totalEncodedBytesTarget)
                    .setTotalSamplesSent(outboundAudioTrack.totalSamplesSent)
                    .setSamplesEncodedWithSilk(outboundAudioTrack.samplesEncodedWithSilk)
                    .setSamplesEncodedWithCelt(outboundAudioTrack.samplesEncodedWithCelt)
                    .setVoiceActivityFlag(outboundAudioTrack.voiceActivityFlag)
                    .setTotalPacketSendDelay(outboundAudioTrack.totalPacketSendDelay)
                    .setAverageRtcpInterval(outboundAudioTrack.averageRtcpInterval)
                    .setPerDscpPacketsSent(outboundAudioTrack.perDscpPacketsSent)
                    .setNackCount(outboundAudioTrack.nackCount)
                    .setEncoderImplementation(outboundAudioTrack.encoderImplementation)

                    /* Remote Inbound specific fields related to Audio */
                    .setPacketsReceived(outboundAudioTrack.packetsReceived)
                    .setPacketsLost(outboundAudioTrack.packetsLost)
                    .setJitter(outboundAudioTrack.jitter)
                    .setPacketsDiscarded(outboundAudioTrack.packetsDiscarded)
                    .setPacketsRepaired(outboundAudioTrack.packetsRepaired)
                    .setBurstPacketsLost(outboundAudioTrack.burstPacketsLost)
                    .setBurstPacketsDiscarded(outboundAudioTrack.burstPacketsDiscarded)
                    .setBurstPacketsLost(outboundAudioTrack.burstPacketsLost)
                    .setBurstDiscardCount(outboundAudioTrack.burstDiscardCount)
                    .setBurstLossRate(outboundAudioTrack.burstLossRate)
                    .setBurstDiscardRate(outboundAudioTrack.burstDiscardRate)
                    .setGapLossRate(outboundAudioTrack.gapLossRate)
                    .setGapDiscardRate(outboundAudioTrack.gapDiscardRate)
                    .setRoundTripTime(outboundAudioTrack.roundTripTime)
                    .setTotalRoundTripTime(outboundAudioTrack.totalRoundTripTime)
                    .setFractionLost(outboundAudioTrack.fractionLost)
                    .setReportsReceived(outboundAudioTrack.reportsReceived)
                    .setRoundTripTimeMeasurements(outboundAudioTrack.roundTripTimeMeasurements)

                    /* MediaSource related stats */
                    .setRelayedSource(outboundAudioTrack.relayedSource)
                    .setAudioLevel(outboundAudioTrack.audioLevel)
                    .setTotalAudioEnergy(outboundAudioTrack.totalAudioEnergy)
                    .setTotalSamplesDuration(outboundAudioTrack.totalSamplesDuration)
                    .setEchoReturnLoss(outboundAudioTrack.echoReturnLoss)
                    .setEchoReturnLossEnhancement(outboundAudioTrack.echoReturnLossEnhancement)

                    /* Sender related stats */
                    .setEnded(outboundAudioTrack.ended)

                    /* Codec Specific fields  */
                    .setPayloadType(outboundAudioTrack.payloadType)
                    .setMimeType(outboundAudioTrack.mimeType)
                    .setClockRate(outboundAudioTrack.clockRate)
                    .setChannels(outboundAudioTrack.channels)
                    .setSdpFmtpLine(outboundAudioTrack.sdpFmtpLine)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building OutboundAudioTrackReport", ex);
            return null;
        }
    }

    private OutboundVideoTrackReport createOutboundVideoTrackReport(
            UUID callId,
            ObservedClientSample observedClientSample,
            String peerConnectionLabel,
            ClientSample.OutboundVideoTrack outboundVideoTrack
    ) {
        try {
            var result = OutboundVideoTrackReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(observedClientSample.getMarker())
                    .setTimestamp(observedClientSample.getTimestamp())

                    /* Peer Connection Report Fields */
                    .setCallId(callId.toString())
                    .setRoomId(observedClientSample.getRoomId())
                    .setClientId(observedClientSample.getClientId().toString())
                    .setUserId(observedClientSample.getUserId())
                    .setPeerConnectionId(outboundVideoTrack.peerConnectionId)
                    .setLabel(peerConnectionLabel)
                    .setTrackId(outboundVideoTrack.trackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(observedClientSample.getSampleSeq())

                    /* OutboundRTP related fields specific for Audio*/
                    .setSsrc(outboundVideoTrack.ssrc)
                    .setPacketsSent(outboundVideoTrack.packetsSent)
                    .setBytesSent(outboundVideoTrack.bytesSent)
                    .setRid(outboundVideoTrack.rid)
                    .setLastPacketSentTimestamp(outboundVideoTrack.lastPacketSentTimestamp)
                    .setHeaderBytesSent(outboundVideoTrack.headerBytesSent)
                    .setPacketsDiscarded(outboundVideoTrack.packetsDiscarded)
                    .setBytesDiscardedOnSend(outboundVideoTrack.bytesDiscardedOnSend)
                    .setPacketsSent(outboundVideoTrack.fecPacketsSent)
                    .setRetransmittedPacketsSent(outboundVideoTrack.retransmittedPacketsSent)
                    .setRetransmittedBytesSent(outboundVideoTrack.retransmittedBytesSent)
                    .setTargetBitrate(outboundVideoTrack.targetBitrate)
                    .setTotalEncodedBytesTarget(outboundVideoTrack.totalEncodedBytesTarget)
                    .setFrameWidth(outboundVideoTrack.frameWidth)
                    .setFrameHeight(outboundVideoTrack.frameHeight)
                    .setFrameBitDepth(outboundVideoTrack.frameBitDepth)
                    .setFramesPerSecond(outboundVideoTrack.framesPerSecond)
                    .setFramesSent(outboundVideoTrack.framesSent)
                    .setHugeFramesSent(outboundVideoTrack.hugeFramesSent)
                    .setFramesEncoded(outboundVideoTrack.framesEncoded)
                    .setKeyFramesEncoded(outboundVideoTrack.keyFramesEncoded)
                    .setFramesDiscardedOnSend(outboundVideoTrack.framesDiscardedOnSend)
                    .setQpSum(outboundVideoTrack.qpSum)
                    .setTotalEncodeTime(outboundVideoTrack.totalEncodeTime)
                    .setTotalPacketSendDelay(outboundVideoTrack.totalPacketSendDelay)
                    .setAverageRtcpInterval(outboundVideoTrack.averageRtcpInterval)
                    .setQualityLimitationReason(outboundVideoTrack.qualityLimitationReason)
                    .setQualityLimitationDurationNone(outboundVideoTrack.qualityLimitationDurationNone)
                    .setQualityLimitationDurationCPU(outboundVideoTrack.qualityLimitationDurationCPU)
                    .setQualityLimitationDurationBandwidth(outboundVideoTrack.qualityLimitationDurationBandwidth)
                    .setQualityLimitationDurationOther(outboundVideoTrack.qualityLimitationDurationOther)
                    .setQualityLimitationResolutionChanges(outboundVideoTrack.qualityLimitationResolutionChanges)
                    .setPerDscpPacketsSent(outboundVideoTrack.perDscpPacketsSent)
                    .setNackCount(outboundVideoTrack.nackCount)
                    .setFirCount(outboundVideoTrack.firCount)
                    .setPliCount(outboundVideoTrack.pliCount)
                    .setSliCount(outboundVideoTrack.sliCount)
                    .setEncoderImplementation(outboundVideoTrack.encoderImplementation)


                    /* Remote Inbound specific fields related to Video */
                    .setPacketsReceived(outboundVideoTrack.packetsReceived)
                    .setPacketsLost(outboundVideoTrack.packetsLost)
                    .setJitter(outboundVideoTrack.jitter)
                    .setPacketsDiscarded(outboundVideoTrack.packetsDiscarded)
                    .setPacketsRepaired(outboundVideoTrack.packetsRepaired)
                    .setBurstPacketsLost(outboundVideoTrack.burstPacketsLost)
                    .setBurstPacketsDiscarded(outboundVideoTrack.burstPacketsDiscarded)
                    .setBurstLossCount(outboundVideoTrack.burstLossCount)
                    .setBurstDiscardCount(outboundVideoTrack.burstLossCount)
                    .setBurstLossRate(outboundVideoTrack.burstLossRate)
                    .setBurstDiscardRate(outboundVideoTrack.burstDiscardRate)
                    .setGapLossRate(outboundVideoTrack.gapLossRate)
                    .setGapDiscardRate(outboundVideoTrack.gapDiscardRate)
                    .setFramesDropped(outboundVideoTrack.framesDropped)
                    .setPartialFramesLost(outboundVideoTrack.partialFramesLost)
                    .setFullFramesLost(outboundVideoTrack.fullFramesLost)
                    .setRoundTripTime(outboundVideoTrack.roundTripTime)
                    .setTotalRoundTripTime(outboundVideoTrack.totalRoundTripTime)
                    .setFractionLost(outboundVideoTrack.fractionLost)
                    .setReportsReceived(outboundVideoTrack.reportsReceived)
                    .setRoundTripTimeMeasurements(outboundVideoTrack.roundTripTimeMeasurements)

                    /* MediaSource related stats */
                    .setRelayedSource(outboundVideoTrack.relayedSource)
                    .setEncodedFrameWidth(outboundVideoTrack.width)
                    .setEncodedFrameHeight(outboundVideoTrack.height)
                    .setEncodedFrameBitDepth(outboundVideoTrack.bitDepth)
                    .setEncodedFramesPerSecond(outboundVideoTrack.framesPerSecond)

                    /* Sender related stats */
                    .setEnded(outboundVideoTrack.ended)

                    /* Codec Specific fields  */
                    .setPayloadType(outboundVideoTrack.payloadType)
                    .setMimeType(outboundVideoTrack.mimeType)
                    .setClockRate(outboundVideoTrack.clockRate)
                    .setChannels(outboundVideoTrack.channels)
                    .setSdpFmtpLine(outboundVideoTrack.sdpFmtpLine)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building OutboundAudioTrackReport", ex);
            return null;
        }
    }

    private ClientTransportReport createPeerConnectionTransportReport(
        UUID callId,
        ObservedClientSample observedClientSample,
        ClientSample.PeerConnectionTransport peerConnectionTransport
    ) {

        try {
            var result = ClientTransportReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(observedClientSample.getMarker())
                    .setTimestamp(observedClientSample.getTimestamp())

                    /* Peer Connection Report Fields */
                    .setCallId(callId.toString())
                    .setRoomId(observedClientSample.getRoomId())
                    .setClientId(observedClientSample.getClientId().toString())
                    .setUserId(observedClientSample.getUserId())
                    .setPeerConnectionId(peerConnectionTransport.peerConnectionId)
                    .setLabel(peerConnectionTransport.label)

                    /* Transport stats */
                    .setPacketsSent(peerConnectionTransport.packetsSent)
                    .setPacketsReceived(peerConnectionTransport.packetsReceived)
                    .setBytesSent(peerConnectionTransport.bytesSent)
                    .setBytesReceived(peerConnectionTransport.bytesReceived)
                    .setIceRole(peerConnectionTransport.iceRole)
                    .setIceLocalUsernameFragment(peerConnectionTransport.iceLocalUsernameFragment)
                    .setDtlsState(peerConnectionTransport.dtlsState)
                    .setIceTransportState(peerConnectionTransport.iceState)
                    .setTlsVersion(peerConnectionTransport.tlsVersion)
                    .setDtlsCipher(peerConnectionTransport.dtlsCipher)
                    .setSrtpCipher(peerConnectionTransport.srtpCipher)
                    .setTlsGroup(peerConnectionTransport.tlsGroup)
                    .setSelectedCandidatePairChanges(peerConnectionTransport.selectedCandidatePairChanges)

                    /* ICE Local Candidate */
                    .setLocalAddress(peerConnectionTransport.localAddress)
                    .setLocalPort(peerConnectionTransport.localPort)
                    .setLocalProtocol(peerConnectionTransport.localProtocol)
                    .setLocalCandidateType(peerConnectionTransport.localCandidateType)
                    .setLocalCandidateICEServerUrl(peerConnectionTransport.localCandidateICEServerUrl)
                    .setLocalCandidateRelayProtocol(peerConnectionTransport.localCandidateRelayProtocol)

                    /* ICE Remote Candidate */
                    .setRemoteAddress(peerConnectionTransport.remoteAddress)
                    .setRemotePort(peerConnectionTransport.remotePort)
                    .setRemoteProtocol(peerConnectionTransport.remoteProtocol)
                    .setRemoteCandidateType(peerConnectionTransport.remoteCandidateType)
                    .setRemoteCandidateICEServerUrl(peerConnectionTransport.remoteCandidateICEServerUrl)
                    .setRemoteCandidateRelayProtocol(peerConnectionTransport.remoteCandidateRelayProtocol)

                    /* ICE Candidate Pair*/
                    .setCandidatePairState(peerConnectionTransport.candidatePairState)
                    .setCandidatePairPacketsSent(peerConnectionTransport.candidatePairPacketsSent)
                    .setCandidatePairPacketsReceived(peerConnectionTransport.candidatePairPacketsReceived)
                    .setCandidatePairBytesSent(peerConnectionTransport.candidatePairBytesSent)
                    .setCandidatePairBytesReceived(peerConnectionTransport.bytesReceived)
                    .setCandidatePairLastPacketSentTimestamp(peerConnectionTransport.candidatePairLastPacketSentTimestamp)
                    .setCandidatePairLastPacketReceivedTimestamp(peerConnectionTransport.candidatePairLastPacketReceivedTimestamp)
                    .setCandidatePairFirstRequestTimestamp(peerConnectionTransport.candidatePairFirstRequestTimestamp)
                    .setCandidatePairLastRequestTimestamp(peerConnectionTransport.candidatePairLastRequestTimestamp)
                    .setCandidatePairTotalRoundTripTime(peerConnectionTransport.candidatePairTotalRoundTripTime)
                    .setCandidatePairCurrentRoundTripTime(peerConnectionTransport.candidatePairCurrentRoundTripTime)
                    .setCandidatePairAvailableOutgoingBitrate(peerConnectionTransport.candidatePairAvailableOutgoingBitrate)
                    .setCandidatePairAvailableIncomingBitrate(peerConnectionTransport.candidatePairAvailableOutgoingBitrate)
                    .setCandidatePairCircuitBreakerTriggerCount(peerConnectionTransport.candidatePairCircuitBreakerTriggerCount)
                    .setCandidatePairRequestsReceived(peerConnectionTransport.candidatePairRequestsReceived)
                    .setCandidatePairRequestsSent(peerConnectionTransport.candidatePairRequestsSent)
                    .setCandidatePairResponsesReceived(peerConnectionTransport.candidatePairResponsesReceived)
                    .setCandidatePairRetransmissionReceived(peerConnectionTransport.candidatePairRetransmissionReceived)
                    .setCandidatePairRetransmissionSent(peerConnectionTransport.candidatePairRetransmissionSent)
                    .setCandidatePairConsentRequestsSent(peerConnectionTransport.candidatePairConsentRequestsSent)
                    .setCandidatePairConsentExpiredTimestamp(peerConnectionTransport.candidatePairConsentExpiredTimestamp)
                    .setCandidatePairPacketsDiscardedOnSend(peerConnectionTransport.candidatePairPacketsDiscardedOnSend)
                    .setCandidatePairBytesDiscardedOnSend(peerConnectionTransport.candidatePairBytesDiscardedOnSend)
                    .setCandidatePairRequestBytesSent(peerConnectionTransport.candidatePairRequestBytesSent)
                    .setCandidatePairConsentRequestBytesSent(peerConnectionTransport.candidatePairConsentRequestBytesSent)
                    .setCandidatePairResponseBytesSent(peerConnectionTransport.candidatePairResponseBytesSent)

                    /* SCTP stats */
                    .setSctpSmoothedRoundTripTime(peerConnectionTransport.sctpSmoothedRoundTripTime)
                    .setSctpCongestionWindow(peerConnectionTransport.sctpCongestionWindow)
                    .setSctpReceiverWindow(peerConnectionTransport.sctpReceiverWindow)
                    .setSctpMtu(peerConnectionTransport.sctpMtu)
                    .setSctpUnackData(peerConnectionTransport.sctpUnackData)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building PeerConnectionTransport report", ex);
            return null;
        }

    }


    private ClientDataChannelReport createClientDataChannelReport(
            UUID callId,
            ObservedClientSample observedClientSample,
            String peerConnectionLabel,
            ClientSample.DataChannel dataChannel
    ) {
        try {
            var result = ClientDataChannelReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(observedClientSample.getMarker())
                    .setTimestamp(observedClientSample.getTimestamp())

                    /* Peer Connection Report Fields */
                    .setCallId(callId.toString())
                    .setRoomId(observedClientSample.getRoomId())
                    .setClientId(observedClientSample.getClientId().toString())
                    .setUserId(observedClientSample.getUserId())
                    .setPeerConnectionId(dataChannel.peerConnectionId)
                    .setPeerConnectionLabel(peerConnectionLabel)

                    /* Sample Based Report Fields */
                    .setSampleSeq(observedClientSample.getSampleSeq())

                    /* Data Channel stats */
                    .setLabel(dataChannel.label)
                    .setProtocol(dataChannel.protocol)
                    .setMessagesSent(dataChannel.messagesSent)
                    .setBytesSent(dataChannel.bytesSent)
                    .setMessagesReceived(dataChannel.messagesReceived)
                    .setBytesReceived(dataChannel.bytesReceived)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building ClientDataChannelReport", ex);
            return null;
        }
    }

    private ClientExtensionReport createClientExtensionReport(
            UUID callId,
            ObservedClientSample observedClientSample,
            ClientSample.ExtensionStat extensionStat
    ) {
        try {
            var result = ClientExtensionReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(observedClientSample.getMarker())
                    .setTimestamp(observedClientSample.getTimestamp())

                    /* Peer Connection Report Fields */
                    .setCallId(callId.toString())
                    .setRoomId(observedClientSample.getRoomId())
                    .setClientId(observedClientSample.getClientId().toString())
                    .setUserId(observedClientSample.getUserId())

                    /* Sample Based Report Fields */
                    .setSampleSeq(observedClientSample.getSampleSeq())

                    /* Data Channel stats */
                    .setExtensionType(extensionStat.type)
                    .setPayload(extensionStat.payload)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building ClientDataChannelReport", ex);
            return null;
        }
    }


    private Map<UUID, FetchTracksRelationsTask.MatchedIds> makeInboundTrackMatchIds(CollectedCallSamples collectedCallSamples) {
        Set<UUID> inboundTrackIds = new HashSet<>();
        collectedCallSamples.stream()
                .flatMap(CallSamples::stream)
                .flatMap(ClientSamples::stream)
                .forEach(clientSample -> {
                    ClientSampleVisitor.streamInboundAudioTracks(clientSample)
                            .map(t -> UUIDAdapter.tryParse(t.trackId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(inboundTrackIds::add);

                    ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                            .map(t -> UUIDAdapter.tryParse(t.trackId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(inboundTrackIds::add);
                });
        var task = matchCallTracksTaskProvider.get()
                .whereMediaTrackIds(inboundTrackIds);

        if (!task.execute().succeeded()) {
            logger.warn("Cannot match inbound tracks to outbound tracks, because the task execution is failed");
            return Collections.EMPTY_MAP;
        }

        var result = task.getResult().inboundTrackMatchIds;
        return result;
    }
}
