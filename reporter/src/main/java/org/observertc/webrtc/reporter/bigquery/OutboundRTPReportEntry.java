package org.observertc.webrtc.reporter.bigquery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.MediaType;
import org.observertc.webrtc.common.reports.OutboundRTPReport;
import org.observertc.webrtc.reporter.TimeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboundRTPReportEntry implements BigQueryEntry {
	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String SSRC_FIELD_NAME = "SSRC";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String BYTES_SENT_FIELD_NAME = "bytesSent";
	public static final String ENCODER_IMPLEMENTATION_FIELD_NAME = "encoderImplementation";
	public static final String FIR_COUNT_FIELD_NAME = "firCount";
	public static final String FRAMES_ENCODED_FIELD_NAME = "framesEncoded";
	public static final String NACK_COUNT_FIELD_NAME = "nackCount";
	public static final String HEADER_BYTES_SENT_FIELD_NAME = "headerBytesSent";
	public static final String KEYFRAMES_ENCODED_FIELD_NAME = "keyFramesEncoded";
	public static final String MEDIA_TYPE_FIELD_NAME = "mediaType";
	public static final String PACKETS_SENT_FIELD_NAME = "packetsSent";
	public static final String PLI_COUNT_FIELD_NAME = "pliCount";
	public static final String QP_SUM_FIELD_NAME = "qpSum";
	public static final String QUALITY_LIMITATION_REASON_FIELD_NAME = "qualityLimitationReason";
	public static final String QUALITY_LIMITATION_RESOLUTION_CHANGES_FIELD_NAME = "qualityLimitationResolutionChanges";
	public static final String RETRANSMITTED_BYTES_FIELD_NAME = "retransmittedBytesSent";
	public static final String RETRANSMITTED_PACKETS_SENT_FIELD_NAME = "retransmittedPacketsSent";
	public static final String TOTAL_ENCODED_TIME_FIELD_NAME = "totalEncodeTime";
	public static final String TOTAL_PACKET_SEND_DELAY_FIELD_NAME = "totalPacketSendDelay";
	public static final String TOTAL_ENCODED_BYTES_TARGET_FIELD_NAME = "totalEncodedBytesTarget";


	private static Logger logger = LoggerFactory.getLogger(OutboundRTPReportEntry.class);

	public static OutboundRTPReportEntry from(
			OutboundRTPReport report) {
		OutboundRTPReportEntry result = new OutboundRTPReportEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withTimestamp(report.timestamp)
				.withMediaType(report.mediaType)
				.withBytesSent(report.bytesSent)
				.withEncoderImplementation(report.encoderImplementation)
				.withFirCount(report.firCount)
				.withFramesEncoded(report.framesEncoded)
				.withHeaderBytesSent(report.headerBytesSent)
				.withKeyFramesEncoded(report.keyFramesEncoded)
				.withNackCount(report.nackCount)
				.withPacketsSent(report.packetsSent)
				.withPLICount(report.pliCount)
				.withQPSum(report.qpSum)
				.withQualityLimitationReason(report.qualityLimitationReason)
				.withQualityLimitationResolutionChanges(report.qualityLimitationResolutionChanges)
				.withRetransmittedBytesSent(report.retransmittedBytesSent)
				.withRetransmittedPacketsSent(report.retransmittedPacketsSent)
				.withTotalEncodedTime(report.totalEncodeTime)
				.withTotalPacketsSendDelay(report.totalPacketSendDelay)
				.withTotalEncodedByesTarget(report.totalEncodedBytesTarget);
		return result;
	}

	private OutboundRTPReportEntry withEncoderImplementation(String value) {
		this.values.put(ENCODER_IMPLEMENTATION_FIELD_NAME, value);
		return this;
	}

	private final Map<String, Object> values;

	public OutboundRTPReportEntry() {
		this.values = new HashMap<>();
	}

	public OutboundRTPReportEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}


	public OutboundRTPReportEntry withTimestamp(LocalDateTime value) {
		if (value == null) {
			logger.warn("No valid sample timestamp");
			return this;
		}
		Long epoch = TimeConverter.GMTLocalDateTimeToEpoch(value);
		this.values.put(TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	public OutboundRTPReportEntry withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public OutboundRTPReportEntry withSSRC(Long value) {
		this.values.put(SSRC_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withMediaType(MediaType value) {
		String name = null;
		if (value != null) {
			name = value.name();
		}
		this.values.put(MEDIA_TYPE_FIELD_NAME, name);
		return this;
	}

	public OutboundRTPReportEntry withBytesSent(Long value) {
		this.values.put(BYTES_SENT_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withFirCount(Integer value) {
		this.values.put(FIR_COUNT_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withFramesEncoded(Integer value) {
		this.values.put(FRAMES_ENCODED_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withHeaderBytesSent(Long value) {
		this.values.put(HEADER_BYTES_SENT_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withKeyFramesEncoded(Integer value) {
		this.values.put(KEYFRAMES_ENCODED_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withTotalEncodedByesTarget(Long value) {
		this.values.put(TOTAL_ENCODED_BYTES_TARGET_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withNackCount(Integer value) {
		this.values.put(NACK_COUNT_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withPacketsSent(Integer value) {
		this.values.put(PACKETS_SENT_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withPLICount(Integer value) {
		this.values.put(PLI_COUNT_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withQPSum(Double value) {
		this.values.put(QP_SUM_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withQualityLimitationReason(String value) {
		this.values.put(QUALITY_LIMITATION_REASON_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withQualityLimitationResolutionChanges(Double value) {
		this.values.put(QUALITY_LIMITATION_RESOLUTION_CHANGES_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withRetransmittedBytesSent(Long value) {
		this.values.put(RETRANSMITTED_BYTES_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withRetransmittedPacketsSent(Integer value) {
		this.values.put(RETRANSMITTED_PACKETS_SENT_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withTotalEncodedTime(Long value) {
		this.values.put(TOTAL_ENCODED_TIME_FIELD_NAME, value);
		return this;
	}

	public OutboundRTPReportEntry withTotalPacketsSendDelay(Double value) {
		this.values.put(TOTAL_PACKET_SEND_DELAY_FIELD_NAME, value);
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		return this.values;
	}
}
