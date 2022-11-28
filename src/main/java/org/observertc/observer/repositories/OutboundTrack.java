package org.observertc.observer.repositories;

import org.observertc.observer.configs.MediaKind;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class OutboundTrack {

    private final ServiceRoomId serviceRoomId;
    private final PeerConnectionsRepository peerConnectionsRepository;
    private final AtomicReference<Models.OutboundTrack> modelHolder;
    private final OutboundTracksRepository outboundTracksRepository;
    private final SfuMediaStreamsRepository sfuMediaStreamsRepository;

    OutboundTrack(PeerConnectionsRepository peerConnectionsRepository,
                  Models.OutboundTrack model,
                  OutboundTracksRepository outboundTracksRepository,
                  SfuMediaStreamsRepository sfuMediaStreamsRepository
    ) {
        this.peerConnectionsRepository = peerConnectionsRepository;
        this.modelHolder = new AtomicReference<>(model);
        this.outboundTracksRepository = outboundTracksRepository;
        this.sfuMediaStreamsRepository = sfuMediaStreamsRepository;
        this.serviceRoomId = ServiceRoomId.make(model.getServiceId(), model.getRoomId());
    }

    public ServiceRoomId getServiceRoomId() {
        return serviceRoomId;
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

    public String getUserId() {
        var model = modelHolder.get();
        if (!model.hasUserId()) {
            return null;
        }
        return model.getUserId();
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

    public MediaKind getKind() {
        var model = modelHolder.get();
        return MediaKind.valueOf(model.getKind());
    }

    public Long getAdded() {
        var model = modelHolder.get();
        return model.getAdded();
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
        var newModel = Models.OutboundTrack.newBuilder(model)
                .setTouched(timestamp)
                .build();
        this.updateModel(newModel);
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

    public SfuMediaStream getMediaStream() {
        var model = this.modelHolder.get();
        if (!model.hasSfuStreamId()) {
            return null;
        }
        var sfuStreamId = model.getSfuStreamId();
        return this.sfuMediaStreamsRepository.get(sfuStreamId);
    }

    boolean createMediaStream() {
        var model = this.modelHolder.get();
        if (!model.hasSfuStreamId()) {
            return false;
        }
        var sfuMediaStreamModelBuilder = Models.SfuMediaStream.newBuilder()
                .setServiceId(model.getServiceId())
                .setSfuStreamId(model.getSfuStreamId())
                .setCallId(model.getCallId())
                .setClientId(model.getClientId())
                .setPeerConnectionId(model.getPeerConnectionId())
                .setTrackId(model.getTrackId())
                .setKind(model.getKind())
                ;

        if (model.hasUserId()) {
            sfuMediaStreamModelBuilder.setUserId(model.getUserId());
        }
        this.sfuMediaStreamsRepository.update(sfuMediaStreamModelBuilder.build());
        return true;
    }

    public void addSSRC(Long ssrc) {
        var model = this.modelHolder.get();
        if (0 < model.getSsrcCount()) {
            if (model.getSsrcList().contains(ssrc)) {
                return;
            }
        }

        var newModel = Models.OutboundTrack.newBuilder(model)
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
        var newModel = Models.OutboundTrack.newBuilder(model)
                .clearSsrc()
                .addAllSsrc(newList)
                .build();
        this.updateModel(newModel);
        return true;
    }

    public Models.OutboundTrack getModel() {
        return this.modelHolder.get();
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }

    private void updateModel(Models.OutboundTrack newModel) {
        this.modelHolder.set(newModel);
        this.outboundTracksRepository.update(newModel);
    }
}
