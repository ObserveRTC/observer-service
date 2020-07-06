package org.observertc.webrtc.observer.service.repositories.mappers;

import org.observertc.webrtc.observer.service.dto.ObserverDTO;
import org.observertc.webrtc.observer.service.UUIDAdapter;
import org.observertc.webrtc.observer.service.jooq.Tables;
import org.jooq.Record;
import org.jooq.RecordMapper;

public class ObserversRecordMapper<R extends Record> implements RecordMapper<R, ObserverDTO> {
	@Override
	public ObserverDTO map(R record) {
		ObserverDTO result = new ObserverDTO();
		byte[] uuid = record.getValue(Tables.OBSERVERS.UUID);
		result.uuid = UUIDAdapter.toUUID(uuid);
		result.name = record.getValue(Tables.OBSERVERS.NAME);
		result.description = record.getValue(Tables.OBSERVERS.DESCRIPTION);
		return result;
	}
}
