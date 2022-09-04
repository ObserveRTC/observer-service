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
        Instant started = Instant.now();
        try {
            this.process(observedClientSamples);
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
        Map<String, Client> clients = this.fetchExistingClients(observedClientSamples);
        Map<String, PeerConnection> peerConnections = this.fetchExistingPeerConnections(observedClientSamples);
        Map<String, InboundTrack> inboundTracks = this.fetchExistingInboundTracks(observedClientSamples);
        Map<String, OutboundTrack> outboundTracks = this.fetchExistingOutboundTracks(observedClientSamples);

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
                client = call.addClient(
                        clientSample.clientId,
                        clientSample.userId,
                        observedClientSample.getMediaUnitId(),
                        observedClientSample.getTimeZoneId(),
                        timestamp,
                        marker
                );
                client.touch(timestamp);
                clients.put(client.getClientId(), client);
                newClientModels.add(client.getModel());
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
                var result = peerConnections.get(peerConnectionId);
                if (result == null) {
                    result = finalClient.addPeerConnection(
                            peerConnectionId,
                            timestamp,
                            marker
                    );
                    peerConnections.put(result.getPeerConnectionId(), result);
                    newPeerConnectionModels.add(result.getModel());
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
                var inboundAudioTrack = inboundTracks.get(track.trackId);
                if (inboundAudioTrack == null) {
                    inboundAudioTrack = peerConnection.addInboundTrack(
                            track.trackId,
                            timestamp,
                            track.sfuStreamId,
                            track.sfuSinkId,
                            MediaKind.AUDIO,
                            track.ssrc,
                            marker
                    );
                    inboundTracks.put(inboundAudioTrack.getTrackId(), inboundAudioTrack);
                    newInboundTrackModels.add(inboundAudioTrack.getModel());
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
                var inboundVideoTrack = inboundTracks.get(track.trackId);
                if (inboundVideoTrack == null) {
                    inboundVideoTrack = peerConnection.addInboundTrack(
                            track.trackId,
                            timestamp,
                            track.sfuStreamId,
                            track.sfuSinkId,
                            MediaKind.VIDEO,
                            track.ssrc,
                            marker
                    );
                    inboundTracks.put(inboundVideoTrack.getTrackId(), inboundVideoTrack);
                    newInboundTrackModels.add(inboundVideoTrack.getModel());
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
                var outboundAudioTrack = outboundTracks.get(track.trackId);
                if (outboundAudioTrack == null) {
                    outboundAudioTrack = peerConnection.addOutboundTrack(
                            track.trackId,
                            timestamp,
                            track.sfuStreamId,
                            MediaKind.AUDIO,
                            track.ssrc,
                            marker
                    );
                    outboundTracks.put(outboundAudioTrack.getTrackId(), outboundAudioTrack);
                    newOutboundTrackModels.add(outboundAudioTrack.getModel());
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
                var outboundVideoTrack = outboundTracks.get(track.trackId);
                if (outboundVideoTrack == null) {
                    outboundVideoTrack = peerConnection.addOutboundTrack(
                            track.trackId,
                            timestamp,
                            track.sfuStreamId,
                            MediaKind.VIDEO,
                            track.ssrc,
                            marker
                    );
                    outboundTracks.put(outboundVideoTrack.getTrackId(), outboundVideoTrack);
                    newOutboundTrackModels.add(outboundVideoTrack.getModel());
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

    private Map<String, InboundTrack> fetchExistingInboundTracks(ObservedClientSamples samples) {
        var result = new HashMap<String, InboundTrack>();
        var existingInboundTracks = this.inboundTracksRepository.getAll(samples.getInboundTrackIds());
        if (existingInboundTracks != null && 0 < existingInboundTracks.size()) {
            result.putAll(existingInboundTracks);
        }
        return result;
    }

    private Map<String, OutboundTrack> fetchExistingOutboundTracks(ObservedClientSamples samples) {
        var result = new HashMap<String, OutboundTrack>();
        var existingOutboundTracks = this.outboundTracksRepository.getAll(samples.getOutboundTrackIds());
        if (existingOutboundTracks != null && 0 < existingOutboundTracks.size()) {
            result.putAll(existingOutboundTracks);
        }
        return result;
    }

    private Map<String, PeerConnection> fetchExistingPeerConnections(ObservedClientSamples samples) {
        var result = new HashMap<String, PeerConnection>();
        var existingPeerConnections = this.peerConnectionsRepository.getAll(samples.getPeerConnectionIds());
        if (existingPeerConnections != null && 0 < existingPeerConnections.size()) {
            result.putAll(existingPeerConnections);
        }
        return result;
    }

    private Map<String, Client> fetchExistingClients(ObservedClientSamples samples) {
        var result = new HashMap<String, Client>();
        var existingClients = this.clientsRepository.getAll(samples.getClientIds());
        if (existingClients != null && 0 < existingClients.size()) {
            result.putAll(existingClients);
        }
        return result;
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
            if (clientSample.callId == call.getCallId()) {
                // all good, this is the call we are in
                continue;
            }
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
