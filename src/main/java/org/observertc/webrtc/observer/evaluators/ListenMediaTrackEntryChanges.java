package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.repositories.tasks.CreateCallEventReportsTaskProvider;
import org.observertc.webrtc.observer.repositories.tasks.RemoveMediaTracksTask;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class ListenMediaTrackEntryChanges implements EntryListener<UUID, MediaTrackDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ListenMediaTrackEntryChanges.class);

    private Subject<CallEventReport> callEventReportSubject = PublishSubject.create();

    private Subject<MediaTrackDTO> addedMediaTracls = PublishSubject.create();
    private Subject<RemovedMediaTrack> removedMediaTracks = PublishSubject.create();

    public Observable<CallEventReport> getObservableCallEventReports() {
        return this.callEventReportSubject;
    }

    @Inject
    Provider<RemoveMediaTracksTask> removeMediaTracksTaskProvider;

    @Inject
    CreateCallEventReportsTaskProvider createCallEventReportsTaskProvider;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
        this.addedMediaTracls
                .buffer(115, TimeUnit.SECONDS)
                .subscribe(this::addMediaTracks);

        this.removedMediaTracks
                .buffer(10, TimeUnit.SECONDS)
                .subscribe(this::removeMediaTracks);
    }

    @Override
    public void entryAdded(EntryEvent<UUID, MediaTrackDTO> event) {
        MediaTrackDTO addedMediaTrackDTO = event.getValue();
        synchronized (this) {
            this.addedMediaTracls.onNext(addedMediaTrackDTO);
        }
    }

    @Override
    public void entryEvicted(EntryEvent<UUID, MediaTrackDTO> event) {
        // ignore this, as we are interested in expired
    }

    @Override
    public void entryExpired(EntryEvent<UUID, MediaTrackDTO> event) {
        MediaTrackDTO removedMediaTrackDTO = event.getOldValue();
        if (Objects.isNull(removedMediaTrackDTO)) {
            logger.warn("MediaTrack DTO is expired, but the removed value is null {}", event.toString());
            return;
        }
        Long estimatedLeave = Instant.now().minusSeconds(this.observerConfig.evaluators.mediaTracksMaxIdleTime).toEpochMilli();
        RemovedMediaTrack closedPeerConnection = new RemovedMediaTrack(removedMediaTrackDTO, estimatedLeave);
        synchronized (this) {
            this.removedMediaTracks.onNext(closedPeerConnection);
        }
    }

    @Override
    public void entryRemoved(EntryEvent<UUID, MediaTrackDTO> event) {
        logger.info("ClientDTO {} has been removed", event.getValue());
    }

    @Override
    public void entryUpdated(EntryEvent<UUID, MediaTrackDTO> event) {
        // ignore this event
    }

    @Override
    public void mapCleared(MapEvent event) {
        logger.info("Source map has been cleared, {} items are removed", event.getNumberOfEntriesAffected());
    }

    @Override
    public void mapEvicted(MapEvent event) {
        logger.info("Source map has been evicted, {} items are removed", event.getNumberOfEntriesAffected());
    }

    private void addMediaTracks(List<MediaTrackDTO> addedMediaTrackDTOs) {
        if (Objects.isNull(addedMediaTrackDTOs) || addedMediaTrackDTOs.size() < 1) {
            return;
        }
        var task = this.createCallEventReportsTaskProvider.getCreateMediaTrackAddedReportsTask();
        addedMediaTrackDTOs.forEach(task::withDTO);

        if (!task.execute().succeeded()) {
            logger.warn("Making add client report has been failed");
            return;
        }

        this.forwardCallReports(
                task.getResult()
        );
    }

    private void removeMediaTracks(List<RemovedMediaTrack> input) {
        if (Objects.isNull(input) || input.size() < 1) {
            return;
        }
        var removeMediaTracksTask = removeMediaTracksTaskProvider.get();
        input.stream().filter(Objects::nonNull).forEach(removedMediaTrack -> removeMediaTracksTask.addremovedMediaTrackDTO(removedMediaTrack.mediaTrackDTO));
        if (!removeMediaTracksTask.execute().succeeded()) {
            logger.warn("Remove PeerConnections failed");
            return;
        }

        var reportingTask = this.createCallEventReportsTaskProvider.getCreateMediaTrackRemovedReportsTask();
        input.stream().filter(Objects::nonNull).forEach(removedMediaTrack -> reportingTask.withDTOAndTimestamp(removedMediaTrack.mediaTrackDTO, removedMediaTrack.estimatedLeave));

        if (!reportingTask.execute().succeeded()) {
            logger.warn("Making add client report has been failed");
            return;
        }
        this.forwardCallReports(
                reportingTask.getResult()
        );
    }

    private void forwardCallReports(List<CallEventReport> callEventReports) {
        if (Objects.isNull(callEventReports) || callEventReports.size() < 1) {
            return;
        }
        synchronized (this) {
            callEventReports.stream().forEach(this.callEventReportSubject::onNext);
        }
    }

    private class RemovedMediaTrack {
        private final MediaTrackDTO mediaTrackDTO;
        private final Long estimatedLeave;

        private RemovedMediaTrack(MediaTrackDTO mediaTrackDTO, Long estimatedLeave) {
            this.mediaTrackDTO = mediaTrackDTO;
            this.estimatedLeave = estimatedLeave;
        }
    }
}
