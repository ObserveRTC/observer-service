package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    public SfuTransport getSfuTransport() {
        var model = this.modelHolder.get();
        return this.sfuTransportsRepository.get(model.getSfuTransportId());
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

    public Collection<String> getOutboundRtpPadIds() {
        var model = this.modelHolder.get();
        if (model.getOutboundSfuRtpPadIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getOutboundSfuRtpPadIdsList();
    }

    public SfuOutboundRtpPad addOutboundRtpPad(String rtpPadId, Long timestamp) throws AlreadyCreatedException {
        var model = modelHolder.get();
        if (0 < model.getOutboundSfuRtpPadIdsCount()) {
            var rtpPadIds = model.getOutboundSfuRtpPadIdsList();
            if (rtpPadIds.contains(rtpPadId)) {
                throw AlreadyCreatedException.wrapSfuOutboundRtpPad(rtpPadId);
            }
        }

        var sfuOutboundRtpPadModel = Models.SfuOutboundRtpPad.newBuilder()
                .setServiceId(model.getServiceId())
                .setMediaUnitId(model.getMediaUnitId())
                .setMarker(model.getMarker())
                .build();

        var newModel = Models.SfuMediaSink.newBuilder(model)
                .addOutboundSfuRtpPadIds(rtpPadId)
                .build();

        this.updateModel(newModel);
        this.sfuOutboundRtpPadsRepository.update(sfuOutboundRtpPadModel);
        return this.sfuOutboundRtpPadsRepository.wrapSfuOutboundRtpPad(sfuOutboundRtpPadModel);
    }

    public boolean removeOutboundRtpPad(String rtpPadId) {
        var model = modelHolder.get();
        if (model.getOutboundSfuRtpPadIdsCount() < 1) {
            return false;
        }
        var rtpPadIds = model.getOutboundSfuRtpPadIdsList();
        if (!rtpPadIds.contains(rtpPadId)) {
            return false;
        }
        var newOutboundRtpPadIds = rtpPadIds.stream().filter(actualPcId -> actualPcId != rtpPadId)
                .collect(Collectors.toSet());

        var newModel = Models.SfuMediaSink.newBuilder(model)
                .clearOutboundSfuRtpPadIds()
                .addAllOutboundSfuRtpPadIds(newOutboundRtpPadIds)
                .build();

        this.updateModel(newModel);
        this.sfuOutboundRtpPadsRepository.delete(rtpPadId);
        return true;
    }

    private void updateModel(Models.SfuMediaSink newModel) {
        this.modelHolder.set(newModel);
        this.sfuMediaSinksRepository.update(newModel);
    }
}
