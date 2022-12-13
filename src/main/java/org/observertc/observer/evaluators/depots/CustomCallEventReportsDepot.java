package org.observertc.observer.evaluators.depots;

import org.observertc.observer.common.Utils;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.CallEventReport;
import org.observertc.schemas.reports.CallMetaReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class CustomCallEventReportsDepot implements Supplier<List<CallEventReport>> {

    private static final Logger logger = LoggerFactory.getLogger(CallMetaReport.class);

    private ObservedClientSample observedClientSample = null;
    private Samples.ClientSample.CustomCallEvent customCallEvent;
    private List<CallEventReport> buffer = new LinkedList<>();


    public CustomCallEventReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public CustomCallEventReportsDepot setCustomCallEvent(Samples.ClientSample.CustomCallEvent value) {
        this.customCallEvent = value;
        return this;
    }

    private CustomCallEventReportsDepot clean() {
        this.observedClientSample = null;
        this.customCallEvent = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(customCallEvent)) {
                logger.warn("Cannot assemble {} without customCallEvent", this.getClass().getSimpleName());
                return;
            }
            var clientSample = observedClientSample.getClientSample();
            if (Objects.isNull(clientSample.callId)) {
                logger.warn("Cannot assemble {} when a callId is null for service {}, mediaUnitId: {}", this.getClass().getSimpleName(), observedClientSample.getServiceId(), observedClientSample.getMediaUnitId());
                return;
            }
            var timestamp = Utils.firstNotNull(customCallEvent.timestamp, clientSample.timestamp);
            var report = CallEventReport.newBuilder()
                    .setName(customCallEvent.name)
                    .setValue(customCallEvent.value)
                    .setMessage(customCallEvent.message)
                    .setAttachments(customCallEvent.attachments)
                    .setPeerConnectionId(customCallEvent.peerConnectionId)
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setRoomId(clientSample.roomId)
                    .setCallId(clientSample.callId)
                    .setUserId(clientSample.userId)
                    .setClientId(clientSample.clientId)
                    .setSampleSeq(clientSample.sampleSeq)
                    .setMarker(clientSample.marker)
                    .setTimestamp(timestamp)
                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }

    }

    @Override
    public List<CallEventReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
