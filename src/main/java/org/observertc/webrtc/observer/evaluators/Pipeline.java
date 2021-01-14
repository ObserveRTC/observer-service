package org.observertc.webrtc.observer.evaluators;

import io.reactivex.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;

@Singleton
public class Pipeline {

    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);
    private boolean run = false;

    @Inject
    PCObserver pcObserver;

    @Inject
    ObservedPCSEvaluator observedPCSEvaluator;

    @Inject
    ReportSink reportSink;

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

    public void input(ObservedPCS observedPCS) {
        this.pcObserver.onNext(observedPCS);
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
                .subscribe(this.reportSink);


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
                .subscribe(this.reportSink);

        // InboundRTP -> ReportSunk
        this.observedPCSEvaluator
                .getInboundRTPReports()
                .subscribe(this.reportSink.bypassInput());

        // OutboundRTP -> ReportSink
        this.observedPCSEvaluator
                .getOutboundRTPReports()
                .subscribe(this.reportSink.bypassInput());

        // RemoteInboundRTP -> ReportSink
        this.observedPCSEvaluator
                .getRemoteInboundRTPReports()
                .subscribe(this.reportSink.bypassInput());

        // UserMediaError -> ReportSink
        this.observedPCSEvaluator
                .getUserMediaErrorReports()
                .subscribe(this.reportSink.bypassInput());

        // ICELocalCandidate -> ReportSink
        this.observedPCSEvaluator
                .getICELocalCandidateReports()
                .subscribe(this.reportSink.bypassInput());

        // ICERemoteCandidate -> ReportSink
        this.observedPCSEvaluator
                .getICERemoteCandidateReports()
                .subscribe(this.reportSink.bypassInput());

        // ICECandidatePair -> ReportSink
        this.observedPCSEvaluator
                .getICECandidatePairReports()
                .subscribe(this.reportSink.bypassInput());

        // Track -> ReportSink
        this.observedPCSEvaluator
                .getTrackReports()
                .subscribe(this.reportSink.bypassInput());

        // MediaSource -> ReportSink
        this.observedPCSEvaluator
                .getMediaSourceReports()
                .subscribe(this.reportSink.bypassInput());

        // Extension -> ReportSink
        this.observedPCSEvaluator
                .getExtensionReports()
                .subscribe(this.reportSink.bypassInput());

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
        this.iceConnectionObserver.getObservableICEConnection()
                .subscribe(this.iceConnectionsEvaluator.getNewICEConnectionsInput());

        // ICEConnectionObserver -> ICEConnectionEvaluator
        this.iceConnectionObserver.getObservableExpiredICECandidatePairUpdates()
                .subscribe(this.iceConnectionsEvaluator.getExpiredICECandidatePairs());


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
