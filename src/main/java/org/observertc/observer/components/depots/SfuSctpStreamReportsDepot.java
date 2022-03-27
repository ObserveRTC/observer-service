package org.observertc.observer.components.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.schemas.reports.SfuSctpStreamReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class SfuSctpStreamReportsDepot implements Supplier<List<SfuSctpStreamReport>> {

    private static final Logger logger = LoggerFactory.getLogger(SfuSctpStreamReportsDepot.class);

    private ObservedSfuSample observedSfuSample = null;
    private Samples.SfuSample.SfuSctpChannel sctpChannel = null;
    private List<SfuSctpStreamReport> buffer = new LinkedList<>();


    public SfuSctpStreamReportsDepot setObservedSfuSample(ObservedSfuSample value) {
        this.observedSfuSample = value;
        return this;
    }

    public SfuSctpStreamReportsDepot setSctpChannel(Samples.SfuSample.SfuSctpChannel value) {
        this.sctpChannel = value;
        return this;
    }

    private SfuSctpStreamReportsDepot clean() {
        this.observedSfuSample = null;
        this.sctpChannel = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(sctpChannel)) {
                logger.warn("Cannot assemble {} without sctpChannel", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(observedSfuSample)) {
                logger.warn("Cannot assemble {} without observedSfuSample", this.getClass().getSimpleName());
                return;
            }
            var sfuSample = observedSfuSample.getSfuSample();
            String transportId = UUIDAdapter.toStringOrNull(sctpChannel.transportId);
            String sfuId = UUIDAdapter.toStringOrNull(sfuSample.sfuId);
            var report = SfuSctpStreamReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedSfuSample.getServiceId())
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId)
                    .setMarker(sfuSample.marker)
                    .setTimestamp(sfuSample.timestamp)

                    /* Helper field */
//                    .setCallId(callId)
                    .setTransportId(transportId)
//                    .setRoomId()
                    .setStreamId(sctpChannel.streamId)

                    /* SCTP Stats */
                    .setLabel(sctpChannel.label)
                    .setProtocol(sctpChannel.protocol)
                    .setSctpSmoothedRoundTripTime(sctpChannel.sctpSmoothedRoundTripTime)
                    .setSctpCongestionWindow(sctpChannel.sctpCongestionWindow)
                    .setSctpReceiverWindow(sctpChannel.sctpReceiverWindow)
                    .setSctpUnackData(sctpChannel.sctpUnackData)
                    .setMessageReceived(sctpChannel.messageReceived)
                    .setMessageSent(sctpChannel.messageSent)
                    .setBytesReceived(sctpChannel.bytesReceived)
                    .setBytesSent(sctpChannel.bytesSent)

                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<SfuSctpStreamReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
