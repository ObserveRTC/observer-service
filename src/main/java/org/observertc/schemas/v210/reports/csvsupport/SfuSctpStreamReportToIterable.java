package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.SfuSctpStreamReport;

public class SfuSctpStreamReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (SfuSctpStreamReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload. mediaUnitId);
		result.add(payload. sfuId);
		result.add(payload. timestamp);
		result.add(payload. transportId);
		result.add(payload. streamId);
		result.add(payload. bytesReceived);
		result.add(payload. bytesSent);
		result.add(payload. callId);
		result.add(payload. internal);
		result.add(payload. label);
		result.add(payload. marker);
		result.add(payload. messageReceived);
		result.add(payload. messageSent);
		result.add(payload. protocol);
		result.add(payload. roomId);
		result.add(payload. sctpCongestionWindow);
		result.add(payload. sctpMtu);
		result.add(payload. sctpReceiverWindow);
		result.add(payload. sctpSmoothedRoundTripTime);
		result.add(payload. sctpUnackData);

		return result;
	}
}