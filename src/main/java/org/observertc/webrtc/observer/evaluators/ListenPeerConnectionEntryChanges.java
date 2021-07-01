package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.tasks.CreateCallEventReportsTaskProvider;
import org.observertc.webrtc.observer.repositories.tasks.RemovePeerConnectionsTask;
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
public class ListenPeerConnectionEntryChanges implements EntryListener<UUID, PeerConnectionDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ListenPeerConnectionEntryChanges.class);

    private Subject<CallEventReport> callEventReportSubject = PublishSubject.create();

    private Subject<PeerConnectionDTO> addedPeerConnections = PublishSubject.create();
    private Subject<ClosedPeerConnection> removedPeerConnections = PublishSubject.create();

    public Observable<CallEventReport> getObservableCallEventReports() {
        return this.callEventReportSubject;
    }

    @Inject
    CreateCallEventReportsTaskProvider createCallEventReportsTaskProvider;

    @Inject
    Provider<RemovePeerConnectionsTask> removePeerConnectionsTaskProvider;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
        this.addedPeerConnections
                .buffer(55, TimeUnit.SECONDS)
                .subscribe(this::addPeerConnections);

        this.removedPeerConnections
                .buffer(25, TimeUnit.SECONDS)
                .subscribe(this::removePeerConnections);
    }

    @Override
    public void entryAdded(EntryEvent<UUID, PeerConnectionDTO> event) {
        PeerConnectionDTO addedPeerConnectionDTO = event.getValue();
        synchronized (this) {
            this.addedPeerConnections.onNext(addedPeerConnectionDTO);
        }
    }

    @Override
    public void entryEvicted(EntryEvent<UUID, PeerConnectionDTO> event) {
        // ignore this, as we are interested in expired
    }

    @Override
    public void entryExpired(EntryEvent<UUID, PeerConnectionDTO> event) {
        PeerConnectionDTO removedPeerConnectionDTO = event.getOldValue();
        if (Objects.isNull(removedPeerConnectionDTO)) {
            logger.warn("PeerConnection DTO is expired, but the value is null {}", event.toString());
            return;
        }
        Long estimatedLeave = Instant.now().minusSeconds(this.observerConfig.evaluators.clientMaxIdleTime).toEpochMilli();
        ClosedPeerConnection closedPeerConnection = new ClosedPeerConnection(removedPeerConnectionDTO, estimatedLeave);
        synchronized (this) {
            this.removedPeerConnections.onNext(closedPeerConnection);
        }
        logger.debug("PeerConnectionDTO {} has been expired", removedPeerConnectionDTO);
    }

    @Override
    public void entryRemoved(EntryEvent<UUID, PeerConnectionDTO> event) {
        PeerConnectionDTO removedPeerConnectionDTO = event.getValue();
        if (Objects.isNull(removedPeerConnectionDTO)) {
            logger.warn("PeerConnection DTO is removed, but the value is null {}", event.toString());
            return;
        }
        Long estimatedLeave = Instant.now().toEpochMilli();
        ClosedPeerConnection closedPeerConnection = new ClosedPeerConnection(removedPeerConnectionDTO, estimatedLeave);
        synchronized (this) {
            this.removedPeerConnections.onNext(closedPeerConnection);
        }
        logger.debug("PeerConnectionDTO {} has been removed", event.getValue());
    }

    @Override
    public void entryUpdated(EntryEvent<UUID, PeerConnectionDTO> event) {
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

    private void addPeerConnections(List<PeerConnectionDTO> addedPeerConnectionDTOs) {
        if (Objects.isNull(addedPeerConnectionDTOs) || addedPeerConnectionDTOs.size() < 1) {
            return;
        }
        var task = this.createCallEventReportsTaskProvider.getCreatePeerConnectionOpenedReportsTask();
        addedPeerConnectionDTOs.forEach(task::withDTO);

        if (!task.execute().succeeded()) {
            logger.warn("Making add client report has been failed");
            return;
        }

        this.forwardCallReports(
                task.getResult()
        );
    }

    private void removePeerConnections(List<ClosedPeerConnection> input) {
        if (Objects.isNull(input) || input.size() < 1) {
            return;
        }
        var removePeerConnectionsTask = removePeerConnectionsTaskProvider.get();
        input.stream().filter(Objects::nonNull).forEach(closedPeerConnection -> removePeerConnectionsTask.addRemovedPeerConnectionDTO(closedPeerConnection.peerConnectionDTO));
        if (!removePeerConnectionsTask.execute().succeeded()) {
            logger.warn("Remove PeerConnections failed");
            return;
        }

        var task = this.createCallEventReportsTaskProvider.getCreatePeerConnectionClosedReportsTask();
        input.stream().filter(Objects::nonNull).forEach(closedPeerConnection -> task.withDTOAndTimestamp(closedPeerConnection.peerConnectionDTO, closedPeerConnection.estimatedLeave));

        if (!task.execute().succeeded()) {
            logger.warn("Making add client report has been failed");
            return;
        }
        this.forwardCallReports(
                task.getResult()
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

    private class ClosedPeerConnection {
        private final PeerConnectionDTO peerConnectionDTO;
        private final Long estimatedLeave;

        private ClosedPeerConnection(PeerConnectionDTO peerConnectionDTO, Long estimatedLeave) {
            this.peerConnectionDTO = peerConnectionDTO;
            this.estimatedLeave = estimatedLeave;
        }
    }
}
