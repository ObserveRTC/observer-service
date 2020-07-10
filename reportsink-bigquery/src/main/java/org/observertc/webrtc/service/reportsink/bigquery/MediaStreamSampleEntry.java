package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.MediaStreamSampleReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MediaStreamSampleEntry implements BigQueryEntry {


	public static MediaStreamSampleEntry from(MediaStreamSampleReport report) {
		MediaStreamSampleEntryRecord bytesReceivedRecord = MediaStreamSampleEntryRecord.from(report.bytesReceivedRecord);
		MediaStreamSampleEntryRecord bytesSentRecord = MediaStreamSampleEntryRecord.from(report.bytesSentRecord);
		MediaStreamSampleEntryRecord packetsSent = MediaStreamSampleEntryRecord.from(report.packetsSentRecord);
		MediaStreamSampleEntryRecord packetsReceived = MediaStreamSampleEntryRecord.from(report.packetsReceivedRecord);
		MediaStreamSampleEntryRecord packetsLost = MediaStreamSampleEntryRecord.from(report.packetsLostRecord);
		MediaStreamSampleEntryRecord RTT = MediaStreamSampleEntryRecord.from(report.RTTRecord);
		return new MediaStreamSampleEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withBytesReceivedRecord(bytesReceivedRecord)
				.withBytesSentRecord(bytesSentRecord)
				.withPacketsLostRecord(packetsLost)
				.withPacketsReceivedRecord(packetsReceived)
				.withPacketsSentRecord(packetsSent)
				.withRTTRecord(RTT)
				.withFirstSampledTimestamp(report.firstSample)
				.withLastSampledTimestamp(report.lastSample);
	}

	private static Logger logger = LoggerFactory.getLogger(MediaStreamSampleEntry.class);
	private static final String OBSERVER_UUID_FIELD_NAME = "peerConnectionUUID";
	private static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	private static final String BYTES_RECEIVED_FIELD_NAME = "bytesReceived";
	private static final String BYTES_SENT_FIELD_NAME = "bytesSent";
	private static final String PACKETS_RECEIVED_FIELD_NAME = "packetsReceived";
	private static final String PACKETS_SENT_FIELD_NAME = "packetsSent";
	private static final String PACKETS_LOST_FIELD_NAME = "packetsLost";
	private static final String SSRC_FIELD_NAME = "SSRC";
	private static final String RTT_FIELD_NAME = "RTT";
	private static final String FIRST_SAMPLE_TIMESTAMP_FIELD_NAME = "firstSample";
	private static final String LAST_SAMPLE_TIMESTAMP_FIELD_NAME = "lastSample";

	private final Map<String, Object> values = new HashMap<>();

	public MediaStreamSampleEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}


	public MediaStreamSampleEntry withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public MediaStreamSampleEntry withSSRC(Long value) {
		this.values.put(SSRC_FIELD_NAME, value);
		return this;
	}

	public MediaStreamSampleEntry withRTTRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(RTT_FIELD_NAME, record.toMap());
		return this;
	}

	public MediaStreamSampleEntry withBytesReceivedRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(BYTES_RECEIVED_FIELD_NAME, record.toMap());
		return this;
	}

	public MediaStreamSampleEntry withBytesSentRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(BYTES_SENT_FIELD_NAME, record.toMap());
		return this;
	}

	public MediaStreamSampleEntry withPacketsSentRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(PACKETS_SENT_FIELD_NAME, record.toMap());
		return this;
	}

	public MediaStreamSampleEntry withPacketsLostRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(PACKETS_LOST_FIELD_NAME, record.toMap());
		return this;
	}

	public MediaStreamSampleEntry withPacketsReceivedRecord(MediaStreamSampleEntryRecord record) {
		this.values.put(PACKETS_RECEIVED_FIELD_NAME, record.toMap());
		return this;
	}

	public MediaStreamSampleEntry withFirstSampledTimestamp(LocalDateTime firstSample) {
		if (firstSample == null) {
			logger.warn("No First sample");
			return this;
		}
		ZoneId zoneId = ZoneId.systemDefault();
		Long epoch = firstSample.atZone(zoneId).toEpochSecond();
		this.values.put(FIRST_SAMPLE_TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	public MediaStreamSampleEntry withLastSampledTimestamp(LocalDateTime lastSample) {
		if (lastSample == null) {
			logger.warn("No last sample");
			return this;
		}
		ZoneId zoneId = ZoneId.systemDefault();
		Long epoch = lastSample.atZone(zoneId).toEpochSecond();
		this.values.put(LAST_SAMPLE_TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}


}