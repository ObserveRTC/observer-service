package org.observertc.observer.repositories;

import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Client {

    private final ServiceRoomId serviceRoomId;
    private final CallsRepository callsRepo;
    private final AtomicReference<Models.Client> modelHolder;
    private final ClientsRepository clientsRepo;
    private PeerConnectionsRepository peerConnectionsRepositoryRepo;

    Client(CallsRepository callsRepo, Models.Client model, ClientsRepository clientsRepo, PeerConnectionsRepository peerConnectionsRepositoryRepo) {
        this.callsRepo = callsRepo;
        this.modelHolder = new AtomicReference<>(model);
        this.clientsRepo = clientsRepo;
        this.peerConnectionsRepositoryRepo = peerConnectionsRepositoryRepo;
        this.serviceRoomId = ServiceRoomId.make(model.getServiceId(), model.getRoomId());
    }

    public Call getCall() {
        return this.callsRepo.get(this.serviceRoomId);
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

    public String getUserId() {
        var model = modelHolder.get();
        return model.getUserId();
    }

    public String getMediaUnitId() {
        var model = modelHolder.get();
        return model.getMediaUnitId();
    }

    public String getMarker() {
        var model = this.modelHolder.get();
        return model.getMarker();
    }

    public boolean hasPeerConnection(String peerConnectionId)  {
        var model = modelHolder.get();
        var peerConnectionIds = model.getPeerConnectionIdsList();
        return peerConnectionIds.contains(peerConnectionId);
    }

    public Collection<String> getPeerConnectionIds() {
        var model = this.modelHolder.get();
        if (model.getPeerConnectionIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getPeerConnectionIdsList();
    }

    public Map<String, PeerConnection> getPeerConnections() {
        var peerConnectionIds = this.getPeerConnectionIds();
        return this.peerConnectionsRepositoryRepo.getAll(peerConnectionIds);
    }

    public PeerConnection addPeerConnection(String peerConnectionId, Long timestamp) throws AlreadyCreatedException {
        var model = modelHolder.get();
        if (0 < model.getPeerConnectionIdsCount()) {
            var peerConnectionIds = model.getPeerConnectionIdsList();
            if (peerConnectionIds.contains(peerConnectionId)) {
                throw AlreadyCreatedException.wrapPeerConnectionId(peerConnectionId);
            }
        }

        var peerConnectionModel = Models.PeerConnection.newBuilder()
                .setServiceId(model.getServiceId())
                .setRoomId(model.getRoomId())
                .setCallId(model.getCallId())
                .setClientId(model.getClientId())
                .setPeerConnectionId(peerConnectionId)
                .setMediaUnitId(model.getMediaUnitId())
                .setOpened(timestamp)
                .setMarker(model.getMarker())
                .build();

        var newModel = Models.Client.newBuilder(model)
                .addPeerConnectionIds(peerConnectionId)
                .build();

        this.updateModel(newModel);
        this.peerConnectionsRepositoryRepo.update(peerConnectionModel);
        return this.peerConnectionsRepositoryRepo.wrapPeerConnection(peerConnectionModel);
    }

    public boolean removePeerConnection(String peerConnectionId) {
        var model = modelHolder.get();
        if (model.getPeerConnectionIdsCount() < 1) {
            return false;
        }
        var rtpPadIds = model.getPeerConnectionIdsList();
        if (!rtpPadIds.contains(peerConnectionId)) {
            return false;
        }

        var peerConnectionIds = model.getPeerConnectionIdsList();
        var newPeerConnectionIds = peerConnectionIds.stream().filter(actualPcId -> actualPcId != peerConnectionId)
                .collect(Collectors.toSet());

        var newModel = Models.Client.newBuilder(model)
                .clearPeerConnectionIds()
                .addAllPeerConnectionIds(newPeerConnectionIds)
                .build();

        this.updateModel(newModel);
        this.peerConnectionsRepositoryRepo.delete(peerConnectionId);
        return true;
    }

    private void updateModel(Models.Client newModel) {
        this.modelHolder.set(newModel);
        this.clientsRepo.update(newModel);
    }

}
