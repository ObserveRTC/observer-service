package org.observertc.observer.repositories;

import org.observertc.observer.configs.MediaKind;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class InboundTrack {

    private final ServiceRoomId serviceRoomId;
    private final PeerConnectionsRepository peerConnectionsRepository;
    private final AtomicReference<Models.InboundTrack> modelHolder;
    private final InboundTracksRepository inboundTracksRepository;
    private final SfuMediaSinksRepository sfuMediaSinksRepository;

    InboundTrack(PeerConnectionsRepository peerConnectionsRepository,
                 Models.InboundTrack model,
                 InboundTracksRepository inboundTracksRepository,
                 SfuMediaSinksRepository sfuMediaSinksRepository
    ) {
        this.peerConnectionsRepository = peerConnectionsRepository;
        this.modelHolder = new AtomicReference<>(model);
        this.inboundTracksRepository = inboundTracksRepository;
        this.sfuMediaSinksRepository = sfuMediaSinksRepository;
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

    public Long getSampleTouched() {
        var model = modelHolder.get();
        if (!model.hasSampleTouched()) {
            return null;
        }
        return model.getSampleTouched();
    }

    public void touchBySample(Long timestamp) {
        var model = modelHolder.get();
        var newModel = Models.InboundTrack.newBuilder(model)
                .setSampleTouched(timestamp)
                .build();
        this.updateModel(newModel);
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
        var newModel = Models.InboundTrack.newBuilder(model)
                .setServerTouched(timestamp)
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

    public String getSfuSinkId() {
        var model = this.modelHolder.get();
        if (!model.hasSfuSinkId()) {
            return null;
        }
        return model.getSfuSinkId();
    }

    public SfuMediaSink getMediaSink() {
        var model = this.modelHolder.get();
        if (!model.hasSfuSinkId()) {
            return null;
        }
        var sfuSinkId = model.getSfuSinkId();
        return  this.sfuMediaSinksRepository.get(sfuSinkId);
    }

    boolean createMediaSink() {
        var model = this.modelHolder.get();
        if (!model.hasSfuStreamId() || !model.hasSfuSinkId()) {
            return false;
        }

        var sfuMediaSinkModelBuilder = Models.SfuMediaSink.newBuilder()
                .setServiceId(model.getServiceId())
                .setSfuStreamId(model.getSfuStreamId())
                .setSfuSinkId(model.getSfuSinkId())
                .setInternal(false)

                .setCallId(model.getCallId())
                .setClientId(model.getClientId())
                .setPeerConnectionId(model.getPeerConnectionId())
                .setTrackId(model.getTrackId())
                .setKind(model.getKind())
                ;

        if (model.hasUserId()) {
            sfuMediaSinkModelBuilder.setUserId(model.getUserId());
        }
        this.sfuMediaSinksRepository.update(sfuMediaSinkModelBuilder.build());
        return true;
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

        var newModel = Models.InboundTrack.newBuilder(model)
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
        var newModel = Models.InboundTrack.newBuilder(model)
                .clearSsrc()
                .addAllSsrc(newList)
                .build();
        this.updateModel(newModel);
        return true;
    }

    public Models.InboundTrack getModel() {
        return this.modelHolder.get();
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }

    private void updateModel(Models.InboundTrack newModel) {
        this.modelHolder.set(newModel);
        this.inboundTracksRepository.update(newModel);
    }
}
