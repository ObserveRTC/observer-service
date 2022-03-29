package org.observertc.observer.components.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.schemas.reports.SfuInboundRtpPadReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class SfuInboundRtpPadReportsDepot implements Supplier<List<SfuInboundRtpPadReport>> {

    private static final Logger logger = LoggerFactory.getLogger(SfuInboundRtpPadReportsDepot.class);

    private ObservedSfuSample observedSfuSample = null;
    private Samples.SfuSample.SfuInboundRtpPad sfuInboundRtpPad = null;
    private String callId = null;
    private String clientId = null;
    private String trackId = null;
    private List<SfuInboundRtpPadReport> buffer = new LinkedList<>();


    public SfuInboundRtpPadReportsDepot setObservedSfuSample(ObservedSfuSample value) {
        this.observedSfuSample = value;
        return this;
    }

    public SfuInboundRtpPadReportsDepot setSfuInboundRtpPad(Samples.SfuSample.SfuInboundRtpPad value) {
        this.sfuInboundRtpPad = value;
        return this;
    }

    public SfuInboundRtpPadReportsDepot setCallId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.callId = value.toString();
        return this;
    }

    public SfuInboundRtpPadReportsDepot setClientId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.clientId = value.toString();
        return this;
    }

    public SfuInboundRtpPadReportsDepot setTrackId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.trackId = value.toString();
        return this;
    }

    private SfuInboundRtpPadReportsDepot clean() {
        this.observedSfuSample = null;
        this.sfuInboundRtpPad = null;
        this.callId = null;
        this.clientId = null;
        this.trackId = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(sfuInboundRtpPad)) {
                logger.warn("Cannot assemble {} without sfuTransport", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(observedSfuSample)) {
                logger.warn("Cannot assemble {} without observedSfuSample", this.getClass().getSimpleName());
                return;
            }
            var sfuSample = observedSfuSample.getSfuSample();
            String transportId = UUIDAdapter.toStringOrNull(sfuInboundRtpPad.transportId);
            String sfuId = UUIDAdapter.toStringOrNull(sfuSample.sfuId);
            String padId = UUIDAdapter.toStringOrNull(sfuInboundRtpPad.padId);
            String streamId = UUIDAdapter.toStringOrNull(sfuInboundRtpPad.streamId);
            var report = SfuInboundRtpPadReport.newBuilder()

                    /* Report MetaFields */
                    /* .setServiceId() // not given */
                    .setServiceId(observedSfuSample.getServiceId())
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId)
                    .setMarker(sfuSample.marker)
                    .setTimestamp(sfuSample.timestamp)

                    /* Report Fields */
                    .setTransportId(transportId)
                    .setSfuStreamId(streamId)
                    .setInternal(sfuInboundRtpPad.internal)

                    .setSsrc(sfuInboundRtpPad.ssrc)
                    .setRtpPadId(padId)
                    .setTrackId(trackId)
                    .setClientId(clientId)
                    .setCallId(callId)

                    /* RTP Stats */
                    .setMediaType(sfuInboundRtpPad.mediaType)
                    .setPayloadType(sfuInboundRtpPad.payloadType)
                    .setMimeType(sfuInboundRtpPad.mimeType)
                    .setClockRate(sfuInboundRtpPad.clockRate)
                    .setSdpFmtpLine(sfuInboundRtpPad.sdpFmtpLine)
                    .setRid(sfuInboundRtpPad.rid)
                    .setRtxSsrc(sfuInboundRtpPad.rtxSsrc)
                    .setPacketsDuplicated(sfuInboundRtpPad.packetsDuplicated)
                    .setTargetBitrate(sfuInboundRtpPad.targetBitrate)
                    .setVoiceActivityFlag(sfuInboundRtpPad.voiceActivityFlag)
                    .setFirCount(sfuInboundRtpPad.firCount)
                    .setPliCount(sfuInboundRtpPad.pliCount)
                    .setNackCount(sfuInboundRtpPad.nackCount)
                    .setSliCount(sfuInboundRtpPad.sliCount)
                    .setPacketsLost(sfuInboundRtpPad.packetsLost)
                    .setPacketsReceived(sfuInboundRtpPad.packetsReceived)
                    .setPacketsDiscarded(sfuInboundRtpPad.packetsDiscarded)
                    .setPacketsRepaired(sfuInboundRtpPad.packetsRepaired)
                    .setPacketsFailedDecryption(sfuInboundRtpPad.packetsFailedDecryption)
                    .setFecPacketsReceived(sfuInboundRtpPad.fecPacketsReceived)
                    .setFecPacketsDiscarded(sfuInboundRtpPad.fecPacketsDiscarded)
                    .setBytesReceived(sfuInboundRtpPad.bytesReceived)
                    .setRtcpSrReceived(sfuInboundRtpPad.rtcpSrReceived)
                    .setRtcpRrSent(sfuInboundRtpPad.rtcpRrSent)
                    .setRtxPacketsReceived(sfuInboundRtpPad.rtxPacketsReceived)
                    .setRtxPacketsDiscarded(sfuInboundRtpPad.rtxPacketsDiscarded)
                    .setFramesReceived(sfuInboundRtpPad.framesReceived)
                    .setFramesDecoded(sfuInboundRtpPad.framesDecoded)
                    .setKeyFramesDecoded(sfuInboundRtpPad.keyFramesDecoded)
                    .setFractionLost(sfuInboundRtpPad.fractionLost)
                    .setJitter(sfuInboundRtpPad.jitter)
                    .setRoundTripTime(sfuInboundRtpPad.roundTripTime)

                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<SfuInboundRtpPadReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
