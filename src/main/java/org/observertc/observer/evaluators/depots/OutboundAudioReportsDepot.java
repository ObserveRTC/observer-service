package org.observertc.observer.evaluators.depots;

import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.OutboundAudioTrackReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class OutboundAudioReportsDepot implements Supplier<List<OutboundAudioTrackReport>> {

    private static final Logger logger = LoggerFactory.getLogger(OutboundAudioReportsDepot.class);

    private String peerConnectionLabel = null;
    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.OutboundAudioTrack outboundAudioTrack = null;

    private List<OutboundAudioTrackReport> buffer = new LinkedList<>();

    public OutboundAudioReportsDepot setPeerConnectionLabel(String value) {
        this.peerConnectionLabel = value;
        return this;
    }

    public OutboundAudioReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public OutboundAudioReportsDepot setOutboundAudioTrack(Samples.ClientSample.OutboundAudioTrack value) {
        this.outboundAudioTrack = value;
        return this;
    }

    private OutboundAudioReportsDepot clean() {
        this.observedClientSample = null;
        this.outboundAudioTrack = null;
        this.peerConnectionLabel = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(outboundAudioTrack)) {
                logger.warn("Cannot assemble {} without outboundAudioTrack", this.getClass().getSimpleName());
                return;
            }
            var clientSample = observedClientSample.getClientSample();
            if (Objects.isNull(clientSample.callId)) {
                logger.warn("Cannot assemble {} when a callId is null for service {}, mediaUnitId: {}", this.getClass().getSimpleName(), observedClientSample.getServiceId(), observedClientSample.getMediaUnitId());
                return;
            }
            var report = OutboundAudioTrackReport.newBuilder()
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
                    .setPeerConnectionId(outboundAudioTrack.peerConnectionId)
                    .setLabel(peerConnectionLabel)
                    .setTrackId(outboundAudioTrack.trackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(clientSample.sampleSeq)

                    /* OutboundRTP related fields specific for Audio*/
                    .setPeerConnectionId(outboundAudioTrack.peerConnectionId)
                    .setTrackId(outboundAudioTrack.trackId)
                    .setSfuStreamId(outboundAudioTrack.sfuStreamId)
                    .setSampleSeq(clientSample.sampleSeq)
                    .setSsrc(outboundAudioTrack.ssrc)
                    .setPacketsSent(outboundAudioTrack.packetsSent)
                    .setBytesSent(outboundAudioTrack.bytesSent)
                    .setRid(outboundAudioTrack.rid)
                    .setHeaderBytesSent(outboundAudioTrack.headerBytesSent)
                    .setRetransmittedPacketsSent(outboundAudioTrack.retransmittedPacketsSent)
                    .setRetransmittedBytesSent(outboundAudioTrack.retransmittedBytesSent)
                    .setTargetBitrate(outboundAudioTrack.targetBitrate)
                    .setTotalEncodedBytesTarget(outboundAudioTrack.totalEncodedBytesTarget)
                    .setTotalPacketSendDelay(outboundAudioTrack.totalPacketSendDelay)
                    .setAverageRtcpInterval(outboundAudioTrack.averageRtcpInterval)
                    .setNackCount(outboundAudioTrack.nackCount)
                    .setEncoderImplementation(outboundAudioTrack.encoderImplementation)
                    .setActive(outboundAudioTrack.active)

                    /* Remote Inbound specific fields related to Audio */
                    .setPacketsReceived(outboundAudioTrack.packetsReceived)
                    .setPacketsLost(outboundAudioTrack.packetsLost)
                    .setJitter(outboundAudioTrack.jitter)
                    .setRoundTripTime(outboundAudioTrack.roundTripTime)
                    .setTotalRoundTripTime(outboundAudioTrack.totalRoundTripTime)
                    .setFractionLost(outboundAudioTrack.fractionLost)
                    .setRoundTripTimeMeasurements(outboundAudioTrack.roundTripTimeMeasurements)

                    /* MediaSource related stats */
                    .setRelayedSource(outboundAudioTrack.relayedSource)
                    .setAudioLevel(outboundAudioTrack.audioLevel)
                    .setTotalAudioEnergy(outboundAudioTrack.totalAudioEnergy)
                    .setTotalSamplesDuration(outboundAudioTrack.totalSamplesDuration)
                    .setEchoReturnLoss(outboundAudioTrack.echoReturnLoss)
                    .setEchoReturnLossEnhancement(outboundAudioTrack.echoReturnLossEnhancement)
                    .setDroppedSamplesDuration(outboundAudioTrack.droppedSamplesDuration)
                    .setDroppedSamplesEvents(outboundAudioTrack.droppedSamplesEvents)
                    .setTotalCaptureDelay(outboundAudioTrack.totalCaptureDelay)
                    .setTotalSamplesCaptured(outboundAudioTrack.totalSamplesCaptured)

                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<OutboundAudioTrackReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
