package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class OutboundVideoTrack {

    private final PeerConnectionsRepository peerConnectionsRepository;
    private final AtomicReference<Models.OutboundVideoTrack> modelHolder;
    private final OutboundVideoTracksRepository outboundVideoTracksRepository;

    OutboundVideoTrack(PeerConnectionsRepository peerConnectionsRepository,
                       Models.OutboundVideoTrack model,
                       OutboundVideoTracksRepository outboundVideoTracksRepository
    ) {
        this.peerConnectionsRepository = peerConnectionsRepository;
        this.modelHolder = new AtomicReference<>(model);
        this.outboundVideoTracksRepository = outboundVideoTracksRepository;
    }

    public PeerConnection getPeerConnection() {
        var model = modelHolder.get();
        return this.peerConnectionsRepository.get(model.getPeerConnectionId());
    }

    public String getServiceId(){
        var model = modelHolder.get();
        return model.getServiceId();
    }

    public String getRoomId() {
        var model = modelHolder.get();
        return model.getRoomId();
    }

    public String getCallId() {
        var model = modelHolder.get();
        return model.getCallId();
    }

    public String getClientId() {
        var model = modelHolder.get();
        return model.getClientId();
    }

    public String getPeerConnectionId() {
        var model = modelHolder.get();
        return model.getPeerConnectionId();
    }

    public String getTrackId() {
        var model = modelHolder.get();
        return model.getTrackId();
    }

    public String getMediaUnitId() {
        var model = modelHolder.get();
        return model.getMediaUnitId();
    }

    public String getMarker() {
        var model = this.modelHolder.get();
        return model.getMarker();
    }

    public String getSfuStreamId() {
        var model = this.modelHolder.get();
        if (!model.hasSfuStreamId()) {
            return null;
        }
        return model.getSfuStreamId();
    }

    public List<Long> getSSSRCs() {
        var model = this.modelHolder.get();
        return model.getSsrcList();
    }

    public boolean hasSSRC(Long ssrc) {
        var model = this.modelHolder.get();
        if (model.getSsrcCount() < 1) {
            return false;
        }
        return model.getSsrcList().contains(ssrc);
    }

    public void addSSRC(Long ssrc) {
        var model = this.modelHolder.get();
        if (0 < model.getSsrcCount()) {
            if (model.getSsrcList().contains(ssrc)) {
                return;
            }
        }

        var newModel = Models.OutboundVideoTrack.newBuilder(model)
                .addSsrc(ssrc)
                .build();

        this.updateModel(newModel);
    }

    public boolean removeSSRC(Long ssrc) {
        var model = this.modelHolder.get();
        if (model.getSsrcCount() < 1) {
            return false;
        }
        if (!model.getSsrcList().contains(ssrc)) {
            return false;
        }

        var newList = model.getSsrcList().stream().filter(savedSSRC -> savedSSRC != ssrc)
                .collect(Collectors.toSet());
        var newModel = Models.OutboundVideoTrack.newBuilder(model)
                .clearSsrc()
                .addAllSsrc(newList)
                .build();
        this.updateModel(newModel);
        return true;
    }

    private void updateModel(Models.OutboundVideoTrack newModel) {
        this.modelHolder.set(newModel);
        this.outboundVideoTracksRepository.update(newModel);
    }
}
