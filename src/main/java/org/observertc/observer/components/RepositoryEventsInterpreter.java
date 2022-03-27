package org.observertc.observer.components;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.observertc.observer.common.ObservableCollector;
import org.observertc.observer.components.eventreports.*;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.reports.Report;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.observer.repositories.SfuRtpPadEvents;
import org.observertc.schemas.reports.CallEventReport;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Singleton
public class RepositoryEventsInterpreter {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryEventsInterpreter.class);

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    SfuRtpPadEvents sfuRtpPadEvents;

    @Inject
    CallStartedReports callStartedReports;

    @Inject
    CallEndedReports callEndedReports;

    @Inject
    ClientJoinedReports clientJoinedReports;

    @Inject
    ClientLeftReports clientLeftReports;

    @Inject
    PeerConnectionOpenedReports peerConnectionOpenedReports;

    @Inject
    PeerConnectionClosedReports peerConnectionClosedReports;

    @Inject
    MediaTrackAddedReports mediaTrackAddedReports;

    @Inject
    MediaTrackRemovedReports mediaTrackRemovedReports;

    @Inject
    SfuJoinedReports sfuJoinedReports;

    @Inject
    SfuLeftReports sfuLeftReports;

    @Inject
    SfuTransportOpenedReports sfuTransportOpenedReports;

    @Inject
    SfuTransportClosedReports sfuTransportClosedReports;

    @Inject
    SfuRtpPadAddedReports sfuRtpPadAddedReports;

    @Inject
    SfuRtpPadRemovedReports sfuRtpPadRemovedReports;

    private ScheduledFuture<?> scheduler = null;
    private final ObservableCollector<Report> collector;

    public RepositoryEventsInterpreter(
            ObserverConfig observerConfig
    ) {
        var maxItems = observerConfig.buffers.debouncers.maxItems;
        var maxTimeInMs = observerConfig.buffers.debouncers.maxItems;
        this.collector = ObservableCollector.<Report>builder()
                .withScheduler(Schedulers.computation())
                .withMaxItems(maxItems)
                .withMaxTimeInMs(maxTimeInMs)
                .build();

    }

    @PostConstruct
    void setup() {
        this.repositoryEvents.addedCalls()
                .map(this.callStartedReports::mapAddedCalls)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.removedCalls()
                .map(this.callEndedReports::mapCallDTOs)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.addedClients()
                .map(this.clientJoinedReports::mapAddedCalls)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.removedClients()
                .map(this.clientLeftReports::mapRemovedClients)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.expiredClients()
                .map(this.clientLeftReports::mapExpiredClients)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.addedPeerConnection()
                .map(this.peerConnectionOpenedReports::mapAddedPeerConnections)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.removedPeerConnection()
                .map(this.peerConnectionClosedReports::mapRemovedPeerConnections)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.expiredPeerConnection()
                .map(this.peerConnectionClosedReports::mapExpiredPeerConnections)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.addedMediaTracks()
                .map(this.mediaTrackAddedReports::mapAddedMediaTracks)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.removedMediaTracks()
                .map(this.mediaTrackRemovedReports::mapRemovedMediaTracks)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.expiredMediaTracks()
                .map(this.mediaTrackRemovedReports::mapExpiredMediaTracks)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.addedSfu()
                .map(this.sfuJoinedReports::mapSfuDTOs)
                .subscribe(this::collectSfuEventReports);

        this.repositoryEvents.removedSfu()
                .map(this.sfuLeftReports::mapRemovedSfuDTOs)
                .subscribe(this::collectSfuEventReports);

        this.repositoryEvents.expiredSfu()
                .map(this.sfuLeftReports::mapExpiredSfuDTOs)
                .subscribe(this::collectSfuEventReports);

        this.repositoryEvents.addedSfuTransports()
                .map(this.sfuTransportOpenedReports::mapAddedSfuTransport)
                .subscribe(this::collectSfuEventReports);

        this.repositoryEvents.removedSfuTransports()
                .map(this.sfuTransportClosedReports::mapRemovedSfuTransport)
                .subscribe(this::collectSfuEventReports);

        this.repositoryEvents.expiredSfuTransports()
                .map(this.sfuTransportClosedReports::mapExpiredSfuTransport)
                .subscribe(this::collectSfuEventReports);

        this.sfuRtpPadEvents.completedSfuRtpPads()
                .map(this.sfuRtpPadAddedReports::mapCompletedSfuRtpPads)
                .subscribe(this::collectSfuEventReports);

        this.sfuRtpPadEvents.disposedSfuRtpPads()
                .map(this.sfuRtpPadRemovedReports::mapDisposedSfuRtpPads)
                .subscribe(this::collectSfuEventReports);

    }

    @PostConstruct
    void teardown() {
        if (Objects.nonNull(this.scheduler)) {
            try {
                this.scheduler.cancel(true);
            } catch (Throwable t) {
                logger.warn("Unexpected exception while shutting down a scheduler", t);
            }
        }
        this.collector.flush();
    }

    public Observable<List<Report>> observableReports() {
        return this.collector.observableEmittedItems();
    }

    private void collectSfuEventReports(List<SfuEventReport> sfuEventReports) {
        if (sfuEventReports.size() < 1) {
            return;
        }
        var reports = sfuEventReports.stream().map(Report::fromSfuEventReport).collect(Collectors.toList());
        this.collector.addAll(reports);
    }

    private void collectCallEventReports(List<CallEventReport> callEventReports) {
        if (callEventReports.size() < 1) {
            return;
        }
        var reports = callEventReports.stream().map(Report::fromCallEventReport).collect(Collectors.toList());
        this.collector.addAll(reports);
    }
}
