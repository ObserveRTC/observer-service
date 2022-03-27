package org.observertc.observer.components.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.schemas.reports.SFUTransportReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class SfuTransportReportsDepot implements Supplier<List<SFUTransportReport>> {

    private static final Logger logger = LoggerFactory.getLogger(SfuTransportReportsDepot.class);

    private ObservedSfuSample observedSfuSample = null;
    private Samples.SfuSample.SfuTransport sfuTransport = null;
    private String callId = null;
    private List<SFUTransportReport> buffer = new LinkedList<>();


    public SfuTransportReportsDepot setObservedSfuSample(ObservedSfuSample value) {
        this.observedSfuSample = value;
        return this;
    }

    public SfuTransportReportsDepot setSfuTransport(Samples.SfuSample.SfuTransport value) {
        this.sfuTransport = value;
        return this;
    }

    public SfuTransportReportsDepot setCallId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.callId = value.toString();
        return this;
    }

    private SfuTransportReportsDepot clean() {
        this.observedSfuSample = null;
        this.sfuTransport = null;
        this.callId = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(sfuTransport)) {
                logger.warn("Cannot assemble {} without sfuTransport", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(observedSfuSample)) {
                logger.warn("Cannot assemble {} without observedSfuSample", this.getClass().getSimpleName());
                return;
            }
            var sfuSample = observedSfuSample.getSfuSample();
            String transportId = UUIDAdapter.toStringOrNull(sfuTransport.transportId);
            String sfuId = UUIDAdapter.toStringOrNull(sfuSample.sfuId);
            var report = SFUTransportReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedSfuSample.getServiceId())
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setMarker(sfuSample.marker)
                    .setTimestamp(sfuSample.timestamp)


                    /* Report Fields */
                    .setTransportId(transportId)
                    .setSfuId(sfuId)
                    .setCallId(callId)

                    /* Transport stats */
                    .setDtlsState(sfuTransport.dtlsState)
                    .setIceState(sfuTransport.iceState)
                    .setSctpState(sfuTransport.sctpState)
                    .setIceRole(sfuTransport.iceRole)
                    .setLocalAddress(sfuTransport.localAddress)
                    .setLocalPort(sfuTransport.localPort)
                    .setProtocol(sfuTransport.protocol)
                    .setRemoteAddress(sfuTransport.remoteAddress)
                    .setRemotePort(sfuTransport.remotePort)

                    /* RTP related stats */
                    .setRtpBytesReceived(sfuTransport.rtpBytesReceived)
                    .setRtpBytesSent(sfuTransport.rtpBytesSent)
                    .setRtpPacketsReceived(sfuTransport.rtpPacketsReceived)
                    .setRtpPacketsSent(sfuTransport.rtpPacketsSent)
                    .setRtpPacketsLost(sfuTransport.rtpPacketsLost)

                    /* RTX related stats */
                    .setRtxBytesReceived(sfuTransport.rtxBytesReceived)
                    .setRtxBytesSent(sfuTransport.rtxBytesSent)
                    .setRtxPacketsReceived(sfuTransport.rtxPacketsReceived)
                    .setRtxPacketsSent(sfuTransport.rtxPacketsSent)
//                    .setRtxPacketsLost(sfuTransport.rtxPacket)
                    .setRtxPacketsDiscarded(sfuTransport.rtxPacketsDiscarded)

                    /* SCTP related stats */
                    .setSctpBytesReceived(sfuTransport.sctpBytesReceived)
                    .setSctpBytesSent(sfuTransport.sctpBytesSent)
                    .setSctpPacketsReceived(sfuTransport.sctpPacketsReceived)
                    .setSctpPacketsSent(sfuTransport.sctpPacketsSent)

                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<SFUTransportReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
