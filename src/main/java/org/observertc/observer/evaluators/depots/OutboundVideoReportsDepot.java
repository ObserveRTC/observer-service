package org.observertc.observer.evaluators.depots;

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
            if (Objects.isNull(clientSample.callId)) {
                logger.warn("Cannot assemble {} when a callId is null for service {}, mediaUnitId: {}", this.getClass().getSimpleName(), observedClientSample.getServiceId(), observedClientSample.getMediaUnitId());
                return;
            }
            var report = OutboundVideoTrackReport.newBuilder()
                    /* Report MetaFields */
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setMarker(clientSample.marker)
                    .setTimestamp(clientSample.timestamp)

                    /* Peer Connection Report Fields */
                    .setCallId(clientSample.callId)
                    .setRoomId(clientSample.roomId)
                    .setClientId(clientSample.clientId)
                    .setUserId(clientSample.userId)
                    .setPeerConnectionId(outboundVideoTrack.peerConnectionId)
                    .setLabel(peerConnectionLabel)
                    .setTrackId(outboundVideoTrack.trackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(clientSample.sampleSeq)

                    /* OutboundRTP related fields specific for Audio*/
                    .setPeerConnectionId(outboundVideoTrack.peerConnectionId)
                    .setTrackId(outboundVideoTrack.trackId)
                    .setSfuStreamId(outboundVideoTrack.sfuStreamId)
                    .setSampleSeq(clientSample.sampleSeq)
                    .setSsrc(outboundVideoTrack.ssrc)
                    .setPacketsSent(outboundVideoTrack.packetsSent)
                    .setBytesSent(outboundVideoTrack.bytesSent)
                    .setRid(outboundVideoTrack.rid)
                    .setHeaderBytesSent(outboundVideoTrack.headerBytesSent)
                    .setRetransmittedPacketsSent(outboundVideoTrack.retransmittedPacketsSent)
                    .setRetransmittedBytesSent(outboundVideoTrack.retransmittedBytesSent)
                    .setTargetBitrate(outboundVideoTrack.targetBitrate)
                    .setTotalEncodedBytesTarget(outboundVideoTrack.totalEncodedBytesTarget)
                    .setTotalPacketSendDelay(outboundVideoTrack.totalPacketSendDelay)
                    .setAverageRtcpInterval(outboundVideoTrack.averageRtcpInterval)
                    .setNackCount(outboundVideoTrack.nackCount)
                    .setEncoderImplementation(outboundVideoTrack.encoderImplementation)
                    .setActive(outboundVideoTrack.active)
                    .setFrameWidth(outboundVideoTrack.frameWidth)
                    .setFrameHeight(outboundVideoTrack.frameHeight)
                    .setFramesPerSecond(outboundVideoTrack.framesPerSecond)
                    .setFramesSent(outboundVideoTrack.framesSent)
                    .setHugeFramesSent(outboundVideoTrack.hugeFramesSent)
                    .setFramesEncoded(outboundVideoTrack.framesEncoded)
                    .setKeyFramesEncoded(outboundVideoTrack.keyFramesEncoded)
                    .setQpSum(outboundVideoTrack.qpSum)
                    .setTotalEncodeTime(outboundVideoTrack.totalEncodeTime)
                    .setQualityLimitationDurationNone(outboundVideoTrack.qualityLimitationDurationNone)
                    .setQualityLimitationDurationCPU(outboundVideoTrack.qualityLimitationDurationCPU)
                    .setQualityLimitationDurationBandwidth(outboundVideoTrack.qualityLimitationDurationBandwidth)
                    .setQualityLimitationDurationOther(outboundVideoTrack.qualityLimitationDurationOther)
                    .setQualityLimitationReason(outboundVideoTrack.qualityLimitationReason)
                    .setQualityLimitationResolutionChanges(outboundVideoTrack.qualityLimitationResolutionChanges)
                    .setFirCount(outboundVideoTrack.firCount)
                    .setPliCount(outboundVideoTrack.pliCount)

                    /* Remote Inbound specific fields related to Video */
                    .setPacketsReceived(outboundVideoTrack.packetsReceived)
                    .setPacketsLost(outboundVideoTrack.packetsLost)
                    .setJitter(outboundVideoTrack.jitter)
                    .setRoundTripTime(outboundVideoTrack.roundTripTime)
                    .setTotalRoundTripTime(outboundVideoTrack.totalRoundTripTime)
                    .setFractionLost(outboundVideoTrack.fractionLost)
                    .setRoundTripTimeMeasurements(outboundVideoTrack.roundTripTimeMeasurements)
                    .setFramesDropped(outboundVideoTrack.framesDropped)

                    /* MediaSource related stats */
                    .setRelayedSource(outboundVideoTrack.relayedSource)
                    .setWidth(outboundVideoTrack.width)
                    .setHeight(outboundVideoTrack.height)
                    .setFrames(outboundVideoTrack.frames)

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
