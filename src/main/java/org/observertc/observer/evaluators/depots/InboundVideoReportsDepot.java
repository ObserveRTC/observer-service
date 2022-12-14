package org.observertc.observer.evaluators.depots;

import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.InboundVideoTrackReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class InboundVideoReportsDepot implements Supplier<List<InboundVideoTrackReport>> {

    private static final Logger logger = LoggerFactory.getLogger(InboundVideoReportsDepot.class);

    private String remoteClientId = null;
    private String remoteUserId = null;
    private String remotePeerConnectionId = null;
    private String remoteTrackId = null;
    private String peerConnectionLabel = null;

    private List<InboundVideoTrackReport> buffer = new LinkedList<>();
    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.InboundVideoTrack inboundVideoTrack = null;


    public InboundVideoReportsDepot setPeerConnectionLabel(String value) {
        this.peerConnectionLabel = value;
        return this;
    }

    public InboundVideoReportsDepot setRemoteClientId(String value) {
        this.remoteClientId = value;
        return this;
    }

    public InboundVideoReportsDepot setRemoteUserId(String value) {
        this.remoteUserId = value;
        return this;
    }

    public InboundVideoReportsDepot setRemotePeerConnectionId(String value) {
        this.remotePeerConnectionId = value;
        return this;
    }

    public InboundVideoReportsDepot setRemoteTrackId(String value) {
        this.remoteTrackId = value;
        return this;
    }

    public InboundVideoReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public InboundVideoReportsDepot setInboundVideoTrack(Samples.ClientSample.InboundVideoTrack value) {
        this.inboundVideoTrack = value;
        return this;
    }

    private void clean() {
        this.remoteClientId = null;
        this.remoteUserId = null;
        this.remotePeerConnectionId = null;
        this.remoteTrackId = null;
        this.observedClientSample = null;
        this.inboundVideoTrack = null;
        this.peerConnectionLabel = null;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(inboundVideoTrack)) {
                logger.warn("Cannot assemble {} without inboundVideoTrack", this.getClass().getSimpleName());
                return;
            }
            var clientSample = observedClientSample.getClientSample();
            if (Objects.isNull(clientSample.callId)) {
                logger.warn("Cannot assemble {} when a callId is null for service {}, mediaUnitId: {}", this.getClass().getSimpleName(), observedClientSample.getServiceId(), observedClientSample.getMediaUnitId());
                return;
            }
            var report = InboundVideoTrackReport.newBuilder()
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
                    .setPeerConnectionId(inboundVideoTrack.peerConnectionId)
                    .setLabel(peerConnectionLabel)
                    .setTrackId(inboundVideoTrack.trackId)

                    /* Remote Identifier */
                    .setRemoteClientId(this.remoteClientId)
                    .setRemoteUserId(this.remoteUserId)
                    .setRemotePeerConnectionId(this.remotePeerConnectionId)
                    .setRemoteTrackId(this.remoteTrackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(clientSample.sampleSeq)


                    /* Inbound RTP Video specific fields */
                    .setPeerConnectionId(inboundVideoTrack.peerConnectionId)
                    .setTrackId(inboundVideoTrack.trackId)
                    .setSfuStreamId(inboundVideoTrack.sfuStreamId)
                    .setSfuSinkId(inboundVideoTrack.sfuSinkId)
                    .setRemoteTrackId(this.remoteTrackId)
                    .setRemoteUserId(this.remoteUserId)
                    .setRemoteClientId(inboundVideoTrack.remoteClientId)
                    .setRemotePeerConnectionId(this.remotePeerConnectionId)
                    .setSampleSeq(clientSample.sampleSeq)
                    .setSsrc(inboundVideoTrack.ssrc)
                    .setPacketsReceived(inboundVideoTrack.packetsReceived)
                    .setPacketsLost(inboundVideoTrack.packetsLost)
                    .setJitter(inboundVideoTrack.jitter)
                    .setFramesDropped(inboundVideoTrack.framesDropped)
                    .setLastPacketReceivedTimestamp(inboundVideoTrack.lastPacketReceivedTimestamp)
                    .setHeaderBytesReceived(inboundVideoTrack.headerBytesReceived)
                    .setPacketsDiscarded(inboundVideoTrack.packetsDiscarded)
                    .setFecPacketsReceived(inboundVideoTrack.fecPacketsReceived)
                    .setFecPacketsDiscarded(inboundVideoTrack.fecPacketsDiscarded)
                    .setBytesReceived(inboundVideoTrack.bytesReceived)
                    .setNackCount(inboundVideoTrack.nackCount)
                    .setTotalProcessingDelay(inboundVideoTrack.totalProcessingDelay)
                    .setEstimatedPlayoutTimestamp(inboundVideoTrack.estimatedPlayoutTimestamp)
                    .setJitterBufferDelay(inboundVideoTrack.jitterBufferDelay)
                    .setJitterBufferTargetDelay(inboundVideoTrack.jitterBufferTargetDelay)
                    .setJitterBufferEmittedCount(inboundVideoTrack.jitterBufferEmittedCount)
                    .setJitterBufferMinimumDelay(inboundVideoTrack.jitterBufferMinimumDelay)
                    .setDecoderImplementation(inboundVideoTrack.decoderImplementation)
                    .setFramesDecoded(inboundVideoTrack.framesDecoded)
                    .setKeyFramesDecoded(inboundVideoTrack.keyFramesDecoded)
                    .setFrameWidth(inboundVideoTrack.frameWidth)
                    .setFrameHeight(inboundVideoTrack.frameHeight)
                    .setFramesPerSecond(inboundVideoTrack.framesPerSecond)
                    .setQpSum(inboundVideoTrack.qpSum)
                    .setTotalDecodeTime(inboundVideoTrack.totalDecodeTime)
                    .setTotalInterFrameDelay(inboundVideoTrack.totalInterFrameDelay)
                    .setTotalSquaredInterFrameDelay(inboundVideoTrack.totalSquaredInterFrameDelay)
                    .setFirCount(inboundVideoTrack.firCount)
                    .setPliCount(inboundVideoTrack.pliCount)
                    .setFramesReceived(inboundVideoTrack.framesReceived)

                    /* Remote Outbound RTP Video specific fields */
                    .setPacketsSent(inboundVideoTrack.packetsSent)
                    .setBytesSent(inboundVideoTrack.bytesSent)
                    .setRemoteTimestamp(inboundVideoTrack.remoteTimestamp)
                    .setReportsSent(inboundVideoTrack.reportsSent)
                    .setRoundTripTime(inboundVideoTrack.roundTripTime)
                    .setTotalRoundTripTime(inboundVideoTrack.totalRoundTripTime)
                    .setRoundTripTimeMeasurements(inboundVideoTrack.roundTripTimeMeasurements)

                    .build();

            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<InboundVideoTrackReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
