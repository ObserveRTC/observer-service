package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.IceCandidatePairReport;

public class IceCandidatePairReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (IceCandidatePairReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload. mediaUnitId);
		result.add(payload. timestamp);
		result.add(payload. callId);
		result.add(payload. clientId);
		result.add(payload. peerConnectionId);
		result.add(payload. sampleSeq);
		result.add(payload. availableIncomingBitrate);
		result.add(payload. availableOutgoingBitrate);
		result.add(payload. bytesDiscardedOnSend);
		result.add(payload. bytesReceived);
		result.add(payload. bytesSent);
		result.add(payload. consentRequestsSent);
		result.add(payload. currentRoundTripTime);
		result.add(payload. label);
		result.add(payload. lastPacketReceivedTimestamp);
		result.add(payload. lastPacketSentTimestamp);
		result.add(payload. localCandidateId);
		result.add(payload. marker);
		result.add(payload. nominated);
		result.add(payload. packetsDiscardedOnSend);
		result.add(payload. packetsReceived);
		result.add(payload. packetsSent);
		result.add(payload. remoteCandidateId);
		result.add(payload. requestsReceived);
		result.add(payload. requestsSent);
		result.add(payload. responsesReceived);
		result.add(payload. responsesSent);
		result.add(payload. roomId);
		result.add(payload. state);
		result.add(payload. totalRoundTripTime);
		result.add(payload. transportId);
		result.add(payload. userId);

		return result;
	}
}