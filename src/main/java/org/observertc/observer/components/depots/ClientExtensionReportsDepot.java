package org.observertc.observer.components.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.CallMetaReport;
import org.observertc.schemas.reports.ClientExtensionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ClientExtensionReportsDepot implements Supplier<List<ClientExtensionReport>> {

    private static final Logger logger = LoggerFactory.getLogger(CallMetaReport.class);

    private ObservedClientSample observedClientSample = null;
    private String payload;
    private String type;
    private List<ClientExtensionReport> buffer = new LinkedList<>();


    public ClientExtensionReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public ClientExtensionReportsDepot setType(String value) {
        this.type = value;
        return this;
    }

    public ClientExtensionReportsDepot setPayload(String value) {
        this.payload = value;
        return this;
    }

    private void clean() {
        this.observedClientSample = null;
        this.type = null;
        this.payload = null;
        return;
    }

    public void assemble() {
        if (Objects.isNull(observedClientSample)) {
            logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
            return;
        }
        if (Objects.isNull(type)) {
            logger.warn("Cannot assemble {} without type", this.getClass().getSimpleName());
            return;
        }
        if (Objects.isNull(payload)) {
            logger.warn("Cannot assemble {} without payload", this.getClass().getSimpleName());
            return;
        }
        var clientSample = observedClientSample.getClientSample();
        var callId = UUIDAdapter.toStringOrNull(clientSample.callId);
        var clientId = UUIDAdapter.toStringOrNull(clientSample.clientId);
        try {
            var report = ClientExtensionReport.newBuilder()
                    .setServiceId(observedClientSample.getServiceId())
                    .setMediaUnitId(observedClientSample.getMediaUnitId())
                    .setRoomId(clientSample.roomId)
                    .setCallId(callId)
                    .setUserId(clientSample.userId)
                    .setClientId(clientId)
                    .setTimestamp(clientSample.timestamp)
                    .setSampleSeq(clientSample.sampleSeq)
//                    .setPeerConnectionId()
                    .setExtensionType(type)
                    .setPayload(payload)
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
    public List<ClientExtensionReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
