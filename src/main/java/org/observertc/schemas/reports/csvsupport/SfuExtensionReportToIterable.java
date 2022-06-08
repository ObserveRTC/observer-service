package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.SfuExtensionReport;

public class SfuExtensionReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (SfuExtensionReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload.timestamp);
		result.add(payload.extensionType);
		result.add(payload.marker);
		result.add(payload.mediaUnitId);
		result.add(payload.payload);
		result.add(payload.sfuId);

		return result;
	}
}