package com.observertc.gatekeeper.webrtcstat.repositories.mappers;

import com.observertc.gatekeeper.webrtcstat.UUIDAdapter;
import com.observertc.gatekeeper.webrtcstat.jooq.Tables;
import com.observertc.gatekeeper.webrtcstat.model.SSRCMapEntry;
import org.jooq.Record;
import org.jooq.RecordMapper;

public class SSRCMapEntryRecordMapper<R extends Record> implements RecordMapper<R, SSRCMapEntry> {
	@Override
	public SSRCMapEntry map(R record) {
		SSRCMapEntry result = new SSRCMapEntry();
		byte[] peerConnectionUUID = record.getValue(Tables.SSRCMAP.PEERCONNECTION);
		byte[] observerUUID = record.getValue(Tables.SSRCMAP.OBSERVER);
		result.SSRC = record.getValue(Tables.SSRCMAP.SSRC);
		result.peerConnectionUUID = UUIDAdapter.toUUID(peerConnectionUUID);
		result.observerUUID = UUIDAdapter.toUUID(observerUUID);
		return result;
	}
}
