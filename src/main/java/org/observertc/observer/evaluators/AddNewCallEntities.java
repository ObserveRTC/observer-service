package org.observertc.observer.evaluators;

import io.micrometer.core.annotation.Timed;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.tasks.AddClientsTask;
import org.observertc.observer.repositories.tasks.AddMediaTracksTasks;
import org.observertc.observer.repositories.tasks.AddPeerConnectionsTask;
import org.observertc.observer.repositories.tasks.RefreshCallsTask;
import org.observertc.observer.samples.*;
import org.observertc.webrtc.observer.samples.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class AddNewCallEntities implements Consumer<CollectedCallSamples> {
    private static final Logger logger = LoggerFactory.getLogger(AddNewCallEntities.class);

    @Inject
    Provider<RefreshCallsTask> refreshTaskProvider;

    @Inject
    Provider<AddClientsTask> addClientsTaskProvider;

    @Inject
    Provider<AddPeerConnectionsTask> peerConnectionsTaskProvider;

    @Inject
    Provider<AddMediaTracksTasks> addMediaTrackTaskProvider;


    @Override
    @Timed(value = ExposedMetrics.OBSERVERTC_EVALUATORS_ADD_NEW_CALL_ENTITIES_EXECUTION_TIME)
    public void accept(CollectedCallSamples collectedCallSamples) throws Throwable {
        Set<UUID> clientIds = collectedCallSamples.getClientIds();
        Set<UUID> peerConnectionIds = collectedCallSamples.getPeerConnectionIds();
        Set<UUID> mediaTrackIds = collectedCallSamples.getMediaTrackIds();
        RefreshCallsTask refreshCallsTask = refreshTaskProvider.get()
                .withClientIds(clientIds)
                .withPeerConnectionIds(peerConnectionIds)
                .withMediaTrackIds(mediaTrackIds);
        if (!refreshCallsTask.execute().succeeded()) {
            logger.warn("Unsuccessful execution of {}. Entities are not refreshed, new entities are not identified!", RefreshCallsTask.class.getSimpleName());
            return;
        }

        Map<UUID, ObservedNewEntity<ClientDTO>> newClients = new HashMap<>();
        Map<UUID, ObservedNewEntity<PeerConnectionDTO>> newPeerConnections = new HashMap<>();
        Map<UUID, ObservedNewEntity<MediaTrackDTO>> newMediaTracks = new HashMap<>();
        RefreshCallsTask.Report report = refreshCallsTask.getResult();
        for (CallSamples callSamples : collectedCallSamples) {
            var callId = callSamples.getCallId();
            for (ClientSamples clientSamples : callSamples) {
                var clientId = clientSamples.getClientId();
                var observedSample = clientSamples;
                if (!report.foundClientIds.contains(clientId) && !newClients.containsKey(clientId)) {
                    var clientDTO = ClientDTO.builder()
                            .withServiceId(clientSamples.getServiceId())
                            .withRoomId(clientSamples.getRoomId())
                            .withCallId(callId)
                            .withUserId(clientSamples.getUserId())
                            .withClientId(clientId)
                            .withConnectedTimestamp(clientSamples.getMinTimestamp())
                            .withTimeZoneId(clientSamples.getTimeZoneId())
                            .withMediaUnitId(clientSamples.getMediaUnitId())
                            .build();
                    var observedNewEntity = new ObservedNewEntity<ClientDTO>(clientDTO, observedSample, callSamples.getCallId());
                    newClients.put(clientId, observedNewEntity);
                }
                for (ClientSample clientSample : clientSamples) {
                    ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
                            .forEach(peerConnectionTransport -> {
                                var peerConnectionId = UUID.fromString(peerConnectionTransport.peerConnectionId);
                                if (report.foundPeerConnectionIdsToClientIds.containsKey(peerConnectionId) || newPeerConnections.containsKey(peerConnectionId)) {
                                    return;
                                }
                                var newPeerConnection = PeerConnectionDTO.builder()
                                        .withCallId(callId)
                                        .withServiceId(clientSamples.getServiceId())
                                        .withRoomId(clientSamples.getRoomId())

                                        .withUserId(clientSamples.getUserId())
                                        .withMediaUnitId(clientSamples.getMediaUnitId())

                                        .withPeerConnectionId(peerConnectionId)
                                        .withCreatedTimestamp(clientSamples.getMinTimestamp())
                                        .withClientId(clientId)
                                        .build();
                                var observedNewEntity = new ObservedNewEntity<PeerConnectionDTO>(newPeerConnection, observedSample, callSamples.getCallId());
                                newPeerConnections.put(peerConnectionId, observedNewEntity);
                            });

                    ClientSampleVisitor.streamInboundAudioTracks(clientSample)
                            .forEach(track -> {
                                UUID trackId = UUID.fromString(track.trackId);
                                if (report.foundMediaTrackIdsToPeerConnectionIds.containsKey(trackId) || newMediaTracks.containsKey(trackId)) {
                                    return;
                                }
                                UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                                UUID rtpStreamId = UUIDAdapter.tryParseOrNull(track.rtpStreamId);
                                Long SSRC = track.ssrc;
                                var mediaTrackDTO = MediaTrackDTO.builder()
                                        .withCallId(callId)
                                        .withServiceId(clientSamples.getServiceId())
                                        .withRoomId(clientSamples.getRoomId())

                                        .withClientId(clientId)
                                        .withUserId(clientSamples.getUserId())
                                        .withMediaUnitId(clientSamples.getMediaUnitId())

                                        .withTrackId(trackId)
                                        .withRtpStreamId(rtpStreamId)
                                        .withDirection(StreamDirection.INBOUND)
                                        .withPeerConnectionId(peerConnectionId)
                                        .withSSRC(SSRC)
                                        .withAddedTimestamp(clientSamples.getMinTimestamp())
                                        .build();
                                ObservedNewEntity<MediaTrackDTO> observedNewEntity = new ObservedNewEntity<MediaTrackDTO>(mediaTrackDTO, observedSample, callSamples.getCallId());
                                newMediaTracks.put(trackId, observedNewEntity);
                            });

                    ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                            .forEach(track -> {
                                UUID trackId = UUID.fromString(track.trackId);
                                if (report.foundMediaTrackIdsToPeerConnectionIds.containsKey(trackId) || newMediaTracks.containsKey(trackId)) {
                                    return;
                                }
                                UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                                UUID rtpStreamId = UUIDAdapter.tryParseOrNull(track.rtpStreamId);
                                Long SSRC = track.ssrc;
                                var mediaTrackDTO = MediaTrackDTO.builder()
                                        .withCallId(callId)
                                        .withServiceId(clientSamples.getServiceId())
                                        .withRoomId(clientSamples.getRoomId())

                                        .withClientId(clientId)
                                        .withUserId(clientSamples.getUserId())
                                        .withMediaUnitId(clientSamples.getMediaUnitId())

                                        .withTrackId(trackId)
                                        .withRtpStreamId(rtpStreamId)
                                        .withDirection(StreamDirection.INBOUND)
                                        .withPeerConnectionId(peerConnectionId)
                                        .withSSRC(SSRC)
                                        .withAddedTimestamp(clientSamples.getMinTimestamp())
                                        .build();
                                ObservedNewEntity<MediaTrackDTO> observedNewEntity = new ObservedNewEntity<MediaTrackDTO>(mediaTrackDTO, observedSample, callSamples.getCallId());
                                newMediaTracks.put(trackId, observedNewEntity);
                            });
                    ClientSampleVisitor.streamOutboundAudioTracks(clientSample)
                            .forEach(track -> {
                                UUID trackId = UUID.fromString(track.trackId);
                                if (report.foundMediaTrackIdsToPeerConnectionIds.containsKey(trackId) || newMediaTracks.containsKey(trackId)) {
                                    return;
                                }
                                UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                                UUID rtpStreamId = UUIDAdapter.tryParseOrNull(track.rtpStreamId);
                                Long SSRC = track.ssrc;
                                var mediaTrackDTO = MediaTrackDTO.builder()
                                        .withCallId(callId)
                                        .withServiceId(clientSamples.getServiceId())
                                        .withRoomId(clientSamples.getRoomId())

                                        .withClientId(clientId)
                                        .withUserId(clientSamples.getUserId())
                                        .withMediaUnitId(clientSamples.getMediaUnitId())

                                        .withTrackId(trackId)
                                        .withRtpStreamId(rtpStreamId)
                                        .withDirection(StreamDirection.OUTBOUND)
                                        .withPeerConnectionId(peerConnectionId)
                                        .withSSRC(SSRC)
                                        .withAddedTimestamp(clientSamples.getMinTimestamp())
                                        .build();
                                ObservedNewEntity<MediaTrackDTO> observedNewEntity = new ObservedNewEntity<MediaTrackDTO>(mediaTrackDTO, observedSample, callSamples.getCallId());
                                newMediaTracks.put(trackId, observedNewEntity);
                            });

                    ClientSampleVisitor.streamOutboundVideoTracks(clientSample)
                            .forEach(track -> {
                                UUID trackId = UUID.fromString(track.trackId);
                                if (report.foundMediaTrackIdsToPeerConnectionIds.containsKey(trackId) || newMediaTracks.containsKey(trackId)) {
                                    return;
                                }
                                UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                                UUID rtpStreamId = UUIDAdapter.tryParseOrNull(track.rtpStreamId);
                                Long SSRC = track.ssrc;
                                var mediaTrackDTO = MediaTrackDTO.builder()
                                        .withCallId(callId)
                                        .withServiceId(clientSamples.getServiceId())
                                        .withRoomId(clientSamples.getRoomId())

                                        .withClientId(clientId)
                                        .withUserId(clientSamples.getUserId())
                                        .withMediaUnitId(clientSamples.getMediaUnitId())

                                        .withTrackId(trackId)
                                        .withRtpStreamId(rtpStreamId)
                                        .withDirection(StreamDirection.OUTBOUND)
                                        .withPeerConnectionId(peerConnectionId)
                                        .withSSRC(SSRC)
                                        .withAddedTimestamp(clientSamples.getMinTimestamp())
                                        .build();
                                ObservedNewEntity<MediaTrackDTO> observedNewEntity = new ObservedNewEntity<MediaTrackDTO>(mediaTrackDTO, observedSample, callSamples.getCallId());
                                newMediaTracks.put(trackId, observedNewEntity);
                            });
                }
            }
        }
        if (0 < newClients.size()) {
            this.addNewClients(newClients);
        }
        if (0 < newPeerConnections.size()) {
            this.addNewPeerConnections(newPeerConnections);
        }
        if (0 < newMediaTracks.size()) {
            this.addNewMediaTracks(newMediaTracks);
        }
    }

    private void addNewMediaTracks(Map<UUID, ObservedNewEntity<MediaTrackDTO>> newMediaTracks) {
        var DTOs = newMediaTracks.values().stream().map(e -> e.dto).collect(Collectors.toMap(
                e -> e.trackId,
                Function.identity()
        ));
        var task = addMediaTrackTaskProvider.get().withMediaTrackDTOs(DTOs);

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
        }
    }

    private void addNewPeerConnections(Map<UUID, ObservedNewEntity<PeerConnectionDTO>> newPeerConnections) {
        var DTOs = newPeerConnections.values().stream().map(e -> e.dto).collect(Collectors.toMap(
                e -> e.peerConnectionId,
                Function.identity()
        ));
        var task = peerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(DTOs)
                ;
        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
        }
    }

    private void addNewClients(Map<UUID, ObservedNewEntity<ClientDTO>> newClients) {
        var DTOs = newClients.values().stream().map(e -> e.dto).collect(Collectors.toMap(
                e -> e.clientId,
                Function.identity()
        ));
        var task = addClientsTaskProvider.get()
                .withClientDTOs(DTOs)
        ;

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
        }
    }

    private class ObservedNewEntity<T> {
        final T dto;
//        final ObservedSample observedSample;
        final UUID callId;

        private ObservedNewEntity(T dto, ObservedClientSample observedSample, UUID callId) {
            this.dto = dto;
//            this.observedSample = observedSample;
            this.callId = callId;
        }
    }
}
