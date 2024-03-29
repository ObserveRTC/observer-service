package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.concurrent.atomic.AtomicReference;

public class SfuOutboundRtpPad {

    private final AtomicReference<Models.SfuOutboundRtpPad> modelHolder;
    private final SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;
    private final SfuMediaSinksRepository sfuMediaSinksRepository;

    SfuOutboundRtpPad(
            Models.SfuOutboundRtpPad model,
            SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository,
            SfuMediaSinksRepository sfuMediaSinksRepository
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfuOutboundRtpPadsRepository = sfuOutboundRtpPadsRepository;
        this.sfuMediaSinksRepository = sfuMediaSinksRepository;
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

    public String getSfuSinkId() {
        var model = this.modelHolder.get();
        return model.getSfuSinkId();
    }

    public SfuMediaSink getSfuSink() {
        var model = this.modelHolder.get();
        return this.sfuMediaSinksRepository.get(model.getSfuSinkId());
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

    public void touch(Long sampleTimestamp, Long serverTimestamp) {
        var model = modelHolder.get();
        Models.SfuOutboundRtpPad.Builder newModel = null;
        if (sampleTimestamp != null) {
            newModel = Models.SfuOutboundRtpPad.newBuilder(model)
                    .setSampleTouched(sampleTimestamp);
        }
        if (serverTimestamp != null) {
            if (newModel == null) newModel = Models.SfuOutboundRtpPad.newBuilder(model);
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
        var newModel = Models.SfuOutboundRtpPad.newBuilder(model)
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
        var newModel = Models.SfuOutboundRtpPad.newBuilder(model)
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

    public SfuMediaSink getMediaSink() {
        var model = this.modelHolder.get();
        if (!model.hasSfuSinkId()) {
            return null;
        }
        var sfuSinkId = model.getSfuSinkId();
        return this.sfuMediaSinksRepository.get(sfuSinkId);
    }

    boolean createMediaSink() {
        var model = this.modelHolder.get();
        if (!model.hasSfuStreamId() || !model.hasSfuSinkId()) {
            return false;
        }

        var sfuMediaSinkModelBuilder = Models.SfuMediaSink.newBuilder()
                .setServiceId(model.getServiceId())
                .setSfuStreamId(model.getSfuStreamId())
                .setSfuSinkId(model.getSfuSinkId())
                .setInternal(true)
                ;
        this.sfuMediaSinksRepository.update(sfuMediaSinkModelBuilder.build());
        return true;
    }

    public Models.SfuOutboundRtpPad getModel() {
        return this.modelHolder.get();
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }

    private void updateModel(Models.SfuOutboundRtpPad newModel) {
        this.modelHolder.set(newModel);
        this.sfuOutboundRtpPadsRepository.update(newModel);
    }
}
