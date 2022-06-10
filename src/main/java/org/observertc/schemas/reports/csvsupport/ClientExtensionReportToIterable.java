package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.ClientExtensionReport;

public class ClientExtensionReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (ClientExtensionReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload.timestamp);
		result.add(payload.extensionType);
		result.add(payload.callId);
		result.add(payload.clientId);
		result.add(payload.marker);
		result.add(payload.mediaUnitId);
		result.add(payload.payload);
		result.add(payload.peerConnectionId);
		result.add(payload.roomId);
		result.add(payload.sampleSeq);
		result.add(payload.userId);

		return result;
	}
}