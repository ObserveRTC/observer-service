package org.observertc.webrtc.service.reportsink.bigquery;

import org.observertc.webrtc.common.reports.MediaStreamSample;
import org.observertc.webrtc.common.reportsink.MediaStreamReports;
import org.observertc.webrtc.common.reportsink.ReportResponse;

public class BigQueryMediaStreamReports implements MediaStreamReports {
	private static final String MEDIA_STREAM_SAMPLES_TABLE_NAME = "StreamSamples";
	private final BigQueryTable<MediaStreamSampleEntry> mediaStreamSamples;

	public BigQueryMediaStreamReports(BigQueryService bigQueryService) {
		this.mediaStreamSamples = new BigQueryTable(bigQueryService, MEDIA_STREAM_SAMPLES_TABLE_NAME);
	}

	@Override
	public ReportResponse sample(MediaStreamSample mediaStreamSample) {
		MediaStreamSampleEntry mediaStreamSampleEntry = MediaStreamSampleEntry.from(mediaStreamSample);
		this.mediaStreamSamples.insert(mediaStreamSampleEntry);
		return null;
	}
}
