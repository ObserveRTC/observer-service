package org.observertc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.MediaKind;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.eventreports.*;
import org.observertc.observer.metrics.EvaluatorMetrics;
import org.observertc.observer.repositories.*;
import org.observertc.observer.samples.ClientSampleVisitor;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Prototype
public class CallEntitiesUpdater implements Consumer<ObservedClientSamples> {
    private static final Logger logger = LoggerFactory.getLogger(CallEntitiesUpdater.class);
    private static final String METRIC_COMPONENT_NAME = CallEntitiesUpdater.class.getSimpleName();

    @Inject
    EvaluatorMetrics exposedMetrics;

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @Inject
    InboundTracksRepository inboundTracksRepository;

    @Inject
    OutboundTracksRepository outboundTracksRepository;

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

    private Subject<ObservedClientSamples> output = PublishSubject.create();

    public Observable<ObservedClientSamples> observableClientSamples() {
        return this.output;
    }

    public void accept(ObservedClientSamples observedClientSamples) {
        if (observedClientSamples == null) {
            return;
        }
        if (observedClientSamples.isEmpty()) {
            this.output.onNext(observedClientSamples);
            return;
        }
        Instant started = Instant.now();
        try {
            this.process(observedClientSamples);
        } catch(Exception ex) {
            logger.warn("Exception occurred while processing clientsamples", ex);
        } finally {
            this.exposedMetrics.addTaskExecutionTime(METRIC_COMPONENT_NAME, started, Instant.now());
        }
    }

