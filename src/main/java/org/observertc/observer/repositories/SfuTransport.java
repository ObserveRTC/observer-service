package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class SfuTransport {

    private final AtomicReference<Models.SfuTransport> modelHolder;
    private final SfusRepository sfusRepository;
    private final SfuTransportsRepository sfuTransportsRepository;

    SfuTransport(
            Models.SfuTransport model,
            SfusRepository sfusRepository,
            SfuTransportsRepository sfuTransportsRepository
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfusRepository = sfusRepository;
        this.sfuTransportsRepository = sfuTransportsRepository;
    }

    public String getServiceId() {
        var model = this.modelHolder.get();
        return model.getServiceId();
    }

    public Sfu getSfu() {
        var model = this.modelHolder.get();
        return this.sfusRepository.get(model.getSfuId());
    }

    public String getSfuId() {
        var model = this.modelHolder.get();
        return model.getSfuId();
    }

    public String getSfuTransportId() {
        var model = this.modelHolder.get();
        return model.getTransportId();
    }

    public boolean isInternal() {
        var model = this.modelHolder.get();
        return model.getInternal();
    }

    public Long getOpened() {
        var model = this.modelHolder.get();
        return model.getOpened();
    }

    public String getMediaUnitId() {
        var model = this.modelHolder.get();
        return model.getMediaUnitId();
    }

    public String getMarker() {
        var model = this.modelHolder.get();
        return model.getMarker();
    }

    public Collection<String> getSfuStreamIds() {
        var model = this.modelHolder.get();
        if (model.getSfuMediaSinkIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getSfuMediaStreamIdsList();
    }

    public Collection<String> getSfuSinkIds() {
        var model = this.modelHolder.get();
        if (model.getSfuMediaSinkIdsCount() < 1) {
            return Collections.emptySet();
        }
        return model.getSfuMediaSinkIdsList();
    }


}
