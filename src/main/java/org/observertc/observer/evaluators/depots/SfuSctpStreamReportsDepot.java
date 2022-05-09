package org.observertc.observer.evaluators.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.schemas.reports.SfuSctpStreamReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class SfuSctpStreamReportsDepot implements Supplier<List<SfuSctpStreamReport>> {

    private static final Logger logger = LoggerFactory.getLogger(SfuSctpStreamReportsDepot.class);

    private ObservedSfuSample observedSfuSample = null;
    private Samples.SfuSample.SfuSctpChannel sctpChannel = null;
    private List<SfuSctpStreamReport> buffer = new LinkedList<>();
    private UUID callId = null;
    private String roomId = null;

    public SfuSctpStreamReportsDepot setObservedSfuSample(ObservedSfuSample value) {
        this.observedSfuSample = value;
        return this;
    }

    public SfuSctpStreamReportsDepot setCallId(UUID value) {
        this.callId = value;
        return this;
    }

    public SfuSctpStreamReportsDepot setRoomId(String value) {
        this.roomId = value;
        return this;
    }

    public SfuSctpStreamReportsDepot setSctpChannel(Samples.SfuSample.SfuSctpChannel value) {
        this.sctpChannel = value;
        return this;
    }

    private SfuSctpStreamReportsDepot clean() {
        this.observedSfuSample = null;
        this.sctpChannel = null;
        this.callId = null;
        this.roomId = null;
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
            String streamId = UUIDAdapter.toStringOrNull(sctpChannel.streamId);
            String callId = UUIDAdapter.toStringOrNull(this.callId);
            var report = SfuSctpStreamReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedSfuSample.getServiceId())
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId)
                    .setMarker(sfuSample.marker)
                    .setTimestamp(sfuSample.timestamp)

                    /* Helper field */
                    .setCallId(callId)
                    .setRoomId(roomId)
                    .setTransportId(transportId)
                    .setStreamId(streamId)
                    .setInternal(sctpChannel.internal)

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
                    .setSctpMtu(sctpChannel.sctpMtu)

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
