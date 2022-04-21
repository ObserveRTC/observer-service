package org.observertc.observer.evaluators.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.OutboundVideoTrackReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class OutboundVideoReportsDepot implements Supplier<List<OutboundVideoTrackReport>> {

    private static final Logger logger = LoggerFactory.getLogger(OutboundVideoReportsDepot.class);

    private String peerConnectionLabel = null;
    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.OutboundVideoTrack outboundVideoTrack = null;
    private List<OutboundVideoTrackReport> buffer = new LinkedList<>();

    public OutboundVideoReportsDepot setPeerConnectionLabel(String value) {
        this.peerConnectionLabel = value;
        return this;
    }

    public OutboundVideoReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public OutboundVideoReportsDepot setOutboundVideoTrack(Samples.ClientSample.OutboundVideoTrack value) {
        this.outboundVideoTrack = value;
        return this;
    }

    private OutboundVideoReportsDepot clean() {
        this.observedClientSample = null;
        this.outboundVideoTrack = null;
        this.peerConnectionLabel = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(outboundVideoTrack)) {
                logger.warn("Cannot assemble {} without outboundVideoTrack", this.getClass().getSimpleName());
                return;
            }
            var clientSample = observedClientSample.getClientSample();
            String callId = UUIDAdapter.toStringOrNull(clientSample.callId);
            String clientId = UUIDAdapter.toStringOrNull(clientSample.clientId);
            String peerConnectionId = UUIDAdapter.toStringOrNull(outboundVideoTrack.peerConnectionId);
            var trackId = UUIDAdapter.toStringOrNull(outboundVideoTrack.trackId);
            var sfuStreamId = UUIDAdapter.toStringOrNull(outboundVideoTrack.sfuStreamId);
            var report = OutboundVideoTrackReport.newBuilder()
                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(clientSample.marker)
                    .setTimestamp(clientSample.timestamp)

                    /* Peer Connection Report Fields */
                    .setCallId(callId)
                    .setRoomId(clientSample.roomId)
                    .setClientId(clientId)
                    .setUserId(clientSample.userId)
                    .setPeerConnectionId(peerConnectionId)
                    .setLabel(peerConnectionLabel)
                    .setTrackId(trackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(clientSample.sampleSeq)

                    /* OutboundRTP related fields specific for Audio*/
                    .setSfuStreamId(sfuStreamId)
                    .setSsrc(outboundVideoTrack.ssrc)
                    .setPacketsSent(outboundVideoTrack.packetsSent)
                    .setBytesSent(outboundVideoTrack.bytesSent)
                    .setRid(outboundVideoTrack.rid)
                    .setLastPacketSentTimestamp(outboundVideoTrack.lastPacketSentTimestamp)
                    .setHeaderBytesSent(outboundVideoTrack.headerBytesSent)
                    .setPacketsDiscardedOnSend(outboundVideoTrack.packetsDiscardedOnSend)
                    .setPacketsDiscarded(outboundVideoTrack.packetsDiscarded)
                    .setBytesDiscardedOnSend(outboundVideoTrack.bytesDiscardedOnSend)
                    .setFecPacketsSent(outboundVideoTrack.fecPacketsSent)
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
                    .setBurstDiscardCount(outboundVideoTrack.burstDiscardCount)
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
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }
    }

    @Override
    public List<OutboundVideoTrackReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
