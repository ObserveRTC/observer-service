package org.observertc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.BackgroundTasksExecutor;
import org.observertc.observer.ServerTimestamps;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.MediaKind;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.eventreports.*;
import org.observertc.observer.metrics.EvaluatorMetrics;
import org.observertc.observer.repositories.AlreadyCreatedException;
import org.observertc.observer.repositories.Call;
import org.observertc.observer.repositories.CallsRepository;
import org.observertc.observer.repositories.Client;
import org.observertc.observer.repositories.tasks.CleanCallEntities;
import org.observertc.observer.repositories.tasks.CommitCallEntities;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

@Singleton
public class CallEntitiesUpdater implements Consumer<ObservedClientSamples> {
    private static final Logger logger = LoggerFactory.getLogger(CallEntitiesUpdater.class);
    private static final String METRIC_COMPONENT_NAME = CallEntitiesUpdater.class.getSimpleName();

    @Inject
    EvaluatorMetrics exposedMetrics;

    @Inject
    CallsRepository callsRepository;

    @Inject
    CallStartedReports callStartedReports;

    @Inject
    CallEndedReports callEndedReports;

    @Inject
    ClientJoinedReports clientJoinedReports;

    @Inject
    PeerConnectionOpenedReports peerConnectionOpenedReports;

    @Inject
    InboundTrackAddedReports inboundTrackAddedReports;

    @Inject
    OutboundTrackAddedReports outboundTrackAddedReports;

    @Inject
    ObserverConfig.EvaluatorsConfig.CallUpdater config;

    @Inject
    CallsFetcherInMasterAssigningMode callsFetcherInMasterAssigningMode;

    @Inject
    CallsFetcherInSlaveAssigningMode callsFetcherInSlaveAssigningMode;

    @Inject
    ServerTimestamps serverTimestamps;

    @Inject
    ObserverConfig observerConfig;

    @Inject
    BackgroundTasksExecutor backgroundTasksExecutor;

    @Inject
    CleanCallEntities cleanCallEntities;

    @Inject
    CommitCallEntities commitCallEntities;

    @PostConstruct
    void setup() {
        if (!this.config.enabled) {
            logger.warn("Call Entities Updater is DISABLED, hence no stream analysis can happen");
        }
        this.backgroundTasksExecutor.addPeriodicTask(
                this.getClass().getSimpleName(),
                this.cleanCallEntities::createTask,
                60 * 1000
        );
    }

    @PreDestroy
    void teardown() {
        this.backgroundTasksExecutor.removePeriodicTask(this.getClass().getSimpleName());
    }

    private Subject<ObservedClientSamples> output = PublishSubject.create();

    public Observable<ObservedClientSamples> observableClientSamples() {
        return this.output;
    }

    public void accept(ObservedClientSamples observedClientSamples) {
        if (observedClientSamples == null) {
            return;
        }
        if (observedClientSamples.isEmpty() || !this.config.enabled) {
            this.output.onNext(observedClientSamples);
            return;
        }
        Instant started = Instant.now();
        try {
            this.commitCallEntities.lock();
            this.process(observedClientSamples);
        } catch(Exception ex) {
            logger.warn("Exception occurred while processing clientsamples", ex);
        } finally {
            this.commitCallEntities.unlock();
            if (!this.commitCallEntities.executeSync()) {
                logger.warn("Committing failed");
            }
            this.exposedMetrics.addTaskExecutionTime(METRIC_COMPONENT_NAME, started, Instant.now());
        }
    }

