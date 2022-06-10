package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.ClientTransportReport;

public class ClientTransportReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (ClientTransportReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload.mediaUnitId);
		result.add(payload.timestamp);
		result.add(payload.callId);
		result.add(payload.clientId);
		result.add(payload.peerConnectionId);
		result.add(payload.bytesReceived);
		result.add(payload.bytesSent);
		result.add(payload.candidatePairAvailableIncomingBitrate);
		result.add(payload.candidatePairAvailableOutgoingBitrate);
		result.add(payload.candidatePairBytesDiscardedOnSend);
		result.add(payload.candidatePairBytesReceived);
		result.add(payload.candidatePairBytesSent);
		result.add(payload.candidatePairCircuitBreakerTriggerCount);
		result.add(payload.candidatePairConsentExpiredTimestamp);
		result.add(payload.candidatePairConsentRequestBytesSent);
		result.add(payload.candidatePairConsentRequestsSent);
		result.add(payload.candidatePairCurrentRoundTripTime);
		result.add(payload.candidatePairFirstRequestTimestamp);
		result.add(payload.candidatePairLastPacketReceivedTimestamp);
		result.add(payload.candidatePairLastPacketSentTimestamp);
		result.add(payload.candidatePairLastRequestTimestamp);
		result.add(payload.candidatePairLastResponseTimestamp);
		result.add(payload.candidatePairPacketsDiscardedOnSend);
		result.add(payload.candidatePairPacketsReceived);
		result.add(payload.candidatePairPacketsSent);
		result.add(payload.candidatePairRequestBytesSent);
		result.add(payload.candidatePairRequestsReceived);
		result.add(payload.candidatePairRequestsSent);
		result.add(payload.candidatePairResponseBytesSent);
		result.add(payload.candidatePairResponsesReceived);
		result.add(payload.candidatePairResponsesSent);
		result.add(payload.candidatePairRetransmissionReceived);
		result.add(payload.candidatePairRetransmissionSent);
		result.add(payload.candidatePairState);
		result.add(payload.candidatePairTotalRoundTripTime);
		result.add(payload.dtlsCipher);
		result.add(payload.dtlsState);
		result.add(payload.iceLocalUsernameFragment);
		result.add(payload.iceRole);
		result.add(payload.iceTransportState);
		result.add(payload.label);
		result.add(payload.localAddress);
		result.add(payload.localCandidateICEServerUrl);
		result.add(payload.localCandidateRelayProtocol);
		result.add(payload.localCandidateType);
		result.add(payload.localPort);
		result.add(payload.localProtocol);
		result.add(payload.marker);
		result.add(payload.packetsReceived);
		result.add(payload.packetsSent);
		result.add(payload.remoteAddress);
		result.add(payload.remoteCandidateICEServerUrl);
		result.add(payload.remoteCandidateRelayProtocol);
		result.add(payload.remoteCandidateType);
		result.add(payload.remotePort);
		result.add(payload.remoteProtocol);
		result.add(payload.roomId);
		result.add(payload.sctpCongestionWindow);
		result.add(payload.sctpMtu);
		result.add(payload.sctpReceiverWindow);
		result.add(payload.sctpSmoothedRoundTripTime);
		result.add(payload.sctpUnackData);
		result.add(payload.selectedCandidatePairChanges);
		result.add(payload.srtpCipher);
		result.add(payload.tlsGroup);
		result.add(payload.tlsVersion);
		result.add(payload.userId);

		return result;
	}
}