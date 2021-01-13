package org.observertc.webrtc.observer.evaluators.mediaunits;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.ReportRecord;
import org.observertc.webrtc.observer.models.ICEConnectionEntity;
import org.observertc.webrtc.schemas.reports.ICECandidatePair;
import org.observertc.webrtc.schemas.reports.ICELocalCandidate;
import org.observertc.webrtc.schemas.reports.ICERemoteCandidate;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Singleton
public class ICEConnectionObserver {
    private static final Logger logger = LoggerFactory.getLogger(ICEConnectionObserver.class);
    private Subject<ReportRecord> ICECandidatePairs = PublishSubject.create();
    private Subject<ReportRecord> ICERemoteCandidates = PublishSubject.create();
    private Subject<ReportRecord> ICELocalCandidates = PublishSubject.create();

    private Subject<ICEConnectionEntity> iceConnections = PublishSubject.create();

    private Map<String, Report> localCandidates = new HashMap<>();
    private Map<String, Report> remoteCandidates = new HashMap<>();
    private Map<String, Report> missingCandidates = new HashMap<>();
    private Instant lastCleaned = Instant.now();

    public Observer<ReportRecord> getICECandidatePairs() {
        return this.ICECandidatePairs;
    }

    public Observer<ReportRecord> getICELocalCandidates() {
        return this.ICELocalCandidates;
    }

    public Observer<ReportRecord> getICERemoteCandidates() {
        return this.ICERemoteCandidates;
    }

    public Observable<ICEConnectionEntity> getObservableICEConnection() {
        return this.iceConnections;
    }

    public ICEConnectionObserver() {
        this.ICECandidatePairs
                .filter(this::reportFilter)
                .map(reportRecord -> reportRecord.value)
                .subscribe(this::evaluateCandidatePairReport);
        this.ICERemoteCandidates
                .filter(this::reportFilter)
                .map(reportRecord -> reportRecord.value)
                .subscribe(this::evaluateICERemoteCandidateReport);
        this.ICELocalCandidates
                .filter(this::reportFilter)
                .map(reportRecord -> reportRecord.value)
                .subscribe(this::evaluateICELocalCandidateReport);
    }

    private boolean reportFilter (ReportRecord reportRecord) {
        if (Objects.isNull(reportRecord)) {
            return false;
        }
        Report report = reportRecord.value;
        ICERemoteCandidate iceRemoteCandidate = (ICERemoteCandidate) report.getPayload();
        if (Objects.isNull(iceRemoteCandidate)) {
            return false;
        }
        return true;
    }

    private void evaluateICERemoteCandidateReport(@NotNull Report iceRemoteCandidateReport) {
        ICERemoteCandidate iceRemoteCandidate = (ICERemoteCandidate) iceRemoteCandidateReport.getPayload();
        if (Objects.isNull(iceRemoteCandidate)) {
            logger.warn("ICERemoteCandidate was null for report {}", iceRemoteCandidateReport);
            return;
        }
        String remoteCandidateId = iceRemoteCandidate.getCandidateId();
        Report iceCandidatePairReport = this.missingCandidates.get(remoteCandidateId);
        if (Objects.isNull(iceCandidatePairReport)) {
            this.remoteCandidates.put(remoteCandidateId, iceRemoteCandidateReport);
            return;
        }
        ICECandidatePair iceCandidatePair = (ICECandidatePair) iceCandidatePairReport.getPayload();

        Report iceLocalCandidateReport = this.localCandidates.get(iceCandidatePair.getRemoteCandidateID());
        if (Objects.isNull(iceLocalCandidateReport)) {
            this.remoteCandidates.put(remoteCandidateId, iceLocalCandidateReport);
            return;
        }

        this.process(iceLocalCandidateReport, iceRemoteCandidateReport, iceCandidatePairReport);
    }

