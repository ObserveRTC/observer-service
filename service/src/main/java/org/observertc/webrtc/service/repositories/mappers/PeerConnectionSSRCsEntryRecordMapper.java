package org.observertc.webrtc.service.repositories.mappers;

import org.jooq.Record;
import org.jooq.RecordMapper;
import org.observertc.webrtc.service.UUIDAdapter;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.model.PeerConnectionSSRCsEntry;

public class PeerConnectionSSRCsEntryRecordMapper<R extends Record> implements RecordMapper<R, PeerConnectionSSRCsEntry> {
	@Override
	public PeerConnectionSSRCsEntry map(R record) {
		PeerConnectionSSRCsEntry result = new PeerConnectionSSRCsEntry();
		byte[] peerConnectionUUID = record.getValue(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION);
		byte[] observerUUID = record.getValue(Tables.PEERCONNECTIONSSRCS.OBSERVER);
		result.SSRC = record.getValue(Tables.PEERCONNECTIONSSRCS.SSRC);
		result.peerConnectionUUID = UUIDAdapter.toUUID(peerConnectionUUID);
		result.observerUUID = UUIDAdapter.toUUID(observerUUID);
		return result;
	}
}
