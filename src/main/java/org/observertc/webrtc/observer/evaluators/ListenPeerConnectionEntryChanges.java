package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
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
import java.util.stream.Collectors;

/**
 * Responsible to Order appropriate updates on new calls, clients, peer connections
 */
@Singleton
public class ListenPeerConnectionEntryChanges implements EntryListener<UUID, PeerConnectionDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ListenPeerConnectionEntryChanges.class);

    private Subject<CallEventReport> callEventReportSubject = PublishSubject.create();

    private Subject<ClosedPeerConnection> removedPeerConnections = PublishSubject.create();

    public Observable<CallEventReport> getObservableCallEventReports() {
        return this.callEventReportSubject;
    }

    @Inject
    Provider<RemovePeerConnectionsTask> removePeerConnectionsTaskProvider;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
        this.removedPeerConnections
                .buffer(15, TimeUnit.SECONDS)
                .subscribe(this::removePeerConnections);
    }

    @Override
    public void entryAdded(EntryEvent<UUID, PeerConnectionDTO> event) {
        logger.debug("PeerConnectionDTO {} has been added", event.getValue());
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

        var builders = removePeerConnectionsTask.getResult();
        var reports = builders.stream().map(CallEventReport.Builder::build).collect(Collectors.toList());

        this.forwardCallReports(reports);
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
