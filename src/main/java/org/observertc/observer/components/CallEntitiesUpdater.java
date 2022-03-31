package org.observertc.observer.components;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.components.depots.ClientDTOsDepot;
import org.observertc.observer.components.depots.MediaTrackDTOsDepot;
import org.observertc.observer.components.depots.PeerConnectionDTOsDepot;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.repositories.tasks.*;
import org.observertc.observer.samples.ClientSampleVisitor;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Prototype
public class CallEntitiesUpdater implements Consumer<ObservedClientSamples> {
    private static final Logger logger = LoggerFactory.getLogger(CallEntitiesUpdater.class);

    @Inject
    Provider<CreateCallIfNotExistsTask> createCallIfNotExistsTaskProvider;

    @Inject
    Provider<FindCallIdsByServiceRoomIds> findCallsTaskProvider;

    @Inject
    Provider<RefreshCallsTask> refreshCallsTaskProvider;

    @Inject
    Provider<AddClientsTask> addClientsTaskProvider;

    @Inject
    Provider<AddPeerConnectionsTask> peerConnectionsTaskProvider;

    @Inject
    Provider<AddMediaTracksTask> addMediaTrackTaskProvider;

    private Subject<ObservedClientSamples> output = PublishSubject.create();
    private final ClientDTOsDepot clientsDepot = new ClientDTOsDepot();
    private final PeerConnectionDTOsDepot peerConnectionsDepot = new PeerConnectionDTOsDepot();
    private final MediaTrackDTOsDepot mediaTracksDepot = new MediaTrackDTOsDepot();

    public Observable<ObservedClientSamples> observableClientSamples() {
        return this.output;
    }


