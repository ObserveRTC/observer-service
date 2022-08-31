package org.observertc.observer.repositories;

import com.hazelcast.map.IMap;
import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.ClientMessageEvent;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.UUID;
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
    InboundAudioTracksRepository inboundAudioTracksRepository;

    @Inject
    InboundVideoTracksRepository inboundVideoTracksRepository;

    @Inject
    OutboundAudioTracksRepository outboundAudioTracksRepository;

    @Inject
    OutboundVideoTracksRepository outboundVideoTracksRepository;

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

    public Observable<List<Models.Call>> addedCalls() {
        return this.callsRepository.observableCreatedEntries().map(
                events -> events.stream().map(ModifiedStorageEntry::getNewValue).collect(Collectors.toList())
        );
    }


    public Observable<List<Models.Call>> removedCalls() {
        return this.callsRepository.observableDeletedEntries().map(
                events -> events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
        );
    }

    public Observable<List<Models.Client>> addedClients() {
        return this.clientsRepository.observableCreatedEntries().map(
                events -> events.stream().map(ModifiedStorageEntry::getNewValue).collect(Collectors.toList())
        );
    }

    public Observable<List<RepositoryExpiredEvent<Models.Client>>> expiredClients() {
        var expirationTimeInMs = this.config.repository.clientMaxIdleTimeInS;
        return this.clientsRepository.observableExpiredEntries()
                .map(events -> events.stream().map(event ->
                        RepositoryExpiredEvent.make(event.getOldValue(), event.getTimestamp() - expirationTimeInMs)).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.Client>> removedClients() {
        return this.clientsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.PeerConnection>> addedPeerConnection() {
        return this.peerConnectionsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(event -> event.getNewValue()).collect(Collectors.toList())
                );
    }

    public Observable<List<RepositoryExpiredEvent<Models.PeerConnection>>> expiredPeerConnection() {
        var expirationTimeInMs = this.config.repository.peerConnectionsMaxIdleTime;
        return this.peerConnectionsRepository.observableExpiredEntries()
                .map(events -> events.stream().map(event ->
                        RepositoryExpiredEvent.make(event.getOldValue(), event.getTimestamp() - expirationTimeInMs)).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.PeerConnection>> removedPeerConnection() {
        return this.peerConnectionsRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.InboundAudioTrack>> addedInboundAudioTrack() {
        return this.inboundAudioTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(event -> event.getNewValue()).collect(Collectors.toList())
                );
    }

    public Observable<List<RepositoryExpiredEvent<Models.InboundAudioTrack>>> expiredInboundAudioTrack() {
        var expirationTimeInMs = this.config.repository.mediaTracksMaxIdleTimeInS;
        return this.inboundAudioTracksRepository.observableExpiredEntries()
                .map(events -> events.stream().map(event ->
                        RepositoryExpiredEvent.make(event.getOldValue(), event.getTimestamp() - expirationTimeInMs)).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.InboundAudioTrack>> removedInboundAudioTrack() {
        return this.inboundAudioTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }


    public Observable<List<Models.InboundVideoTrack>> addedInboundVideoTrack() {
        return this.inboundVideoTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(event -> event.getNewValue()).collect(Collectors.toList())
                );
    }

    public Observable<List<RepositoryExpiredEvent<Models.InboundVideoTrack>>> expiredInboundVideoTrack() {
        var expirationTimeInMs = this.config.repository.mediaTracksMaxIdleTimeInS;
        return this.inboundVideoTracksRepository.observableExpiredEntries()
                .map(events -> events.stream().map(event ->
                        RepositoryExpiredEvent.make(event.getOldValue(), event.getTimestamp() - expirationTimeInMs)).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.InboundVideoTrack>> removedInboundVideoTrack() {
        return this.inboundVideoTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }








    public Observable<List<Models.OutboundAudioTrack>> addedOutboundAudioTrack() {
        return this.outboundAudioTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(event -> event.getNewValue()).collect(Collectors.toList())
                );
    }

    public Observable<List<RepositoryExpiredEvent<Models.OutboundAudioTrack>>> expiredOutboundAudioTrack() {
        var expirationTimeInMs = this.config.repository.mediaTracksMaxIdleTimeInS;
        return this.outboundAudioTracksRepository.observableExpiredEntries()
                .map(events -> events.stream().map(event ->
                        RepositoryExpiredEvent.make(event.getOldValue(), event.getTimestamp() - expirationTimeInMs)).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.OutboundAudioTrack>> removedOutboundAudioTrack() {
        return this.outboundAudioTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }


    public Observable<List<Models.OutboundVideoTrack>> addedOutboundVideoTrack() {
        return this.outboundVideoTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(event -> event.getNewValue()).collect(Collectors.toList())
                );
    }

    public Observable<List<RepositoryExpiredEvent<Models.OutboundVideoTrack>>> expiredOutboundVideoTrack() {
        var expirationTimeInMs = this.config.repository.mediaTracksMaxIdleTimeInS;
        return this.outboundVideoTracksRepository.observableExpiredEntries()
                .map(events -> events.stream().map(event ->
                        RepositoryExpiredEvent.make(event.getOldValue(), event.getTimestamp() - expirationTimeInMs)).collect(Collectors.toList())
                );
    }

    public Observable<List<Models.OutboundVideoTrack>> removedOutboundVideoTrack() {
        return this.outboundVideoTracksRepository.observableDeletedEntries()
                .map(events ->
                        events.stream().map(ModifiedStorageEntry::getOldValue).collect(Collectors.toList())
                );
    }










    public Observable<List<Models.InboundAudioTrack>> addedSfuRtpPads() {
        var result = this.getObservableList(this.addedSfuRtpPad);
        return result;
    }

    public Observable<List<RepositoryUpdatedEvent<SfuRtpPadDTO>>> updatedSfuRtpPads() {
        var result= this.getObservableList(this.updatedSfuRtpPad);
        return result;
    }

    public Observable<List<RepositoryExpiredEvent<SfuRtpPadDTO>>> expiredSfuRtpPads() {
        var result= this.getObservableList(this.expiredSfuRtpPad);
        return result;
    }

    public Observable<List<SfuRtpPadDTO>> removedSfuRtpPads() {
        var result= this.getObservableList(this.removedSfuRtpPad);
        return result;
    }


    public Observable<List<SfuTransportDTO>> addedSfuTransports() {
        var result = this.getObservableList(this.addedSfuTransport);
        return result;
    }

    public Observable<List<RepositoryUpdatedEvent<SfuTransportDTO>>> updatedSfuTransports() {
        var result= this.getObservableList(this.updatedSfuTransport);
        return result;
    }

    public Observable<List<RepositoryExpiredEvent<SfuTransportDTO>>> expiredSfuTransports() {
        var result= this.getObservableList(this.expiredSfuTransport);
        return result;
    }

    public Observable<List<SfuTransportDTO>> removedSfuTransports() {
        var result= this.getObservableList(this.removedSfuTransport);
        return result;
    }

    public Observable<List<SfuDTO>> addedSfu() {
        var result = this.getObservableList(this.addedSfu);
        return result;
    }

    public Observable<List<RepositoryUpdatedEvent<ClientMessageEvent>>> updatedClientMessageEvents() {
        var result = this.getObservableList(this.updatedClientMessages);
        return result;
    }

    public Observable<List<RepositoryExpiredEvent<SfuDTO>>> expiredSfu() {
        var result= this.getObservableList(this.expiredSfu);
        return result;
    }

    public Observable<List<SfuDTO>> removedSfu() {
        var result= this.getObservableList(this.removedSfu);
        return result;
    }

    public Observable<List<SfuStreamDTO>> addedSfuStreams() {
        var result= this.getObservableList(this.addedSfuStreams);
        return result;
    }

    public Observable<List<RepositoryUpdatedEvent<SfuStreamDTO>>> updatedSfuStreams() {
        var result= this.getObservableList(this.updatedSfuStreams);
        return result;
    }

    public Observable<List<SfuStreamDTO>> removedSfuStreams() {
        var result= this.getObservableList(this.removedSfuStreams);
        return result;
    }

    public Observable<List<SfuSinkDTO>> addedSfuSinks() {
        var result= this.getObservableList(this.addedSfuSinks);
        return result;
    }

    public Observable<List<RepositoryUpdatedEvent<SfuSinkDTO>>> updatedSuSinks() {
        var result= this.getObservableList(this.updatedSfuSinks);
        return result;
    }

    public Observable<List<SfuSinkDTO>> removedSfuSinks() {
        var result= this.getObservableList(this.removedSfuSinks);
        return result;
    }

    private<K, V> RepositoryEvents add(String context, IMap<K, V> map, Consumer<EntryListenerBuilder<K, V>> setup) {
        var builder = EntryListenerBuilder.<K, V>create(context);
        try {
            setup.accept(builder);
        } catch (Throwable throwable) {
            logger.warn("Unexpected exception occurred at eventListenerBuilder, context: {}", builder.context, throwable);

        }
        UUID listenerId = map.addLocalEntryListener(builder.build());
        this.destructors.add(() -> {
            map.removeEntryListener(listenerId);
            logger.info("EventListener for {} is destroyed", context);
        });
        return this;
    }
}
