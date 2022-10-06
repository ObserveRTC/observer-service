package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.PeerConnectionTransportReport;

public class PeerConnectionTransportReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (PeerConnectionTransportReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload. mediaUnitId);
		result.add(payload. timestamp);
		result.add(payload. callId);
		result.add(payload. clientId);
		result.add(payload. peerConnectionId);
		result.add(payload. sampleSeq);
		result.add(payload. bytesReceived);
		result.add(payload. bytesSent);
		result.add(payload. dtlsCipher);
		result.add(payload. dtlsState);
		result.add(payload. iceLocalUsernameFragment);
		result.add(payload. iceRole);
		result.add(payload. iceState);
		result.add(payload. label);
		result.add(payload. localCertificateId);
		result.add(payload. marker);
		result.add(payload. packetsReceived);
		result.add(payload. packetsSent);
		result.add(payload. remoteCertificateId);
		result.add(payload. roomId);
		result.add(payload. selectedCandidatePairChanges);
		result.add(payload. selectedCandidatePairId);
		result.add(payload. srtpCipher);
		result.add(payload. tlsGroup);
		result.add(payload. tlsVersion);
		result.add(payload. userId);

		return result;
	}
}