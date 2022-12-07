package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Sfu {

    private final AtomicReference<Models.Sfu> modelHolder;
    private final SfusRepository sfusRepository;
    private final SfuTransportsRepository sfuTransportsRepository;

    Sfu(Models.Sfu model, SfusRepository sfusRepository, SfuTransportsRepository sfuTransportsRepository) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfusRepository = sfusRepository;
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

    public Long getJoined() {
        var model = this.modelHolder.get();
        return model.getJoined();
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
        var newModel = Models.Sfu.newBuilder(model)
                .setSampleTouched(timestamp)
                .build();
        this.updateModel(newModel);
    }

    public void touch(Long sampleTimestamp, Long serverTimestamp) {
        var model = modelHolder.get();
        Models.Sfu.Builder newModel = null;
        if (sampleTimestamp != null) {
            newModel = Models.Sfu.newBuilder(model)
                    .setSampleTouched(sampleTimestamp);
        }
        if (serverTimestamp != null) {
            if (newModel == null) newModel = Models.Sfu.newBuilder(model);
            newModel.setServerTouched(serverTimestamp);
        }
        if (newModel == null) {
            return;
        }
        this.updateModel(newModel.build());
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
        var newModel = Models.Sfu.newBuilder(model)
                .setServerTouched(timestamp)
                .build();
        this.updateModel(newModel);
    }

    public String getMediaUnitId() {
        var model = this.modelHolder.get();
        return model.getMediaUnitId();
    }

    public String getTimeZoneId() {
        var model = this.modelHolder.get();
        return model.getTimeZoneId();
    }

    public String getMarker() {
        var model = this.modelHolder.get();
        return model.getMarker();
    }


    public boolean hasSfuTransport(String sfuTransportId) {
        var model = this.modelHolder.get();
        if (model.getSfuTransportIdsCount() < 1) {
            return false;
        }
        return model.getSfuTransportIdsList().contains(sfuTransportId);
    }

    public Collection<String> getSfuTransportIds() {
        var model = this.modelHolder.get();
        if (model.getSfuTransportIdsCount() < 1) {
            return Collections.emptySet();
        }
        return model.getSfuTransportIdsList();
    }

    public Map<String, SfuTransport> getSfuTransports() {
        var sfuTransportIds = this.getSfuTransportIds();
        if (sfuTransportIds.size() < 1) {
            return Collections.emptyMap();
        }
        return this.sfuTransportsRepository.getAll(sfuTransportIds);
    }

    public SfuTransport getSfuTransport(String sfuTransportId) {
        var model = this.modelHolder.get();
        if (model.getSfuTransportIdsCount() < 1) {
            return null;
        }
        if (!model.getSfuTransportIdsList().contains(sfuTransportId)) {
            return null;
        }
        return this.sfuTransportsRepository.get(sfuTransportId);
    }

    public SfuTransport addSfuTransport(String sfuTransportId, boolean internal, Long timestamp, String marker) throws AlreadyCreatedException {
        var model = modelHolder.get();
        var sfuTransportIds = model.getSfuTransportIdsList();
        if (sfuTransportIds.contains(sfuTransportId)) {
            throw AlreadyCreatedException.wrapSfuTransport(sfuTransportId);
        }
        var sfuTransportModelBuilder = Models.SfuTransport.newBuilder()
                .setServiceId(model.getServiceId())
                .setSfuId(model.getSfuId())
                .setTransportId(sfuTransportId)
                .setInternal(internal)
                .setOpened(timestamp)
                .setSampleTouched(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                ;

        if (marker != null) {
            sfuTransportModelBuilder.setMarker(marker);
        }
        var sfuTransportModel = sfuTransportModelBuilder.build();
        var newModel = Models.Sfu.newBuilder(model)
                .addSfuTransportIds(sfuTransportId)
                .build();

        this.updateModel(newModel);
        this.sfuTransportsRepository.update(sfuTransportModel);
        return this.sfuTransportsRepository.wrapSfuTransport(sfuTransportModel);
    }

    public boolean removeSfuTransport(String sfuTransportId) {
        var model = this.modelHolder.get();
        if (model.getSfuTransportIdsCount() < 1) {
            return false;
        }
        var sfuTransportIds = model.getSfuTransportIdsList();
        if (!sfuTransportIds.contains(sfuTransportId)) {
            return false;
        }

        var newSfuTransportIds = sfuTransportIds.stream()
                .filter(savedTransportId -> savedTransportId != sfuTransportId)
                .collect(Collectors.toSet());

        var newModel = Models.Sfu.newBuilder(model)
                .clearSfuTransportIds()
                .addAllSfuTransportIds(newSfuTransportIds)
                .build();

        this.updateModel(newModel);
        this.sfuTransportsRepository.delete(sfuTransportId);
        return true;
    }

    public Models.Sfu getModel() {
        return this.modelHolder.get();
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }

    private void updateModel(Models.Sfu newModel) {
        this.modelHolder.set(newModel);
        this.sfusRepository.update(newModel);
    }
}
