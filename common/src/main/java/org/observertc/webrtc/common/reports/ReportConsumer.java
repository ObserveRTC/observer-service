package org.observertc.webrtc.common.reports;

public interface ReportConsumer<T, R> {

	default R process(T meta, Report report) {
		switch (report.type) {
			case FINISHED_CALL:
				FinishedCallReport finishedCallReport = (FinishedCallReport) report;
				return this.processFinishedCallReport(meta, finishedCallReport);
			case JOINED_PEER_CONNECTION:
				JoinedPeerConnectionReport joinedPeerConnectionReport = (JoinedPeerConnectionReport) report;
				return this.processJoinedPeerConnectionReport(meta, joinedPeerConnectionReport);
			case INITIATED_CALL:
				InitiatedCallReport initiatedCallReport = (InitiatedCallReport) report;
				return this.processInitiatedCallReport(meta, initiatedCallReport);
			case DETACHED_PEER_CONNECTION:
				DetachedPeerConnectionReport detachedPeerConnectionReport = (DetachedPeerConnectionReport) report;
				return this.processDetachedPeerConnectionReport(meta, detachedPeerConnectionReport);
			case REMOTE_INBOUND_RTP_REPORT:
				RemoteInboundRTPReport remoteInboundRTPReport = (RemoteInboundRTPReport) report;
				return this.processRemoteInboundRTPReport(meta, remoteInboundRTPReport);
			case INBOUND_RTP_REPORT:
				InboundRTPReport inboundRTPReport = (InboundRTPReport) report;
				return this.processInboundRTPReport(meta, inboundRTPReport);
			case OUTBOUND_RTP_REPORT:
				OutboundRTPReport outboundRTPReport = (OutboundRTPReport) report;
				return this.processOutboundRTPReport(meta, outboundRTPReport);
			case ICE_CANDIDATE_PAIR_REPORT:
				ICECandidatePairReport iceCandidatePairReport = (ICECandidatePairReport) report;
				return this.processICECandidatePairReport(meta, iceCandidatePairReport);
			case ICE_LOCAL_CANDIDATE_REPORT:
				ICELocalCandidateReport iceLocalCandidateReport = (ICELocalCandidateReport) report;
				return this.processICELocalCandidateReport(meta, iceLocalCandidateReport);
			case ICE_REMOTE_CANDIDATE_REPORT:
				ICERemoteCandidateReport iceRemoteCandidateReport = (ICERemoteCandidateReport) report;
				return this.processICERemoteCandidateReport(meta, iceRemoteCandidateReport);
			case TRACK_REPORT:
				TrackReport trackReport = (TrackReport) report;
				return this.processTrackReport(meta, trackReport);
			case MEDIA_SOURCE_REPORT:
				MediaSourceReport mediaSourceReport = (MediaSourceReport) report;
				return this.processMediaSourceReport(meta, mediaSourceReport);
			default:
				return this.unprocessable(meta, report);
		}
	}

	R processMediaSourceReport(T meta, MediaSourceReport mediaSourceReport);

	R processTrackReport(T meta, TrackReport trackReport);

	R processJoinedPeerConnectionReport(T meta, JoinedPeerConnectionReport report);

	R processDetachedPeerConnectionReport(T meta, DetachedPeerConnectionReport report);

	R processInitiatedCallReport(T meta, InitiatedCallReport report);

	R processFinishedCallReport(T meta, FinishedCallReport report);

	R processRemoteInboundRTPReport(T meta, RemoteInboundRTPReport report);

	R processInboundRTPReport(T meta, InboundRTPReport report);

	R processOutboundRTPReport(T meta, OutboundRTPReport report);

	R processICECandidatePairReport(T meta, ICECandidatePairReport report);

	R processICELocalCandidateReport(T meta, ICELocalCandidateReport report);

	R processICERemoteCandidateReport(T meta, ICERemoteCandidateReport report);


	default R unprocessable(T meta, Report report) {
		return null;
	}
}
