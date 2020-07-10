package org.observertc.webrtc.common.reports;

public class MediaStreamSampleRecordReport {

	public static MediaStreamSampleRecordReport of(
			Long minimum,
			Long maximum,
			Long presented,
			Long empty,
			Long sum) {
		MediaStreamSampleRecordReport result = new MediaStreamSampleRecordReport();
		result.minimum = minimum;
		result.maximum = maximum;
		result.presented = presented;
		result.empty = empty;
		result.sum = sum;
		return result;
	}

	public Long minimum;

	public Long maximum;

	public Long presented;

	public Long empty;

	public Long sum;
}
