package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.concurrent.atomic.AtomicReference;

public class SfuInboundRtpPad {

    private final AtomicReference<Models.SfuInboundRtpPad> modelHolder;
    private final SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;
    private final SfuMediaStreamsRepository sfuMediaStreamsRepository;

    SfuInboundRtpPad(
            Models.SfuInboundRtpPad model,
            SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository,
            SfuMediaStreamsRepository sfuMediaStreamsRepository
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfuInboundRtpPadsRepository = sfuInboundRtpPadsRepository;
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
        if (!model.hasInternal()) {
            return false;
        }
        return model.getInternal();
    }

    public Long getAdded() {
        var model = this.modelHolder.get();
        return model.getAdded();
    }

    public void touch(Long sampleTimestamp, Long serverTimestamp) {
        var model = modelHolder.get();
        Models.SfuInboundRtpPad.Builder newModel = null;
        if (sampleTimestamp != null) {
            newModel = Models.SfuInboundRtpPad.newBuilder(model)
                    .setSampleTouched(sampleTimestamp);
        }
        if (serverTimestamp != null) {
            if (newModel == null) newModel = Models.SfuInboundRtpPad.newBuilder(model);
            newModel.setServerTouched(serverTimestamp);
        }
        if (newModel == null) {
            return;
        }
        this.updateModel(newModel.build());
    }

    public Long getSampleTouched() {
        var model = modelHolder.get();
        if (!model.hasSampleTouched()) {
            return null;
        }
        return model.getSampleTouched();
    }

    public void touchBySample(Long timestamp) {
        var model = modelHolder.get();
        var newModel = Models.SfuInboundRtpPad.newBuilder(model)
                .setSampleTouched(timestamp)
                .build();
        this.updateModel(newModel);
    }

    public Long getServerTouch() {
        var model = this.modelHolder.get();
        if (!model.hasServerTouched()) {
            return null;
        }
        return model.getServerTouched();
    }

    public void touchByServer(Long timestamp) {
        var model = modelHolder.get();
        var newModel = Models.SfuInboundRtpPad.newBuilder(model)
                .setServerTouched(timestamp)
                .build();
        this.updateModel(newModel);
    }

    public String getMediaUnitId() {
        var model = this.modelHolder.get();
        return model.getMediaUnitId();
    }

    public String getMarker() {
        var model = this.modelHolder.get();
        return model.getMarker();
    }

    private void updateModel(Models.SfuInboundRtpPad newModel) {
        this.modelHolder.set(newModel);
        this.sfuInboundRtpPadsRepository.update(newModel);
    }

    public Models.SfuInboundRtpPad getModel() {
        return this.modelHolder.get();
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }

}
