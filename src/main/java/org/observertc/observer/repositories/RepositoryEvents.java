package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class RepositoryEvents {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryEvents.class);

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
    SfusRepository sfusRepository;

    @Inject
    SfuTransportsRepository sfuTransportsRepository;

    @Inject
    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    @Inject
    SfuSctpStreamsRepository sfuSctpStreamsRepository;

    @Inject
    ObserverConfig config;

    public RepositoryEvents() {

    }

    @PostConstruct
    void setup() {

    }

    @PreDestroy
    void teardown() {
    }

    public Observable<List<Models.Call>> expiredCalls() {
        return this.callsRepository.observableExpiredEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.Call>> deletedCalls() {
        return this.callsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.Client>> deletedClients() {
        return this.clientsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.PeerConnection>> deletedPeerConnections() {
        return this.peerConnectionsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.InboundTrack>> deletedInboundTrack() {
        return this.inboundTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.OutboundTrack>> deletedOutboundTrack() {
        return this.outboundTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.Sfu>> expiredSfu() {
        return this.sfusRepository.observableExpiredEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.Sfu>> deletedSfu() {
        return this.sfusRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.SfuTransport>> expiredSfuTransports() {
        return this.sfuTransportsRepository.observableExpiredEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.SfuTransport>> deletedSfuTransports() {
        return this.sfuTransportsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.SfuInboundRtpPad>> deletedSfuInboundRtpPads() {
        return this.sfuInboundRtpPadsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.SfuOutboundRtpPad>> deletedSfuOutboundRtpPads() {
        return this.sfuOutboundRtpPadsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.SfuSctpStream>> deletedSfuSctpStream() {
        return this.sfuSctpStreamsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }
}
