package org.observertc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.evaluators.eventreports.*;
import org.observertc.observer.reports.Report;
import org.observertc.observer.repositories.*;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class CallEventReportsAdder {

    private static final Logger logger = LoggerFactory.getLogger(CallEventReportsAdder.class);

    private final Subject<List<Report>> input = PublishSubject.create();
    private final Subject<List<Report>> output = PublishSubject.create();
    private final List<Report> collectedReports = new LinkedList<>();

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @Inject
    InboundTracksRepository inboundTracksRepository;

    @Inject
    OutboundTracksRepository outboundTracksRepository;

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
    InboundTrackAddedReports inboundTrackAddedReports;

    @Inject
    InboundTrackRemovedReports inboundTrackRemovedReports;

    @Inject
    OutboundTrackAddedReports outboundTrackAddedReports;

    @Inject
    OutboundTrackRemovedReports outboundTrackRemovedReports;

    @PostConstruct
    void setup() {

        this.repositoryEvents.expiredClients()
                .subscribe(clientModels -> this.processExpiredClients(clientModels));

        this.repositoryEvents.deletedClients()
                .subscribe(clientModels -> this.clientLeftReports.accept(clientModels));

        this.repositoryEvents.deletedPeerConnections()
                .subscribe(pcModels -> this.peerConnectionClosedReports.accept(pcModels));

        this.repositoryEvents.deletedInboundTrack()
                .subscribe(inboundTrackModels -> this.inboundTrackRemovedReports.accept(inboundTrackModels));

        this.repositoryEvents.deletedOutboundTrack()
                .subscribe(outboundTrackModels -> this.outboundTrackRemovedReports.accept(outboundTrackModels));

        this.callStartedReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.callEndedReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.clientJoinedReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.clientLeftReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.peerConnectionOpenedReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.peerConnectionClosedReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.inboundTrackAddedReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.inboundTrackRemovedReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.outboundTrackAddedReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.outboundTrackRemovedReports.getOutput()
                .subscribe(this::collectCallEventReports);

        this.input.subscribe(incomingReports -> {
            var forwardedReports = new LinkedList<Report>();
            if (incomingReports != null && 0 < incomingReports.size()) {
                forwardedReports.addAll(incomingReports);
            }
            this.drainCollectedReports(forwardedReports);
            this.output.onNext(forwardedReports);
        });
    }

    @PreDestroy
    void teardown() {
        this.flush();
    }

    void flush() {
        var remainingReports = new LinkedList<Report>();
        this.drainCollectedReports(remainingReports);
        if (0 < remainingReports.size()) {
            this.output.onNext(remainingReports);
        }
    }

    public Observer<List<Report>> reportsObserver() {
        return this.input;
    }

    public Observable<List<Report>> observableReports() {
        return this.output;
    }

    private void collectCallEventReports(List<CallEventReport> callEventReports) {
        if (callEventReports.size() < 1) {
            return;
        }
        var reports = callEventReports.stream().map(Report::fromCallEventReport).collect(Collectors.toList());
        synchronized (this) {
            this.collectedReports.addAll(reports);
        }
    }

    private void processExpiredClients(List<Models.Client> expiredClientModels) {
        if (expiredClientModels == null || expiredClientModels.size() < 1) {
            logger.warn("Something is wrong");
            return;
        }
        this.clientLeftReports.accept(expiredClientModels);

        var peerConnectionIds = expiredClientModels.stream()
                .filter(model -> 0 < model.getPeerConnectionIdsCount())
                .map(Models.Client::getPeerConnectionIdsList)
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        this.peerConnectionsRepository.fetchRecursively(peerConnectionIds);
        this.peerConnectionsRepository.deleteAll(peerConnectionIds);

        for (var clientModel : expiredClientModels) {
            var serviceRoomId = ServiceRoomId.make(clientModel.getServiceId(), clientModel.getRoomId());
            var call = this.callsRepository.get(serviceRoomId);
            if (call == null) {
                continue;
            }
            call.removeClient(clientModel.getClientId());
        }

        this.callsRepository.save();
        var removedCallModels = this.callsRepository.removeExpiredCalls();
        if (0 < removedCallModels.size()) {
            this.callEndedReports.accept(removedCallModels.values());
        }
    }


    private void drainCollectedReports(List<Report> target) {
        synchronized (this) {
            if (this.collectedReports.size() < 1) {
                return;
            }
            target.addAll(this.collectedReports);
            this.collectedReports.clear();
        }
    }

}
