package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.Map;
import org.observertc.webrtc.common.reports.OutboundStreamReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OutboundStreamReportEntry extends MediaStreamReportEntry<OutboundStreamReportEntry> {

	private static Logger logger = LoggerFactory.getLogger(OutboundStreamReportEntry.class);
	public static final String BYTES_SENT_FIELD_NAME = "bytesSent";
	public static final String PACKETS_SENT_FIELD_NAME = "packetsSent";

	public static OutboundStreamReportEntry from(OutboundStreamReport report) {
		MediaStreamReportEntryRecord bytesSentRecord = MediaStreamReportEntryRecord.from(report.bytesSentRecord);
		MediaStreamReportEntryRecord packetsSent = MediaStreamReportEntryRecord.from(report.packetsSentRecord);
		return new OutboundStreamReportEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withBytesSentRecord(bytesSentRecord)
				.withPacketsSentRecord(packetsSent)
				.withFirstSampledTimestamp(report.firstSample)
				.withLastSampledTimestamp(report.lastSample);
	}


	public OutboundStreamReportEntry withBytesSentRecord(MediaStreamReportEntryRecord record) {
		this.values.put(BYTES_SENT_FIELD_NAME, record.toMap());
		return this;
	}

	public OutboundStreamReportEntry withPacketsSentRecord(MediaStreamReportEntryRecord record) {
		this.values.put(PACKETS_SENT_FIELD_NAME, record.toMap());
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}


}