    private void process(ObservedClientSamples observedClientSamples) {
        if (observedClientSamples.isEmpty()) {
            return;
        }
        Map<ServiceRoomId, Call> calls = this.fetchCalls(observedClientSamples);
        var newClientModels = new LinkedList<Models.Client>();
        var newPeerConnectionModels = new LinkedList<Models.PeerConnection>();
        var newInboundTrackModels = new LinkedList<Models.InboundTrack>();
        var newOutboundTrackModels = new LinkedList<Models.OutboundTrack>();
//        Map<String, Client> clients = this.fetchExistingClients(observedClientSamples);
//        Map<String, PeerConnection> peerConnections = this.fetchExistingPeerConnections(observedClientSamples);
//        Map<String, InboundTrack> inboundTracks = this.fetchExistingInboundTracks(observedClientSamples);
//        Map<String, OutboundTrack> outboundTracks = this.fetchExistingOutboundTracks(observedClientSamples);

        for (var observedClientSample : observedClientSamples) {
            var serviceRoomId = observedClientSample.getServiceRoomId();
            var call = calls.get(serviceRoomId);
            if (call == null) {
                logger.warn("Have not inserted call for serviceRoom {}", serviceRoomId);
                continue;
            }
            var clientSample = observedClientSample.getClientSample();
            var timestamp = clientSample.timestamp;
            var marker = clientSample.marker;
            var client = call.getClient(clientSample.clientId);
            clientSample.callId = call.getCallId();
            if (client == null) {
                try {
                    client = call.addClient(
                            clientSample.clientId,
                            clientSample.userId,
                            observedClientSample.getMediaUnitId(),
                            observedClientSample.getTimeZoneId(),
                            timestamp,
                            marker
                    );
                    newClientModels.add(client.getModel());
                } catch (AlreadyCreatedException ex) {
                    logger.warn("Client {} for call {} in room {} (service: {}) is already created",
                            clientSample.clientId,
                            call.getCallId(),
                            call.getServiceRoomId().roomId,
                            call.getServiceRoomId().serviceId
                    );
                }

            } else {
                var lastTouch = client.getTouched();
                if (lastTouch == null || lastTouch < timestamp) {
                    client.touch(timestamp);
                }
            }

            Client finalClient = client;
            Function<String, PeerConnection> getPeerConnection = peerConnectionId -> {
                if (peerConnectionId == null) {
                    return null;
                }
                if (finalClient == null) {
                    logger.warn("No client we have to retrieve for peer connection. why?");
                    return null;
                }
                var result = finalClient.getPeerConnection(peerConnectionId);
                if (result == null) {
                    try {
                        result = finalClient.addPeerConnection(
                                peerConnectionId,
                                timestamp,
                                marker
                        );
                        newPeerConnectionModels.add(result.getModel());
                    } catch (AlreadyCreatedException ex) {
                        logger.warn("PeerConnection {} for call {} in room {} (service: {}) is already created",
                                peerConnectionId,
                                call.getCallId(),
                                call.getServiceRoomId().roomId,
                                call.getServiceRoomId().serviceId
                        );
                    }

                } else {
                    var lastTouch = result.getTouched();
                    if (lastTouch == null || lastTouch < timestamp) {
                        result.touch(timestamp);
                    }
                    result.touch(timestamp);
                }
                return result;
            };
            ClientSampleVisitor.streamPeerConnectionTransports(clientSample).forEach(pcTransport -> {
                var peerConnection = getPeerConnection.apply(pcTransport.peerConnectionId);
            });

            ClientSampleVisitor.streamInboundAudioTracks(clientSample).forEach(track -> {
                var peerConnection = getPeerConnection.apply(track.peerConnectionId);
                if (peerConnection == null) {
                    logger.warn("Peer Connection Id is null for inbound audio track sample {}", JsonUtils.objectToString(track));
                    return;
                }
                var inboundAudioTrack = peerConnection.getInboundTrack(track.trackId);
                if (inboundAudioTrack == null) {
                    try {
                        inboundAudioTrack = peerConnection.addInboundTrack(
                                track.trackId,
                                timestamp,
                                track.sfuStreamId,
                                track.sfuSinkId,
                                MediaKind.AUDIO,
                                track.ssrc,
                                marker
                        );
                        newInboundTrackModels.add(inboundAudioTrack.getModel());
                    } catch (AlreadyCreatedException ex) {
                        logger.warn("inboundAudioTrack {} for call {} in room {} (service: {}) is already created",
                                track.trackId,
                                call.getCallId(),
                                call.getServiceRoomId().roomId,
                                call.getServiceRoomId().serviceId
                        );
                    }
                } else {
                    var lastTouch = inboundAudioTrack.getTouched();
                    if (lastTouch == null || lastTouch < timestamp) {
                        inboundAudioTrack.touch(timestamp);
                    }
                    if (!inboundAudioTrack.hasSSRC(track.ssrc)) {
                        inboundAudioTrack.addSSRC(track.ssrc);
                    }
                }

            });

            ClientSampleVisitor.streamInboundVideoTracks(clientSample).forEach(track -> {
                var peerConnection = getPeerConnection.apply(track.peerConnectionId);
                if (peerConnection == null) {
                    logger.warn("Peer Connection Id is null for inbound audio track sample {}", JsonUtils.objectToString(track));
                    return;
                }
                var inboundVideoTrack = peerConnection.getInboundTrack(track.trackId);
                if (inboundVideoTrack == null) {
                    try {
                        inboundVideoTrack = peerConnection.addInboundTrack(
                                track.trackId,
                                timestamp,
                                track.sfuStreamId,
                                track.sfuSinkId,
                                MediaKind.VIDEO,
                                track.ssrc,
                                marker
                        );
                        newInboundTrackModels.add(inboundVideoTrack.getModel());
                    } catch (AlreadyCreatedException ex) {
                        logger.warn("inboundVideoTrack {} for call {} in room {} (service: {}) is already created",
                                track.trackId,
                                call.getCallId(),
                                call.getServiceRoomId().roomId,
                                call.getServiceRoomId().serviceId
                        );
                    }
                } else {
                    var lastTouch = inboundVideoTrack.getTouched();
                    if (lastTouch == null || lastTouch < timestamp) {
                        inboundVideoTrack.touch(timestamp);
                    }
                    if (!inboundVideoTrack.hasSSRC(track.ssrc)) {
                        inboundVideoTrack.addSSRC(track.ssrc);
                    }
                }
            });

            ClientSampleVisitor.streamOutboundAudioTracks(clientSample).forEach(track -> {
                var peerConnection = getPeerConnection.apply(track.peerConnectionId);
                if (peerConnection == null) {
                    logger.warn("Peer Connection Id is null for inbound audio track sample {}", JsonUtils.objectToString(track));
                    return;
                }
                var outboundAudioTrack = peerConnection.getOutboundTrack(track.trackId);

                if (outboundAudioTrack == null) {
                    try {
                        outboundAudioTrack = peerConnection.addOutboundTrack(
                                track.trackId,
                                timestamp,
                                track.sfuStreamId,
                                MediaKind.AUDIO,
                                track.ssrc,
                                marker
                        );
                        newOutboundTrackModels.add(outboundAudioTrack.getModel());
                    } catch (AlreadyCreatedException ex) {
                        logger.warn("outboundAudioTrack {} for call {} in room {} (service: {}) is already created",
                                track.trackId,
                                call.getCallId(),
                                call.getServiceRoomId().roomId,
                                call.getServiceRoomId().serviceId
                        );
                    }
                } else {
                    var lastTouch = outboundAudioTrack.getTouched();
                    if (lastTouch == null || lastTouch < timestamp) {
                        outboundAudioTrack.touch(timestamp);
                    }
                    if (!outboundAudioTrack.hasSSRC(track.ssrc)) {
                        outboundAudioTrack.addSSRC(track.ssrc);
                    }
                }
            });

            ClientSampleVisitor.streamOutboundVideoTracks(clientSample).forEach(track -> {
                var peerConnection = getPeerConnection.apply(track.peerConnectionId);
                if (peerConnection == null) {
                    logger.warn("Peer Connection Id is null for inbound audio track sample {}", JsonUtils.objectToString(track));
                    return;
                }
                var outboundVideoTrack = peerConnection.getOutboundTrack(track.trackId);
                if (outboundVideoTrack == null) {
                    try {
                        outboundVideoTrack = peerConnection.addOutboundTrack(
                                track.trackId,
                                timestamp,
                                track.sfuStreamId,
                                MediaKind.VIDEO,
                                track.ssrc,
                                marker
                        );
                        newOutboundTrackModels.add(outboundVideoTrack.getModel());
                    } catch (AlreadyCreatedException ex) {
                        logger.warn("OutboundVideoTrack {} for call {} in room {} (service: {}) is already created",
                                track.trackId,
                                call.getCallId(),
                                call.getServiceRoomId().roomId,
                                call.getServiceRoomId().serviceId
                        );
                    }
                } else {
                    var lastTouch = outboundVideoTrack.getTouched();
                    if (lastTouch == null || lastTouch < timestamp) {
                        outboundVideoTrack.touch(timestamp);
                    }
                    if (!outboundVideoTrack.hasSSRC(track.ssrc)) {
                        outboundVideoTrack.addSSRC(track.ssrc);
                    }
                }

            });
        }
        this.callsRepository.save();

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

    private Map<ServiceRoomId, Call> fetchCalls(ObservedClientSamples observedClientSamples) {
        var serviceRoomIds = observedClientSamples.getServiceRoomIds();
        var existingCalls = this.callsRepository.getAll(serviceRoomIds);
        var result = new HashMap<ServiceRoomId, Call>();
        if (existingCalls != null && 0 < existingCalls.size()) {
            result.putAll(existingCalls);
        }
        var visitedServiceRoomIds = new HashSet<ServiceRoomId>();
        var missingCalls = new HashSet<CallsRepository.CreateCallInfo>();
        var toRemove = new HashMap<ServiceRoomId, Models.Call>();
        for (var observedClientSample : observedClientSamples) {
            var serviceRoomId = observedClientSample.getServiceRoomId();
            if (visitedServiceRoomIds.contains(serviceRoomId)) {
                continue;
            }
            visitedServiceRoomIds.add(serviceRoomId);
            var clientSample = observedClientSample.getClientSample();

            var call = result.get(serviceRoomId);
            if (call == null) {
                missingCalls.add(new CallsRepository.CreateCallInfo(
                        serviceRoomId,
                        clientSample.marker,
                        clientSample.callId
                ));
                continue;
            }
            if (this.config.callIdAssignMode == null || ObserverConfig.EvaluatorsConfig.CallUpdater.CallIdAssignMode.MASTER.equals(this.config.callIdAssignMode)) {
                result.put(call.getServiceRoomId(), call);
                continue;
            }
            if (clientSample.callId == null) {
                logger.warn("Observer callIdAssignMode is in slave mode, but callId for room {} in samples did not provided. The observer ignores it and stick with the current callId {}", serviceRoomId, call.getCallId());
                continue;
            }
            if (clientSample.callId.equalsIgnoreCase(call.getCallId())) {
                // all good, this is the call we are in
                continue;
            }
            logger.info("Call \"{}\" must be removed in service {}, room: {}, because a new call with id \"{}\" is received", call.getCallId(), serviceRoomId.serviceId, serviceRoomId.roomId, clientSample.callId);
            toRemove.put(serviceRoomId, call.getModel());
            missingCalls.add(new CallsRepository.CreateCallInfo(
                    serviceRoomId,
                    clientSample.marker,
                    clientSample.callId
            ));
            result.remove(serviceRoomId);
        }
        if (missingCalls.size() < 1) {
            this.callsRepository.fetchRecursively(result.keySet());
            return result;
        }

        if (0 < toRemove.size()) {
            this.callsRepository.removeAll(toRemove.keySet());
            this.callsRepository.save();
            this.callEndedReports.accept(toRemove.values());
            toRemove.clear();
        }

        var alreadyInserted = this.callsRepository.insertAll(missingCalls);
        var newExistingCalls = this.callsRepository.fetchRecursively(serviceRoomIds);
        result.putAll(newExistingCalls);
        var newModels = new LinkedList<Models.Call>();
        for (var callInfo : missingCalls) {
            var serviceRoomId = callInfo.serviceRoomId();
            if (alreadyInserted.containsKey(serviceRoomId)) {
                continue;
            }
            var call = result.get(serviceRoomId);
            newModels.add(call.getModel());
        }
        if (0 < newModels.size()) {
            this.callStartedReports.accept(newModels);
        }
        return result;
    }
}
