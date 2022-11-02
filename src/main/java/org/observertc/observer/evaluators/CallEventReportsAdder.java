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

        this.repositoryEvents.expiredCalls()
                .subscribe(callModels -> this.processRemovedCalls(callModels));

        this.repositoryEvents.deletedCalls()
                .subscribe(callModels -> this.processRemovedCalls(callModels));

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

    private void processRemovedCalls(List<Models.Call> callModels) {
        if (callModels == null || callModels.size() < 1) {
            logger.warn("Expired call models should not be null or less than 1");
            return;
        }
        var clientIds = callModels.stream()
                .filter(callModel -> 0 < callModel.getClientIdsCount())
                .flatMap(callModel -> callModel.getClientIdsList().stream())
                .collect(Collectors.toSet());

        if (0 < clientIds.size()) {
            this.clientsRepository.deleteAll(clientIds);
            this.clientsRepository.save();
        }
        this.callEndedReports.accept(callModels);
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
