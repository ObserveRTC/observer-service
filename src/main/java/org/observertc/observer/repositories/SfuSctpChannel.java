package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.concurrent.atomic.AtomicReference;

public class SfuSctpChannel {

    private final AtomicReference<Models.SfuSctpChannel> modelHolder;
    private final SfuSctpChannelsRepository sfuSctpStreamsRepository;
    private final SfuTransportsRepository sfuTransportsRepository;

    SfuSctpChannel(
            Models.SfuSctpChannel model,
            SfuSctpChannelsRepository sfuSctpStreamsRepository,
            SfuTransportsRepository sfuTransportsRepository
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfuSctpStreamsRepository = sfuSctpStreamsRepository;
        this.sfuTransportsRepository = sfuTransportsRepository;
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

    public SfuTransport getSfuTransport() {
        var model = this.modelHolder.get();
        return this.sfuTransportsRepository.get(model.getSfuTransportId());
    }

    public String getSfuSctpStreamId() {
        var model = this.modelHolder.get();
        return model.getSfuSctpStreamId();
    }

    public String getSfuSctpChannelId() {
        var model = this.modelHolder.get();
        return model.getSfuSctpChannelId();
    }

    public Long getOpened() {
        var model = this.modelHolder.get();
        return model.getOpened();
    }

    public Long getSampleTouched() {
        var model = modelHolder.get();
        if (!model.hasSampleTouched()) {
            return null;
        }
        return model.getSampleTouched();
    }

    public void touch(Long sampleTimestamp, Long serverTimestamp) {
        var model = modelHolder.get();
        Models.SfuSctpChannel.Builder newModel = null;
        if (sampleTimestamp != null) {
            newModel = Models.SfuSctpChannel.newBuilder(model)
                    .setSampleTouched(sampleTimestamp);
        }
        if (serverTimestamp != null) {
            if (newModel == null) newModel = Models.SfuSctpChannel.newBuilder(model);
            newModel.setServerTouched(serverTimestamp);
        }
        if (newModel == null) {
            return;
        }
        this.updateModel(newModel.build());
    }

    public void touchBySample(Long timestamp) {
        var model = modelHolder.get();
        var newModel = Models.SfuSctpChannel.newBuilder(model)
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
        var newModel = Models.SfuSctpChannel.newBuilder(model)
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

    public Models.SfuSctpChannel getModel() {
        return this.modelHolder.get();
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }

    private void updateModel(Models.SfuSctpChannel newModel) {
        this.modelHolder.set(newModel);
        this.sfuSctpStreamsRepository.update(newModel);
    }
}
