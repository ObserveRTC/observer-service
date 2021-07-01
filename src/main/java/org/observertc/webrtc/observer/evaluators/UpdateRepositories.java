package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.dto.StreamDirection;
import org.observertc.webrtc.observer.repositories.tasks.AddClientsTask;
import org.observertc.webrtc.observer.repositories.tasks.AddMediaTracksTasks;
import org.observertc.webrtc.observer.repositories.tasks.AddPeerConnectionsTask;
import org.observertc.webrtc.observer.repositories.tasks.RefreshTask;
import org.observertc.webrtc.observer.samples.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class UpdateRepositories implements Consumer<CollectedCallSamples> {
    private static final Logger logger = LoggerFactory.getLogger(UpdateRepositories.class);

    @Inject
    Provider<RefreshTask> refreshTaskProvider;

    @Inject
    Provider<AddClientsTask> addClientsTaskProvider;

    @Inject
    Provider<AddMediaTracksTasks> addMediaTrackTaskProvider;

    @Inject
    Provider<AddPeerConnectionsTask> peerConnectionsTaskProvider;

    @Override
    public void accept(CollectedCallSamples collectedCallSamples) throws Throwable {
        Set<UUID> clientIds = collectedCallSamples.getClientIds();
        Set<UUID> peerConnectionIds = collectedCallSamples.getPeerConnectionIds();
        Set<UUID> mediaTrackIds = collectedCallSamples.getMediaTrackIds();
        RefreshTask refreshTask = refreshTaskProvider.get()
                .withClientIds(clientIds)
                .withPeerConnectionIds(peerConnectionIds)
                .withMediaTrackIds(mediaTrackIds);

        if (!refreshTask.execute().succeeded()) {
            logger.warn("Unsuccessful execution of {}. Entities are not refreshed, new entities are not identified!", RefreshTask.class.getSimpleName());
            return;
        }
        Map<UUID, ClientDTO> newClients = new HashMap<>();
        Map<UUID, PeerConnectionDTO> newPeerConnections = new HashMap<>();
        Map<UUID, MediaTrackDTO> newMediaTracks = new HashMap<>();
        RefreshTask.Report report = refreshTask.getResult();
        for (CallSamples callSamples : collectedCallSamples) {
            var callId = callSamples.getCallId();
            for (ClientSamples clientSamples : callSamples) {
                var clientId = clientSamples.getClientId();
                if (!report.foundClientIds.contains(clientId)) {
                    var clientDTO = ClientDTO.builder()
                            .withCallId(callId)
                            .withUserId(clientSamples.getUserId())
                            .withClientId(clientId)
                            .withConnectedTimestamp(clientSamples.getMinTimestamp())
                            .withTimeZoneId(clientSamples.getTimeZoneId())
                            .withMediaUnitId(clientSamples.getMediaUnitId())
                            .build();

                    newClients.put(clientId, clientDTO);
                }
                for (ClientSample clientSample : clientSamples) {
                    ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
                            .forEach(peerConnectionTransport -> {
                                var peerConnectionId = UUID.fromString(peerConnectionTransport.peerConnectionId);
                                if (report.foundPeerConnectionIdsToClientIds.containsKey(peerConnectionId)) {
                                    return;
                                }
                                var newPeerConnection = PeerConnectionDTO.builder()
                                        .withPeerConnectionId(peerConnectionId)
                                        .withCreatedTimestamp(clientSamples.getMinTimestamp())
                                        .withClientId(clientSamples.getClientId())
                                        .build();
                                newPeerConnections.put(peerConnectionId, newPeerConnection);
                            });

                    ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                            .forEach(track -> {
                                UUID trackId = UUID.fromString(track.trackId);
                                if (report.foundMediaTrackIdsToPeerConnectionIds.containsKey(trackId)) {
                                    return;
                                }
                                UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                                Long SSRC = track.ssrc;
                                var mediaTrackDTO = MediaTrackDTO.builder()
                                        .withTrackId(trackId)
                                        .withDirection(StreamDirection.INBOUND)
                                        .withPeerConnectionId(peerConnectionId)
                                        .withSSRC(SSRC)
                                        .withAddedTimestamp(clientSamples.getMinTimestamp())
                                        .build();

                                newMediaTracks.put(mediaTrackDTO.trackId, mediaTrackDTO);
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

    private boolean addNewMediaTracks(Map<UUID, MediaTrackDTO> newMediaTracks) {
        var task = addMediaTrackTaskProvider.get()
                .withMediaTrackDTOs(newMediaTracks)
                ;

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return false;
        }

        return task.getResult();
    }

    private boolean addNewPeerConnections(Map<UUID, PeerConnectionDTO> newPeerConnections) {
        var task = peerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(newPeerConnections)
                ;

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return false;
        }

        return task.getResult();
    }

    private boolean addNewClients(Map<UUID, ClientDTO> newClients) {
        var task = addClientsTaskProvider.get()
                .withClientDTOs(newClients)
        ;

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return false;
        }

        return task.getResult();
    }
}
