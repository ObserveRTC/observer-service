package org.observertc.webrtc.service.processors.mediastreams;//package com.observertc.gatekeeper.webrtcstat.processors.samples;

import java.util.function.BiConsumer;
import javax.inject.Singleton;
import org.observertc.webrtc.common.reports.MediaStreamRecordReport;

@Singleton
public class MediaStreamSampleRecordReportUpdater implements BiConsumer<MediaStreamRecordReport, Long> {

	public void update(MediaStreamRecordReport record, Integer value) {
		this.accept(record, value.longValue());
	}

	@Override
	public void accept(MediaStreamRecordReport record, Long value) {
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
