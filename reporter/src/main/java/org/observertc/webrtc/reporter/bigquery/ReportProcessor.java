/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.reporter.bigquery;

import java.util.function.Consumer;
import org.observertc.webrtc.common.reports.avro.DetachedPeerConnection;
import org.observertc.webrtc.common.reports.avro.FinishedCall;
import org.observertc.webrtc.common.reports.avro.ICECandidatePair;
import org.observertc.webrtc.common.reports.avro.ICELocalCandidate;
import org.observertc.webrtc.common.reports.avro.ICERemoteCandidate;
import org.observertc.webrtc.common.reports.avro.InboundRTP;
import org.observertc.webrtc.common.reports.avro.InitiatedCall;
import org.observertc.webrtc.common.reports.avro.JoinedPeerConnection;
import org.observertc.webrtc.common.reports.avro.OutboundRTP;
import org.observertc.webrtc.common.reports.avro.RemoteInboundRTP;
import org.observertc.webrtc.common.reports.avro.Report;
import org.observertc.webrtc.common.reports.avro.Track;

public interface ReportProcessor extends Consumer<Report> {

	default void apply(Report report) {
		switch (report.getType()) {
			case FINISHED_CALL:
				FinishedCall finishedCall = (FinishedCall) report.getPayload();
				this.processFinishedCallReport(report, finishedCall);
			case JOINED_PEER_CONNECTION:
				JoinedPeerConnection joinedPeerConnectionReport = (JoinedPeerConnection) report.getPayload();
				this.processJoinedPeerConnectionReport(report, joinedPeerConnectionReport);
			case INITIATED_CALL:
				InitiatedCall initiatedCallReport = (InitiatedCall) report.getPayload();
				this.processInitiatedCallReport(report, initiatedCallReport);
			case DETACHED_PEER_CONNECTION:
				DetachedPeerConnection detachedPeerConnectionReport = (DetachedPeerConnection) report.getPayload();
				this.processDetachedPeerConnectionReport(report, detachedPeerConnectionReport);
			case INBOUND_RTP:
				RemoteInboundRTP remoteInboundRTPReport = (RemoteInboundRTP) report.getPayload();
				this.processRemoteInboundRTPReport(report, remoteInboundRTPReport);
			case OUTBOUND_RTP:
				InboundRTP inboundRTPReport = (InboundRTP) report.getPayload();
				this.processInboundRTPReport(report, inboundRTPReport);
			case MEDIA_SOURCE:
				OutboundRTP outboundRTPReport = (OutboundRTP) report.getPayload();
				this.processOutboundRTPReport(report, outboundRTPReport);
			case ICE_CANDIDATE_PAIR:
				ICECandidatePair iceCandidatePairReport = (ICECandidatePair) report.getPayload();
				this.processICECandidatePairReport(report, iceCandidatePairReport);
			case ICE_LOCAL_CANDIDATE:
				ICELocalCandidate iceLocalCandidateReport = (ICELocalCandidate) report.getPayload();
				this.processICELocalCandidateReport(report, iceLocalCandidateReport);
			case ICE_REMOTE_CANDIDATE:
				ICERemoteCandidate iceRemoteCandidateReport = (ICERemoteCandidate) report.getPayload();
				this.processICERemoteCandidateReport(report, iceRemoteCandidateReport);
			case TRACK:
				Track trackReport = (Track) report.getPayload();
				this.processTrackReport(report, trackReport);
			default:
				this.unprocessable(report);
		}
	}


	default void unprocessable(Report report) {

	}

	void processTrackReport(Report report, Track trackReport);

	void processICELocalCandidateReport(Report report, ICELocalCandidate iceLocalCandidateReport);

	void processICECandidatePairReport(Report report, ICECandidatePair iceCandidatePairReport);

	void processOutboundRTPReport(Report report, OutboundRTP outboundRTPReport);

	void processInboundRTPReport(Report report, InboundRTP inboundRTPReport);

	void processRemoteInboundRTPReport(Report report, RemoteInboundRTP remoteInboundRTPReport);

	void processDetachedPeerConnectionReport(Report report, DetachedPeerConnection detachedPeerConnectionReport);

	void processInitiatedCallReport(Report report, InitiatedCall initiatedCallReport);

	void processJoinedPeerConnectionReport(Report report, JoinedPeerConnection joinedPeerConnectionReport);

	void processFinishedCallReport(Report report, FinishedCall finishedCall);

	void processICERemoteCandidateReport(Report report, ICERemoteCandidate iceRemoteCandidateReport);

}
