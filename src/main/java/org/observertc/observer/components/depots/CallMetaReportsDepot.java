package org.observertc.observer.components.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.events.CallMetaType;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.CallMetaReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class CallMetaReportsDepot implements Supplier<List<CallMetaReport>> {

    private static final Logger logger = LoggerFactory.getLogger(CallMetaReport.class);

    private ObservedClientSample observedClientSample = null;
    private String peerConnectionId = null;
    private String payload;
    private CallMetaType metaType;
    private Long sampleTimestamp = null;
    private List<CallMetaReport> buffer = new LinkedList<>();


    public CallMetaReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public CallMetaReportsDepot setMetaType(CallMetaType value) {
        this.metaType = value;
        return this;
    }

    public CallMetaReportsDepot setPeerConnectionId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.peerConnectionId = value.toString();
        return this;
    }

    public CallMetaReportsDepot setSampleTimestamp(Long value) {
        if (Objects.isNull(value)) return this;
        this.sampleTimestamp = value;
        return this;
    }

    public CallMetaReportsDepot setPayload(String value) {
        this.payload = value;
        return this;
    }

    private CallMetaReportsDepot clean() {
        this.observedClientSample = null;
        this.metaType = null;
        this.payload = null;
        this.peerConnectionId = null;
        this.sampleTimestamp = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(metaType)) {
                logger.warn("Cannot assemble {} without metaType", this.getClass().getSimpleName());
                return;
            }
            var clientSample = observedClientSample.getClientSample();
            var callId = UUIDAdapter.toStringOrNull(clientSample.callId);
            var clientId = UUIDAdapter.toStringOrNull(clientSample.clientId);
            var sampleTimestamp = Objects.nonNull(this.sampleTimestamp) ? this.sampleTimestamp : clientSample.timestamp;
            var report = CallMetaReport.newBuilder()
                    .setType(this.metaType.name())
                    .setPayload(payload)
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setRoomId(clientSample.roomId)
                    .setCallId(callId)
                    .setPeerConnectionId(this.peerConnectionId)
                    .setUserId(clientSample.userId)
                    .setClientId(clientId)
                    .setTimestamp(clientSample.timestamp)
                    .setSampleSeq(clientSample.sampleSeq)
                    .setSampleTimestamp(sampleTimestamp)
                    .setMarker(clientSample.marker)

                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<CallMetaReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
