package org.observertc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.eventreports.*;
import org.observertc.observer.reports.Report;
import org.observertc.observer.repositories.*;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.CallEventReport;
import org.observertc.schemas.reports.ObserverEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Singleton
public class CallEventReportsAdder {

    private static final Logger logger = LoggerFactory.getLogger(CallEventReportsAdder.class);

    private final Subject<List<Report>> input = PublishSubject.create();
    private final Subject<List<Report>> output = PublishSubject.create();
    private final List<Report> collectedReports = new LinkedList<>();

    @Inject
    ObserverConfig observerConfig;

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    RoomsRepository roomsRepository;

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

    @Inject
    CallSummaryReports callSummaryReports;

    @PostConstruct
    void setup() {

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

        this.callSummaryReports.getOutput()
                .subscribe(this::collectObserverEventReports);

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

    private void collectObserverEventReports(List<ObserverEventReport> observerEventReports) {
        if (observerEventReports.size() < 1) {
            return;
        }
        var reports = observerEventReports.stream().map(Report::fromObserverEventReport).collect(Collectors.toList());
        synchronized (this) {
            this.collectedReports.addAll(reports);
        }
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
        var endedCallIds = callModels.stream().map(Models.Call::getCallId).collect(Collectors.toSet());
        if (this.observerConfig.evaluators.observerReports.createCallSummaryReports && endedCallIds != null && 0 < endedCallIds.size()) {
            // make summary report
        }
        var serviceRoomsToCallIds = callModels.stream().collect(groupingBy(callModel -> ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId())));
        var rooms = Utils.firstNotNull(this.roomsRepository.getAll(serviceRoomsToCallIds.keySet()), Collections.<ServiceRoomId, Room>emptyMap());
        var roomsToDelete = new HashSet<ServiceRoomId>();

        for (var entry : serviceRoomsToCallIds.entrySet()) {
            var serviceRoomId = entry.getKey();
            var callIds = entry.getValue();
            var existingRoom = rooms.get(serviceRoomId);
            if (existingRoom == null) {
                continue;
            }
            if (callIds.contains(existingRoom.getCallId())) {
                roomsToDelete.add(serviceRoomId);
            }
        }
        if (0 < roomsToDelete.size()) {
            var removedRoomIds = this.roomsRepository.removeAll(roomsToDelete);
            Utils.firstNotNull(removedRoomIds, Collections.<ServiceRoomId>emptyList()).stream().forEach(serviceRoomId -> {
                logger.info("Room {} for service {} is removed", serviceRoomId.roomId, serviceRoomId.serviceId);
            });
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
