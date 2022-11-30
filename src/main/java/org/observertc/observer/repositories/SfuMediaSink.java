package org.observertc.observer.repositories;

import org.observertc.observer.configs.MediaKind;
import org.observertc.schemas.dtos.Models;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SfuMediaSink {

    private final AtomicReference<Models.SfuMediaSink> modelHolder;
    private final SfuMediaSinksRepository sfuMediaSinksRepository;
    private final SfuMediaStreamsRepository sfuMediaStreamsRepository;
    private final SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    SfuMediaSink(
            Models.SfuMediaSink model,
            SfuMediaSinksRepository sfuMediaSinksRepository,
            SfuMediaStreamsRepository sfuMediaStreamsRepository,
            SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository
    ) {
        this.modelHolder = new AtomicReference<>(model);
        this.sfuMediaSinksRepository = sfuMediaSinksRepository;
        this.sfuMediaStreamsRepository = sfuMediaStreamsRepository;
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

    public SfuMediaStream getMediaStream() {
        var model = this.modelHolder.get();
        if (!model.hasSfuStreamId()) {
            return null;
        }
        return this.sfuMediaStreamsRepository.get(model.getSfuStreamId());
    }

    public boolean isInternal() {
        var model = this.modelHolder.get();
        if (!model.hasInternal()) {
            return false;
        }
        return model.getInternal();
    }

    public String getSfuSinkId() {
        var model = this.modelHolder.get();
        return model.getSfuSinkId();
    }

    public String getCallId() {
        var model = this.modelHolder.get();
        if (!model.hasCallId()) {
            return null;
        }
        return model.getCallId();
    }

    public String getClientId() {
        var model = this.modelHolder.get();
        if (!model.hasClientId()) {
            return null;
        }
        return model.getClientId();
    }

    public MediaKind getKind() {
        var model = this.modelHolder.get();
        if (!model.hasKind()) {
            return null;
        }
        return MediaKind.valueOf(model.getKind());
    }

    public String getPeerConnectionId() {
        var model = this.modelHolder.get();
        if (!model.hasPeerConnectionId()) {
            return null;
        }
        return model.getPeerConnectionId();
    }

    public String getTrackId() {
        var model = this.modelHolder.get();
        if (!model.hasTrackId()) {
            return null;
        }
        return model.getTrackId();
    }

    public String getUserId() {
        var model = this.modelHolder.get();
        if (!model.hasUserId()) {
            return null;
        }
        return model.getUserId();
    }

    public boolean hasSfuOutboundRtpPadId(String rtpPadId) {
        var model = modelHolder.get();
        if (model.getOutboundSfuRtpPadIdsCount() < 1) {
            return false;
        }
        var outboundSfuRtpPadIds = model.getOutboundSfuRtpPadIdsList();
        return outboundSfuRtpPadIds.contains(rtpPadId);
    }

    public void addSfuOutboundRtpPadId(String rtpPadId) {
        var model = modelHolder.get();
        if (this.hasSfuOutboundRtpPadId(rtpPadId)) {
            throw AlreadyCreatedException.wrapSfuInboundRtpPad(rtpPadId);
        }
        var newModel = Models.SfuMediaSink.newBuilder(model)
                .addOutboundSfuRtpPadIds(rtpPadId)
                .build();
        this.updateModel(newModel);
    }

    public Set<String> getSfuOutboundSfuRtpPadIds() {
        var model = modelHolder.get();
        if (model.getOutboundSfuRtpPadIdsCount() < 1) {
            return Collections.emptySet();
        }
        var list = model.getOutboundSfuRtpPadIdsList();
        return list.stream().collect(Collectors.toSet());
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
