package org.observertc.observer.repositories;

import org.observertc.observer.configs.MediaKind;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PeerConnection {

    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);

    private final ServiceRoomId serviceRoomId;
    private final ClientsRepository clientsRepository;
    private final AtomicReference<Models.PeerConnection> modelHolder;
    private final PeerConnectionsRepository peerConnectionsRepository;
    private final InboundTracksRepository inboundTracksRepository;
    private final OutboundTracksRepository outboundTracksRepository;


    PeerConnection(ClientsRepository clientsRepository,
                   Models.PeerConnection model,
                   PeerConnectionsRepository peerConnectionsRepository,
                   InboundTracksRepository inboundTracksRepository,
                   OutboundTracksRepository outboundTracksRepository
    ) {
        this.clientsRepository = clientsRepository;
        this.modelHolder = new AtomicReference<>(model);
        this.peerConnectionsRepository = peerConnectionsRepository;
        this.inboundTracksRepository = inboundTracksRepository;
        this.outboundTracksRepository = outboundTracksRepository;
        this.serviceRoomId = ServiceRoomId.make(model.getServiceId(), model.getRoomId());
    }

    public ServiceRoomId getServiceRoomId() {
        return serviceRoomId;
    }

    public Client getClient() {
        var model = modelHolder.get();
        return this.clientsRepository.get(model.getClientId());
    }

    public String getServiceId(){
        var model = modelHolder.get();
        return model.getServiceId();
    }

    public String getLabel(){
        var model = modelHolder.get();
        if (!model.hasLabel()) {
            return null;
        }
        return model.getLabel();
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

    public Long getOpened() {
        var model = modelHolder.get();
        return model.getOpened();
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
        var newModel = Models.PeerConnection.newBuilder(model)
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
        var newModel = Models.PeerConnection.newBuilder(model)
                .setServerTouched(timestamp)
                .build();
        this.updateModel(newModel);
    }

    public void touch(Long sampleTimestamp, Long serverTimestamp) {
        var model = modelHolder.get();
        Models.PeerConnection.Builder newModel = null;
        if (sampleTimestamp != null) {
            newModel = Models.PeerConnection.newBuilder(model)
                    .setSampleTouched(sampleTimestamp);
        }
        if (serverTimestamp != null) {
            if (newModel == null) newModel = Models.PeerConnection.newBuilder(model);
            newModel.setServerTouched(serverTimestamp);
        }
        if (newModel == null) {
            return;
        }
        this.updateModel(newModel.build());
    }

    public String getMediaUnitId() {
        var model = modelHolder.get();
        return model.getMediaUnitId();
    }

    public String getMarker() {
        var model = this.modelHolder.get();
        return model.getMarker();
    }

    public Collection<String> getInboundTrackIds() {
        var model = this.modelHolder.get();
        if (model.getInboundTrackIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getInboundTrackIdsList();
    }

    public Map<String, InboundTrack> getInboundTracks() {
        var trackIds = this.getInboundTrackIds();
        return this.inboundTracksRepository.getAll(trackIds);
    }

    public InboundTrack getInboundTrack(String trackId) {
        return this.inboundTracksRepository.get(trackId);
    }

    public boolean hasInboundTrack(String trackId) {
        var inboundTrackIds = this.getInboundTrackIds();
        return inboundTrackIds.contains(trackId);
    }

    public InboundTrack addInboundTrack(String trackId, Long timestamp, String sfuStreamId, String sfuSinkId, MediaKind kind, Long ssrc, String marker) {
        var model = modelHolder.get();
        var trackIds = this.getInboundTrackIds();
        if (trackIds.contains(trackId)) {
            throw AlreadyCreatedException.wrapInboundAudioTrack(trackId);
        }
        var inboundTrackModelBuilder = Models.InboundTrack.newBuilder()
                .setServiceId(model.getServiceId())
                .setRoomId(model.getRoomId())
                .setCallId(model.getCallId())
                .setClientId(model.getClientId())
                .setPeerConnectionId(model.getPeerConnectionId())
                .setTrackId(trackId)
                .setKind(kind.name())
                .setAdded(timestamp)
                .setSampleTouched(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                // marker
                // userId
                // sfu stream id
                // sfu sink id
                .addSsrc(ssrc)
                ;
        if (model.hasUserId()) {
            inboundTrackModelBuilder.setUserId(model.getUserId());
        }
        if (marker != null) {
            inboundTrackModelBuilder.setMarker(marker);
        }
        if (sfuStreamId != null) {
            inboundTrackModelBuilder.setSfuStreamId(sfuStreamId);
        }
        if (sfuSinkId != null) {
            inboundTrackModelBuilder.setSfuSinkId(sfuSinkId);
        }
        var inboundTrackModel = inboundTrackModelBuilder.build();

        var newModel = Models.PeerConnection.newBuilder(model)
                .addInboundTrackIds(trackId)
                .build();

        this.updateModel(newModel);
        this.inboundTracksRepository.update(inboundTrackModel);
        var result = this.inboundTracksRepository.wrapInboundTrack(inboundTrackModel);

        if (sfuStreamId != null && sfuSinkId != null) {
            // create mediaSink
            if (!result.createMediaSink()) {
                logger.warn("Media Sink {} has not been created for track {} in room {}, mediaUnit: {} service: {}",
                        sfuSinkId,
                        trackId,
                        serviceRoomId.roomId,
                        serviceRoomId.serviceId,
                        getMediaUnitId()
                );
            }
        }
        return result;
    }

    public boolean removeInboundTrack(String trackId) {
        var model = modelHolder.get();
        var trackIds = this.getInboundTrackIds();
        if (!trackIds.contains(trackId)) {
            return false;
        }
        var newInboundTrackIds = trackIds.stream().filter(actualTrackId -> actualTrackId != trackId)
                .collect(Collectors.toSet());

        var newModel = Models.PeerConnection.newBuilder(model)
                .clearInboundTrackIds()
                .addAllInboundTrackIds(newInboundTrackIds)
                .build();

        this.updateModel(newModel);
        this.inboundTracksRepository.delete(trackId);
        return true;
    }

    public Collection<String> getOutboundTrackIds() {
        var model = this.modelHolder.get();
        if (model.getOutboundTrackIdsCount() < 1) {
            return Collections.emptyList();
        }
        return model.getOutboundTrackIdsList();
    }

    public Map<String, OutboundTrack> getOutboundTracks() {
        var trackIds = this.getOutboundTrackIds();
        return this.outboundTracksRepository.getAll(trackIds);
    }

    public OutboundTrack getOutboundTrack(String trackId) {
        return this.outboundTracksRepository.get(trackId);
    }

    public boolean hasOutboundTrack(String trackId) {
        var outboundTrackIds = this.getOutboundTrackIds();
        return outboundTrackIds.contains(trackId);
    }

    public OutboundTrack addOutboundTrack(String trackId, Long timestamp, String sfuStreamId, MediaKind kind, Long ssrc, String marker) {
        var model = modelHolder.get();
        var trackIds = this.getOutboundTrackIds();
        if (trackIds.contains(trackId)) {
            throw AlreadyCreatedException.wrapOutboundAudioTrack(trackId);
        }
        var outboundTrackModelBuilder = Models.OutboundTrack.newBuilder()
                .setServiceId(model.getServiceId())
                .setRoomId(model.getRoomId())
                .setCallId(model.getCallId())
                .setClientId(model.getClientId())
                .setPeerConnectionId(model.getPeerConnectionId())
                .setTrackId(trackId)
                .setAdded(timestamp)
                .setKind(kind.name())

                .setSampleTouched(timestamp)
                .setMediaUnitId(model.getMediaUnitId())
                // userId
                // marker
                // sfuStreamId
                .addSsrc(ssrc)
                ;
        if (model.hasUserId()) {
            outboundTrackModelBuilder.setUserId(model.getUserId());
        }
        if (marker != null) {
            outboundTrackModelBuilder.setMarker(marker);
        }
        if (sfuStreamId != null) {
            outboundTrackModelBuilder.setSfuStreamId(sfuStreamId);
        }

        var outboundTrackModel = outboundTrackModelBuilder.build();
        var newModel = Models.PeerConnection.newBuilder(model)
                .addOutboundTrackIds(trackId)
                .build();

        this.updateModel(newModel);
        this.outboundTracksRepository.update(outboundTrackModel);

        var result = this.outboundTracksRepository.wrapOutboundAudioTrack(outboundTrackModel);
        if (sfuStreamId != null) {
            // create mediaSink
            if (!result.createMediaStream()) {
                logger.warn("Media Sink {} has not been created for track {} in room {}, mediaUnit: {} service: {}",
                        trackId,
                        serviceRoomId.roomId,
                        serviceRoomId.serviceId,
                        getMediaUnitId()
                );
            }
        }
        return result;
    }

    public boolean removeOutboundTrack(String trackId) {
        var model = modelHolder.get();
        var trackIds = this.getOutboundTrackIds();
        if (!trackIds.contains(trackId)) {
            return false;
        }
        var newOutboundTrackIds = trackIds.stream().filter(actualTrackId -> actualTrackId != trackId)
                .collect(Collectors.toSet());

        var newModel = Models.PeerConnection.newBuilder(model)
                .clearOutboundTrackIds()
                .addAllOutboundTrackIds(newOutboundTrackIds)
                .build();

        this.updateModel(newModel);
        this.outboundTracksRepository.delete(trackId);
        return true;
    }

    public Models.PeerConnection getModel() {
        return this.modelHolder.get();
    }

    private void updateModel(Models.PeerConnection newModel) {
        this.modelHolder.set(newModel);
        this.peerConnectionsRepository.update(newModel);
    }

    @Override
    public String toString() {
        var model = this.modelHolder.get();
        return model.toString();
    }
}
