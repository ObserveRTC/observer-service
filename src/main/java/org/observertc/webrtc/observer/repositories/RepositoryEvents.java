package org.observertc.webrtc.observer.repositories;

import com.hazelcast.map.IMap;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
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

    private Subject<MediaTrackDTO> addedMediaTrack = PublishSubject.create();
    private Subject<MediaTrackDTO> removedMediaTrack = PublishSubject.create();
    private Subject<SfuRtpPadDTO> addedSfuRtpPad = PublishSubject.create();
    private Subject<RepositoryUpdateEvent<SfuRtpPadDTO>> updatedSfuRtpPad = PublishSubject.create();
    private Subject<SfuRtpPadDTO> removedSfuRtpPad = PublishSubject.create();

    private List<Runnable> destructors = new LinkedList<>();

    @PostConstruct
    void setup() {
        this.add(
                "Media Track DTO Related Events",
                this.hazelcastMaps.getMediaTracks(),
                builder -> {
                    builder.onEntryAdded(event -> {
                        MediaTrackDTO mediaTrackDTO = event.getValue();
                        addedMediaTrack.onNext(mediaTrackDTO);
                    }).onEntryRemoved(event -> {
                        MediaTrackDTO mediaTrackDTO = event.getValue();
                        removedMediaTrack.onNext(mediaTrackDTO);
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
                        updatedSfuRtpPad.onNext(RepositoryUpdateEvent.make(oldSfuRtpPadDTO, newSfuRtpPadDTO));
                    });
                });
    }

    @PreDestroy
    void teardown() {
        this.destructors.forEach(destructor -> {
            destructor.run();
        });
    }

    public Observable<List<SfuRtpPadDTO>> addedSfuRtpPads() {
        return this.getObservableList(this.addedSfuRtpPad);
    }

    public Observable<List<SfuRtpPadDTO>> removedSfuRtpPads() {
        return this.getObservableList(this.removedSfuRtpPad);
    }

    public Observable<List<RepositoryUpdateEvent<SfuRtpPadDTO>>> updatedSfuRtpPads() {
        return this.getObservableList(this.updatedSfuRtpPad);
    }

    public Observable<List<MediaTrackDTO>> addedMediaTrack() {
        return this.getObservableList(this.addedMediaTrack);
    }

    public Observable<List<MediaTrackDTO>> removedMediaTrack() {
        return this.getObservableList(this.removedMediaTrack);
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
