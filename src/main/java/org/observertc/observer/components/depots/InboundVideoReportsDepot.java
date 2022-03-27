package org.observertc.observer.components.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.InboundVideoTrackReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class InboundVideoReportsDepot implements Supplier<List<InboundVideoTrackReport>> {

    private static final Logger logger = LoggerFactory.getLogger(InboundVideoReportsDepot.class);

    private String remoteClientId = null;
    private String remoteUserId = null;
    private String remotePeerConnectionId = null;
    private String remoteTrackId = null;
    private List<InboundVideoTrackReport> buffer = new LinkedList<>();
    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.InboundVideoTrack inboundVideoTrack = null;
    private String peerConnectionLabel = null;

    public InboundVideoReportsDepot setPeerConnectionLabel(String value) {
        this.peerConnectionLabel = value;
        return this;
    }

    public InboundVideoReportsDepot setRemoteClientId(UUID value) {
        this.remoteClientId = UUIDAdapter.toStringOrNull(value);
        return this;
    }

    public InboundVideoReportsDepot setRemoteUserId(String value) {
        this.remoteUserId = value;
        return this;
    }

    public InboundVideoReportsDepot setRemotePeerConnectionId(UUID value) {
        this.remotePeerConnectionId = UUIDAdapter.toStringOrNull(value);
        return this;
    }

    public InboundVideoReportsDepot setRemoteTrackId(UUID value) {
        this.remoteTrackId = UUIDAdapter.toStringOrNull(value);
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
            String callId = UUIDAdapter.toStringOrNull(clientSample.callId);
            String clientId = UUIDAdapter.toStringOrNull(clientSample.clientId);

            String peerConnectionId = UUIDAdapter.toStringOrNull(inboundVideoTrack.peerConnectionId);
            var trackId = UUIDAdapter.toStringOrNull(inboundVideoTrack.trackId);
            var report = InboundVideoTrackReport.newBuilder()
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

                    /* Remote Identifier */
                    .setRemoteClientId(this.remoteClientId)
                    .setRemoteUserId(this.remoteUserId)
                    .setRemotePeerConnectionId(this.remotePeerConnectionId)
                    .setRemoteTrackId(this.remoteTrackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(clientSample.sampleSeq)


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
