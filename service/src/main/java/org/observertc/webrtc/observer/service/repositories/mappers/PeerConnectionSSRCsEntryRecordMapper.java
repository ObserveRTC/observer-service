package org.observertc.webrtc.observer.service.repositories.mappers;

import org.observertc.webrtc.observer.service.UUIDAdapter;
import org.observertc.webrtc.observer.service.jooq.Tables;
import org.observertc.webrtc.observer.service.model.PeerConnectionSSRCsEntry;
import org.jooq.Record;
import org.jooq.RecordMapper;

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
