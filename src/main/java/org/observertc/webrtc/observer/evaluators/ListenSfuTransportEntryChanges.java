package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.SfuTransportDTO;
import org.observertc.webrtc.observer.repositories.tasks.RemoveSfuTransportsTask;
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
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class ListenSfuTransportEntryChanges implements EntryListener<UUID, SfuTransportDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ListenSfuTransportEntryChanges.class);

    private Subject<SfuEventReport> sfuEventReportSubject = PublishSubject.create();

    private Subject<RemovedDTO> removedDTOSubject = PublishSubject.create();

    public Observable<SfuEventReport> getObservableSfuEventReports() {
        return this.sfuEventReportSubject;
    }

    @Inject
    Provider<RemoveSfuTransportsTask> removeSfuTransportsTaskProvider;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
        this.removedDTOSubject
                .buffer(5, TimeUnit.SECONDS)
                .subscribe(this::removeDTOs);
    }

    @Override
    public void entryAdded(EntryEvent<UUID, SfuTransportDTO> event) {
        logger.debug("SfuTransportDTO {} has been added", event.getValue());
    }

    @Override
    public void entryEvicted(EntryEvent<UUID, SfuTransportDTO> event) {
        // ignore this, as we are interested in expired
    }

    @Override
    public void entryExpired(EntryEvent<UUID, SfuTransportDTO> event) {
        SfuTransportDTO DTO = event.getOldValue();
        if (Objects.isNull(DTO)) {
            logger.warn("SfuTransportDTO is expired, but the removed value is null {}", event.toString());
            return;
        }
        Long estimatedLeave = Instant.now().minusSeconds(this.observerConfig.repositories.sfuTransportMaxIdleTime).toEpochMilli();
        RemovedDTO removedDTO = new RemovedDTO(DTO, estimatedLeave);
        synchronized (this) {
            this.removedDTOSubject.onNext(removedDTO);
        }
        logger.debug("SfuTransportDTO {} has been expired", event.getValue());
    }

    @Override
    public void entryRemoved(EntryEvent<UUID, SfuTransportDTO> event) {
        logger.debug("SfuTransportDTO {} has been removed", event.getValue());
    }

    @Override
    public void entryUpdated(EntryEvent<UUID, SfuTransportDTO> event) {
        // ignore this event
    }

    @Override
    public void mapCleared(MapEvent event) {
        logger.info("SfuTransportDTO map has been cleared, {} items are removed", event.getNumberOfEntriesAffected());
    }

    @Override
    public void mapEvicted(MapEvent event) {
        logger.info("SfuTransportDTO map has been evicted, {} items are removed", event.getNumberOfEntriesAffected());
    }

    private void removeDTOs(List<RemovedDTO> input) {
        if (Objects.isNull(input) || input.size() < 1) {
            return;
        }
        var task = removeSfuTransportsTaskProvider.get();
        input.stream().filter(Objects::nonNull).forEach(removedMediaTrack -> task.addRemovedSfuDTO(removedMediaTrack.DTO));
        if (!task.execute().succeeded()) {
            logger.warn("Remove SfuTransportDTO failed");
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
        private final SfuTransportDTO DTO;
        private final Long estimatedLeave;

        private RemovedDTO(SfuTransportDTO DTO, Long estimatedLeave) {
            this.DTO = DTO;
            this.estimatedLeave = estimatedLeave;
        }
    }
}
