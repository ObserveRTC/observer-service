package org.observertc.observer.evaluators.depots;

import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.InboundAudioTrackReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class InboundAudioReportsDepot implements Supplier<List<InboundAudioTrackReport>> {

    private static final Logger logger = LoggerFactory.getLogger(InboundAudioReportsDepot.class);

    private String remoteClientId = null;
    private String remoteUserId = null;
    private String remotePeerConnectionId = null;
    private String remoteTrackId = null;
    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.InboundAudioTrack inboundAudioTrack = null;
    private List<InboundAudioTrackReport> buffer = new LinkedList<>();
    private String peerConnectionLabel = null;

    public InboundAudioReportsDepot setPeerConnectionLabel(String value) {
        this.peerConnectionLabel = value;
        return this;
    }
    public InboundAudioReportsDepot setRemoteClientId(String value) {
        this.remoteClientId = value;
        return this;
    }

    public InboundAudioReportsDepot setRemoteUserId(String value) {
        this.remoteUserId = value;
        return this;
    }

    public InboundAudioReportsDepot setRemotePeerConnectionId(String value) {
        this.remotePeerConnectionId = value;
        return this;
    }

    public InboundAudioReportsDepot setRemoteTrackId(String value) {
        this.remoteTrackId = value;
        return this;
    }

    public InboundAudioReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public InboundAudioReportsDepot setInboundAudioTrack(Samples.ClientSample.InboundAudioTrack value) {
        this.inboundAudioTrack = value;
        return this;
    }

    private InboundAudioReportsDepot clean() {
        this.remoteClientId = null;
        this.remoteUserId = null;
        this.remotePeerConnectionId = null;
        this.remoteTrackId = null;
        this.observedClientSample = null;
        this.inboundAudioTrack = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(inboundAudioTrack)) {
                logger.warn("Cannot assemble {} without inboundAudioTrack", this.getClass().getSimpleName());
                return;
            }
            var clientSample = observedClientSample.getClientSample();
            var report = InboundAudioTrackReport.newBuilder()
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
                    .setPeerConnectionId(inboundAudioTrack.peerConnectionId)
                    .setLabel(peerConnectionLabel)
                    .setTrackId(inboundAudioTrack.trackId)

                    /* Remote Identifier */
                    .setRemoteClientId(this.remoteClientId)
                    .setRemoteUserId(this.remoteUserId)
                    .setRemotePeerConnectionId(this.remotePeerConnectionId)
                    .setRemoteTrackId(this.remoteTrackId)

                    /* Sample Based Report Fields */
                    .setSampleSeq(clientSample.sampleSeq)

                    /* Inbound RTP Audio specific fields */
                    .setSfuStreamId(inboundAudioTrack.sfuStreamId)
                    .setSfuSinkId(inboundAudioTrack.sfuSinkId)
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
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<InboundAudioTrackReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
