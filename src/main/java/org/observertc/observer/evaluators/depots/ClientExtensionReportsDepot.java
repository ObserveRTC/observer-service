package org.observertc.observer.evaluators.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.schemas.reports.CallMetaReport;
import org.observertc.schemas.reports.ClientExtensionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class ClientExtensionReportsDepot implements Supplier<List<ClientExtensionReport>> {

    private static final Logger logger = LoggerFactory.getLogger(CallMetaReport.class);

    private ObservedClientSample observedClientSample = null;
    private String payload;
    private String extensionType;
    private String peerConnectionId = null;
    private List<ClientExtensionReport> buffer = new LinkedList<>();


    public ClientExtensionReportsDepot setObservedClientSample(ObservedClientSample value) {
        this.observedClientSample = value;
        return this;
    }

    public ClientExtensionReportsDepot setExtensionType(String value) {
        this.extensionType = value;
        return this;
    }

    public ClientExtensionReportsDepot setPeerConnectionId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.peerConnectionId = value.toString();
        return this;
    }

    public ClientExtensionReportsDepot setPayload(String value) {
        this.payload = value;
        return this;
    }

    private void clean() {
        this.observedClientSample = null;
        this.extensionType = null;
        this.payload = null;
        this.peerConnectionId = null;
        return;
    }

    public void assemble() {
        if (Objects.isNull(observedClientSample)) {
            logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
            return;
        }
        if (Objects.isNull(extensionType)) {
            logger.warn("Cannot assemble {} without extensionType", this.getClass().getSimpleName());
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
                    .setPeerConnectionId(peerConnectionId)
                    .setExtensionType(extensionType)
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