    private void process(ObservedClientSamples observedClientSamples) {
        Map<ServiceRoomId, Call> calls;
        Map<String, Client> existingRemedyClients;
        Set<String> unregisteredRemedyClientIds;
        switch (config.callIdAssignMode) {
            case SLAVE -> {
                var fetchedCallsForRooms = this.callsFetcherInSlaveAssigningMode.fetchFor(observedClientSamples);
                calls = fetchedCallsForRooms.actualCalls();
                existingRemedyClients = fetchedCallsForRooms.existingRemedyClients();
                unregisteredRemedyClientIds = Utils.firstNotNull(fetchedCallsForRooms.unregisteredRemedyClientIds(), Collections.emptySet());
            }
            default -> {
                var fetchedCallsForRooms = this.callsFetcherInMasterAssigningMode.fetchFor(observedClientSamples);
                calls = fetchedCallsForRooms.actualCalls();
                existingRemedyClients = fetchedCallsForRooms.existingRemedyClients();
                unregisteredRemedyClientIds = Utils.firstNotNull(fetchedCallsForRooms.unregisteredRemedyClientIds(), Collections.emptySet());
            }
        }
        var newClientModels = new LinkedList<Models.Client>();
        var newPeerConnectionModels = new LinkedList<Models.PeerConnection>();
        var newInboundTrackModels = new LinkedList<Models.InboundTrack>();
        var newOutboundTrackModels = new LinkedList<Models.OutboundTrack>();
        var serverTimestamp = this.serverTimestamps.instant().toEpochMilli();
        for (var observedRoom : observedClientSamples.observedRooms()) {
            Call call = null;
            for (var observedClient : observedRoom) {
                var clientId = observedClient.getClientId();
                if (clientId == null) {
                    logger.warn("ClientId was not for samples", JsonUtils.objectToString(observedClient.observedClientSamples()));
                    continue;
                }
                var client = existingRemedyClients.get(clientId);
                if (client != null) {
                    // it's a remedy client
                    call = client.getCall();
                    if (call == null) {
                        logger.warn("Remedy client {} for service: {}, room: {} referencing to a call {} does not exists.",
                                client.getClientId(),
                                client.getServiceId(),
                                client.getRoomId(),
                                client.getCallId()
                        );
                        continue;
                    }
                } else if (unregisteredRemedyClientIds.contains(clientId)){
                    // this is a new client, but points to a remedy call
                    var remedyCallId = observedClient.streamObservedClientSamples()
                            .map(c -> c.getClientSample().callId)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                    logger.info("Client ({}) is not registered and it reports to a remedy call {} does not accept new clients. room: {}, service: {}",
                            clientId,
                            remedyCallId,
                            observedRoom.getServiceRoomId().roomId,
                            observedRoom.getServiceRoomId().serviceId
                    );
                    continue;
                } else {
                    // this should belong to a normal call fetched previously
                    call = calls.get(observedRoom.getServiceRoomId());
                    if (call == null) {
                        logger.warn("Cannot find call for service {}, room {}",
                                observedRoom.getServiceRoomId().serviceId,
                                observedRoom.getServiceRoomId().roomId
                        );
                        continue;
                    }
                    client = call.getClient(clientId);
                }

                call.touch(observedRoom.getMaxTimestamp(), serverTimestamp);

                if (client == null) {
                    try {
                        client = call.addClient(
                                observedClient.getClientId(),
                                observedClient.getUserId(),
                                observedClient.getMediaUnitId(),
                                observedClient.getTimeZoneId(),
                                observedClient.getMinTimestamp(),
                                observedClient.getMarker()
                        );
                        newClientModels.add(client.getModel());
                    } catch (AlreadyCreatedException ex) {
                        logger.warn("Client {} for call {} in room {} (service: {}) is already created",
                                observedClient.getClientId(),
                                call.getCallId(),
                                call.getServiceRoomId().roomId,
                                call.getServiceRoomId().serviceId
                        );
                        client = call.getClient(observedClient.getClientId());
                    }
                    if (client == null) {
                        continue;
                    }
                }
                client.touch(observedClient.getMaxTimestamp(), serverTimestamp);

                for (var observedPeerConnection : observedClient.observedPeerConnections()) {
                    var peerConnection = client.getPeerConnection(observedPeerConnection.getPeerConnectionId());
                    if (peerConnection == null) {
                        try {
                            peerConnection = client.addPeerConnection(
                                    observedPeerConnection.getPeerConnectionId(),
                                    observedPeerConnection.getMinTimestamp(),
                                    observedPeerConnection.getMarker()
                            );
                            newPeerConnectionModels.add(peerConnection.getModel());
                        } catch (AlreadyCreatedException ex) {
                            logger.warn("PeerConnection {} for call {} in room {} (service: {}) is already created",
                                    observedPeerConnection.getPeerConnectionId(),
                                    call.getCallId(),
                                    call.getServiceRoomId().roomId,
                                    call.getServiceRoomId().serviceId
                            );
                            peerConnection = client.getPeerConnection(observedPeerConnection.getPeerConnectionId());
                        }
                        if (peerConnection == null) {
                            continue;
                        }
                    }
                    peerConnection.touch(observedClient.getMaxTimestamp(), serverTimestamp);
                    for (var observedInboundAudioTrack : observedPeerConnection.observedInboundAudioTracks()) {
                        var inboundAudioTrack = peerConnection.getInboundTrack(observedInboundAudioTrack.getTrackId());
                        if (inboundAudioTrack == null) {
                            try {
                                inboundAudioTrack = peerConnection.addInboundTrack(
                                        observedInboundAudioTrack.getTrackId(),
                                        observedInboundAudioTrack.getMinTimestamp(),
                                        observedInboundAudioTrack.getSfuStreamId(),
                                        observedInboundAudioTrack.getSfuSinkId(),
                                        MediaKind.AUDIO,
                                        observedInboundAudioTrack.getSSRC(),
                                        observedInboundAudioTrack.getMarker()
                                );
                                newInboundTrackModels.add(inboundAudioTrack.getModel());
                            } catch (AlreadyCreatedException ex) {
                                logger.warn("inboundAudioTrack {} for call {} in room {} (service: {}) is already created",
                                        observedInboundAudioTrack.getTrackId(),
                                        call.getCallId(),
                                        call.getServiceRoomId().roomId,
                                        call.getServiceRoomId().serviceId
                                );
                                inboundAudioTrack = peerConnection.getInboundTrack(observedInboundAudioTrack.getTrackId());
                            }
                            if (inboundAudioTrack == null) {
                                logger.warn("Skip processing inbound audio track observation for trackId: {}", observedInboundAudioTrack.getTrackId());
                                continue;
                            }
                        } else {
                            var lastTouch = inboundAudioTrack.getSampleTouched();
                            if (lastTouch == null || lastTouch < observedInboundAudioTrack.getMaxTimestamp()) {
                                inboundAudioTrack.touchBySample(observedInboundAudioTrack.getMaxTimestamp());
                            }
                            if (!inboundAudioTrack.hasSSRC(observedInboundAudioTrack.getSSRC())) {
                                inboundAudioTrack.addSSRC(observedInboundAudioTrack.getSSRC());
                            }
                        }
                        var serverTouch = inboundAudioTrack.getServerTouch();
                        if (serverTouch == null || serverTouch <  serverTimestamp) {
                            inboundAudioTrack.touchByServer(serverTimestamp);
                        }
                    }

                    for (var observedInboundVideoTrack : observedPeerConnection.observedInboundVideoTracks()) {
                        var inboundVideoTrack = peerConnection.getInboundTrack(observedInboundVideoTrack.getTrackId());
                        if (inboundVideoTrack == null) {
                            try {
                                inboundVideoTrack = peerConnection.addInboundTrack(
                                        observedInboundVideoTrack.getTrackId(),
                                        observedInboundVideoTrack.getMinTimestamp(),
                                        observedInboundVideoTrack.getSfuStreamId(),
                                        observedInboundVideoTrack.getSfuSinkId(),
                                        MediaKind.VIDEO,
                                        observedInboundVideoTrack.getSSRC(),
                                        observedInboundVideoTrack.getMarker()
                                );
                                newInboundTrackModels.add(inboundVideoTrack.getModel());
                            } catch (AlreadyCreatedException ex) {
                                logger.warn("InboundVideoTrack {} for call {} in room {} (service: {}) is already created",
                                        observedInboundVideoTrack.getTrackId(),
                                        call.getCallId(),
                                        call.getServiceRoomId().roomId,
                                        call.getServiceRoomId().serviceId
                                );
                            }
                            if (inboundVideoTrack == null) {
                                logger.warn("Skip processing inbound audio track observation for trackId: {}", observedInboundVideoTrack.getTrackId());
                                continue;
                            }
                        } else {
                            var lastTouch = inboundVideoTrack.getSampleTouched();
                            if (lastTouch == null || lastTouch < observedInboundVideoTrack.getMaxTimestamp()) {
                                inboundVideoTrack.touchBySample(observedInboundVideoTrack.getMaxTimestamp());
                            }
                            if (!inboundVideoTrack.hasSSRC(observedInboundVideoTrack.getSSRC())) {
                                inboundVideoTrack.addSSRC(observedInboundVideoTrack.getSSRC());
                            }
                        }
                        var serverTouch = inboundVideoTrack.getServerTouch();
                        if (serverTouch == null || serverTouch <  serverTimestamp) {
                            inboundVideoTrack.touchByServer(serverTimestamp);
                        }
                    }


                    for (var observedOutboundAudioTrack : observedPeerConnection.observedOutboundAudioTracks()) {
                        var outboundAudioTrack = peerConnection.getOutboundTrack(observedOutboundAudioTrack.getTrackId());
                        if (outboundAudioTrack == null) {
                            try {
                                outboundAudioTrack = peerConnection.addOutboundTrack(
                                        observedOutboundAudioTrack.getTrackId(),
                                        observedOutboundAudioTrack.getMinTimestamp(),
                                        observedOutboundAudioTrack.getSfuStreamId(),
                                        MediaKind.AUDIO,
                                        observedOutboundAudioTrack.getSSRC(),
                                        observedOutboundAudioTrack.getMarker()
                                );
                                newOutboundTrackModels.add(outboundAudioTrack.getModel());
                            } catch (AlreadyCreatedException ex) {
                                logger.warn("outboundAudioTrack {} for call {} in room {} (service: {}) is already created",
                                        observedOutboundAudioTrack.getTrackId(),
                                        call.getCallId(),
                                        call.getServiceRoomId().roomId,
                                        call.getServiceRoomId().serviceId
                                );
                                outboundAudioTrack = peerConnection.getOutboundTrack(observedOutboundAudioTrack.getTrackId());
                            }
                            if (outboundAudioTrack == null) {
                                logger.warn("Skip processing outbound audio track observation for trackId: {}", observedOutboundAudioTrack.getTrackId());
                                continue;
                            }
                        } else {
                            var lastTouch = outboundAudioTrack.getSampleTouched();
                            if (lastTouch == null || lastTouch < observedOutboundAudioTrack.getMaxTimestamp()) {
                                outboundAudioTrack.touchBySample(observedOutboundAudioTrack.getMaxTimestamp());
                            }
                            if (!outboundAudioTrack.hasSSRC(observedOutboundAudioTrack.getSSRC())) {
                                outboundAudioTrack.addSSRC(observedOutboundAudioTrack.getSSRC());
                            }
                        }
                        var serverTouch = outboundAudioTrack.getServerTouch();
                        if (serverTouch == null || serverTouch <  serverTimestamp) {
                            outboundAudioTrack.touchByServer(serverTimestamp);
                        }
                    }

                    for (var observedOutboundVideoTrack : observedPeerConnection.observedOutboundVideoTracks()) {
                        var outboundVideoTrack = peerConnection.getOutboundTrack(observedOutboundVideoTrack.getTrackId());
                        if (outboundVideoTrack == null) {
                            try {
                                outboundVideoTrack = peerConnection.addOutboundTrack(
                                        observedOutboundVideoTrack.getTrackId(),
                                        observedOutboundVideoTrack.getMinTimestamp(),
                                        observedOutboundVideoTrack.getSfuStreamId(),
                                        MediaKind.VIDEO,
                                        observedOutboundVideoTrack.getSSRC(),
                                        observedOutboundVideoTrack.getMarker()
                                );
                                newOutboundTrackModels.add(outboundVideoTrack.getModel());
                            } catch (AlreadyCreatedException ex) {
                                logger.warn("OutboundVideoTrack {} for call {} in room {} (service: {}) is already created",
                                        observedOutboundVideoTrack.getTrackId(),
                                        call.getCallId(),
                                        call.getServiceRoomId().roomId,
                                        call.getServiceRoomId().serviceId
                                );
                            }
                            if (outboundVideoTrack == null) {
                                logger.warn("Skip processing outbound audio track observation for trackId: {}", observedOutboundVideoTrack.getTrackId());
                                continue;
                            }
                        } else {
                            var lastTouch = outboundVideoTrack.getSampleTouched();
                            if (lastTouch == null || lastTouch < observedOutboundVideoTrack.getMaxTimestamp()) {
                                outboundVideoTrack.touchBySample(observedOutboundVideoTrack.getMaxTimestamp());
                            }
                            if (!outboundVideoTrack.hasSSRC(observedOutboundVideoTrack.getSSRC())) {
                                outboundVideoTrack.addSSRC(observedOutboundVideoTrack.getSSRC());
                            }
                        }
                        var serverTouch = outboundVideoTrack.getServerTouch();
                        if (serverTouch == null || serverTouch <  serverTimestamp) {
                            outboundVideoTrack.touchByServer(serverTimestamp);
                        }
                    }
                }
            }
        }

        if (0 < newClientModels.size()) {
            this.clientJoinedReports.accept(newClientModels);
        }
        if (0 < newPeerConnectionModels.size()) {
            this.peerConnectionOpenedReports.accept(newPeerConnectionModels);
        }
        if (0 < newInboundTrackModels.size()) {
            this.inboundTrackAddedReports.accept(newInboundTrackModels);
        }
        if (0 < newOutboundTrackModels.size()) {
            this.outboundTrackAddedReports.accept(newOutboundTrackModels);
        }
        if (0 < observedClientSamples.size()) {
            synchronized (this) {
                this.output.onNext(observedClientSamples);
            }
        }
    }




}
