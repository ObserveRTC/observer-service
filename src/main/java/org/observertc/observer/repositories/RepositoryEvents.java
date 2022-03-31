package org.observertc.observer.repositories;

import com.hazelcast.map.IMap;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.observer.common.ClientMessageEvent;
import org.observertc.observer.common.ObservablePassiveCollector;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class RepositoryEvents {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryEvents.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ObserverConfig observerConfig;

    private ObservablePassiveCollector<CallDTO> addedCalls = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<CallDTO> removedCalls = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<ClientDTO> addedClient = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryExpiredEvent<ClientDTO>> expiredClient = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<ClientDTO> removedClient = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<SfuStreamDTO> addedSfuStreams = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryUpdatedEvent<SfuStreamDTO>> updatedSfuStreams = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<SfuStreamDTO> removedSfuStreams = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<SfuSinkDTO> addedSfuSinks = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryUpdatedEvent<SfuSinkDTO>> updatedSfuSinks = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<SfuSinkDTO> removedSfuSinks = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<PeerConnectionDTO> addedPeerConnection = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryExpiredEvent<PeerConnectionDTO>> expiredPeerConnection = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<PeerConnectionDTO> removedPeerConnection = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<MediaTrackDTO> addedMediaTrack = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryExpiredEvent<MediaTrackDTO>> expiredMediaTrack = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<MediaTrackDTO> removedMediaTrack = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<SfuRtpPadDTO> addedSfuRtpPad = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryExpiredEvent<SfuRtpPadDTO>> expiredSfuRtpPad = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryUpdatedEvent<SfuRtpPadDTO>> updatedSfuRtpPad = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<SfuRtpPadDTO> removedSfuRtpPad = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<SfuTransportDTO> addedSfuTransport = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryExpiredEvent<SfuTransportDTO>> expiredSfuTransport = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryUpdatedEvent<SfuTransportDTO>> updatedSfuTransport = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<SfuTransportDTO> removedSfuTransport = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<SfuDTO> addedSfu = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<RepositoryExpiredEvent<SfuDTO>> expiredSfu = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<SfuDTO> removedSfu = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<RepositoryUpdatedEvent<ClientMessageEvent>> updatedClientMessages = ObservablePassiveCollector.create();
    private ObservablePassiveCollector<ClientMessageEvent> removedClientMessages = ObservablePassiveCollector.create();

    private ObservablePassiveCollector<UUID> expiredIncompleteSfuRtpPadIds = ObservablePassiveCollector.create();


    private List<Runnable> destructors = new LinkedList<>();
    private Disposable timer = null;
    private Map<UUID, Runnable> debouncers = new ConcurrentHashMap<>();


    public RepositoryEvents() {

    }

    @PostConstruct
    void setup() {
        var debounceConfig = this.observerConfig.buffers.debouncers;
        var debounceTimeInMs = debounceConfig.maxTimeInMs;
        if (debounceTimeInMs < 1) {
            debounceTimeInMs = 1000;
            logger.info("No debounce time is given, but {} must have one, so it uses a default 1000ms", this.getClass().getSimpleName());
        }
        this.timer = Observable.interval(debounceTimeInMs, debounceTimeInMs, TimeUnit.MILLISECONDS).subscribe(counter -> {
            this.debouncers.values().stream().forEach(timeChecker -> {
                try {
                    timeChecker.run();
                } catch (Exception ex) {
                    logger.warn("Error occurred while running timeChecker");
                }
            });
        });

        ObserverConfig.RepositoryConfig repositoryConfig = this.observerConfig.repository;
        this.add(
                "Call DTO Storage Events",
                this.hazelcastMaps.getCalls(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        CallDTO callDTO = event.getValue();
                        addedCalls.add(callDTO);
                    }).onEntryRemoved(event -> {
                        CallDTO callDTO= event.getOldValue();
                        removedCalls.add(callDTO);
                    });
                });

        this.add(
                "Client DTO Storage Events",
                this.hazelcastMaps.getClients(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        ClientDTO clientDTO = event.getValue();
                        addedClient.add(clientDTO);
                    }).onEntryRemoved(event -> {
                        ClientDTO clientDTO = event.getOldValue();
                        removedClient.add(clientDTO);
                    }).onEntryExpired(event -> {
                        ClientDTO clientDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.clientMaxIdleTimeInS).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<ClientDTO>make(clientDTO, estimatedLastTouch);
                        expiredClient.add(forwardedEvent);
                    });
                });

        this.add(
                "Peer Connection DTO Related Events",
                this.hazelcastMaps.getPeerConnections(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        PeerConnectionDTO peerConnectionDTO = event.getValue();
                        addedPeerConnection.add(peerConnectionDTO);
                    }).onEntryRemoved(event -> {
                        PeerConnectionDTO peerConnectionDTO = event.getOldValue();
                        removedPeerConnection.add(peerConnectionDTO);
                    }).onEntryExpired(event -> {
                        PeerConnectionDTO peerConnectionDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.clientMaxIdleTimeInS).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<PeerConnectionDTO>make(peerConnectionDTO, estimatedLastTouch);
                        expiredPeerConnection.add(forwardedEvent);
                    });
                });

        this.add(
                "Media Track DTO Storage Events",
                this.hazelcastMaps.getMediaTracks(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        MediaTrackDTO mediaTrackDTO = event.getValue();
                        addedMediaTrack.add(mediaTrackDTO);
                    }).onEntryRemoved(event -> {
                        MediaTrackDTO mediaTrackDTO = event.getOldValue();
                        removedMediaTrack.add(mediaTrackDTO);
                    }).onEntryExpired(event -> {
                        MediaTrackDTO mediaTrackDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.clientMaxIdleTimeInS).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<MediaTrackDTO>make(mediaTrackDTO, estimatedLastTouch);
                        expiredMediaTrack.add(forwardedEvent);
                    });
                });

        this.add(
                "Sfu DTO Storage Events",
                this.hazelcastMaps.getSFUs(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        SfuDTO sfuDTO = event.getValue();
                        addedSfu.add(sfuDTO);
                    }).onEntryRemoved(event -> {
                        SfuDTO sfuDTO = event.getOldValue();
                        removedSfu.add(sfuDTO);
                    }).onEntryExpired(event -> {
                        SfuDTO sfuDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.sfuMaxIdleTimeInS).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<SfuDTO>make(sfuDTO, estimatedLastTouch);
                        expiredSfu.add(forwardedEvent);
                    });
                });

        this.add(
                "Sfu Transport DTO Storage Events",
                this.hazelcastMaps.getSFUTransports(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        SfuTransportDTO sfuTransportDTO = event.getValue();
                        addedSfuTransport.add(sfuTransportDTO);
                    }).onEntryRemoved(event -> {
                        SfuTransportDTO sfuTransportDTO = event.getOldValue();
                        removedSfuTransport.add(sfuTransportDTO);
                    }).onEntryUpdated(event -> {
                        SfuTransportDTO oldSfuTransportDTO = event.getOldValue();
                        SfuTransportDTO newSfuTransportDTO = event.getValue();
                        updatedSfuTransport.add(RepositoryUpdatedEvent.make(oldSfuTransportDTO, newSfuTransportDTO));
                    }).onEntryExpired(event -> {
                        SfuTransportDTO sfuTransportDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.sfuTransportMaxIdleTimeInS).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<SfuTransportDTO>make(sfuTransportDTO, estimatedLastTouch);
                        expiredSfuTransport.add(forwardedEvent);
                    });
                });

        this.add(
                "Sfu Rtp Pad DTO Events",
                this.hazelcastMaps.getSFURtpPads(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        SfuRtpPadDTO sfuRtpPadDTO = event.getValue();
                        addedSfuRtpPad.add(sfuRtpPadDTO);
                    }).onEntryRemoved(event -> {
                        SfuRtpPadDTO sfuRtpPadDTO = event.getOldValue();
                        removedSfuRtpPad.add(sfuRtpPadDTO);
                    }).onEntryUpdated(event -> {
                        SfuRtpPadDTO oldSfuRtpPadDTO = event.getOldValue();
                        SfuRtpPadDTO newSfuRtpPadDTO = event.getValue();
                        updatedSfuRtpPad.add(RepositoryUpdatedEvent.make(oldSfuRtpPadDTO, newSfuRtpPadDTO));
                    }).onEntryExpired(event -> {
                        var sfuRtpPadDTO = event.getOldValue();
                        var estimatedLastTouch = Instant.now().minusSeconds(repositoryConfig.sfuRtpPadMaxIdleTimeInS).toEpochMilli();
                        var forwardedEvent = RepositoryExpiredEvent.<SfuRtpPadDTO>make(sfuRtpPadDTO, estimatedLastTouch);
                        expiredSfuRtpPad.add(forwardedEvent);
                    });
                });

        this.add(
                "Sfu Stream Related Events",
                this.hazelcastMaps.getSfuStreams(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        var subject = event.getValue();
                        addedSfuStreams.add(subject);
                    }).onEntryRemoved(event -> {
                        var subject = event.getOldValue();
                        removedSfuStreams.add(subject);
                    }).onEntryUpdated(event -> {
                        var forwardedEvent = RepositoryUpdatedEvent.make(event.getOldValue(), event.getValue());
                        updatedSfuStreams.add(forwardedEvent);
                    });
                });

        this.add(
                "Sfu Sink Related Events",
                this.hazelcastMaps.getSfuSinks(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        var subject = event.getValue();
                        addedSfuSinks.add(subject);
                    }).onEntryRemoved(event -> {
                        var subject = event.getOldValue();
                        removedSfuSinks.add(subject);
                    }).onEntryUpdated(event -> {
                        var forwardedEvent = RepositoryUpdatedEvent.make(event.getOldValue(), event.getValue());
                        updatedSfuSinks.add(forwardedEvent);
                    });
                });

        this.add(
                "General Client Message Event",
                this.hazelcastMaps.getGeneralEntries(),
                builder -> {
                    builder.onEntryUpdated(event -> {
                        UUID clientId = event.getKey();
                        GeneralEntryDTO oldEntryDTO = event.getOldValue();
                        GeneralEntryDTO newEntryDTO = event.getValue();
                        var oldMessage = ClientMessageEvent.of(clientId, oldEntryDTO);
                        var newMessage = ClientMessageEvent.of(clientId, newEntryDTO);
                        var forwardedEvent = RepositoryUpdatedEvent.make(oldMessage, newMessage);
                        updatedClientMessages.add(forwardedEvent);
                    });
                });
    }

    @PreDestroy
    void teardown() {
        if (Objects.nonNull(this.timer)) {
            this.timer.dispose();
        }
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

    private<T> Observable<List<T>> getObservableList(ObservablePassiveCollector<T> source) {
        if (!this.debouncers.containsKey(source.getId())) {
            var maxTimeInMs = this.observerConfig.buffers.debouncers.maxTimeInMs;
            var maxItems = this.observerConfig.buffers.debouncers.maxItems;
            Runnable debouncer = () -> {
                var collectingTimeInsMs = source.getCollectingTimeInMs();
                if (0 < maxTimeInMs && maxTimeInMs <= collectingTimeInsMs) {
                    source.flush();
                    return;
                }
                if (0 < maxItems && maxItems < source.getCollectedNumberOfItems()) {
                    source.flush();
                    return;
                }
            };
            this.debouncers.put(source.getId(), debouncer);
        }
        return source.observableEmittedItems();
    }
}
