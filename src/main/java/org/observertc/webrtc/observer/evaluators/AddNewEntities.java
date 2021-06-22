package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.repositories.tasks.RefreshTask;
import org.observertc.webrtc.observer.samples.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Prototype
public class AddNewEntities implements Consumer<CollectedCallSamples> {
    private static final Logger logger = LoggerFactory.getLogger(AddNewEntities.class);

    @Inject
    Provider<RefreshTask> refreshTaskProvider;


    @Override
    public void accept(CollectedCallSamples collectedCallSamples) throws Throwable {
        Set<UUID> clientIds = collectedCallSamples.getClientIds();
        Set<UUID> peerConnectionIds = collectedCallSamples.getPeerConnectionIds();
        Set<String> mediaTrackKeys = collectedCallSamples.getMediaTrackKeys();
        RefreshTask refreshTask = refreshTaskProvider.get()
                .withClientIds(clientIds)
                .withPeerConnectionIds(peerConnectionIds)
                .withMediaTrackKeys(mediaTrackKeys);

        if (!refreshTask.execute().succeeded()) {
            logger.warn("Unsuccessful execution of {}. Entities are not refreshed, new entities are not identified!", RefreshTask.class.getSimpleName());
            return;
        }
        Map<UUID, NewClient> newClients = new HashMap<>();
        Map<UUID, NewPeerConnection> newPeerConnections = new HashMap<>();
        Map<MediaTrackId, NewMediaTrack> newMediaTracks = new HashMap<>();
        RefreshTask.Report report = refreshTask.getResult();
        for (CallSamples callSamples : collectedCallSamples) {
            var callId = callSamples.getCallId();
            for (ClientSamples clientSamples : callSamples) {
                var clientId = clientSamples.getClientId();
                var observedSample = clientSamples;
                if (!report.foundClientIds.contains(clientId)) {
                    var newClient = new NewClient(clientId);
                    newClient.withClientSamples(clientSamples)
                            // inherited builder methods
                            .withCallId(callId)
                            ;

                    newClients.put(clientId, newClient);
                }
                for (ClientSample clientSample : clientSamples) {
                    ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
                            .forEach(peerConnectionTransport -> {
                                var peerConnectionId = UUID.fromString(peerConnectionTransport.peerConnectionId);
                                if (report.foundPeerConnectionIdsToClientIds.containsKey(peerConnectionId)) {
                                    return;
                                }
                                var newPeerConnection = new NewPeerConnection(peerConnectionId)
                                        .withCallId(callId)
                                        .withClientSamples(clientSamples)
                                        .withPcTransport(peerConnectionTransport);
                                newPeerConnections.put(peerConnectionId, newPeerConnection);
                            });

                    ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                            .forEach(track -> {
                                UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                                Long SSRC = track.ssrc;
                                var newMediaTrack = new NewMediaTrack(peerConnectionId, SSRC)
                                        .withCallId(callId)
                                        .withClientSamples(clientSamples)
                                        .with;
                                newMediaTracks.put(mediaTrackId, newMediaTrack);
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

    private boolean addNewMediaTracks(Map<MediaTrackId, NewMediaTrack> newMediaTracks) {

    }

    private boolean addNewPeerConnections(Map<UUID, NewPeerConnection> newPeerConnections) {

    }

    private boolean addNewClients(Map<UUID, NewClient> newClients) {

    }
}
