package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.SfuRtpStreamPodDTO;
import org.observertc.webrtc.observer.repositories.tasks.RemoveSfuRtpStreamsTask;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
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
import java.util.stream.Collectors;

/**
 * Responsible to Order appropriate updates
 */
@Singleton
public class ListenSfuRtpStreamEntryChanges implements EntryListener<UUID, SfuRtpStreamPodDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ListenSfuRtpStreamEntryChanges.class);

    private Subject<SfuEventReport> sfuEventReportSubject = PublishSubject.create();

    private Subject<RemovedDTO> removedDTOSubject = PublishSubject.create();

    public Observable<SfuEventReport> getObservableSfuEventReports() {
        return this.sfuEventReportSubject;
    }

    @Inject
    Provider<RemoveSfuRtpStreamsTask> removeSfuRtpStreamsTaskProvider;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
        this.removedDTOSubject
                .buffer(5, TimeUnit.SECONDS)
                .subscribe(this::removeDTOs);
    }

    @Override
    public void entryAdded(EntryEvent<UUID, SfuRtpStreamPodDTO> event) {
        logger.debug("SfuRtpStreamDTO {} has been added", event.getValue());
    }

    @Override
    public void entryEvicted(EntryEvent<UUID, SfuRtpStreamPodDTO> event) {
        // ignore this, as we are interested in expired
    }

    @Override
    public void entryExpired(EntryEvent<UUID, SfuRtpStreamPodDTO> event) {
        SfuRtpStreamPodDTO DTO = event.getOldValue();
        if (Objects.isNull(DTO)) {
            logger.warn("SfuRtpStreamDTO is expired, but the removed value is null {}", event.toString());
            return;
        }
        Long estimatedLeave = Instant.now().minusSeconds(this.observerConfig.repositories.sfuTransportMaxIdleTime).toEpochMilli();
        RemovedDTO removedDTO = new RemovedDTO(DTO, estimatedLeave);
        synchronized (this) {
            this.removedDTOSubject.onNext(removedDTO);
        }
        logger.debug("SfuRtpStreamDTO {} has been expired", event.getValue());
    }

    @Override
    public void entryRemoved(EntryEvent<UUID, SfuRtpStreamPodDTO> event) {
        logger.debug("SfuRtpStreamDTO {} has been removed", event.getValue());
    }

    @Override
    public void entryUpdated(EntryEvent<UUID, SfuRtpStreamPodDTO> event) {
        // ignore this event
    }

    @Override
    public void mapCleared(MapEvent event) {
        logger.info("SfuRtpStreamDTO map has been cleared, {} items are removed", event.getNumberOfEntriesAffected());
    }

    @Override
    public void mapEvicted(MapEvent event) {
        logger.info("SfuRtpStreamDTO map has been evicted, {} items are removed", event.getNumberOfEntriesAffected());
    }

    private void removeDTOs(List<RemovedDTO> input) {
        if (Objects.isNull(input) || input.size() < 1) {
            return;
        }
        var task = removeSfuRtpStreamsTaskProvider.get();
        input.stream().filter(Objects::nonNull).forEach(removedMediaTrack -> task.addRemovedSfuRtpStreamPodDTO(removedMediaTrack.DTO));
        if (!task.execute().succeeded()) {
            logger.warn("Remove SfuRtpStreamDTO failed");
            return;
        }

        var builders = task.getResult();
        var reports = builders.stream().map(SfuEventReport.Builder::build).collect(Collectors.toList());

        this.forwardSfuReports(reports);
    }

    private void forwardSfuReports(List<SfuEventReport> sfuEventReports) {
        if (Objects.isNull(sfuEventReports) || sfuEventReports.size() < 1) {
            return;
        }
        synchronized (this) {
            sfuEventReports.stream().forEach(this.sfuEventReportSubject::onNext);
        }
    }

    private class RemovedDTO {
        private final SfuRtpStreamPodDTO DTO;
        private final Long estimatedLeave;

        private RemovedDTO(SfuRtpStreamPodDTO DTO, Long estimatedLeave) {
            this.DTO = DTO;
            this.estimatedLeave = estimatedLeave;
        }
    }
}
