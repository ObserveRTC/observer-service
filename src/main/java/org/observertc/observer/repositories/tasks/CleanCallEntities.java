package org.observertc.observer.repositories.tasks;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.ServerTimestamps;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Task;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class CleanCallEntities {

    private static final Logger logger = LoggerFactory.getLogger(CleanCallEntities.class);

    @Inject
    ObserverConfig observerConfig;

    @Inject
    ServerTimestamps serverTimestamps;

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

    private Long lastCallCleaned = null;
    private Long lastClientCleaned = null;
    private Long lastPeerConnectionCleaned = null;
    private Long lastInboundTrackCleaned = null;
    private Long lastOutboundTrackCleaned = null;


    public Task<Void> createTask() {
        var result = ChainedTask.<Void>builder()
                .addActionStage("Clean Calls", this::cleanCalls)
                .addActionStage("Clean Clients", this::cleanClients)
                .addActionStage("Clean Peer Connections", this::cleanPeerConnections)
                .addActionStage("Clean Inbound Tracks", this::cleanInboundTracks)
                .addActionStage("Clean Outbound Tracks", this::cleanOutboundTracks)
                .addActionStage("Commit Call Entity changes", () -> {
                    this.inboundTracksRepository.save();
                    this.outboundTracksRepository.save();
                    this.peerConnectionsRepository.save();
                    this.clientsRepository.save();
                    this.callsRepository.save();
                });
        return result.build();
    }

    private void cleanCalls() {
        var serverTimestamp = this.serverTimestamps.instant().getEpochSecond();
        var callMaxIdleTimeInS = this.observerConfig.repository.callMaxIdleTimeInS;
        var thresholdInMs = serverTimestamp - callMaxIdleTimeInS;
        if (this.lastCallCleaned != null && thresholdInMs <  this.lastCallCleaned) {
            return;
        }
        this.lastCallCleaned = serverTimestamp;

        var expiredCalls = Utils.firstNotNull(this.callsRepository.getAllLocallyStored(), Collections.<String, Call>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for Call {}, because the serverTouched is null", obj.getCallId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        call -> call.getCallId(),
                        Function.identity()
                ));
        if (expiredCalls.size() < 1) {
            return;
        }
        logger.info("Found {} expired calls", expiredCalls.size());
        this.callsRepository.removeAll(expiredCalls.keySet());
    }


    private void cleanClients() {
        var serverTimestamp = this.serverTimestamps.instant().getEpochSecond();
        var clientsMaxIdleInMs = this.observerConfig.repository.clientsMaxIdle;
        var thresholdInMs = serverTimestamp - clientsMaxIdleInMs;
        if (this.lastClientCleaned != null && thresholdInMs <  this.lastCallCleaned) {
            return;
        }
        this.lastClientCleaned = serverTimestamp;

        var expiredClients = Utils.firstNotNull(this.clientsRepository.getAllLocallyStored(), Collections.<String, Client>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for Client {}, because the serverTouched is null", obj.getClientId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        client -> client.getClientId(),
                        Function.identity()
                ));
        if (expiredClients.size() < 1) {
            return;
        }
        logger.info("Found {} expired clients", expiredClients.size());
        this.clientsRepository.deleteAll(expiredClients.keySet());
    }

    private void cleanPeerConnections() {
        var serverTimestamp = this.serverTimestamps.instant().getEpochSecond();
        var peerConnectionMaxIdleInMs = this.observerConfig.repository.peerConnectionsMaxIdle;
        var thresholdInMs = serverTimestamp - peerConnectionMaxIdleInMs;
        if (this.lastPeerConnectionCleaned != null && thresholdInMs <  this.lastPeerConnectionCleaned) {
            return;
        }
        this.lastPeerConnectionCleaned = serverTimestamp;

        var expiredPeerConnections = Utils.firstNotNull(this.peerConnectionsRepository.getAllLocallyStored(), Collections.<String, PeerConnection>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for PeerConnection {}, because the serverTouched is null", obj.getPeerConnectionId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        peerConnection -> peerConnection.getPeerConnectionId(),
                        Function.identity()
                ));
        if (expiredPeerConnections.size() < 1) {
            return;
        }
        logger.info("Found {} expired peer connection", expiredPeerConnections.size());
        this.peerConnectionsRepository.deleteAll(expiredPeerConnections.keySet());
    }

    private void cleanInboundTracks() {
        var serverTimestamp = this.serverTimestamps.instant().getEpochSecond();
        var inboundTracksMaxIdle = this.observerConfig.repository.inboundTracksMaxIdle;
        var thresholdInMs = serverTimestamp - inboundTracksMaxIdle;
        if (this.lastInboundTrackCleaned != null && thresholdInMs <  this.lastInboundTrackCleaned) {
            return;
        }
        this.lastInboundTrackCleaned = serverTimestamp;

        var expiredInboundTracks = Utils.firstNotNull(this.inboundTracksRepository.getAllLocallyStored(), Collections.<String, InboundTrack>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for InboundTrack {}, because the serverTouched is null", obj.getTrackId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        inboundTrack -> inboundTrack.getTrackId(),
                        Function.identity()
                ));
        if (expiredInboundTracks.size() < 1) {
            return;
        }
        logger.info("Found {} expired inbound track", expiredInboundTracks.size());
        this.inboundTracksRepository.deleteAll(expiredInboundTracks.keySet());
    }

    private void cleanOutboundTracks() {
        var serverTimestamp = this.serverTimestamps.instant().getEpochSecond();
        var outboundTracksMaxIdle = this.observerConfig.repository.outboundTracksMaxIdle;
        var thresholdInMs = serverTimestamp - outboundTracksMaxIdle;
        if (this.lastOutboundTrackCleaned != null && thresholdInMs <  this.lastOutboundTrackCleaned) {
            return;
        }
        this.lastOutboundTrackCleaned = serverTimestamp;

        var expiredOutboundTracks = Utils.firstNotNull(this.outboundTracksRepository.getAllLocallyStored(), Collections.<String, OutboundTrack>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for OutboundTrack {}, because the serverTouched is null", obj.getTrackId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        outboundTrack -> outboundTrack.getTrackId(),
                        Function.identity()
                ));
        if (expiredOutboundTracks.size() < 1) {
            return;
        }
        logger.info("Found {} expired outbound track", expiredOutboundTracks.size());
        this.outboundTracksRepository.deleteAll(expiredOutboundTracks.keySet());
    }
}
