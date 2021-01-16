package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.models.ICEConnectionEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.ICEConnectionsRepository;
import org.observertc.webrtc.schemas.reports.*;
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
    private Subject<Report> ICECandidatePairs = PublishSubject.create();
    private Subject<Report> ICERemoteCandidates = PublishSubject.create();
    private Subject<Report> ICELocalCandidates = PublishSubject.create();
    private Subject<ICECandidatePairUpdate> expiredICECandidatePairs = PublishSubject.create();

    private Subject<ICEConnectionEntity> newICEConnections = PublishSubject.create();
    private Subject<ICEConnectionEntity> updateICEConnections = PublishSubject.create();

    private Map<String, Report> localCandidates = new HashMap<>();
    private Map<String, Report> remoteCandidates = new HashMap<>();
    private Map<String, ICECandidatePairUpdate> candidatePairUpdates = new HashMap<>();
    private Instant lastCleaned = Instant.now();

    public Observer<Report> getICECandidatePairs() {
        return this.ICECandidatePairs;
    }

    public Observer<Report> getICELocalCandidates() {
        return this.ICELocalCandidates;
    }

    public Observer<Report> getICERemoteCandidates() {
        return this.ICERemoteCandidates;
    }

    public Observable<ICEConnectionEntity> getObservableNewICEConnection() {
        return this.newICEConnections;
    }
    public Observable<ICEConnectionEntity> getObservableUpdatedICEConnection() {
        return this.updateICEConnections;
    }

    public Observable<ICECandidatePairUpdate> getObservableExpiredICECandidatePairUpdates() {
        return this.expiredICECandidatePairs;
    }

    public ICEConnectionObserver() {
        this.ICECandidatePairs
                .filter(this::reportFilter)
                .filter(report -> report.getType().equals(ReportType.ICE_CANDIDATE_PAIR))
                .subscribe(this::evaluateCandidatePairReport);
        this.ICERemoteCandidates
                .filter(this::reportFilter)
                .filter(report -> report.getType().equals(ReportType.ICE_REMOTE_CANDIDATE))
                .subscribe(this::evaluateICERemoteCandidateReport);
        this.ICELocalCandidates
                .filter(this::reportFilter)
                .filter(report -> report.getType().equals(ReportType.ICE_LOCAL_CANDIDATE))
                .subscribe(this::evaluateICELocalCandidateReport);
    }

    private boolean reportFilter (Report report) {
        if (Objects.isNull(report)) {
            return false;
        }
        if (Objects.isNull(report.getPayload())) {
            return false;
        }
        return true;
    }

    private void evaluateICERemoteCandidateReport(@NotNull Report iceRemoteCandidateReport) {
        ICERemoteCandidate iceRemoteCandidate = (ICERemoteCandidate) iceRemoteCandidateReport.getPayload();
        String remoteCandidateId = iceRemoteCandidate.getCandidateId();
        this.remoteCandidates.put(remoteCandidateId, iceRemoteCandidateReport);
//        logger.info("remote candidate is arrived {} {}", remoteCandidateId, iceRemoteCandidateReport);
    }

    private void evaluateICELocalCandidateReport(@NotNull Report iceLocalCandidateReport) {
        ICELocalCandidate iceLocalCandidate = (ICELocalCandidate) iceLocalCandidateReport.getPayload();
        String localCandidateId = iceLocalCandidate.getCandidateId();
        this.localCandidates.put(localCandidateId, iceLocalCandidateReport);
//        logger.info("local candidate is arrived {} {}", localCandidateId, iceLocalCandidateReport);
    }

    private void evaluateCandidatePairReport(@NotNull Report iceCandidatePairReport) {
        ICECandidatePair iceCandidatePair = (ICECandidatePair) iceCandidatePairReport.getPayload();
        boolean doProcess = true;
        Report iceRemoteCandidateReport = this.remoteCandidates.get(iceCandidatePair.getRemoteCandidateID());
//        logger.info("candidate pair is arrived {} {}", iceCandidatePair.getLocalCandidateID(), iceCandidatePair.getRemoteCandidateID());
        if (Objects.isNull(iceRemoteCandidateReport)) {
            doProcess = false;
        }

        Report iceLocalCandidateReport = this.localCandidates.get(iceCandidatePair.getLocalCandidateID());
        if (Objects.isNull(iceLocalCandidateReport)) {
            doProcess = false;
        }

        String key = ICEConnectionsRepository.getKey(
                UUID.fromString(iceCandidatePair.getPeerConnectionUUID()),
                iceCandidatePair.getLocalCandidateID(),
                iceCandidatePair.getRemoteCandidateID()
        );
        ICECandidatePairUpdate update = this.candidatePairUpdates.get(key);
        if (Objects.isNull(update)) {
            Optional<UUID> serviceUUIDHolder = UUIDAdapter.tryParse(iceCandidatePairReport.getServiceUUID());
            Optional<UUID> pcUUIDHolder = UUIDAdapter.tryParse(iceCandidatePairReport.getServiceUUID());
            update = ICECandidatePairUpdate.of(
                    iceCandidatePair.getLocalCandidateID(),
                    iceCandidatePair.getRemoteCandidateID(),
                    serviceUUIDHolder.orElse(null),
                    pcUUIDHolder.orElse(null),
                    iceCandidatePair.getMediaUnitId()
            );
            this.candidatePairUpdates.put(key, update);
        }
        Instant now = Instant.now();

        if (doProcess || (update.processed && 20 < Duration.between(update.updated, now).getSeconds())) {
//            this.process(iceLocalCandidateReport, iceRemoteCandidateReport, iceCandidatePairReport);
            ICEConnectionEntity iceConnectionEntity = this.makeICEConnectionEntity(iceLocalCandidateReport, iceRemoteCandidateReport, iceCandidatePairReport);
            this.localCandidates.remove(iceCandidatePair.getLocalCandidateID());
            this.remoteCandidates.remove(iceCandidatePair.getRemoteCandidateID());
            if (!update.processed) {
                this.newICEConnections.onNext(iceConnectionEntity);
            } else {
                this.updateICEConnections.onNext(iceConnectionEntity);
            }
            update.processed = true;
            update.updated = now;
        }

        if (Objects.isNull(update.updated)) {
            update.updated = now;
        }


        this.clean();
    }

    private ICEConnectionEntity makeICEConnectionEntity(Report iceLocalCandidateReport, Report iceRemoteCandidateReport, @NotNull Report iceCandidatePairReport) {
        ICECandidatePair iceCandidatePair = (ICECandidatePair) iceCandidatePairReport.getPayload();
        CandidateType localCandidateType = null, remoteCandidateType = null;
        if (Objects.nonNull(iceLocalCandidateReport)) {
            ICELocalCandidate iceLocalCandidate = (ICELocalCandidate) iceLocalCandidateReport.getPayload();
            localCandidateType = iceLocalCandidate.getCandidateType();
        }
        if (Objects.nonNull(iceRemoteCandidateReport)) {
            ICERemoteCandidate iceRemoteCandidate = (ICERemoteCandidate) iceRemoteCandidateReport.getPayload();
            remoteCandidateType = iceRemoteCandidate.getCandidateType();
        }

        UUID serviceUUID = UUID.fromString(iceCandidatePairReport.getServiceUUID());
        UUID pcUUID = UUID.fromString(iceCandidatePair.getPeerConnectionUUID());
        String mediaUnitId = iceCandidatePair.getMediaUnitId();
        boolean nominated = false;
        if (Objects.nonNull(iceCandidatePair.getNominated())) {
            nominated = iceCandidatePair.getNominated();
        }
        ICEConnectionEntity result = ICEConnectionEntity.of(
                serviceUUID,
                mediaUnitId,
                pcUUID,
                iceCandidatePair.getLocalCandidateID(),
                iceCandidatePair.getRemoteCandidateID(),
                localCandidateType,
                remoteCandidateType,
                nominated,
                iceCandidatePair.getState()
        );

        return result;
    }

    private void clean() {
        Instant now = Instant.now();
        if (Duration.between(this.lastCleaned, now).getSeconds() < 60) {
            return;
        }
        this.cleanCandidates(this.localCandidates);
        this.cleanCandidates(this.remoteCandidates);
        this.cleanUpdates();
        this.lastCleaned = now;
    }

    private void cleanCandidates(Map<String, Report> candidates) {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Report>> it = candidates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Report> entry = it.next();
            Report report = entry.getValue();
            Instant timestamp = Instant.ofEpochMilli(report.getTimestamp());
            if (30 < Duration.between(timestamp, now).getSeconds()) {
                switch (report.getType()) {
                    case ICE_LOCAL_CANDIDATE:
                        ICELocalCandidate localCandidate = (ICELocalCandidate) report.getPayload();
                        logger.warn("A local candidate is stucked. localId: {}", localCandidate.getCandidateId());
                        break;
                    case ICE_REMOTE_CANDIDATE:
                        ICERemoteCandidate remoteCandidate = (ICERemoteCandidate) report.getPayload();
                        logger.warn("A remote candidate is stucked. remoteId: {}", remoteCandidate.getCandidateId());
                        break;
                }
                it.remove();
            }
        }
    }

    private void cleanUpdates() {
        List<ICECandidatePairUpdate> expiredICEConnections = new LinkedList<>();
        Instant now = Instant.now();
        Iterator<Map.Entry<String, ICECandidatePairUpdate>> it = this.candidatePairUpdates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ICECandidatePairUpdate> entry = it.next();
            ICECandidatePairUpdate update = entry.getValue();
            if (Duration.between(update.updated, now).getSeconds() < 30) {
                continue;
            }
            if (!update.processed) {
                logger.info("ICECandidatePairUpdate ({}) is expired and it " +
                        "was never been processed, so no Connection has been registered in the database",
                        ObjectToString.toString(update));
                it.remove();
                continue;
            }

            expiredICEConnections.add(update);
            it.remove();
        }
        if (expiredICEConnections.size() < 1) {
            return;
        }
        if (!this.expiredICECandidatePairs.hasObservers()) {
            logger.info("No subscriber for expiredICECandidatePairs output, expired ICEConnections cannot be reported though");
            return;
        }
        try {
            expiredICEConnections.stream().forEach(this.expiredICECandidatePairs::onNext);
        } catch (Throwable t) {
            logger.error("Unexpected exception occurred at " + this.getClass().getSimpleName(), t);
        }
    }

}
