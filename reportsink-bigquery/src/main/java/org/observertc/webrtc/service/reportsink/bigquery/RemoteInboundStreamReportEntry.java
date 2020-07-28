package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.Map;
import org.observertc.webrtc.common.reports.RemoteInboundStreamReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RemoteInboundStreamReportEntry extends MediaStreamReportEntry<RemoteInboundStreamReportEntry> {

	public static final String RTT_IN_MS_FIELD_NAME = "RTTInMs";

	private static Logger logger = LoggerFactory.getLogger(RemoteInboundStreamReportEntry.class);

	public static RemoteInboundStreamReportEntry from(RemoteInboundStreamReport report) {
		MediaStreamReportEntryRecord RTTInMsRecord = MediaStreamReportEntryRecord.from(report.RTTInMsRecord);
		return new RemoteInboundStreamReportEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withFirstSampledTimestamp(report.firstSample)
				.withLastSampledTimestamp(report.lastSample)
				.withRttInMsRecord(RTTInMsRecord);
	}


	public RemoteInboundStreamReportEntry withRttInMsRecord(MediaStreamReportEntryRecord record) {
		this.values.put(RTT_IN_MS_FIELD_NAME, record.toMap());
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}


}