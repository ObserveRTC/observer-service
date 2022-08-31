package org.observertc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.eventreports.CallStartedReports;
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
    CallStartedReports callStartedReports;

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @Inject
    InboundAudioTracksRepository inboundAudioTracksRepository;

    @Inject
    InboundVideoTracksRepository inboundVideoTracksRepository;

    @Inject
    OutboundAudioTracksRepository outboundAudioTracksRepository;

    @Inject
    OutboundVideoTracksRepository outboundVideoTracksRepository;

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
        Map<ServiceRoomId, Call> calls = this.getCalls(observedClientSamples);
        Map<String, Client> clients = this.fetchExistingClients(observedClientSamples);
        Map<String, PeerConnection> peerConnections = this.fetchExistingPeerConnections(observedClientSamples);
        Map<String, InboundAudioTrack> inboundAudioTracks = this.fetchExistingInboundAudioTracks(observedClientSamples);
        Map<String, InboundVideoTrack> inboundVideoTracks = this.fetchExistingInboundVideoTracks(observedClientSamples);
        Map<String, OutboundAudioTrack> outboundAudioTracks = this.fetchExistingOutboundAudioTracks(observedClientSamples);;
        Map<String, OutboundVideoTrack> outboundVideoTracks = this.fetchExistingOutboundVideoTracks(observedClientSamples);;

        for (var observedClientSample : observedClientSamples) {
            var serviceRoomId = observedClientSample.getServiceRoomId();
            var call = calls.get(serviceRoomId);
            if (call == null) {
                logger.warn("Have not inserted call for serviceRoom {}", serviceRoomId);
                continue;
            }
            var clientSample = observedClientSample.getClientSample();
            var timestamp = clientSample.timestamp;
            var client = call.getClient(clientSample.clientId);
            clientSample.callId = call.getCallId();
            if (client == null) {
                client = call.addClient(
                        clientSample.clientId,
                        observedClientSample.getMediaUnitId(),
                        observedClientSample.getTimeZoneId(),
                        timestamp
                );
                clients.put(client.getClientId(), client);
            }

            Client finalClient = client;
            Function<String, PeerConnection> getPeerConnection = peerConnectionId -> {
                if (peerConnectionId == null) {
                    return null;
                }
                var result = peerConnections.get(peerConnectionId);
                if (result == null) {
                    finalClient.addPeerConnection(
                            peerConnectionId,
                            timestamp);
                    peerConnections.put(result.getPeerConnectionId(), result);
                }
                return result;
            };

            ClientSampleVisitor.streamInboundAudioTracks(clientSample).forEach(track -> {
                var peerConnection = getPeerConnection.apply(track.peerConnectionId);
                if (peerConnection == null) {
                    logger.warn("Peer Connection Id is null for inbound audio track sample {}", JsonUtils.objectToString(track));
                    return;
                }
                var inboundAudioTrack = inboundAudioTracks.get(track.trackId);
                if (inboundAudioTrack == null) {
                    inboundAudioTrack = peerConnection.addInboundAudioTrack(
                            track.trackId,
                            timestamp,
                            track.sfuStreamId,
                            track.sfuSinkId,
                            track.ssrc
                    );
                    inboundAudioTracks.put(inboundAudioTrack.getTrackId(), inboundAudioTrack);
                }
            });

            ClientSampleVisitor.streamInboundVideoTracks(clientSample).forEach(track -> {
                var peerConnection = getPeerConnection.apply(track.peerConnectionId);
                if (peerConnection == null) {
                    logger.warn("Peer Connection Id is null for inbound audio track sample {}", JsonUtils.objectToString(track));
                    return;
                }
                var inboundVideoTrack = inboundVideoTracks.get(track.trackId);
                if (inboundVideoTrack == null) {
                    inboundVideoTrack = peerConnection.addInboundVideoTrack(
                            track.trackId,
                            timestamp,
                            track.sfuStreamId,
                            track.sfuSinkId,
                            track.ssrc
                    );
                    inboundVideoTracks.put(inboundVideoTrack.getTrackId(), inboundVideoTrack);
                }
            });

            ClientSampleVisitor.streamOutboundAudioTracks(clientSample).forEach(track -> {
                var peerConnection = getPeerConnection.apply(track.peerConnectionId);
                if (peerConnection == null) {
                    logger.warn("Peer Connection Id is null for inbound audio track sample {}", JsonUtils.objectToString(track));
                    return;
                }
                var outboundAudioTrack = outboundAudioTracks.get(track.trackId);
                if (outboundAudioTrack == null) {
                    outboundAudioTrack = peerConnection.addOutboundAudioTrack(
                            track.trackId,
                            timestamp,
                            track.sfuStreamId,
                            track.ssrc
                    );
                    outboundAudioTracks.put(outboundAudioTrack.getTrackId(), outboundAudioTrack);
                }
            });

            ClientSampleVisitor.streamOutboundVideoTracks(clientSample).forEach(track -> {
                var peerConnection = getPeerConnection.apply(track.peerConnectionId);
                if (peerConnection == null) {
                    logger.warn("Peer Connection Id is null for inbound audio track sample {}", JsonUtils.objectToString(track));
                    return;
                }
                var outboundVideoTrack = outboundVideoTracks.get(track.trackId);
                if (outboundVideoTrack == null) {
                    outboundVideoTrack = peerConnection.addOutboundVideoTrack(
                            track.trackId,
                            timestamp,
                            track.sfuStreamId,
                            track.ssrc
                    );
                    outboundVideoTracks.put(outboundVideoTrack.getTrackId(), outboundVideoTrack);
                }
            });
        }

        this.callsRepository.save();

        if (0 < observedClientSamples.size()) {
            synchronized (this) {
                this.output.onNext(observedClientSamples);
            }
        }
    }

    private Map<String, OutboundVideoTrack> fetchExistingOutboundVideoTracks(ObservedClientSamples samples) {
        var result = new HashMap<String, OutboundVideoTrack>();
        var existingClients = this.outboundVideoTracksRepository.getAll(samples.getOutboundVideoTrackIds());
        if (existingClients != null && 0 < existingClients.size()) {
            result.putAll(existingClients);
        }
        return result;
    }

    private Map<String, OutboundAudioTrack> fetchExistingOutboundAudioTracks(ObservedClientSamples samples) {
        var result = new HashMap<String, OutboundAudioTrack>();
        var existingOutboundAudioTracks = this.outboundAudioTracksRepository.getAll(samples.getOutboundAudioTrackIds());
        if (existingOutboundAudioTracks != null && 0 < existingOutboundAudioTracks.size()) {
            result.putAll(existingOutboundAudioTracks);
        }
        return result;
    }

    private Map<String, InboundVideoTrack> fetchExistingInboundVideoTracks(ObservedClientSamples samples) {
        var result = new HashMap<String, InboundVideoTrack>();
        var existingInboundAudioTracks = this.inboundVideoTracksRepository.getAll(samples.getInboundVideoTrackIds());
        if (existingInboundAudioTracks != null && 0 < existingInboundAudioTracks.size()) {
            result.putAll(existingInboundAudioTracks);
        }
        return result;
    }

    private Map<String, InboundAudioTrack> fetchExistingInboundAudioTracks(ObservedClientSamples samples) {
        var result = new HashMap<String, InboundAudioTrack>();
        var existingInboundAudioTracks = this.inboundAudioTracksRepository.getAll(samples.getInboundAudioTrackIds());
        if (existingInboundAudioTracks != null && 0 < existingInboundAudioTracks.size()) {
            result.putAll(existingInboundAudioTracks);
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

    private Map<ServiceRoomId, Call> getCalls(ObservedClientSamples observedClientSamples) {
        var serviceRoomIds = observedClientSamples.getServiceRoomIds();
        var existingCalls = this.callsRepository.getAll(serviceRoomIds);
        Map<ServiceRoomId, Call> result = new HashMap<>();
        if (existingCalls != null && 0 < existingCalls.size()) {
            result.putAll(existingCalls);
        }
        var addedCalls = new LinkedList<Models.Call>();
        for (var serviceRoomId : serviceRoomIds) {
            var call = result.get(serviceRoomId);
            if (call != null) {
                // TODO: place here the slave part of the creating callid
                continue;
            }
            var now = Instant.now().toEpochMilli();
            call = this.callsRepository.add(
                    serviceRoomId,
                    now,
                    null,
                    null
            );
            if (call == null) {
                logger.warn("Did not inserted call for serviceRoom {}", serviceRoomId);
                continue;
            }
            addedCalls.add(call.getModel());
            result.put(call.getServiceRoomId(), call);
        }
        if (0 < addedCalls.size()) {
            this.callStartedReports.accept(addedCalls);
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
}
