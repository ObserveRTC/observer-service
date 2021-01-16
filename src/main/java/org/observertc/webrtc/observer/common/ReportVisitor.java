package org.observertc.webrtc.observer.common;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.schemas.reports.*;

import java.util.Objects;

public interface ReportVisitor<T> extends Function<Report, T> {

    default T apply(Report report) {
        if (Objects.isNull(report)) {
            return null;
        }
        ReportType reportType = report.getType();
        if (Objects.isNull(reportType)) {
            return null;
        }
        switch (reportType) {
            case UNKNOWN:
                return this.visitUnknownType(report);
            case EXTENSION:
                return this.visitExtensionReport(report, (ExtensionReport) report.getPayload());
            case TRACK:
                return this.visitTrackReport(report, (Track) report.getPayload());
            case FINISHED_CALL:
                return this.visitFinishedCallReport(report, (FinishedCall) report.getPayload());
            case INITIATED_CALL:
                return this.visitInitiatedCallReport(report, (InitiatedCall) report.getPayload());
            case JOINED_PEER_CONNECTION:
                return this.visitJoinedPeerConnectionReport(report, (JoinedPeerConnection) report.getPayload());
            case DETACHED_PEER_CONNECTION:
                return this.visitDetachedPeerConnectionReport(report, (DetachedPeerConnection) report.getPayload());
            case INBOUND_RTP:
                return this.visitInboundRTPReport(report, (InboundRTP) report.getPayload());
            case OUTBOUND_RTP:
                return this.visitOutboundRTPReport(report, (OutboundRTP) report.getPayload());
            case REMOTE_INBOUND_RTP:
                return this.visitRemoteInboundRTPReport(report, (RemoteInboundRTP) report.getPayload());
            case MEDIA_SOURCE:
                return this.visitMediaSourceReport(report, (MediaSource) report.getPayload());
            case OBSERVER_EVENT:
                return this.visitObserverReport(report, (ObserverEventReport) report.getPayload());
            case USER_MEDIA_ERROR:
                return this.visitUserMediaErrorReport(report, (UserMediaError) report.getPayload());
            case ICE_CANDIDATE_PAIR:
                return this.visitICECandidatePairReport(report, (ICECandidatePair) report.getPayload());
            case ICE_LOCAL_CANDIDATE:
                return this.visitICELocalCandidateReport(report, (ICELocalCandidate) report.getPayload());
            case ICE_REMOTE_CANDIDATE:
                return this.visitICERemoteCandidateReport(report, (ICERemoteCandidate) report.getPayload());
            default:
                return this.visitUnrecognizedReport(report);
        }
    }

    T visitTrackReport(Report report, Track payload);

    T visitFinishedCallReport(Report report, FinishedCall payload);

    T visitInitiatedCallReport(Report report, InitiatedCall payload);

    T visitJoinedPeerConnectionReport(Report report, JoinedPeerConnection payload);

    T visitDetachedPeerConnectionReport(Report report, DetachedPeerConnection payload);

    T visitInboundRTPReport(Report report, InboundRTP payload);

    T visitOutboundRTPReport(Report report, OutboundRTP payload);

    T visitRemoteInboundRTPReport(Report report, RemoteInboundRTP payload);

    T visitMediaSourceReport(Report report, MediaSource payload);

    T visitObserverReport(Report report, ObserverEventReport payload);

    T visitUserMediaErrorReport(Report report, UserMediaError payload);

    T visitICECandidatePairReport(Report report, ICECandidatePair payload);

    T visitICELocalCandidateReport(Report report, ICELocalCandidate payload);

    T visitICERemoteCandidateReport(Report report, ICERemoteCandidate payload);

    T visitUnrecognizedReport(Report report);

    T visitExtensionReport(Report report, ExtensionReport payload);

    T visitUnknownType(Report report);


}
