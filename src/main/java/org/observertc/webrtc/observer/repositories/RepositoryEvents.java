package org.observertc.webrtc.observer.repositories;

import com.hazelcast.map.IMap;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class RepositoryEvents {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryEvents.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ObserverConfig observerConfig;

    private Subject<CallDTO> addedCalls = PublishSubject.create();
    private Subject<CallDTO> removedCalls = PublishSubject.create();

    private Subject<ClientDTO> addedClient = PublishSubject.create();
    private Subject<RepositoryExpiredEvent<ClientDTO>> expiredClient = PublishSubject.create();
    private Subject<ClientDTO> removedClient = PublishSubject.create();

    private Subject<PeerConnectionDTO> addedPeerConnection = PublishSubject.create();
    private Subject<RepositoryExpiredEvent<PeerConnectionDTO>> expiredPeerConnection = PublishSubject.create();
    private Subject<PeerConnectionDTO> removedPeerConnection = PublishSubject.create();

    private Subject<MediaTrackDTO> addedMediaTrack = PublishSubject.create();
    private Subject<RepositoryExpiredEvent<MediaTrackDTO>> expiredMediaTrack = PublishSubject.create();
    private Subject<MediaTrackDTO> removedMediaTrack = PublishSubject.create();

    private Subject<SfuRtpPadDTO> addedSfuRtpPad = PublishSubject.create();
    private Subject<RepositoryExpiredEvent<SfuRtpPadDTO>> expiredSfuRtpPad = PublishSubject.create();
    private Subject<RepositoryUpdatedEvent<SfuRtpPadDTO>> updatedSfuRtpPad = PublishSubject.create();
    private Subject<SfuRtpPadDTO> removedSfuRtpPad = PublishSubject.create();

    private Subject<SfuTransportDTO> addedSfuTransport = PublishSubject.create();
    private Subject<RepositoryExpiredEvent<SfuTransportDTO>> expiredSfuTransport = PublishSubject.create();
    private Subject<RepositoryUpdatedEvent<SfuTransportDTO>> updatedSfuTransport = PublishSubject.create();
    private Subject<SfuTransportDTO> removedSfuTransport = PublishSubject.create();

    private Subject<SfuDTO> addedSfu = PublishSubject.create();
    private Subject<RepositoryExpiredEvent<SfuDTO>> expiredSfu = PublishSubject.create();
    private Subject<SfuDTO> removedSfu = PublishSubject.create();

    private List<Runnable> destructors = new LinkedList<>();

    @PostConstruct
    void setup() {
        ObserverConfig.RepositoryConfig repositoryConfig = this.observerConfig.repositories;
        this.add(
                "Call DTO Related Events",
                this.hazelcastMaps.getCalls(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        CallDTO callDTO = event.getValue();
                        addedCalls.onNext(callDTO);
                    }).onEntryRemoved(event -> {
                        CallDTO callDTO= event.getOldValue();
                        removedCalls.onNext(callDTO);
                    });
                });

        this.add(
                "Client DTO Related Events",
                this.hazelcastMaps.getClients(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        ClientDTO clientDTO = event.getValue();
                        addedClient.onNext(clientDTO);
                    }).onEntryRemoved(event -> {
                        ClientDTO clientDTO = event.getOldValue();
                        removedClient.onNext(clientDTO);
                    }).onEntryExpired(event -> {
                        ClientDTO clientDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.clientMaxIdleTime).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<ClientDTO>make(clientDTO, estimatedLastTouch);
                        expiredClient.onNext(forwardedEvent);
                    });
                });

        this.add(
                "Peer Connection DTO Related Events",
                this.hazelcastMaps.getPeerConnections(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        PeerConnectionDTO peerConnectionDTO = event.getValue();
                        addedPeerConnection.onNext(peerConnectionDTO);
                    }).onEntryRemoved(event -> {
                        PeerConnectionDTO peerConnectionDTO = event.getOldValue();
                        removedPeerConnection.onNext(peerConnectionDTO);
                    }).onEntryExpired(event -> {
                        PeerConnectionDTO peerConnectionDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.clientMaxIdleTime).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<PeerConnectionDTO>make(peerConnectionDTO, estimatedLastTouch);
                        expiredPeerConnection.onNext(forwardedEvent);
                    });
                });

        this.add(
                "Media Track DTO Related Events",
                this.hazelcastMaps.getMediaTracks(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        MediaTrackDTO mediaTrackDTO = event.getValue();
                        addedMediaTrack.onNext(mediaTrackDTO);
                    }).onEntryRemoved(event -> {
                        MediaTrackDTO mediaTrackDTO = event.getOldValue();
                        removedMediaTrack.onNext(mediaTrackDTO);
                    }).onEntryExpired(event -> {
                        MediaTrackDTO mediaTrackDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.clientMaxIdleTime).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<MediaTrackDTO>make(mediaTrackDTO, estimatedLastTouch);
                        expiredMediaTrack.onNext(forwardedEvent);
                    });
                });

        this.add(
                "Sfu Events",
                this.hazelcastMaps.getSFUs(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        SfuDTO sfuDTO = event.getValue();
                        addedSfu.onNext(sfuDTO);
                    }).onEntryRemoved(event -> {
                        SfuDTO sfuDTO = event.getValue();
                        removedSfu.onNext(sfuDTO);
                    }).onEntryExpired(event -> {
                        SfuDTO sfuDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.sfuMaxIdleTime).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<SfuDTO>make(sfuDTO, estimatedLastTouch);
                        expiredSfu.onNext(forwardedEvent);
                    });
                });

        this.add(
                "Sfu Transport Events",
                this.hazelcastMaps.getSFUTransports(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        SfuTransportDTO sfuTransportDTO = event.getValue();
                        addedSfuTransport.onNext(sfuTransportDTO);
                    }).onEntryRemoved(event -> {
                        SfuTransportDTO sfuTransportDTO = event.getValue();
                        removedSfuTransport.onNext(sfuTransportDTO);
                    }).onEntryUpdated(event -> {
                        SfuTransportDTO oldSfuTransportDTO = event.getOldValue();
                        SfuTransportDTO newSfuTransportDTO = event.getValue();
                        updatedSfuTransport.onNext(RepositoryUpdatedEvent.make(oldSfuTransportDTO, newSfuTransportDTO));
                    }).onEntryExpired(event -> {
                        SfuTransportDTO sfuTransportDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.sfuTransportMaxIdleTime).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<SfuTransportDTO>make(sfuTransportDTO, estimatedLastTouch);
                        expiredSfuTransport.onNext(forwardedEvent);
                    });
                });

        this.add(
                "Sfu Rtp Pad Events",
                this.hazelcastMaps.getSFURtpPads(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        SfuRtpPadDTO sfuRtpPadDTO = event.getValue();
                        addedSfuRtpPad.onNext(sfuRtpPadDTO);
                    }).onEntryRemoved(event -> {
                        SfuRtpPadDTO sfuRtpPadDTO = event.getValue();
                        removedSfuRtpPad.onNext(sfuRtpPadDTO);
                    }).onEntryUpdated(event -> {
                        SfuRtpPadDTO oldSfuRtpPadDTO = event.getOldValue();
                        SfuRtpPadDTO newSfuRtpPadDTO = event.getValue();
                        updatedSfuRtpPad.onNext(RepositoryUpdatedEvent.make(oldSfuRtpPadDTO, newSfuRtpPadDTO));
                    }).onEntryExpired(event -> {
                        var sfuRtpPadDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.sfuRtpPadMaxIdleTime).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<SfuRtpPadDTO>make(sfuRtpPadDTO, estimatedLastTouch);
                        expiredSfuRtpPad.onNext(forwardedEvent);
                    });
                });

    }

    @PreDestroy
    void teardown() {
        this.destructors.forEach(destructor -> {
            destructor.run();
        });
    }

    public Observable<List<CallDTO>> addedCalls() {
        var result = this.getObservableList(this.addedCalls);
        return result;
    }


    public Observable<List<CallDTO>> removedCalls() {
        var result= this.getObservableList(this.removedCalls);
        return result;
    }

    public Observable<List<ClientDTO>> addedClients() {
        var result = this.getObservableList(this.addedClient);
        return result;
    }

    public Observable<List<RepositoryExpiredEvent<ClientDTO>>> expiredClients() {
        var result= this.getObservableList(this.expiredClient);
        return result;
    }

    public Observable<List<ClientDTO>> removedClients() {
        var result= this.getObservableList(this.removedClient);
        return result;
    }

    public Observable<List<PeerConnectionDTO>> addedPeerConnection() {
        var result = this.getObservableList(this.addedPeerConnection);
        return result;
    }

    public Observable<List<RepositoryExpiredEvent<PeerConnectionDTO>>> expiredPeerConnection() {
        var result= this.getObservableList(this.expiredPeerConnection);
        return result;
    }

    public Observable<List<PeerConnectionDTO>> removedPeerConnection() {
        var result= this.getObservableList(this.removedPeerConnection);
        return result;
    }

    public Observable<List<MediaTrackDTO>> addedMediaTracks() {
        var result = this.getObservableList(this.addedMediaTrack);
        return result;
    }

    public Observable<List<RepositoryExpiredEvent<MediaTrackDTO>>> expiredMediaTracks() {
        var result= this.getObservableList(this.expiredMediaTrack);
        return result;
    }

    public Observable<List<MediaTrackDTO>> removedMediaTracks() {
        var result = this.getObservableList(this.removedMediaTrack);
        return result;
    }


    public Observable<List<SfuRtpPadDTO>> addedSfuRtpPads() {
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

    public Observable<List<RepositoryExpiredEvent<SfuDTO>>> expiredSfu() {
        var result= this.getObservableList(this.expiredSfu);
        return result;
    }

    public Observable<List<SfuDTO>> removedSfu() {
        var result= this.getObservableList(this.removedSfu);
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

    private<T> Observable<List<T>> getObservableList(Observable<T> source) {
        int eventsCollectingTimeInS = this.observerConfig.repositories.eventsCollectingTimeInS;
        if (eventsCollectingTimeInS < 1) {
            return source.map(List::of);
        }
        return source.buffer(eventsCollectingTimeInS, TimeUnit.SECONDS);
    }
}
