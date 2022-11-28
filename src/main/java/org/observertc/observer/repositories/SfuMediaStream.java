package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.Collections;
import java.util.Set;
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

    public String getSfuStreamId() {
        var model = this.modelHolder.get();
        return model.getSfuStreamId();
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

    public Set<String> getSfuInboundRtpPadIds() {
        var model = this.modelHolder.get();
        if (model.getSfuInboundSfuRtpPadIdsCount() < 1) {
            return Collections.emptySet();
        }
        var list = model.getSfuInboundSfuRtpPadIdsList();
        return list.stream().collect(Collectors.toSet());
    }


    public boolean hasSfuInboundRtpPadId(String rtpPadId) {
        var model = modelHolder.get();
        if (model.getSfuInboundSfuRtpPadIdsCount() < 1) {
            return false;
        }
        var outboundSfuRtpPadIds = model.getSfuInboundSfuRtpPadIdsList();
        return outboundSfuRtpPadIds.contains(rtpPadId);
    }

    public void addSfuInboundRtpPadId(String rtpPadId) {
        var model = modelHolder.get();
        if (this.hasSfuInboundRtpPadId(rtpPadId)) {
            throw AlreadyCreatedException.wrapSfuInboundRtpPad(rtpPadId);
        }
        var newModel = Models.SfuMediaStream.newBuilder(model)
                .addSfuInboundSfuRtpPadIds(rtpPadId)
                .build();
        this.updateModel(newModel);
    }

    public boolean hasMediaSink(String sinkId) {
        var model = modelHolder.get();
        if (model.getSfuInboundSfuRtpPadIdsCount() < 1) {
            return false;
        }
        var mediaSinkIds = model.getSfuMediaSinkIdsList();
        return mediaSinkIds.contains(sinkId);
    }

    public void addSfuMediaSink(String sinkId) {
        var model = modelHolder.get();
        if (this.hasMediaSink(sinkId)) {
            throw AlreadyCreatedException.wrapSfuMediaSink(sinkId);
        }
        var newModel = Models.SfuMediaStream.newBuilder(model)
                .addSfuMediaSinkIds(sinkId)
                .build();
        this.updateModel(newModel);
    }

    public Set<String> getSfuMediaSinkIds() {
        var model = modelHolder.get();
        if (model.getSfuMediaSinkIdsCount() < 1) {
            return Collections.emptySet();
        }
        var list = model.getSfuInboundSfuRtpPadIdsList();
        return list.stream().collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }

    private void updateModel(Models.SfuMediaStream newModel) {
        this.modelHolder.set(newModel);
        this.sfuMediaStreamsRepository.update(newModel);
    }
}
