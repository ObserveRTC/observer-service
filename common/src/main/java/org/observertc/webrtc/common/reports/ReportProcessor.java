package org.observertc.webrtc.common.reports;

public interface ReportProcessor<T> {

	default T process(Report report) {
		switch (report.type) {
			case FINISHED_CALL:
				FinishedCallReport finishedCallReport = (FinishedCallReport) report;
				return this.process(finishedCallReport);
			case JOINED_PEER_CONNECTION:
				JoinedPeerConnectionReport joinedPeerConnectionReport = (JoinedPeerConnectionReport) report;
				return this.process(joinedPeerConnectionReport);
			case INITIATED_CALL:
				InitiatedCallReport initiatedCallReport = (InitiatedCallReport) report;
				return this.process(initiatedCallReport);
			case DETACHED_PEER_CONNECTION:
				DetachedPeerConnectionReport detachedPeerConnectionReport = (DetachedPeerConnectionReport) report;
				return this.process(detachedPeerConnectionReport);
			case INBOUND_STREAM_REPORT:
				InboundStreamReport inboundStreamSampleReport = (InboundStreamReport) report;
				return this.process(inboundStreamSampleReport);
			case OUTBOUND_STREAM_REPORT:
				OutboundStreamReport outboundStreamReport = (OutboundStreamReport) report;
				return this.process(outboundStreamReport);
			case REMOTE_INBOUND_STREAM_REPORT:
				RemoteInboundStreamReport remoteInboundStreamSampleReport = (RemoteInboundStreamReport) report;
				return this.process(remoteInboundStreamSampleReport);
			case REMOTE_INBOUND_RTP_REPORT:
				RemoteInboundRTPReport remoteInboundRTPReport = (RemoteInboundRTPReport) report;
				return this.process(remoteInboundRTPReport);
			case INBOUND_RTP_REPORT:
				InboundRTPReport inboundRTPReport = (InboundRTPReport) report;
				return this.process(inboundRTPReport);
			case OUTBOUND_RTP_REPORT:
				OutboundRTPReport outboundRTPReport = (OutboundRTPReport) report;
				return this.process(outboundRTPReport);
			default:
				return this.unprocessable(report);
		}
	}

	T process(JoinedPeerConnectionReport report);

	T process(DetachedPeerConnectionReport report);

	T process(InitiatedCallReport report);

	T process(FinishedCallReport report);

	T process(OutboundStreamReport report);

	T process(InboundStreamReport report);

	T process(RemoteInboundStreamReport report);

	T process(RemoteInboundRTPReport report);

	T process(InboundRTPReport report);

	T process(OutboundRTPReport report);


	default T unprocessable(Report report) {
		return null;
	}
}
