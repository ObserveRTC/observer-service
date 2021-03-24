package org.observertc.webrtc.observer.connectors.encoders;

import org.observertc.webrtc.observer.common.ReportVisitor;
import org.observertc.webrtc.schemas.reports.*;

import java.util.UUID;

public class ReportKeyMaker implements ReportVisitor<UUID> {

    @Override
    public UUID visitTrackReport(Report report, Track payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitFinishedCallReport(Report report, FinishedCall payload) {
        return UUID.fromString(report.getServiceUUID());
    }

    @Override
    public UUID visitInitiatedCallReport(Report report, InitiatedCall payload) {
        return UUID.fromString(report.getServiceUUID());
    }

    @Override
    public UUID visitJoinedPeerConnectionReport(Report report, JoinedPeerConnection payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitDetachedPeerConnectionReport(Report report, DetachedPeerConnection payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitInboundRTPReport(Report report, InboundRTP payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitOutboundRTPReport(Report report, OutboundRTP payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitRemoteInboundRTPReport(Report report, RemoteInboundRTP payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitMediaSourceReport(Report report, MediaSource payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitObserverReport(Report report, ObserverEventReport payload) {
        return UUID.fromString(report.getServiceUUID());
    }

    @Override
    public UUID visitUserMediaErrorReport(Report report, UserMediaError payload) {
        return UUID.fromString(report.getServiceUUID());
    }

    @Override
    public UUID visitICECandidatePairReport(Report report, ICECandidatePair payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitICELocalCandidateReport(Report report, ICELocalCandidate payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitICERemoteCandidateReport(Report report, ICERemoteCandidate payload) {
        return UUID.fromString(payload.getPeerConnectionUUID());
    }

    @Override
    public UUID visitUnrecognizedReport(Report report) {
        return UUID.fromString(report.getServiceUUID());
    }

    @Override
    public UUID visitExtensionReport(Report report, ExtensionReport payload) {
        return UUID.fromString(report.getServiceUUID());
    }

    @Override
    public UUID visitUnknownType(Report report) {
        return UUID.fromString(report.getServiceUUID());
    }
}
