package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.concurrent.atomic.AtomicReference;

public class SfuMediaSink {

    private final AtomicReference<Models.SfuMediaSink> modelHolder;
    private final SfuMediaSinksRepository sfuMediaSinksRepository;
    private final SfuTransportsRepository sfuTransportsRepository;
    private final SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    SfuMediaSink(
            Models.SfuMediaSink model,
            SfuMediaSinksRepository sfuMediaSinksRepository,
            SfuTransportsRepository sfuTransportsRepository,
            SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfuMediaSinksRepository = sfuMediaSinksRepository;
        this.sfuTransportsRepository = sfuTransportsRepository;
        this.sfuOutboundRtpPadsRepository = sfuOutboundRtpPadsRepository;
    }

    public String getServiceId() {
        var model = this.modelHolder.get();
        return model.getServiceId();
    }

    public String getSfuStreamId() {
        var model = this.modelHolder.get();
        return model.getSfuStreamId();
    }

    public String getSfuSinkId() {
        var model = this.modelHolder.get();
        return model.getSfuSinkId();
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }

    private void updateModel(Models.SfuMediaSink newModel) {
        this.modelHolder.set(newModel);
        this.sfuMediaSinksRepository.update(newModel);
    }
}
