package org.observertc.webrtc.service.repositories.mappers;

import org.jooq.Record;
import org.jooq.RecordMapper;
import org.observertc.webrtc.service.UUIDAdapter;
import org.observertc.webrtc.service.dto.InboundStreamMeasurementDTO;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.jooq.tables.Inboundstreammeasurements;

public class InboundstreammeasurementsRecordMapper<R extends Record> implements RecordMapper<R, InboundStreamMeasurementDTO> {
	private final static Inboundstreammeasurements TABLE = Tables.INBOUNDSTREAMMEASUREMENTS;

	@Override
	public InboundStreamMeasurementDTO map(R record) {
		InboundStreamMeasurementDTO result = new InboundStreamMeasurementDTO();
		byte[] observerUUID = record.getValue(TABLE.OBSERVERUUID);
		byte[] peerConnectionUUID = record.getValue(TABLE.PEERCONNECTIONUUID);
		result.peerConnectionUUID = UUIDAdapter.toUUID(peerConnectionUUID);
		result.observerUUID = UUIDAdapter.toUUID(observerUUID);
		result.SSRC = record.getValue(TABLE.SSRC);
		result.firstSample = record.getValue(TABLE.FIRSTSAMPLE);
		result.lastSample = record.getValue(TABLE.LASTSAMPLE);
		result.reported = record.getValue(TABLE.REPORTED);
		result.samples_count = record.getValue(TABLE.SAMPLES_COUNT);

		result.bytesReceived_count = record.getValue(TABLE.BYTESRECEIVED_COUNT);
		result.bytesReceived_min = record.getValue(TABLE.BYTESRECEIVED_MIN);
		result.bytesReceived_max = record.getValue(TABLE.BYTESRECEIVED_MAX);
		result.bytesReceived_sum = record.getValue(TABLE.BYTESRECEIVED_SUM);
		result.bytesReceived_last = record.getValue(TABLE.BYTESRECEIVED_LAST);

		result.packetsReceived_count = record.getValue(TABLE.PACKETSRECEIVED_COUNT);
		result.packetsReceived_min = record.getValue(TABLE.PACKETSRECEIVED_MIN);
		result.packetsReceived_max = record.getValue(TABLE.PACKETSRECEIVED_MAX);
		result.packetsReceived_sum = record.getValue(TABLE.PACKETSRECEIVED_SUM);
		result.packetsReceived_last = record.getValue(TABLE.PACKETSRECEIVED_LAST);

		result.packetsLost_count = record.getValue(TABLE.PACKETSLOST_COUNT);
		result.packetsLost_min = record.getValue(TABLE.PACKETSLOST_MIN);
		result.packetsLost_max = record.getValue(TABLE.PACKETSLOST_MAX);
		result.packetsLost_sum = record.getValue(TABLE.PACKETSLOST_SUM);
		result.packetsLost_last = record.getValue(TABLE.PACKETSLOST_LAST);

		result.decoded_frames_count = record.getValue(TABLE.DECODED_FRAMES_COUNT);
		result.qpSum_count = record.getValue(TABLE.QPSUM_COUNT);
		result.qpSum_min = record.getValue(TABLE.QPSUM_MIN);
		result.qpSum_max = record.getValue(TABLE.QPSUM_MAX);
		result.qpSum_sum = record.getValue(TABLE.QPSUM_SUM);
		result.qpSum_last = record.getValue(TABLE.QPSUM_LAST);

		return result;
	}
}
