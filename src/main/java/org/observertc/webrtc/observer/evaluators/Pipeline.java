package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.Connectors;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
import org.observertc.webrtc.observer.evaluators.monitors.CounterMonitorBuilder;
import org.observertc.webrtc.observer.evaluators.monitors.InboundRtpMonitor;
import org.observertc.webrtc.observer.evaluators.monitors.OutboundRtpMonitor;
import org.observertc.webrtc.observer.evaluators.monitors.RemoteInboundRtpMonitor;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.observer.sources.Sources;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Singleton
public class Pipeline {
    public static final int REPORT_VERSION_NUMBER = 2;
    private static final String GENERATED_REPORTS_METRIC_NAME = "observertc_generated_reports";
    private static final String USER_MEDIA_REPORTS_METRIC_NAME = "observertc_user_media_errors";

    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);
    private final Subject<Report> reports = PublishSubject.create();

    @Inject
    ObserverConfigDispatcher configDispatcher;

    @Inject
    Sources sources;

    @Inject
    PCSObserver PCSObserver;

    @Inject
    ObservedPCSEvaluator observedPCSEvaluator;

    @Inject
    ActivePCsEvaluator activePCsEvaluator;

    @Inject
    ExpiredPCsEvaluator expiredPCsEvaluator;

    @Inject
    ObserverConfig.EvaluatorsConfig evaluatorsConfig;

    @Inject
    InboundRtpMonitor inboundRTPMonitor;

    @Inject
    OutboundRtpMonitor outboundRtpMonitor;

    @Inject
    RemoteInboundRtpMonitor remoteInboundRtpMonitor;

    @Inject
    CounterMonitorBuilder counterMonitorBuilder;

    @Inject
    Connectors connectors;

    public void inputUserMediaError(ObservedPCS observedPCS) {
        this.observedPCSEvaluator.onNext(observedPCS);
    }

    @PostConstruct
    void setup() {
        ObserverConfig config = configDispatcher.getConfig();
        var userMediaErrorsMonitor = this.counterMonitorBuilder.build(USER_MEDIA_REPORTS_METRIC_NAME, config.userMediaErrorsMonitor);
        var samplesBuffer = this.sources
                .filter(observedPCS -> Objects.nonNull(observedPCS.peerConnectionUUID))
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


        this.sources
                .subscribe(this.observedPCSEvaluator);

        // InboundRTP -> ReportSink
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
                .map(userMediaErrorsMonitor)
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

        // ClientDetails -> ReportSink
        this.observedPCSEvaluator
                .getClientDetailsReports()
                .subscribe(this.reports);

        if (Objects.nonNull(config.reportMonitor)) {
            var reportMonitor = this.counterMonitorBuilder.build(GENERATED_REPORTS_METRIC_NAME, config.reportMonitor);
            this.reports.map(reportMonitor).subscribe();
        }

        this.reports.subscribe(this.connectors);
    }
}
