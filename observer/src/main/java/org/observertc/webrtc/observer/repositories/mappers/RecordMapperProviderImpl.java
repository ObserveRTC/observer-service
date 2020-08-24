package org.observertc.webrtc.observer.repositories.mappers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Singleton;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.RecordMapperProvider;
import org.jooq.RecordType;
import org.jooq.impl.DefaultRecordMapper;
import org.observertc.webrtc.observer.dto.ObserverDTO;

@Singleton
public class RecordMapperProviderImpl implements RecordMapperProvider {

	private Map<Class, RecordMapper> mappers = new HashMap<>();

	public RecordMapperProviderImpl() {
		this.mappers.put(ObserverDTO.class, new ObserversRecordMapper());
	}

	@Override
	public <R extends Record, E> RecordMapper<R, E> provide(RecordType<R> recordType, Class<? extends E> type) {
		RecordMapper<R, E> result = this.mappers.get(type);
		if (Objects.isNull(result)) {
			result = new DefaultRecordMapper<>(recordType, type);
			this.mappers.put(type, result);
		}
		return result;
	}
}
