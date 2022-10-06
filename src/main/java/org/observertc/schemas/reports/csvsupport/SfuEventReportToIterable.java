package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.SfuEventReport;

public class SfuEventReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (SfuEventReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload. timestamp);
		result.add(payload. name);
		result.add(payload. attachments);
		result.add(payload. callId);
		result.add(payload. marker);
		result.add(payload. mediaSinkId);
		result.add(payload. mediaStreamId);
		result.add(payload. mediaUnitId);
		result.add(payload. message);
		result.add(payload. rtpPadId);
		result.add(payload. sctpStreamId);
		result.add(payload. sfuId);
		result.add(payload. transportId);
		result.add(payload. value);

		return result;
	}
}