package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.InboundRTPReport;
import org.observertc.webrtc.common.reports.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboundRTPReportEntry implements BigQueryEntry {
	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String SSRC_FIELD_NAME = "SSRC";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String BYTES_RECEIVED_FIELD_NAME = "bytesReceived";
	public static final String FIR_COUNT_FIELD_NAME = "firCount";
	public static final String FRAMES_DECODED_FIELD_NAME = "framesDecoded";
	public static final String NACK_COUNT_FIELD_NAME = "nackCount";
	public static final String HEADER_BYTES_RECEIVED_FIELD_NAME = "headerBytesReceived";
	public static final String KEYFRAMES_DECODED_FIELD_NAME = "keyFramesDecoded";
	public static final String MEDIA_TYPE_FIELD_NAME = "mediaType";
	public static final String PACKETS_RECEIVED_FIELD_NAME = "packetsReceived";
	public static final String PLI_COUNT_FIELD_NAME = "pliCount";
	public static final String QP_SUM_FIELD_NAME = "qpSum";
	public static final String TOTAL_DECODE_TIME_FIELD_NAME = "totalDecodeTime";
	public static final String TOTAL_INTERFRAME_DELAY_FIELD_NAME = "totalInterFrameDelay";
	public static final String TOTAL_SQUARED_INITER_FREAME_DELAY_FIELD_NAME = "totalSquaredInterFrameDelay";
	public static final String PACKETS_LOST_FIELD_NAME = "packetsLost";
	public static final String JITTER_FIELD_NAME = "jitter";
	public static final String ESTIMATED_PLAYOUT_TIMESTAMP_FIELD_NAME = "estimatedPlayoutTimestamp";
	public static final String DECODER_IMPLEMENTATION_FIELD_NAME = "decoderImplementation";
	public static final String FEC_PACKETS_DISCARDED_FIELD_NAME = "FECPacketsDiscarded";
	public static final String LAST_PACKET_RECEIVED_TIMESTAMP = "lastPacketReceivedTimestamp";
	public static final String FEC_PACKETS_RECEIVED_FIELD_NAME = "FECPacketsReceived";

	private static Logger logger = LoggerFactory.getLogger(InboundRTPReportEntry.class);

	public static InboundRTPReportEntry from(
			InboundRTPReport report) {
		InboundRTPReportEntry result = new InboundRTPReportEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withSSRC(report.SSRC)
				.withTimestamp(report.timestamp)
				.withMediaType(report.mediaType)
				.withBytesReceived(report.bytesReceived)
				.withFirCount(report.firCount)
				.withFramesDecoded(report.framesDecoded)
				.withHeaderBytesReceived(report.headerBytesReceived)
				.withKeyFramesDecoded(report.keyFramesDecoded)
				.withNackCount(report.nackCount)
				.withPacketsReceived(report.packetsReceived)
				.withPLICount(report.pliCount)
				.withQPSum(report.qpSum)
				.withtDecoderImplementation(report.decoderImplementation)
				.withEstimatedPlayoutTimestamp(report.estimatedPlayoutTimestamp)
				.withJitter(report.jitter)
				.withLastPacketReceivedTimestamp(report.lastPacketReceivedTimestamp)
				.withPacketsLost(report.packetsLost)
				.withTotalDecodeTime(report.totalDecodeTime)
				.withTotalInterFrameDelay(report.totalInterFrameDelay)
				.withTotalSquaredInterFrameDelay(report.totalSquaredInterFrameDelay)
				.withFECPacketsDiscarded(report.fecPacketsDiscarded)
				.withFECPacketsReceived(report.fecPacketsReceived);
		return result;
	}

	private final Map<String, Object> values;

	public InboundRTPReportEntry() {
		this.values = new HashMap<>();
	}

	public InboundRTPReportEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}


	public InboundRTPReportEntry withTimestamp(LocalDateTime value) {
		if (value == null) {
			logger.warn("No valid sample timestamp");
			return this;
		}
		Long epoch = BigQueryServiceTimeConverter.getInstance().toEpoch(value);
		this.values.put(TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	public InboundRTPReportEntry withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public InboundRTPReportEntry withSSRC(Long value) {
		this.values.put(SSRC_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withMediaType(MediaType value) {
		this.values.put(MEDIA_TYPE_FIELD_NAME, value.name());
		return this;
	}

	public InboundRTPReportEntry withBytesReceived(Long value) {
		this.values.put(BYTES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withFirCount(Integer value) {
		this.values.put(FIR_COUNT_FIELD_NAME, value);
		return this;
	}


	public InboundRTPReportEntry withFramesDecoded(Integer value) {
		this.values.put(FRAMES_DECODED_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withHeaderBytesReceived(Long value) {
		this.values.put(HEADER_BYTES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withKeyFramesDecoded(Integer value) {
		this.values.put(KEYFRAMES_DECODED_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withNackCount(Integer value) {
		this.values.put(NACK_COUNT_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withPacketsReceived(Integer value) {
		this.values.put(PACKETS_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withPLICount(Integer value) {
		this.values.put(PLI_COUNT_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withQPSum(Double value) {
		this.values.put(QP_SUM_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withTotalDecodeTime(Double value) {
		this.values.put(TOTAL_DECODE_TIME_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withTotalInterFrameDelay(Double value) {
		this.values.put(TOTAL_INTERFRAME_DELAY_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withTotalSquaredInterFrameDelay(Double value) {
		this.values.put(TOTAL_SQUARED_INITER_FREAME_DELAY_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withPacketsLost(Integer value) {
		this.values.put(TOTAL_DECODE_TIME_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withFECPacketsDiscarded(Integer value) {
		this.values.put(FEC_PACKETS_DISCARDED_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withFECPacketsReceived(Integer value) {
		this.values.put(FEC_PACKETS_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withLastPacketReceivedTimestamp(Long value) {
		this.values.put(LAST_PACKET_RECEIVED_TIMESTAMP, value);
		return this;
	}

	public InboundRTPReportEntry withPacketsLost(Long value) {
		this.values.put(PACKETS_LOST_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withJitter(Double value) {
		this.values.put(JITTER_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withEstimatedPlayoutTimestamp(Long value) {
		this.values.put(ESTIMATED_PLAYOUT_TIMESTAMP_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withtDecoderImplementation(String value) {
		this.values.put(DECODER_IMPLEMENTATION_FIELD_NAME, value);
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		return this.values;
	}
}
