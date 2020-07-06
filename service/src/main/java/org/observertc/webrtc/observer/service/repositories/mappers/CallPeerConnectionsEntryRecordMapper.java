package org.observertc.webrtc.observer.service.repositories.mappers;

import org.observertc.webrtc.observer.service.UUIDAdapter;
import org.observertc.webrtc.observer.service.jooq.Tables;
import org.observertc.webrtc.observer.service.model.CallPeerConnectionsEntry;
import org.jooq.Record;
import org.jooq.RecordMapper;

public class CallPeerConnectionsEntryRecordMapper<R extends Record> implements RecordMapper<R, CallPeerConnectionsEntry> {
	@Override
	public CallPeerConnectionsEntry map(R record) {
		CallPeerConnectionsEntry result = new CallPeerConnectionsEntry();
		byte[] peerConnectionUUID = record.getValue(Tables.CALLPEERCONNECTIONS.PEERCONNECTION);
		byte[] callUUID = record.getValue(Tables.CALLPEERCONNECTIONS.CALLID);
		result.callUUID = UUIDAdapter.toUUID(callUUID);
		result.peerConnectionUUID = UUIDAdapter.toUUID(peerConnectionUUID);
		return result;
	}
}
