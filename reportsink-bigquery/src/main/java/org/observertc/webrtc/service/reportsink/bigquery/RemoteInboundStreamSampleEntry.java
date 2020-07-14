package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.HashMap;
import java.util.Map;
import org.observertc.webrtc.common.reports.RemoteInboundStreamSampleReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RemoteInboundStreamSampleEntry extends MediaStreamSampleEntry<RemoteInboundStreamSampleEntry> {


	public static RemoteInboundStreamSampleEntry from(RemoteInboundStreamSampleReport report) {
		MediaStreamSampleEntryRecord RTTInMsRecord = MediaStreamSampleEntryRecord.from(report.RTTInMsRecord);
		return new RemoteInboundStreamSampleEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withFirstSampledTimestamp(report.firstSample)
				.withLastSampledTimestamp(report.lastSample)
				.withRttInMsRecord(RTTInMsRecord);
	}

	private static Logger logger = LoggerFactory.getLogger(RemoteInboundStreamSampleEntry.class);
	private static final String RTT_IN_MS_FIELD_NAME = "RTTInMs";

	private final Map<String, Object> values = new HashMap<>();

	public RemoteInboundStreamSampleEntry withRttInMsRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(RTT_IN_MS_FIELD_NAME, record.toMap());
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}


}