package org.observertc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.ObservableCollector;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.eventreports.*;
import org.observertc.observer.reports.Report;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.schemas.reports.CallEventReport;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
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
    InboundAudioTrackAddedReports inboundAudioTrackAddedReports;

    @Inject
    InboundAudioTrackRemovedReports inboundAudioTrackRemovedReports;

    @Inject
    InboundVideoTrackAddedReports inboundVideoTrackAddedReports;

    @Inject
    InboundVideoTrackRemovedReports inboundVideoTrackRemovedReports;

    @Inject
    OutboundAudioTrackAddedReports outboundAudioTrackAddedReports;

    @Inject
    OutboundAudioTrackRemovedReports outboundAudioTrackRemovedReports;

    @Inject
    OutboundVideoTrackAddedReports outboundVideoTrackAddedReports;

    @Inject
    OutboundVideoTrackRemovedReports outboundVideoTrackRemovedReports;

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
        var maxTimeInMs = observerConfig.buffers.debouncers.maxTimeInMs;
        this.collector = ObservableCollector.<Report>builder()
                .withScheduler(Schedulers.computation())
                .withMaxItems(maxItems)
                .withMaxTimeInMs(maxTimeInMs)
                .build();
    }

    @PostConstruct
    void setup() {

        this.callStartedReports.getOutput()
                .map(List::of)
                .subscribe(this::collectCallEventReports);

        this.callEndedReports.getOutput()
                .map(List::of)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.addedClients()
                .map(this.clientJoinedReports::mapAddedClient)
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

        this.repositoryEvents.addedInboundAudioTrack()
                .map(this.inboundAudioTrackAddedReports::mapAddedInboundAudioTrack)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.removedInboundAudioTrack()
                .map(this.inboundAudioTrackRemovedReports::mapRemovedInboundAudioTrack)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.expiredInboundAudioTrack()
                .map(this.inboundAudioTrackRemovedReports::mapExpiredInboundAudioTrack)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.addedInboundVideoTrack()
                .map(this.inboundVideoTrackAddedReports::mapAddedInboundVideoTrack)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.removedInboundVideoTrack()
                .map(this.inboundVideoTrackRemovedReports::mapRemovedInboundVideoTrack)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.expiredInboundVideoTrack()
                .map(this.inboundVideoTrackRemovedReports::mapExpiredInboundVideoTrack)
                .subscribe(this::collectCallEventReports);


        this.repositoryEvents.addedOutboundAudioTrack()
                .map(this.outboundAudioTrackAddedReports::mapAddedOutboundAudioTrack)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.removedOutboundAudioTrack()
                .map(this.outboundAudioTrackRemovedReports::mapRemovedOutboundAudioTrack)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.expiredOutboundAudioTrack()
                .map(this.outboundAudioTrackRemovedReports::mapExpiredOutboundAudioTrack)
                .subscribe(this::collectCallEventReports);


        this.repositoryEvents.addedOutboundVideoTrack()
                .map(this.outboundVideoTrackAddedReports::mapAddedOutboundVideoTrack)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.removedOutboundVideoTrack()
                .map(this.outboundVideoTrackRemovedReports::mapRemovedOutboundVideoTrack)
                .subscribe(this::collectCallEventReports);

        this.repositoryEvents.expiredOutboundVideoTrack()
                .map(this.outboundVideoTrackRemovedReports::mapExpiredOutboundVideoTrack)
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

        this.repositoryEvents.removedSfuRtpPads()
                .map(this.sfuRtpPadRemovedReports::mapRemovedSfuRtpPad)
                .subscribe(this::collectSfuEventReports);

        this.repositoryEvents.expiredSfuRtpPads()
                .map(this.sfuRtpPadRemovedReports::mapExpiredSfuRtpPad)
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
