package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.Map;
import org.observertc.webrtc.common.reports.InboundStreamSampleReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InboundStreamSampleEntry extends MediaStreamSampleEntry<InboundStreamSampleEntry> {

	private static Logger logger = LoggerFactory.getLogger(InboundStreamSampleEntry.class);
	public static final String BYTES_RECEIVED_FIELD_NAME = "bytesReceived";
	public static final String PACKETS_LOST_FIELD_NAME = "packetsLost";
	public static final String PACKETS_RECEIVED_FIELD_NAME = "packetsReceived";

	public static InboundStreamSampleEntry from(InboundStreamSampleReport report) {
		MediaStreamSampleEntryRecord bytesReceivedRecord = MediaStreamSampleEntryRecord.from(report.bytesReceivedRecord);
		MediaStreamSampleEntryRecord packetsReceivedRecord = MediaStreamSampleEntryRecord.from(report.packetsReceivedRecord);
		MediaStreamSampleEntryRecord packetsLostRecord = MediaStreamSampleEntryRecord.from(report.packetsLostRecord);
		return new InboundStreamSampleEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withFirstSampledTimestamp(report.firstSample)
				.withLastSampledTimestamp(report.lastSample)
				.withBytesReceivedRecord(bytesReceivedRecord)
				.withPacketsReceivedRecord(packetsReceivedRecord)
				.withPacketsLostRecord(packetsLostRecord);
	}


	public InboundStreamSampleEntry withBytesReceivedRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(BYTES_RECEIVED_FIELD_NAME, record.toMap());
		return this;
	}

	public InboundStreamSampleEntry withPacketsLostRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(PACKETS_LOST_FIELD_NAME, record.toMap());
		return this;
	}

	public InboundStreamSampleEntry withPacketsReceivedRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(PACKETS_RECEIVED_FIELD_NAME, record.toMap());
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}


}