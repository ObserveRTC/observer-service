package org.observertc.webrtc.observer.evaluators;

import io.reactivex.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.Connectors;
import org.observertc.webrtc.observer.connector.Connector;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class Pipeline {
    public static final int REPORT_VERSION_NUMBER = 1;
    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);
    private final Subject<Report> reports = PublishSubject.create();

    @Inject
    PCObserver pcObserver;

    @Inject
    ObservedPCSEvaluator observedPCSEvaluator;

    @Inject
    ActivePCsEvaluator activePCsEvaluator;

    @Inject
    NewPCEvaluator newPCEvaluator;

    @Inject
    ExpiredPCsEvaluator expiredPCsEvaluator;

    @Inject
    ICEConnectionObserver iceConnectionObserver;

    @Inject
    ICEConnectionsEvaluator iceConnectionsEvaluator;

    @Inject
    Connectors connectors;

    public void input(ObservedPCS observedPCS) {
        this.pcObserver.onNext(observedPCS);
        this.observedPCSEvaluator.onNext(observedPCS);
    }

    public void inputUserMediaError(ObservedPCS observedPCS) {
        this.observedPCSEvaluator.onNext(observedPCS);
    }

    @PostConstruct
    void setup() {

        // PCObserver -> ActivePCEvaluator
        this.pcObserver.getActivePCs()
                .subscribe(this.activePCsEvaluator);

        // ActivePCsEvaluator -> NewPCsEvaluator
        this.activePCsEvaluator
                .observableNewPeerConnections()
                .subscribe(this.newPCEvaluator);

        // NewPCsEvaluator -> ReportSink
        this.newPCEvaluator.getReports()
                .subscribe(this.reports);


        // PCObserver -> ExpiredPCEvaluator
        this.pcObserver.getExpiredPCs()
                .subscribe(this.expiredPCsEvaluator);

        // PCObserver -> ICEConnectionEvaluator
        this.pcObserver.getExpiredPCs()
                .lift(new PCObserverICEConnectionInputAdapter())
                .subscribe(this.iceConnectionsEvaluator.getExpiredPCsInput());

        // ExpiredPCEvaluator -> ReportSink
        this.expiredPCsEvaluator
                .observableReports()
                .subscribe(this.reports);

        // InboundRTP -> ReportSunk
        this.observedPCSEvaluator
                .getInboundRTPReports()
                .subscribe(this.reports);

        // OutboundRTP -> ReportSink
        this.observedPCSEvaluator
                .getOutboundRTPReports()
                .subscribe(this.reports);

        // RemoteInboundRTP -> ReportSink
        this.observedPCSEvaluator
                .getRemoteInboundRTPReports()
                .subscribe(this.reports);

        // UserMediaError -> ReportSink
        this.observedPCSEvaluator
                .getUserMediaErrorReports()
                .subscribe(this.reports);

        // ICELocalCandidate -> ReportSink
        this.observedPCSEvaluator
                .getICELocalCandidateReports()
                .subscribe(this.reports);

        // ICERemoteCandidate -> ReportSink
        this.observedPCSEvaluator
                .getICERemoteCandidateReports()
                .subscribe(this.reports);

        // ICECandidatePair -> ReportSink
        this.observedPCSEvaluator
                .getICECandidatePairReports()
                .subscribe(this.reports);

        // Track -> ReportSink
        this.observedPCSEvaluator
                .getTrackReports()
                .subscribe(this.reports);

        // MediaSource -> ReportSink
        this.observedPCSEvaluator
                .getMediaSourceReports()
                .subscribe(this.reports);

        // Extension -> ReportSink
        this.observedPCSEvaluator
                .getExtensionReports()
                .subscribe(this.reports);

        // ICERemoteCandidate Report -> ICEConnectionObserver
        this.observedPCSEvaluator
                .getICERemoteCandidateReports()
                .subscribe(this.iceConnectionObserver.getICERemoteCandidates());

        // ICELocalCandidate Report -> ICEConnectionObserver
        this.observedPCSEvaluator
                .getICELocalCandidateReports()
                .subscribe(this.iceConnectionObserver.getICELocalCandidates());

        // ICECandidatePair Report -> ICEConnectionObserver
        this.observedPCSEvaluator
                .getICECandidatePairReports()
                .subscribe(this.iceConnectionObserver.getICECandidatePairs());

        // ICEConnectionObserver -> ICEConnectionEvaluator
        this.iceConnectionObserver.getObservableNewICEConnection()
                .subscribe(this.iceConnectionsEvaluator.getNewICEConnectionsInput());

        // ICEConnectionObserver.expiredICEConnectionOutput -> ICEConnectionEvaluator
        this.iceConnectionObserver.getObservableExpiredICECandidatePairUpdates()
                .subscribe(this.iceConnectionsEvaluator.getExpiredICECandidatePairs());

        // ICEConnectionObserver.updatedICEConnectionOutput -> ICEConnectionEvaluator
        this.iceConnectionObserver.getObservableUpdatedICEConnection()
                .subscribe(this.iceConnectionsEvaluator.getUpdatedICEConnectionsInput());

        this.addConnectors();
    }

    private void addConnectors() {
        List<Connector> builtConnectors = this.connectors.getConnectors();
        if (builtConnectors.size() < 1) {
            logger.warn("No Connector has been built for the observer. The generated reports will not be forwarded");
            return;
        }
        for (Connector connector : builtConnectors) {
            reports.subscribe(connector);
        }
    }

    private class PCObserverICEConnectionInputAdapter implements ObservableOperator<UUID, Map<UUID, PCState>> {

        @NonNull
        @Override
        public Observer<? super Map<UUID, PCState>> apply(@NonNull Observer<? super UUID> observer) throws Exception {
            return new Observer<Map<UUID, PCState>>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(@NonNull Map<UUID, PCState> uuidpcStateMap) {
                    uuidpcStateMap.keySet().stream().forEach(observer::onNext);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    observer.onError(e);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            };
        }
    }

}
