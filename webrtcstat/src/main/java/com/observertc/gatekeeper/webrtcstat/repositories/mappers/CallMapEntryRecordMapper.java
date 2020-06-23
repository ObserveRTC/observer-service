package com.observertc.gatekeeper.webrtcstat.repositories.mappers;

import com.observertc.gatekeeper.webrtcstat.UUIDAdapter;
import com.observertc.gatekeeper.webrtcstat.jooq.Tables;
import com.observertc.gatekeeper.webrtcstat.model.CallMapEntry;
import org.jooq.Record;
import org.jooq.RecordMapper;

public class CallMapEntryRecordMapper<R extends Record> implements RecordMapper<R, CallMapEntry> {
	@Override
	public CallMapEntry map(R record) {
		CallMapEntry result = new CallMapEntry();
		byte[] peerConnectionUUID = record.getValue(Tables.CALLMAP.PEERCONNECTION);
		byte[] callUUID = record.getValue(Tables.CALLMAP.CALLID);
		result.callUUID = UUIDAdapter.toUUID(callUUID);
		result.peerConnectionUUID = UUIDAdapter.toUUID(peerConnectionUUID);
		return result;
	}
}
