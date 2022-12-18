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
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class SfuEventReportsAdder {

    private static final Logger logger = LoggerFactory.getLogger(SfuEventReportsAdder.class);

    private final Subject<List<Report>> input = PublishSubject.create();
    private final Subject<List<Report>> output = PublishSubject.create();
    private final List<Report> collectedReports = new LinkedList<>();

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    SfusRepository sfusRepository;

    @Inject
    SfuTransportsRepository sfuTransportsRepository;

    @Inject
    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    @Inject
    SfuSctpChannelsRepository sfuSctpStreamsRepository;

    @Inject
    SfuJoinedReports sfuJoinedReports;

    @Inject
    SfuLeftReports sfuLeftReports;

    @Inject
    SfuTransportOpenedReports sfuTransportOpenedReports;

    @Inject
    SfuTransportClosedReports sfuTransportClosedReports;

    @Inject
    SfuInboundRtpPadAddedReports sfuInboundRtpPadAddedReports;

    @Inject
    SfuInboundRtpPadRemovedReports sfuInboundRtpPadRemovedReports;

    @Inject
    SfuOutboundRtpPadAddedReports sfuOutboundRtpPadAddedReports;

    @Inject
    SfuOutboundRtpPadRemovedReports sfuOutboundRtpPadRemovedReports;

    @Inject
    SfuSctpStreamAddedReports sfuSctpStreamAddedReports;

    @Inject
    SfuSctpStreamRemovedReports sfuSctpStreamRemovedReports;

    @PostConstruct
    void setup() {

        this.repositoryEvents.expiredSfu()
                .subscribe(sfuModels -> this.processExpiredSfuModels(sfuModels));

        this.repositoryEvents.deletedSfu()
                .subscribe(sfuModels -> this.sfuLeftReports.accept(sfuModels));

        this.repositoryEvents.expiredSfuTransports()
                .subscribe(sfuTransportModels -> this.processExpiredSfuTransportModels(sfuTransportModels));

        this.repositoryEvents.deletedSfuTransports()
                .subscribe(sfuTransportModels -> this.sfuTransportClosedReports.accept(sfuTransportModels));

        this.repositoryEvents.deletedSfuInboundRtpPads()
                .subscribe(this.sfuInboundRtpPadRemovedReports::accept);

        this.repositoryEvents.deletedSfuOutboundRtpPads()
                .subscribe(this.sfuOutboundRtpPadRemovedReports::accept);

        this.repositoryEvents.deletedSfuSctpChannel()
                .subscribe(this.sfuSctpStreamRemovedReports::accept);

        this.sfuJoinedReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.sfuLeftReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.sfuTransportOpenedReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.sfuTransportClosedReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.sfuInboundRtpPadAddedReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.sfuInboundRtpPadRemovedReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.sfuOutboundRtpPadAddedReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.sfuOutboundRtpPadRemovedReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.sfuSctpStreamAddedReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.sfuSctpStreamRemovedReports.getOutput()
                .subscribe(this::collectSfuEventReports);

        this.input.subscribe(incomingReports -> {
            var forwardedReports = new LinkedList<Report>();
            if (incomingReports != null && 0 < incomingReports.size()) {
                forwardedReports.addAll(incomingReports);
            }
            synchronized (this) {
                forwardedReports.addAll(this.collectedReports);
                this.collectedReports.clear();
            }
            this.output.onNext(forwardedReports);
        });
    }

    @PostConstruct
    void teardown() {
    }

    public Observer<List<Report>> reportsObserver() {
        return this.input;
    }

    public Observable<List<Report>> observableReports() {
        return this.output;
    }

    private void collectSfuEventReports(List<SfuEventReport> sfuEventReports) {
        if (sfuEventReports.size() < 1) {
            return;
        }
        var reports = sfuEventReports.stream().map(Report::fromSfuEventReport).collect(Collectors.toList());
        synchronized (this) {
            this.collectedReports.addAll(reports);
        }
    }

    private void processExpiredSfuModels(List<Models.Sfu> expiredSfuModels) {
        if (expiredSfuModels == null || expiredSfuModels.size() < 1) {
            return;
        }
        var sfuTransportIds = expiredSfuModels.stream()
                .filter(m -> 0 < m.getSfuTransportIdsCount())
                .flatMap(m -> m.getSfuTransportIdsList().stream())
                .collect(Collectors.toSet());
        this.sfuTransportsRepository.deleteAll(sfuTransportIds);
        this.sfuLeftReports.accept(expiredSfuModels);
    }

    private void processExpiredSfuTransportModels(List<Models.SfuTransport> expiredSfuTransportModels) {
        if (expiredSfuTransportModels == null || expiredSfuTransportModels.size() < 1) {
            return;
        }
        var sfuInboundRtpPadIds = new HashSet<String>();
        var sfuOutboundRtpPadIds = new HashSet<String>();
        var sfuSctpStreamIds = new HashSet<String>();
        for (var sfuTransportModel : expiredSfuTransportModels) {
            if (0 < sfuTransportModel.getInboundRtpPadIdsCount()) {
                sfuInboundRtpPadIds.addAll(sfuTransportModel.getInboundRtpPadIdsList());
            }
            if (0 < sfuTransportModel.getOutboundRtpPadIdsCount()) {
                sfuOutboundRtpPadIds.addAll(sfuTransportModel.getOutboundRtpPadIdsList());
            }
            if (0 < sfuTransportModel.getSctpChannelIdsCount()) {
                sfuSctpStreamIds.addAll(sfuTransportModel.getSctpChannelIdsList());
            }
        }
        if (0 < sfuInboundRtpPadIds.size()) {
            this.sfuInboundRtpPadsRepository.deleteAll(sfuInboundRtpPadIds);
        }
        if (0 < sfuOutboundRtpPadIds.size()) {
            this.sfuOutboundRtpPadsRepository.deleteAll(sfuOutboundRtpPadIds);
        }
        if (0 < sfuSctpStreamIds.size()) {
            this.sfuSctpStreamsRepository.deleteAll(sfuSctpStreamIds);
        }
        this.sfuTransportClosedReports.accept(expiredSfuTransportModels);
    }
}
