package org.observertc.webrtc.service.mediastreams;//package com.observertc.gatekeeper.webrtcstat.processors.samples;

import java.util.function.BiConsumer;
import javax.inject.Singleton;
import org.observertc.webrtc.common.reports.MediaStreamSampleRecordReport;

@Singleton
public class MediaStreamSampleRecordReportUpdater implements BiConsumer<MediaStreamSampleRecordReport, Long> {

	public void update(MediaStreamSampleRecordReport record, Integer value) {
		this.accept(record, value.longValue());
	}

	@Override
	public void accept(MediaStreamSampleRecordReport record, Long value) {
		++record.count;
		record.sum += value;
		if (record.minimum == null || value < record.minimum) {
			record.minimum = value;
		}
		if (record.maximum == null || record.maximum < value) {
			record.maximum = value;
		}
	}
}
