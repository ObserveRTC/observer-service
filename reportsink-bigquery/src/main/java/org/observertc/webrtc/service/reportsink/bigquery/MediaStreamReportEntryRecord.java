package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.HashMap;
import java.util.Map;
import org.observertc.webrtc.common.reports.MediaStreamRecordReport;


public class MediaStreamReportEntryRecord {
	public static final String MINIMUM_FIELD_NAME = "minimum";
	public static final String MAXIMUM_FIELD_NAME = "maximum";
	public static final String COUNT_FIELD_NAME = "count";
	public static final String SUM_FIELD_NAME = "sum";

	public static MediaStreamReportEntryRecord from(MediaStreamRecordReport record) {
		return new MediaStreamReportEntryRecord()
				.withCount(record.count)
				.withMaximum(record.maximum)
				.withMinimum(record.minimum)
				.withSum(record.sum);
	}


	private final Map<String, Object> summaryValues = new HashMap<>();


	public MediaStreamReportEntryRecord withMinimum(Long value) {
		this.summaryValues.put(MINIMUM_FIELD_NAME, value);
		return this;
	}

	public MediaStreamReportEntryRecord withMaximum(Long value) {
		this.summaryValues.put(MAXIMUM_FIELD_NAME, value);
		return this;
	}

	public MediaStreamReportEntryRecord withSum(Long value) {
		this.summaryValues.put(SUM_FIELD_NAME, value);
		return this;
	}

	public MediaStreamReportEntryRecord withCount(Long value) {
		this.summaryValues.put(COUNT_FIELD_NAME, value);
		return this;
	}

	public Map<String, Object> toMap() {
		return this.summaryValues;
	}
}