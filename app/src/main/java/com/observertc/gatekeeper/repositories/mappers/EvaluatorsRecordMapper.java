package com.observertc.gatekeeper.repositories.mappers;

import static com.observertc.gatekeeper.jooq.Tables.EVALUATORS;
import com.observertc.gatekeeper.repositories.UUIDAdapter;
import com.observertc.gatekeeper.dto.EvaluatorDTO;
import org.jooq.Record;
import org.jooq.RecordMapper;

public class EvaluatorsRecordMapper<R extends Record> implements RecordMapper<R, EvaluatorDTO> {
	@Override
	public EvaluatorDTO map(R record) {
		EvaluatorDTO result = new EvaluatorDTO();
		byte[] uuid = record.getValue(EVALUATORS.UUID);
		result.uuid = UUIDAdapter.toUUID(uuid);
		result.name = record.getValue(EVALUATORS.NAME);
		result.description = record.getValue(EVALUATORS.DESCRIPTION);
		return result;
	}
}
