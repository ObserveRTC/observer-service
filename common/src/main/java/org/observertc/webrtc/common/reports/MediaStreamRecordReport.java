package org.observertc.webrtc.common.reports;

@Deprecated
public class MediaStreamRecordReport {

	public static MediaStreamRecordReport of(
			Long minimum,
			Long maximum,
			Long count,
			Long sum) {
		MediaStreamRecordReport result = new MediaStreamRecordReport();
		result.minimum = minimum;
		result.maximum = maximum;
		result.count = count;
		result.sum = sum;
		return result;
	}

	public Long minimum;

	public Long maximum;

	public Long count = 0L;

	public Long sum = 0L;
}
