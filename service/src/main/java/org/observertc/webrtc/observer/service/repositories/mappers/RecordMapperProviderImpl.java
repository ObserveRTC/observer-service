package org.observertc.webrtc.observer.service.repositories.mappers;

import org.observertc.webrtc.observer.service.dto.ObserverDTO;
import org.observertc.webrtc.observer.service.model.CallPeerConnectionsEntry;
import org.observertc.webrtc.observer.service.model.PeerConnectionSSRCsEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Singleton;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.RecordMapperProvider;
import org.jooq.RecordType;
import org.jooq.impl.DefaultRecordMapper;

@Singleton
public class RecordMapperProviderImpl implements RecordMapperProvider {

	private Map<Class, RecordMapper> mappers = new HashMap<>();

	public RecordMapperProviderImpl() {
		this.mappers.put(ObserverDTO.class, new ObserversRecordMapper());
		this.mappers.put(PeerConnectionSSRCsEntry.class, new PeerConnectionSSRCsEntryRecordMapper());
		this.mappers.put(CallPeerConnectionsEntry.class, new CallPeerConnectionsEntryRecordMapper());
	}

	@Override
	public <R extends Record, E> RecordMapper<R, E> provide(RecordType<R> recordType, Class<? extends E> type) {
//		if (type == UUID.class) {
//			return new RecordMapper<R, E>() {
//				@Override
//				public E map(R record) {
//					byte[] uuid = (byte[]) record.getValue("uuid");
//					return (E) UUIDAdapter.toUUID(uuid);
//				}
//			};
//		}
		RecordMapper<R, E> result = this.mappers.get(type);
		if (Objects.isNull(result)) {
			result = new DefaultRecordMapper<>(recordType, type);
			this.mappers.put(type, result);
		}
		return result;
//		return new DefaultRecordMapper<>(recordType, type);
	}
}
