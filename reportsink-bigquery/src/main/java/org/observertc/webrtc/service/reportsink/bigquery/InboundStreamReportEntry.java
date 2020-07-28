package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.Map;
import org.observertc.webrtc.common.reports.InboundStreamReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InboundStreamReportEntry extends MediaStreamReportEntry<InboundStreamReportEntry> {

	private static Logger logger = LoggerFactory.getLogger(InboundStreamReportEntry.class);
	public static final String BYTES_RECEIVED_FIELD_NAME = "bytesReceived";
	public static final String PACKETS_LOST_FIELD_NAME = "packetsLost";
	public static final String PACKETS_RECEIVED_FIELD_NAME = "packetsReceived";

	public static InboundStreamReportEntry from(InboundStreamReport report) {
		MediaStreamReportEntryRecord bytesReceivedRecord = MediaStreamReportEntryRecord.from(report.bytesReceivedRecord);
		MediaStreamReportEntryRecord packetsReceivedRecord = MediaStreamReportEntryRecord.from(report.packetsReceivedRecord);
		MediaStreamReportEntryRecord packetsLostRecord = MediaStreamReportEntryRecord.from(report.packetsLostRecord);
		return new InboundStreamReportEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withFirstSampledTimestamp(report.firstSample)
				.withLastSampledTimestamp(report.lastSample)
				.withBytesReceivedRecord(bytesReceivedRecord)
				.withPacketsReceivedRecord(packetsReceivedRecord)
				.withPacketsLostRecord(packetsLostRecord);
	}


	public InboundStreamReportEntry withBytesReceivedRecord(MediaStreamReportEntryRecord record) {
		this.values.put(BYTES_RECEIVED_FIELD_NAME, record.toMap());
		return this;
	}

	public InboundStreamReportEntry withPacketsLostRecord(MediaStreamReportEntryRecord record) {
		this.values.put(PACKETS_LOST_FIELD_NAME, record.toMap());
		return this;
	}

	public InboundStreamReportEntry withPacketsReceivedRecord(MediaStreamReportEntryRecord record) {
		this.values.put(PACKETS_RECEIVED_FIELD_NAME, record.toMap());
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}


}