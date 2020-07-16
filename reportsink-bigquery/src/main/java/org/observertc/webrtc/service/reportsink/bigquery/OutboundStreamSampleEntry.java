package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.Map;
import org.observertc.webrtc.common.reports.OutboundStreamSampleReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OutboundStreamSampleEntry extends MediaStreamSampleEntry<OutboundStreamSampleEntry> {

	private static Logger logger = LoggerFactory.getLogger(OutboundStreamSampleEntry.class);
	public static final String BYTES_SENT_FIELD_NAME = "bytesSent";
	public static final String PACKETS_SENT_FIELD_NAME = "packetsSent";

	public static OutboundStreamSampleEntry from(OutboundStreamSampleReport report) {
		MediaStreamSampleEntryRecord bytesSentRecord = MediaStreamSampleEntryRecord.from(report.bytesSentRecord);
		MediaStreamSampleEntryRecord packetsSent = MediaStreamSampleEntryRecord.from(report.packetsSentRecord);
		return new OutboundStreamSampleEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withBytesSentRecord(bytesSentRecord)
				.withPacketsSentRecord(packetsSent)
				.withFirstSampledTimestamp(report.firstSample)
				.withLastSampledTimestamp(report.lastSample);
	}


	public OutboundStreamSampleEntry withBytesSentRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(BYTES_SENT_FIELD_NAME, record.toMap());
		return this;
	}

	public OutboundStreamSampleEntry withPacketsSentRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(PACKETS_SENT_FIELD_NAME, record.toMap());
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}


}