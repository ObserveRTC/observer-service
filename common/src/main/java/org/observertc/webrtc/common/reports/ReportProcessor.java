//package org.observertc.webrtc.common.reports;
//
//public interface ReportProcessor<T> {
//
//	default T process(Report report) {
//		switch (report.type) {
//			case FINISHED_CALL:
//				FinishedCallReport finishedCallReport = (FinishedCallReport) report;
//				return this.processFinishedCallReport(finishedCallReport);
//			case JOINED_PEER_CONNECTION:
//				JoinedPeerConnectionReport joinedPeerConnectionReport = (JoinedPeerConnectionReport) report;
//				return this.processJoinedPeerConnectionReport(joinedPeerConnectionReport);
//			case INITIATED_CALL:
//				InitiatedCallReport initiatedCallReport = (InitiatedCallReport) report;
//				return this.processInitiatedCallReport(initiatedCallReport);
//			case DETACHED_PEER_CONNECTION:
//				DetachedPeerConnectionReport detachedPeerConnectionReport = (DetachedPeerConnectionReport) report;
//				return this.processDetachedPeerConnectionReport(detachedPeerConnectionReport);
//			case REMOTE_INBOUND_RTP_REPORT:
//				RemoteInboundRTPReport remoteInboundRTPReport = (RemoteInboundRTPReport) report;
//				return this.processRemoteInboundRTPReport(remoteInboundRTPReport);
//			case INBOUND_RTP_REPORT:
//				InboundRTPReport inboundRTPReport = (InboundRTPReport) report;
//				return this.processInboundRTPReport(inboundRTPReport);
//			case OUTBOUND_RTP_REPORT:
//				OutboundRTPReport outboundRTPReport = (OutboundRTPReport) report;
//				return this.processOutboundRTPReport(outboundRTPReport);
//			case ICE_CANDIDATE_PAIR_REPORT:
//				ICECandidatePairReport iceCandidatePairReport = (ICECandidatePairReport) report;
//				return this.processICECandidatePairReport(iceCandidatePairReport);
//			case ICE_LOCAL_CANDIDATE_REPORT:
//				ICELocalCandidateReport iceLocalCandidateReport = (ICELocalCandidateReport) report;
//				return this.processICELocalCandidateReport(iceLocalCandidateReport);
//			case ICE_REMOTE_CANDIDATE_REPORT:
//				ICERemoteCandidateReport iceRemoteCandidateReport = (ICERemoteCandidateReport) report;
//				return this.processICERemoteCandidateReport(iceRemoteCandidateReport);
//			case TRACK_REPORT:
//				TrackReport trackReport = (TrackReport) report;
//				return this.processTrackReport(trackReport);
//			case MEDIA_SOURCE_REPORT:
//				MediaSourceReport mediaSourceReport = (MediaSourceReport) report;
//				return this.processMediaSourceReport(mediaSourceReport);
//			default:
//				return this.unprocessable(report);
//		}
//	}
//
//	T processMediaSourceReport(MediaSourceReport mediaSourceReport);
//
//	T processTrackReport(TrackReport trackReport);
//
//	T processJoinedPeerConnectionReport(JoinedPeerConnectionReport report);
//
//	T processDetachedPeerConnectionReport(DetachedPeerConnectionReport report);
//
//	T processInitiatedCallReport(InitiatedCallReport report);
//
//	T processFinishedCallReport(FinishedCallReport report);
//
//	T processRemoteInboundRTPReport(RemoteInboundRTPReport report);
//
//	T processInboundRTPReport(InboundRTPReport report);
//
//	T processOutboundRTPReport(OutboundRTPReport report);
//
//	T processICECandidatePairReport(ICECandidatePairReport report);
//
//	T processICELocalCandidateReport(ICELocalCandidateReport report);
//
//	T processICERemoteCandidateReport(ICERemoteCandidateReport report);
//
//
//	default T unprocessable(Report report) {
//		return null;
//	}
//}
