package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.concurrent.atomic.AtomicReference;

public class SfuSctpStream {

    private final AtomicReference<Models.SfuSctpStream> modelHolder;
    private final SfuSctpStreamsRepository sfuSctpStreamsRepository;
    private final SfuTransportsRepository sfuTransportsRepository;

    SfuSctpStream(
            Models.SfuSctpStream model,
            SfuSctpStreamsRepository sfuSctpStreamsRepository,
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

    public Long getOpened() {
        var model = this.modelHolder.get();
        return model.getOpened();
    }

    public Long getTouched() {
        var model = modelHolder.get();
        if (!model.hasTouched()) {
            return null;
        }
        return model.getTouched();
    }

    public void touch(Long timestamp) {
        var model = modelHolder.get();
        var newModel = Models.SfuSctpStream.newBuilder(model)
                .setTouched(timestamp)
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

    public Models.SfuSctpStream getModel() {
        return this.modelHolder.get();
    }

    private void updateModel(Models.SfuSctpStream newModel) {
        this.modelHolder.set(newModel);
        this.sfuSctpStreamsRepository.update(newModel);
    }
}
