/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.reporter.bigquery;

import java.util.HashMap;
import java.util.Map;
import org.observertc.webrtc.schemas.reports.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboundRTPReportEntry implements BigQueryEntry {
	public static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
	public static final String SERVICE_NAME_FIELD_NAME = "serviceName";
	public static final String CALL_NAME_FIELD_NAME = "callName";
	public static final String CUSTOMER_PROVIDED_FIELD_NAME = "customerProvided";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String BROWSERID_FIELD_NAME = "browserID";
	public static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitID";
	public static final String USER_ID_FIELD_NAME = "userID";

	public static final String SSRC_FIELD_NAME = "SSRC";
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

	private final Map<String, Object> values;

	public InboundRTPReportEntry() {
		this.values = new HashMap<>();
	}

	public InboundRTPReportEntry withServiceUUID(String value) {
		this.values.put(SERVICE_UUID_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withServiceName(String value) {
		this.values.put(SERVICE_NAME_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withCallName(String value) {
		this.values.put(CALL_NAME_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withUserId(String value) {
		this.values.put(USER_ID_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withCustomProvided(String value) {
		this.values.put(CUSTOMER_PROVIDED_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withPeerConnectionUUID(String value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withBrowserId(String value) {
		this.values.put(BROWSERID_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withTimestamp(Long value) {
		this.values.put(TIMESTAMP_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withMediaUnitId(String value) {
		this.values.put(MEDIA_UNIT_ID_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withSSRC(Long value) {
		this.values.put(SSRC_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withMediaType(MediaType mediaType) {
		if (mediaType == null) {
			return this;
		}
		return this.withMediaType(mediaType.name());
	}

	public InboundRTPReportEntry withMediaType(String value) {
		this.values.put(MEDIA_TYPE_FIELD_NAME, value);
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

	public InboundRTPReportEntry withLastPacketReceivedTimestamp(Double value) {
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

	public InboundRTPReportEntry withEstimatedPlayoutTimestamp(Double value) {
		this.values.put(ESTIMATED_PLAYOUT_TIMESTAMP_FIELD_NAME, value);
		return this;
	}

	public InboundRTPReportEntry withDecoderImplementation(String value) {
		this.values.put(DECODER_IMPLEMENTATION_FIELD_NAME, value);
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		return this.values;
	}
}
