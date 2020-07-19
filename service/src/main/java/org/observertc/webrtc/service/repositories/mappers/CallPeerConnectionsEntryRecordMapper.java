package org.observertc.webrtc.service.repositories.mappers;

import org.jooq.Record;
import org.jooq.RecordMapper;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.model.CallPeerConnectionsEntry;

public class CallPeerConnectionsEntryRecordMapper<R extends Record> implements RecordMapper<R, CallPeerConnectionsEntry> {
	@Override
	public CallPeerConnectionsEntry map(R record) {
		CallPeerConnectionsEntry result = new CallPeerConnectionsEntry();
		byte[] peerConnectionUUID = record.getValue(Tables.CALLPEERCONNECTIONS.PEERCONNECTIONUUID);
		byte[] callUUID = record.getValue(Tables.CALLPEERCONNECTIONS.CALLUUID);
		result.callUUID = UUIDAdapter.toUUID(callUUID);
		result.peerConnectionUUID = UUIDAdapter.toUUID(peerConnectionUUID);
		return result;
	}
}