    public void accept(ObservedClientSamples observedClientSamples) {
        if (observedClientSamples.isEmpty()) {
            return;
        }
        var findCallsTask = findCallsTaskProvider.get()
                .whereServiceRoomIds(observedClientSamples.getServiceRoomIds())
                .withUnmodifiableResult(false)
                ;
        var findDTOs = this.refreshCallsTaskProvider.get()
                .withClientIds(observedClientSamples.getClientIds())
                .withPeerConnectionIds(observedClientSamples.getPeerConnectionIds())
                .withMediaTrackIds(observedClientSamples.getMediaTrackIds())
//                .withUnmodifiableResult(false)
                ;
        if (!findCallsTask.execute().succeeded() || !findDTOs.execute().succeeded()) {
            logger.warn("Interrupted execution of component due to unsuccessful task execution");
            return;
        }
        var findDTOsTaskResult = findDTOs.getResult();
        var roomsToCallIds = findCallsTask.getResult();
        var foundClientIds = findDTOsTaskResult.foundClientIds;
        var foundPeerConnectionIds = findDTOsTaskResult.foundClientIds;
        var foundMediaTrackIds = findDTOsTaskResult.foundTrackIds;
        for (var observedClientSample : observedClientSamples) {
            var serviceRoomId = observedClientSample.getServiceRoomId();
            var clientSample = observedClientSample.getClientSample();
            var callId = roomsToCallIds.get(serviceRoomId);
            if (Objects.isNull(callId)) {
                callId = roomsToCallIds.get(serviceRoomId);
                if (Objects.isNull(callId)) {
                    callId = this.createCallIfNotExists(serviceRoomId, clientSample);
                    if (Objects.isNull(callId)) {
                        logger.warn("Cannot assign callId to clientSample {}", clientSample);
                        continue;
                    }
                    roomsToCallIds.put(serviceRoomId, callId);
                }
                clientSample.callId = callId;
            }
            if (!foundClientIds.contains(clientSample.clientId)) {
                this.clientsDepot.addFromObservedClientSample(observedClientSample);
            }
            ClientSampleVisitor.streamPeerConnectionTransports(clientSample).forEach(pcTransport -> {
                if (foundPeerConnectionIds.contains(pcTransport.peerConnectionId)) return;
                this.peerConnectionsDepot
                        .setObservedClientSample(observedClientSample)
                        .setPeerConnectionTransport(pcTransport)
                        .assemble();
            });
            ClientSampleVisitor.streamInboundAudioTracks(clientSample).forEach(track -> {
                if (foundMediaTrackIds.contains(track.trackId)) return;
                this.mediaTracksDepot
                        .setObservedClientSample(observedClientSample)
                        .setTrackId(track.trackId)
//                        .setSfuStreamId(track.sfuStreamId)
                        .setSfuSinkId(track.sfuSinkId)
                        .setStreamDirection(StreamDirection.INBOUND)
                        .setPeerConnectionId(track.peerConnectionId)
                        .setSSRC(track.ssrc)
                        .assemble();

            });

            ClientSampleVisitor.streamInboundVideoTracks(clientSample).forEach(track -> {
                if (foundMediaTrackIds.contains(track.trackId)) return;
                this.mediaTracksDepot
                        .setObservedClientSample(observedClientSample)
                        .setTrackId(track.trackId)
//                        .setSfuStreamId(track.sfuStreamId)
                        .setSfuSinkId(track.sfuSinkId)
                        .setStreamDirection(StreamDirection.INBOUND)
                        .setPeerConnectionId(track.peerConnectionId)
                        .setSSRC(track.ssrc)
                        .assemble();

            });

            ClientSampleVisitor.streamOutboundAudioTracks(clientSample).forEach(track -> {
                if (foundMediaTrackIds.contains(track.trackId)) return;
                this.mediaTracksDepot
                        .setObservedClientSample(observedClientSample)
                        .setTrackId(track.trackId)
                        .setSfuStreamId(track.sfuStreamId)
//                        .setSfuSinkId(track.sfuSinkId)
                        .setStreamDirection(StreamDirection.OUTBOUND)
                        .setPeerConnectionId(track.peerConnectionId)
                        .setSSRC(track.ssrc)
                        .assemble();

            });

            ClientSampleVisitor.streamOutboundVideoTracks(clientSample).forEach(track -> {
                if (foundMediaTrackIds.contains(track.trackId)) return;
                this.mediaTracksDepot
                        .setObservedClientSample(observedClientSample)
                        .setTrackId(track.trackId)
                        .setSfuStreamId(track.sfuStreamId)
//                        .setSfuSinkId(track.sfuSinkId)
                        .setStreamDirection(StreamDirection.OUTBOUND)
                        .setPeerConnectionId(track.peerConnectionId)
                        .setSSRC(track.ssrc)
                        .assemble();

            });
        }
        var newClientDTOs = this.clientsDepot.get();
        var newPeerConnectionDTOs = this.peerConnectionsDepot.get();
        var newMediaTrackDTOs = this.mediaTracksDepot.get();
        if (0 < newClientDTOs.size()) {
            this.addNewClients(newClientDTOs);
        }
        if (0 < newPeerConnectionDTOs.size()) {
            this.addNewPeerConnections(newPeerConnectionDTOs);
        }
        if (0 < newMediaTrackDTOs.size()) {
            this.addNewMediaTracks(newMediaTrackDTOs);
        }
        if (0 < observedClientSamples.size()) {
            synchronized (this) {
                this.output.onNext(observedClientSamples);
            }
        }

    }

    private UUID createCallIfNotExists(ServiceRoomId serviceRoomId, Samples.ClientSample clientSample) {
        var task = createCallIfNotExistsTaskProvider.get();
        task.withServiceRoomId(serviceRoomId)
                .withStartedTimestamp(clientSample.timestamp)
                .withCallId(clientSample.callId)
                .execute();

        if (!task.succeeded()) {
            return null;
        }
        var result = task.getResult();
        return result;
    }


    private void addNewMediaTracks(Map<UUID, MediaTrackDTO> newMediaTracks) {
        var task = addMediaTrackTaskProvider.get().withMediaTrackDTOs(newMediaTracks);

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
        }
    }

    private void addNewPeerConnections(Map<UUID, PeerConnectionDTO> newPeerConnections) {
        var task = peerConnectionsTaskProvider.get()
                .withPeerConnectionDTOs(newPeerConnections)
                ;
        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
        }
    }

    private void addNewClients(Map<UUID, ClientDTO> newClients) {
        var task = addClientsTaskProvider.get()
                .withClientDTOs(newClients)
                ;

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
        }
    }

}
