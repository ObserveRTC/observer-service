package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.concurrent.atomic.AtomicReference;

public class SfuMediaStream {

    private final AtomicReference<Models.SfuMediaStream> modelHolder;
    private final SfuMediaStreamsRepository sfuMediaStreamsRepository;
    private final SfuMediaSinksRepository sfuMediaSinksRepository;
    private final SfuTransportsRepository sfuTransportsRepository;
    private final SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    SfuMediaStream(
            Models.SfuMediaStream model,
            SfuMediaStreamsRepository sfuMediaStreamsRepository,
            SfuMediaSinksRepository sfuMediaSinksRepository,
            SfuTransportsRepository sfuTransportsRepository,
            SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfuMediaStreamsRepository = sfuMediaStreamsRepository;
        this.sfuMediaSinksRepository = sfuMediaSinksRepository;
        this.sfuTransportsRepository = sfuTransportsRepository;
        this.sfuInboundRtpPadsRepository = sfuInboundRtpPadsRepository;
    }

    public String getServiceId() {
        var model = this.modelHolder.get();
        return model.getServiceId();
    }

    public String getSfuStreamId() {
        var model = this.modelHolder.get();
        return model.getSfuStreamId();
    }

    private void updateModel(Models.SfuMediaStream newModel) {
        this.modelHolder.set(newModel);
        this.sfuMediaStreamsRepository.update(newModel);
    }
}
