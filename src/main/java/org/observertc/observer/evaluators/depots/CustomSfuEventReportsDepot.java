package org.observertc.observer.evaluators.depots;

import org.observertc.observer.common.Utils;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.schemas.reports.CallMetaReport;
import org.observertc.schemas.reports.SfuEventReport;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class CustomSfuEventReportsDepot implements Supplier<List<SfuEventReport>> {

    private static final Logger logger = LoggerFactory.getLogger(CallMetaReport.class);

    private ObservedSfuSample observedSfuSample = null;
    private Samples.SfuSample.CustomSfuEvent customSfuEvent;
    private List<SfuEventReport> buffer = new LinkedList<>();


    public CustomSfuEventReportsDepot setObservedSfuSample(ObservedSfuSample value) {
        this.observedSfuSample = value;
        return this;
    }

    public CustomSfuEventReportsDepot setCustomSfuEvent(Samples.SfuSample.CustomSfuEvent value) {
        this.customSfuEvent = value;
        return this;
    }

    private CustomSfuEventReportsDepot clean() {
        this.observedSfuSample = null;
        this.customSfuEvent = null;
        return this;
    }

    public void assemble() {
        try {
            if (Objects.isNull(observedSfuSample)) {
                logger.warn("Cannot assemble {} without observedClientSample", this.getClass().getSimpleName());
                return;
            }
            if (Objects.isNull(customSfuEvent)) {
                logger.warn("Cannot assemble {} without customSfuEvent", this.getClass().getSimpleName());
                return;
            }
            var sfuSample = observedSfuSample.getSfuSample();
            var timestamp = Utils.firstNotNull(this.customSfuEvent.timestamp, sfuSample.timestamp);
            var report = SfuEventReport.newBuilder()
                    .setName(customSfuEvent.name)
                    .setValue(customSfuEvent.value)
                    .setMessage(customSfuEvent.message)
                    .setAttachments(customSfuEvent.attachments)
                    .setServiceId(observedSfuSample.getServiceId())
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setTransportId(customSfuEvent.transportId)
                    .setMediaSinkId(customSfuEvent.sfuSinkId)
                    .setMarker(sfuSample.marker)
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
    public List<SfuEventReport> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_LIST;
        var result = this.buffer;
        this.buffer = new LinkedList<>();
        return result;
    }
}
