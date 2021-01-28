package org.observertc.webrtc.observer.common;

import org.observertc.webrtc.schemas.reports.*;

public abstract class ReportVisitorAbstract<T> implements ReportVisitor<T> {


    @Override
    public T visitTrackReport(Report report, Track payload) {
        return null;
    }

    @Override
    public T visitFinishedCallReport(Report report, FinishedCall payload) {
        return null;
    }

    @Override
    public T visitInitiatedCallReport(Report report, InitiatedCall payload) {
        return null;
    }

    @Override
    public T visitJoinedPeerConnectionReport(Report report, JoinedPeerConnection payload) {
        return null;
    }

    @Override
    public T visitDetachedPeerConnectionReport(Report report, DetachedPeerConnection payload) {
        return null;
    }

    @Override
    public T visitInboundRTPReport(Report report, InboundRTP payload) {
        return null;
    }

    @Override
    public T visitOutboundRTPReport(Report report, OutboundRTP payload) {
        return null;
    }

    @Override
    public T visitRemoteInboundRTPReport(Report report, RemoteInboundRTP payload) {
        return null;
    }

    @Override
    public T visitMediaSourceReport(Report report, MediaSource payload) {
        return null;
    }

    @Override
    public T visitObserverReport(Report report, ObserverEventReport payload) {
        return null;
    }

    @Override
    public T visitUserMediaErrorReport(Report report, UserMediaError payload) {
        return null;
    }

    @Override
    public T visitICECandidatePairReport(Report report, ICECandidatePair payload) {
        return null;
    }

    @Override
    public T visitICELocalCandidateReport(Report report, ICELocalCandidate payload) {
        return null;
    }

    @Override
    public T visitICERemoteCandidateReport(Report report, ICERemoteCandidate payload) {
        return null;
    }

    @Override
    public T visitUnrecognizedReport(Report report) {
        return null;
    }

    @Override
    public T visitExtensionReport(Report report, ExtensionReport payload) {
        return null;
    }

    @Override
    public T visitUnknownType(Report report) {
        return null;
    }
}
