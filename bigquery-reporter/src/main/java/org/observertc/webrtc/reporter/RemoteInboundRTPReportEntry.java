package org.observertc.webrtc.reporter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.RemoteInboundRTPReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteInboundRTPReportEntry implements BigQueryEntry {
	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String SSRC_FIELD_NAME = "SSRC";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String RTT_IN_MS_FIELD_NAME = "RTT";
	public static final String PACKETSLOST_FIELD_NAME = "packetsLost";
	public static final String JITTER_FIELD_NAME = "jitter";
	public static final String CODEC_FIELD_NAME = "codec";
	public static final String MEDIA_TYPE_FIELD_NAME = "mediaType";

	private final Map<String, Object> values;

	private static Logger logger = LoggerFactory.getLogger(RemoteInboundRTPReportEntry.class);

	public static RemoteInboundRTPReportEntry from(RemoteInboundRTPReport report) {
		String mediaType = null;
		if (report.mediaType != null) {
			mediaType = report.mediaType.name();
		}

		return new RemoteInboundRTPReportEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withTimestamp(report.timestamp)
				.withPacketsLost(report.packetsLost)
				.withRTT(report.RTT)
				.withJitter(report.jitter)
				.withCodec(report.codec)
				.withMediaType(mediaType);

	}

	public RemoteInboundRTPReportEntry() {
		this.values = new HashMap<>();
	}

	public RemoteInboundRTPReportEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}


	public RemoteInboundRTPReportEntry withTimestamp(LocalDateTime value) {
		if (value == null) {
			logger.warn("No valid sample timestamp");
			return this;
		}
		Long epoch = BigQueryServiceTimeConverter.getInstance().toEpoch(value);
		this.values.put(TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	public RemoteInboundRTPReportEntry withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public RemoteInboundRTPReportEntry withSSRC(Long value) {
		this.values.put(SSRC_FIELD_NAME, value);
		return this;
	}

	public RemoteInboundRTPReportEntry withRTT(Double value) {
		this.values.put(RTT_IN_MS_FIELD_NAME, value);
		return this;
	}


	public RemoteInboundRTPReportEntry withPacketsLost(Integer value) {
		this.values.put(PACKETSLOST_FIELD_NAME, value);
		return this;
	}

	public RemoteInboundRTPReportEntry withJitter(Double value) {
		this.values.put(JITTER_FIELD_NAME, value);
		return this;
	}

	public RemoteInboundRTPReportEntry withCodec(String value) {
		this.values.put(CODEC_FIELD_NAME, value);
		return this;
	}

	public RemoteInboundRTPReportEntry withMediaType(String value) {
		this.values.put(MEDIA_TYPE_FIELD_NAME, value);
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}


}