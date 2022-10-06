package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.SfuMetaReport;

public class SfuMetaReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (SfuMetaReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload. timestamp);
		result.add(payload. callId);
		result.add(payload. marker);
		result.add(payload. mediaSinkId);
		result.add(payload. mediaStreamId);
		result.add(payload. mediaUnitId);
		result.add(payload. payload);
		result.add(payload. rtpPadId);
		result.add(payload. sctpStreamId);
		result.add(payload. sfuId);
		result.add(payload. transportId);
		result.add(payload. type);

		return result;
	}
}