    private void evaluateICELocalCandidateReport(@NotNull Report iceLocalCandidateReport) {
        ICELocalCandidate iceLocalCandidate = (ICELocalCandidate) iceLocalCandidateReport.getPayload();
        if (Objects.isNull(iceLocalCandidate)) {
            logger.warn("ICELocalCandidate was null for report {}", iceLocalCandidateReport);
            return;
        }
        String localCandidateId = iceLocalCandidate.getCandidateId();
        Report iceCandidatePairReport = this.missingCandidates.get(localCandidateId);
        if (Objects.isNull(iceCandidatePairReport)) {
            this.localCandidates.put(localCandidateId, iceLocalCandidateReport);
            return;
        }
        ICECandidatePair iceCandidatePair = (ICECandidatePair) iceCandidatePairReport.getPayload();

        Report iceRemoteCandidateReport = this.remoteCandidates.get(iceCandidatePair.getRemoteCandidateID());
        if (Objects.isNull(iceRemoteCandidateReport)) {
            this.localCandidates.put(localCandidateId, iceLocalCandidateReport);
            return;
        }

        this.process(iceLocalCandidateReport, iceRemoteCandidateReport, iceCandidatePairReport);
    }

    private void evaluateCandidatePairReport(@NotNull Report iceCandidatePairReport) {
        ICECandidatePair iceCandidatePair = (ICECandidatePair) iceCandidatePairReport.getPayload();
        if (Objects.isNull(iceCandidatePair)) {
            logger.warn("ICECandidatePair was null for report {}", iceCandidatePairReport);
            return;
        }
        boolean doProcess = true;
        Report iceRemoteCandidateReport = this.remoteCandidates.get(iceCandidatePair.getRemoteCandidateID());
        if (Objects.isNull(iceRemoteCandidateReport)) {
            this.missingCandidates.put(iceCandidatePair.getRemoteCandidateID(), iceCandidatePairReport);
            doProcess = false;
        }

        Report iceLocalCandidateReport = this.localCandidates.get(iceCandidatePair.getRemoteCandidateID());
        if (Objects.isNull(iceLocalCandidateReport)) {
            this.missingCandidates.put(iceCandidatePair.getLocalCandidateID(), iceCandidatePairReport);
            doProcess = false;
        }

        if (doProcess) {
            this.process(iceLocalCandidateReport, iceRemoteCandidateReport, iceCandidatePairReport);
        }
    }

    private void process(Report iceLocalCandidateReport, Report iceRemoteCandidateReport, Report iceCandidatePairReport) {
        ICECandidatePair iceCandidatePair = (ICECandidatePair) iceCandidatePairReport.getPayload();
        ICERemoteCandidate iceRemoteCandidate = (ICERemoteCandidate) iceRemoteCandidateReport.getPayload();
        ICELocalCandidate iceLocalCandidate = (ICELocalCandidate) iceLocalCandidateReport.getPayload();

        this.localCandidates.remove(iceLocalCandidate.getCandidateId());
        this.remoteCandidates.remove(iceRemoteCandidate.getCandidateId());
        this.missingCandidates.remove(iceCandidatePair.getRemoteCandidateID());
        this.missingCandidates.remove(iceCandidatePair.getLocalCandidateID());

        UUID serviceUUID = UUID.fromString(iceCandidatePairReport.getServiceUUID());
        UUID pcUUID = UUID.fromString(iceCandidatePair.getPeerConnectionUUID());
        String mediaUnitId = iceCandidatePair.getMediaUnitId();
        boolean nominated = false;
        if (Objects.nonNull(iceCandidatePair.getNominated())) {
            nominated = iceCandidatePair.getNominated();
        }
        ICEConnectionEntity iceConnectionEntity = ICEConnectionEntity.of(
                serviceUUID,
                mediaUnitId,
                pcUUID,
                iceCandidatePair.getLocalCandidateID(),
                iceCandidatePair.getRemoteCandidateID(),
                iceLocalCandidate.getCandidateType(),
                iceRemoteCandidate.getCandidateType(),
                nominated,
                iceCandidatePair.getState()
        );

        this.iceConnections.onNext(iceConnectionEntity);
        this.clean();
    }

    private void clean() {
        Instant now = Instant.now();
        if (Duration.between(this.lastCleaned, now).getSeconds() < 60) {
            return;
        }
        this.clean(this.localCandidates);
        this.clean(this.remoteCandidates);
        this.clean(this.missingCandidates);
    }

    private void clean(Map<String, Report> candidates) {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Report>> it = candidates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Report> entry = it.next();
            Report report = entry.getValue();
            Instant timestamp = Instant.ofEpochMilli(report.getTimestamp());
            if (30 < Duration.between(timestamp, now).getSeconds()) {
                logger.warn("A report is stucked in candidates map {}", report);
                it.remove();
            }
        }
    }
}
