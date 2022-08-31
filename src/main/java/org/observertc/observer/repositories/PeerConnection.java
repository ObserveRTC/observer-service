package org.observertc.observer.repositories;

import org.observertc.schemas.dtos.Models;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PeerConnection {

    private final ClientsRepository clientsRepository;
    private final AtomicReference<Models.PeerConnection> modelHolder;
    private final PeerConnectionsRepository peerConnectionsRepository;
    private final InboundAudioTracksRepository inboundAudioTracksRepository;
    private final InboundVideoTracksRepository inboundVideoTracksRepository;
    private final OutboundAudioTracksRepository outboundAudioTracksRepository;
    private final OutboundVideoTracksRepository outboundVideoTracksRepository;

    PeerConnection(ClientsRepository clientsRepository,
                   Models.PeerConnection model,
                   PeerConnectionsRepository peerConnectionsRepository,
                   InboundAudioTracksRepository inboundAudioTracksRepository,
                   InboundVideoTracksRepository inboundVideoTracksRepository,
                   OutboundAudioTracksRepository outboundAudioTracksRepository,
                   OutboundVideoTracksRepository outboundVideoTracksRepository
    ) {
        this.clientsRepository = clientsRepository;
        this.modelHolder = new AtomicReference<>(model);
        this.peerConnectionsRepository = peerConnectionsRepository;
        this.inboundAudioTracksRepository = inboundAudioTracksRepository;
        this.inboundVideoTracksRepository = inboundVideoTracksRepository;
        this.outboundAudioTracksRepository = outboundAudioTracksRepository;
        this.outboundVideoTracksRepository = outboundVideoTracksRepository;
    }

    public Client getClient() {
        var model = modelHolder.get();
        return this.clientsRepository.get(model.getClientId());
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

    public String getMediaUnitId() {
        var model = modelHolder.get();
        return model.getMediaUnitId();
    }

    public String getMarker() {
        var model = this.modelHolder.get();
        return model.getMarker();
    }

    public Collection<String> getInboundAudioTrackIds() {
        var model = this.modelHolder.get();
        return model.getInboundAudioTrackIdsList();
    }

    public Map<String, InboundAudioTrack> getInboundAudioTracks() {
        var trackIds = this.getInboundAudioTrackIds();
        return this.inboundAudioTracksRepository.getAll(trackIds);
    }

    public InboundAudioTrack addInboundAudioTrack(String trackId, Long timestamp, String sfuStreamId, String sfuSinkId, Long ssrc) {
        var model = modelHolder.get();
        var trackIds = model.getInboundAudioTrackIdsList();
        if (trackIds.contains(trackId)) {
            throw AlreadyCreatedException.wrapInboundAudioTrack(trackId);
        }
        var inboundAudioTrackModel = Models.InboundAudioTrack.newBuilder()
                .setServiceId(model.getServiceId())
                .setRoomId(model.getRoomId())
                .setCallId(model.getCallId())
                .setUserId(model.getUserId())
                .setClientId(model.getClientId())
                .setPeerConnectionId(model.getPeerConnectionId())
                .setTrackId(trackId)
                .setAdded(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                .setMarker(model.getMarker())
                .setSfuStreamId(sfuStreamId)
                .setSfuSinkId(sfuSinkId)
                .addSsrc(ssrc)
                .build();

        var newModel = Models.PeerConnection.newBuilder(model)
                .addInboundAudioTrackIds(trackId)
                .build();

        this.updateModel(newModel);
        this.inboundAudioTracksRepository.update(inboundAudioTrackModel);
        return this.inboundAudioTracksRepository.wrapInboundAudioTrack(inboundAudioTrackModel);
    }

    public boolean removeInboundAudioTrack(String trackId) {
        var model = modelHolder.get();
        var trackIds = model.getInboundAudioTrackIdsList();
        if (!trackIds.contains(trackId)) {
            return false;
        }
        var newInboundAudioTrackIds = trackIds.stream().filter(actualTrackId -> actualTrackId != trackId)
                .collect(Collectors.toSet());

        var newModel = Models.PeerConnection.newBuilder(model)
                .clearInboundAudioTrackIds()
                .addAllInboundAudioTrackIds(newInboundAudioTrackIds)
                .build();

        this.updateModel(newModel);
        this.inboundAudioTracksRepository.delete(trackId);
        return true;
    }

    public Collection<String> getInboundVideoTrackIds() {
        var model = this.modelHolder.get();
        return model.getInboundVideoTrackIdsList();
    }

    public Map<String, InboundVideoTrack> getInboundVideoTracks() {
        var trackIds = this.getInboundVideoTrackIds();
        return this.inboundVideoTracksRepository.getAll(trackIds);
    }

    public InboundVideoTrack addInboundVideoTrack(String trackId, Long timestamp, String sfuStreamId, String sfuSinkId, Long ssrc) {
        var model = modelHolder.get();
        var trackIds = model.getInboundVideoTrackIdsList();
        if (trackIds.contains(trackId)) {
            throw AlreadyCreatedException.wrapInboundVideoTrack(trackId);
        }
        var InboundVideoTrackModel = Models.InboundVideoTrack.newBuilder()
                .setServiceId(model.getServiceId())
                .setRoomId(model.getRoomId())
                .setCallId(model.getCallId())
                .setUserId(model.getUserId())
                .setClientId(model.getClientId())
                .setPeerConnectionId(model.getPeerConnectionId())
                .setTrackId(trackId)
                .setAdded(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                .setMarker(model.getMarker())
                .setSfuStreamId(sfuStreamId)
                .setSfuSinkId(sfuSinkId)
                .addSsrc(ssrc)
                .build();

        var newModel = Models.PeerConnection.newBuilder(model)
                .addInboundVideoTrackIds(trackId)
                .build();
        if (sfuStreamId != null && sfuSinkId != null) {
            // TODO: here we can add an SfuMediaSink Object if it does not exists
        }
        this.updateModel(newModel);
        this.inboundVideoTracksRepository.update(InboundVideoTrackModel);
        return this.inboundVideoTracksRepository.wrapInboundVideoTrack(InboundVideoTrackModel);
    }

    public boolean removeInboundVideoTrack(String trackId) {
        var model = modelHolder.get();
        var trackIds = model.getInboundVideoTrackIdsList();
        if (!trackIds.contains(trackId)) {
            return false;
        }
        var newInboundVideoTrackIds = trackIds.stream().filter(actualTrackId -> actualTrackId != trackId)
                .collect(Collectors.toSet());

        var newModel = Models.PeerConnection.newBuilder(model)
                .clearInboundVideoTrackIds()
                .addAllInboundVideoTrackIds(newInboundVideoTrackIds)
                .build();

        this.updateModel(newModel);
        this.inboundVideoTracksRepository.delete(trackId);
        return true;
    }

    public Collection<String> getOutboundAudioTrackIds() {
        var model = this.modelHolder.get();
        return model.getOutboundAudioTrackIdsList();
    }

    public Map<String, OutboundAudioTrack> getOutboundAudioTracks() {
        var trackIds = this.getOutboundAudioTrackIds();
        return this.outboundAudioTracksRepository.getAll(trackIds);
    }

    public OutboundAudioTrack addOutboundAudioTrack(String trackId, Long timestamp, String sfuStreamId, Long ssrc) {
        var model = modelHolder.get();
        var trackIds = model.getOutboundAudioTrackIdsList();
        if (trackIds.contains(trackId)) {
            throw AlreadyCreatedException.wrapOutboundAudioTrack(trackId);
        }
        var outboundAudioTrackModel = Models.OutboundAudioTrack.newBuilder()
                .setServiceId(model.getServiceId())
                .setRoomId(model.getRoomId())
                .setCallId(model.getCallId())
                .setUserId(model.getUserId())
                .setClientId(model.getClientId())
                .setPeerConnectionId(model.getPeerConnectionId())
                .setTrackId(trackId)
                .setAdded(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                .setMarker(model.getMarker())
                .setSfuStreamId(sfuStreamId)
                .addSsrc(ssrc)
                .build();

        var newModel = Models.PeerConnection.newBuilder(model)
                .addOutboundAudioTrackIds(trackId)
                .build();

        this.updateModel(newModel);
        this.outboundAudioTracksRepository.update(outboundAudioTrackModel);
        return this.outboundAudioTracksRepository.wrapOutboundAudioTrack(outboundAudioTrackModel);
    }

    public boolean removeOutboundAudioTrack(String trackId) {
        var model = modelHolder.get();
        var trackIds = model.getOutboundAudioTrackIdsList();
        if (!trackIds.contains(trackId)) {
            return false;
        }
        var newOutboundAudioTrackIds = trackIds.stream().filter(actualTrackId -> actualTrackId != trackId)
                .collect(Collectors.toSet());

        var newModel = Models.PeerConnection.newBuilder(model)
                .clearOutboundAudioTrackIds()
                .addAllOutboundAudioTrackIds(newOutboundAudioTrackIds)
                .build();

        this.updateModel(newModel);
        this.outboundAudioTracksRepository.delete(trackId);
        return true;
    }

    public Collection<String> getOutboundVideoTrackIds() {
        var model = this.modelHolder.get();
        return model.getOutboundVideoTrackIdsList();
    }

    public Map<String, OutboundVideoTrack> getOutboundVideoTracks() {
        var trackIds = this.getOutboundVideoTrackIds();
        return this.outboundVideoTracksRepository.getAll(trackIds);
    }

    public OutboundVideoTrack addOutboundVideoTrack(String trackId, Long timestamp, String sfuStreamId, Long ssrc) {
        var model = modelHolder.get();
        var trackIds = model.getOutboundVideoTrackIdsList();
        if (trackIds.contains(trackId)) {
            throw AlreadyCreatedException.wrapOutboundVideoTrack(trackId);
        }
        var outboundVideoTrackModel = Models.OutboundVideoTrack.newBuilder()
                .setServiceId(model.getServiceId())
                .setRoomId(model.getRoomId())
                .setCallId(model.getCallId())
                .setUserId(model.getUserId())
                .setClientId(model.getClientId())
                .setPeerConnectionId(model.getPeerConnectionId())
                .setTrackId(trackId)
                .setAdded(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                .setMarker(model.getMarker())
                .setSfuStreamId(sfuStreamId)
                .addSsrc(ssrc)
                .build();

        var newModel = Models.PeerConnection.newBuilder(model)
                .addOutboundVideoTrackIds(trackId)
                .build();

        this.updateModel(newModel);
        this.outboundVideoTracksRepository.update(outboundVideoTrackModel);
        if (sfuStreamId != null) {
            // TODO: here we can add an SfuMediaStreamObject if it does not exists
        }
        return this.outboundVideoTracksRepository.wrapOutboundVideoTrack(outboundVideoTrackModel);
    }

    public boolean removeOutboundVideoTrack(String trackId) {
        var model = modelHolder.get();
        var trackIds = model.getOutboundVideoTrackIdsList();
        if (!trackIds.contains(trackId)) {
            return false;
        }
        var newOutboundVideoTrackIds = trackIds.stream().filter(actualTrackId -> actualTrackId != trackId)
                .collect(Collectors.toSet());

        var newModel = Models.PeerConnection.newBuilder(model)
                .clearOutboundVideoTrackIds()
                .addAllOutboundVideoTrackIds(newOutboundVideoTrackIds)
                .build();

        this.updateModel(newModel);
        this.outboundVideoTracksRepository.delete(trackId);
        return true;
    }

    private void updateModel(Models.PeerConnection newModel) {
        this.modelHolder.set(newModel);
        this.peerConnectionsRepository.update(newModel);
    }
}
