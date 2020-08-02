package org.observertc.webrtc.common.reports;

public interface ReportProcessor<T> {

	default T process(Report report) {
		switch (report.type) {
			case FINISHED_CALL:
				FinishedCallReport finishedCallReport = (FinishedCallReport) report;
				return this.processFinishedCallReport(finishedCallReport);
			case JOINED_PEER_CONNECTION:
				JoinedPeerConnectionReport joinedPeerConnectionReport = (JoinedPeerConnectionReport) report;
				return this.processJoinedPeerConnectionReport(joinedPeerConnectionReport);
			case INITIATED_CALL:
				InitiatedCallReport initiatedCallReport = (InitiatedCallReport) report;
				return this.processInitiatedCallReport(initiatedCallReport);
			case DETACHED_PEER_CONNECTION:
				DetachedPeerConnectionReport detachedPeerConnectionReport = (DetachedPeerConnectionReport) report;
				return this.processDetachedPeerConnectionReport(detachedPeerConnectionReport);
			case INBOUND_STREAM_REPORT:
				InboundStreamReport inboundStreamSampleReport = (InboundStreamReport) report;
				return this.processInboundStreamReport(inboundStreamSampleReport);
			case OUTBOUND_STREAM_REPORT:
				OutboundStreamReport outboundStreamReport = (OutboundStreamReport) report;
				return this.processOutboundStreamReport(outboundStreamReport);
			case REMOTE_INBOUND_STREAM_REPORT:
				RemoteInboundStreamReport remoteInboundStreamSampleReport = (RemoteInboundStreamReport) report;
				return this.processRemoteInboundStreamReport(remoteInboundStreamSampleReport);
			case REMOTE_INBOUND_RTP_REPORT:
				RemoteInboundRTPReport remoteInboundRTPReport = (RemoteInboundRTPReport) report;
				return this.processRemoteInboundRTPReport(remoteInboundRTPReport);
			case INBOUND_RTP_REPORT:
				InboundRTPReport inboundRTPReport = (InboundRTPReport) report;
				return this.processInboundRTPReport(inboundRTPReport);
			case OUTBOUND_RTP_REPORT:
				OutboundRTPReport outboundRTPReport = (OutboundRTPReport) report;
				return this.processOutboundRTPReport(outboundRTPReport);
			case ICE_CANDIDATE_PAIR_REPORT:
				ICECandidatePairReport iceCandidatePairReport = (ICECandidatePairReport) report;
				return this.processICECandidatePairReport(iceCandidatePairReport);
			default:
				return this.unprocessable(report);
		}
	}

	T processJoinedPeerConnectionReport(JoinedPeerConnectionReport report);

	T processDetachedPeerConnectionReport(DetachedPeerConnectionReport report);

	T processInitiatedCallReport(InitiatedCallReport report);

	T processFinishedCallReport(FinishedCallReport report);

	T processOutboundStreamReport(OutboundStreamReport report);

	T processInboundStreamReport(InboundStreamReport report);

	T processRemoteInboundStreamReport(RemoteInboundStreamReport report);

	T processRemoteInboundRTPReport(RemoteInboundRTPReport report);

	T processInboundRTPReport(InboundRTPReport report);

	T processOutboundRTPReport(OutboundRTPReport report);

	T processICECandidatePairReport(ICECandidatePairReport report);


	default T unprocessable(Report report) {
		return null;
	}
}
