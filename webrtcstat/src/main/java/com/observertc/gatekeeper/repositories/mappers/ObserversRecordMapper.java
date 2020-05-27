package com.observertc.gatekeeper.repositories.mappers;

import static com.observertc.gatekeeper.jooq.Tables.OBSERVERS;
import com.observertc.gatekeeper.dto.ObserverDTO;
import com.observertc.gatekeeper.repositories.UUIDAdapter;
import org.jooq.Record;
import org.jooq.RecordMapper;

public class ObserversRecordMapper<R extends Record> implements RecordMapper<R, ObserverDTO> {
	@Override
	public ObserverDTO map(R record) {
		ObserverDTO result = new ObserverDTO();
		byte[] uuid = record.getValue(OBSERVERS.UUID);
		result.uuid = UUIDAdapter.toUUID(uuid);
		result.name = record.getValue(OBSERVERS.NAME);
		result.description = record.getValue(OBSERVERS.DESCRIPTION);
		return result;
	}
}
