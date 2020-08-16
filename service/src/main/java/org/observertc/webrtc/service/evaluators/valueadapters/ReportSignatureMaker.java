package org.observertc.webrtc.service.evaluators.valueadapters;

import io.micronaut.context.annotation.Prototype;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.ICECandidatePairReport;
import org.observertc.webrtc.common.reports.ICELocalCandidateReport;
import org.observertc.webrtc.common.reports.ICERemoteCandidateReport;
import org.observertc.webrtc.common.reports.InboundRTPReport;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.MediaSourceReport;
import org.observertc.webrtc.common.reports.OutboundRTPReport;
import org.observertc.webrtc.common.reports.RemoteInboundRTPReport;
import org.observertc.webrtc.common.reports.ReportProcessor;
import org.observertc.webrtc.common.reports.TrackReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class ReportSignatureMaker implements ReportProcessor<byte[]> {

	private static final Logger logger = LoggerFactory.getLogger(ReportSignatureMaker.class);

	private final MessageDigest digester;

	public ReportSignatureMaker() throws NoSuchAlgorithmException {
		this.digester = MessageDigest.getInstance("SHA-512");
	}


	@Override
	public byte[] processMediaSourceReport(MediaSourceReport mediaSourceReport) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(mediaSourceReport.peerConnectionUUID, null)
		);
		this.digester.update(
				mediaSourceReport.mediaSourceID.getBytes()
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processTrackReport(TrackReport trackReport) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(trackReport.peerConnectionUUID, null)
		);
		this.digester.update(
				trackReport.trackID.getBytes()
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processJoinedPeerConnectionReport(JoinedPeerConnectionReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null)
		);
		this.digester.update(
				JoinedPeerConnectionReport.class.getName().getBytes()
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processDetachedPeerConnectionReport(DetachedPeerConnectionReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null)
		);
		this.digester.update(
				DetachedPeerConnectionReport.class.getName().getBytes()
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processInitiatedCallReport(InitiatedCallReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.callUUID, null)
		);
		this.digester.update(
				InitiatedCallReport.class.getName().getBytes()
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processFinishedCallReport(FinishedCallReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.callUUID, null)
		);
		this.digester.update(
				FinishedCallReport.class.getName().getBytes()
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processRemoteInboundRTPReport(RemoteInboundRTPReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null)
		);
		this.digester.update(
				NumberConverter.longToBytes(report.SSRC)
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processInboundRTPReport(InboundRTPReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null)
		);
		this.digester.update(
				NumberConverter.longToBytes(report.SSRC)
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processOutboundRTPReport(OutboundRTPReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null)
		);
		this.digester.update(
				NumberConverter.longToBytes(report.SSRC)
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processICECandidatePairReport(ICECandidatePairReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null)
		);
		this.digester.update(
				report.candidateID.getBytes()
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processICELocalCandidateReport(ICELocalCandidateReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null)
		);
		this.digester.update(
				report.candidateID.getBytes()
		);
		return this.digester.digest();
	}

	@Override
	public byte[] processICERemoteCandidateReport(ICERemoteCandidateReport report) {
		this.digester.reset();
		this.digester.update(
				UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null)
		);
		this.digester.update(
				report.candidateID.getBytes()
		);
		return this.digester.digest();
	}
}