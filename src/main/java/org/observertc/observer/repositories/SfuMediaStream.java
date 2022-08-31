package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    public Collection<String> getInboundRtpPadIds() {
        var model = this.modelHolder.get();
        if (model.getInboundSfuRtpPadIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getInboundSfuRtpPadIdsList();
    }

    public boolean hasInboundRtpPad(String rtpPadId) {
        var model = this.modelHolder.get();
        if (model.getInboundSfuRtpPadIdsCount() < 1) {
            return false;
        }
        return model.getInboundSfuRtpPadIdsList().contains(rtpPadId);
    }

    public SfuInboundRtpPad getInboundRtpPad(String rtpPadId) {
        var model = this.modelHolder.get();
        if (model.getInboundSfuRtpPadIdsCount() < 1) {
            return null;
        }
        return this.sfuInboundRtpPadsRepository.get(rtpPadId);
    }

    public Map<String, SfuInboundRtpPad> getInboundRtpPads() {
        var model = this.modelHolder.get();
        if (model.getInboundSfuRtpPadIdsCount() < 1) {
            return Collections.emptyMap();
        }
        var rtpPadIds = this.getInboundRtpPadIds();
        return this.sfuInboundRtpPadsRepository.getAll(rtpPadIds);
    }

    public SfuInboundRtpPad addInboundRtpPad(String rtpPadId, Long ssrc, boolean internal, Long timestamp) throws AlreadyCreatedException {
        var model = modelHolder.get();
        if (0 < model.getInboundSfuRtpPadIdsCount()) {
            var inboundRtpPadIds = model.getInboundSfuRtpPadIdsList();
            if (inboundRtpPadIds.contains(rtpPadId)) {
                throw AlreadyCreatedException.wrapSfuInboundRtpPad(rtpPadId);
            }
        }

        var inboundRtpPadModel = Models.SfuInboundRtpPad.newBuilder()
                .setServiceId(model.getServiceId())
                .setSfuId(model.getSfuId())
                .setSfuTransportId(model.getSfuTransportId())
                .setSfuStreamId(model.getSfuStreamId())
                .setRtpPadId(rtpPadId)
                .setSsrc(ssrc)
                .setInternal(internal)
                .setAdded(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                .setMarker(model.getMarker())
                .build();

        var newModel = Models.SfuMediaStream.newBuilder(model)
                .addInboundSfuRtpPadIds(rtpPadId)
                .build();

        this.updateModel(newModel);
        this.sfuInboundRtpPadsRepository.update(inboundRtpPadModel);
        return this.sfuInboundRtpPadsRepository.wrapInboundRtpPad(inboundRtpPadModel);

    }

    public boolean removeInboundRtpPad(String rtpPadId) {
        var model = modelHolder.get();
        if (model.getInboundSfuRtpPadIdsCount() < 1) {
            return false;
        }
        var inboundRtpPadIds = model.getInboundSfuRtpPadIdsList();
        if (!inboundRtpPadIds.contains(rtpPadId)) {
            return false;
        }
        var newInboundRtpPadIds = inboundRtpPadIds.stream().filter(actualPcId -> actualPcId != rtpPadId)
                .collect(Collectors.toSet());

        var newModel = Models.SfuMediaStream.newBuilder(model)
                .clearInboundSfuRtpPadIds()
                .addAllInboundSfuRtpPadIds(newInboundRtpPadIds)
                .build();

        this.updateModel(newModel);
        this.sfuInboundRtpPadsRepository.delete(rtpPadId);
        return true;
    }

    public Collection<String> getSfuSinkIds() {
        var model = this.modelHolder.get();
        if (model.getSfuSinkIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getSfuSinkIdsList();
    }

    public boolean hasSfuSink(String sfuSinkId) {
        var model = this.modelHolder.get();
        if (model.getSfuSinkIdsCount() < 1) {
            return false;
        }
        return model.getSfuSinkIdsList().contains(sfuSinkId);
    }

    public SfuMediaSink getSfuSink(String sfuSinkId) {

    }

    public Map<String, SfuMediaSink> getSfuSinks() {
        var model = this.modelHolder.get();
        if (model.getSfuSinkIdsCount() < 1) {
            return Collections.emptyMap();
        }
        return this.sfuMediaSinksRepository.getAll(model.getSfuSinkIdsList());
    }

    public SfuMediaSink addSfuSink(String sfuSinkId, Long timestamp) {
        var model = modelHolder.get();
        if (0 < model.getSfuSinkIdsCount()) {
            var sfuSinkIds = model.getSfuSinkIdsList();
            if (sfuSinkIds.contains(sfuSinkId)) {
                throw AlreadyCreatedException.wrapSfuMediaSink(sfuSinkId);
            }
        }

        var sfuMediaSinkModel = Models.SfuMediaSink.newBuilder()
                .setServiceId(model.getServiceId())
                .setSfuId(model.getSfuId())
                .setSfuTransportId(model.getSfuTransportId())
                .setSfuStreamId(model.getSfuStreamId())
                .setSfuSinkId(sfuSinkId)
                .setAdded(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                .setMarker(model.getMarker())
                .build();

        var newModel = Models.SfuMediaStream.newBuilder(model)
                .addSfuSinkIds(sfuSinkId)
                .build();

        this.updateModel(newModel);
        this.sfuMediaSinksRepository.update(sfuMediaSinkModel);
        return this.sfuMediaSinksRepository.wrapSfuSink(sfuMediaSinkModel);

    }

    public boolean removeSfuSink(String sfuSinkId) {

    }

    private void updateModel(Models.SfuMediaStream newModel) {
        this.modelHolder.set(newModel);
        this.sfuMediaStreamsRepository.update(newModel);
    }
}
