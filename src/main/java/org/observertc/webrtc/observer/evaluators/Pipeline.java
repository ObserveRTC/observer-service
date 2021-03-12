package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.Connectors;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.connectors.Connector;
import org.observertc.webrtc.observer.evaluators.rtpmonitors.InboundRtpMonitor;
import org.observertc.webrtc.observer.evaluators.rtpmonitors.OutboundRtpMonitor;
import org.observertc.webrtc.observer.evaluators.rtpmonitors.RemoteInboundRtpMonitor;
import org.observertc.webrtc.observer.monitors.CounterMonitorProvider;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Singleton
public class Pipeline {
    public static final int REPORT_VERSION_NUMBER = 2;
    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);
    private final Subject<Report> reports = PublishSubject.create();

    private final Subject<ObservedPCS> observedPCSSubject = PublishSubject.create();

    public Observer<ObservedPCS> getObservedPCSObserver() {
        return this.observedPCSSubject;
    }

    @Inject
    ObserverConfig config;

    @Inject
    PCSObserver PCSObserver;

    @Inject
    ObservedPCSEvaluator observedPCSEvaluator;

    @Inject
    ActivePCsEvaluator activePCsEvaluator;

    @Inject
    ExpiredPCsEvaluator expiredPCsEvaluator;

    @Inject
    Connectors connectors;

    @Inject
    CounterMonitorProvider counterMonitorProvider;

    @Inject
    ObserverConfig.EvaluatorsConfig evaluatorsConfig;

    @Inject
    InboundRtpMonitor inboundRTPMonitor;

    @Inject
    OutboundRtpMonitor outboundRtpMonitor;

    @Inject
    RemoteInboundRtpMonitor remoteInboundRtpMonitor;


    public void inputUserMediaError(ObservedPCS observedPCS) {

//        this.observedPCSEvaluator.onNext(observedPCS);

    }

    @PostConstruct
    void setup() {
        var source = this.observedPCSSubject;
        // TODO: Insert our load balancer for pcs here.

        var samplesBuffer = source
                .buffer(evaluatorsConfig.observedPCSBufferMaxTimeInS, TimeUnit.SECONDS, evaluatorsConfig.observedPCSBufferMaxItemNums)
                .share();

        samplesBuffer
                .subscribe(this.PCSObserver);

        this.PCSObserver
                .getObservableActivePCs()
                .subscribe(this.activePCsEvaluator);

        this.PCSObserver
                .getObservableExpiredPCs()
                .subscribe(this.expiredPCsEvaluator);


        // ActivePCsEvaluator -> NewPCsEvaluator
        this.activePCsEvaluator
                .getObservableReports()
                .subscribe(this.reports);

        // NewPCsEvaluator -> ReportSink
        this.expiredPCsEvaluator
                .getObservableReports()
                .subscribe(this.reports);


        this.observedPCSSubject
                .subscribe(this.observedPCSEvaluator);

        // InboundRTP -> ReportSunk
        this.observedPCSEvaluator
                .getInboundRTPReports()
                .map(this.inboundRTPMonitor)
                .subscribe(this.reports);

        // OutboundRTP -> ReportSink
        this.observedPCSEvaluator
                .getOutboundRTPReports()
                .map(this.outboundRtpMonitor)
                .subscribe(this.reports);

        // RemoteInboundRTP -> ReportSink
        this.observedPCSEvaluator
                .getRemoteInboundRTPReports()
                .map(this.remoteInboundRtpMonitor)
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

        if (Objects.nonNull(this.config.reportMonitor)) {
            var reportMonitor = this.counterMonitorProvider.buildReportMonitor("generated_reports", this.config.reportMonitor);
            this.reports.lift(reportMonitor).subscribe();
        }

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
}
