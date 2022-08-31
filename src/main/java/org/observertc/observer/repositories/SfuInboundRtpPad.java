package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.concurrent.atomic.AtomicReference;

public class SfuInboundRtpPad {

    private final AtomicReference<Models.SfuInboundRtpPad> modelHolder;
    private final SfuMediaStreamsRepository sfuMediaStreamsRepository;

    SfuInboundRtpPad(
            Models.SfuInboundRtpPad model,
            SfuMediaStreamsRepository sfuMediaStreamsRepository
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfuMediaStreamsRepository = sfuMediaStreamsRepository;
    }

    public String getServiceId() {
        var model = this.modelHolder.get();
        return model.getServiceId();
    }

    public String getSfuId() {
        var model = this.modelHolder.get();
        return model.getSfuId();
    }

    public String getSfuTransportId() {
        var model = this.modelHolder.get();
        return model.getSfuTransportId();
    }

    public String getSfuStreamId() {
        var model = this.modelHolder.get();
        return model.getSfuStreamId();
    }

    public SfuMediaStream getSfuStream() {
        var model = this.modelHolder.get();
        return this.sfuMediaStreamsRepository.get(model.getSfuStreamId());
    }

    public String getRtpPadId() {
        var model = this.modelHolder.get();
        return model.getRtpPadId();
    }

    public Long getSSRC() {
        var model = this.modelHolder.get();
        return model.getSsrc();
    }

    public boolean isInternal() {
        var model = this.modelHolder.get();
        return model.getInternal();
    }

    public Long getAdded() {
        var model = this.modelHolder.get();
        return model.getAdded();
    }

    public String getMediaUnitId() {
        var model = this.modelHolder.get();
        return model.getMediaUnitId();
    }

    public String getMarker() {
        var model = this.modelHolder.get();
        return model.getMarker();
    }

}
