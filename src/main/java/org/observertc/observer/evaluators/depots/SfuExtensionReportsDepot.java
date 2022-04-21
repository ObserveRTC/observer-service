package org.observertc.observer.evaluators.depots;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.schemas.reports.CallMetaReport;
import org.observertc.schemas.reports.SfuExtensionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class SfuExtensionReportsDepot implements Supplier<List<SfuExtensionReport>> {

    private static final Logger logger = LoggerFactory.getLogger(CallMetaReport.class);

    private ObservedSfuSample observedSfuSample = null;
    private String payload;
    private String extensionType;
    private String callId = null;
    private List<SfuExtensionReport> buffer = new LinkedList<>();


    public SfuExtensionReportsDepot setObservedSfuSample(ObservedSfuSample value) {
        this.observedSfuSample = value;
        return this;
    }

    public SfuExtensionReportsDepot setExtensionType(String value) {
        this.extensionType = value;
        return this;
    }

    public SfuExtensionReportsDepot setCallId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.callId = value.toString();
        return this;
    }

    public SfuExtensionReportsDepot setPayload(String value) {
        this.payload = value;
        return this;
    }

    private void clean() {
        this.observedSfuSample = null;
        this.extensionType = null;
        this.payload = null;
        this.callId = null;
        return;
    }

    public void assemble() {
        if (Objects.isNull(observedSfuSample)) {
            logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
            return;
        }
        if (Objects.isNull(extensionType)) {
            logger.warn("Cannot assemble {} without extensionType", this.getClass().getSimpleName());
            return;
        }
        var sfuSample = observedSfuSample.getSfuSample();
        var sfuId = UUIDAdapter.toStringOrNull(sfuSample.sfuId);
        try {
            var report = SfuExtensionReport.newBuilder()
                    .setServiceId(observedSfuSample.getServiceId())
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setMarker(sfuSample.marker)
                    .setTimestamp(sfuSample.timestamp)
                    .setSfuId(sfuId)
                    .setExtensionType(extensionType)
                    .setPayload(payload)
                    .build();
            this.buffer.add(report);
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while assembling in {}", this.getClass().getSimpleName(), ex);
        } finally {
            this.clean();
        }
    }

    @Override
    public List<SfuExtensionReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
