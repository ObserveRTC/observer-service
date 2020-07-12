package org.observertc.webrtc.common.reports;

public class MediaStreamSampleRecordReport {

	public static MediaStreamSampleRecordReport of(
			Long minimum,
			Long maximum,
			Long count,
			Long sum) {
		MediaStreamSampleRecordReport result = new MediaStreamSampleRecordReport();
		result.minimum = minimum;
		result.maximum = maximum;
		result.count = count;
		result.sum = sum;
		return result;
	}

	public Long minimum;

	public Long maximum;

	public Long count;

	public Long sum;
}
