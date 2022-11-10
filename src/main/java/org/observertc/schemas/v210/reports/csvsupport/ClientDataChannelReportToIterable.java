package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.ClientDataChannelReport;

public class ClientDataChannelReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (ClientDataChannelReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload. mediaUnitId);
		result.add(payload. timestamp);
		result.add(payload. callId);
		result.add(payload. clientId);
		result.add(payload. peerConnectionId);
		result.add(payload. sampleSeq);
		result.add(payload. bytesReceived);
		result.add(payload. bytesSent);
		result.add(payload. label);
		result.add(payload. marker);
		result.add(payload. messagesReceived);
		result.add(payload. messagesSent);
		result.add(payload. peerConnectionLabel);
		result.add(payload. protocol);
		result.add(payload. roomId);
		result.add(payload. state);
		result.add(payload. userId);

		return result;
	}
}