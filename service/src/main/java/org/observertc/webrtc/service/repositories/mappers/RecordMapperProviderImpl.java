package org.observertc.webrtc.service.repositories.mappers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Singleton;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.RecordMapperProvider;
import org.jooq.RecordType;
import org.jooq.impl.DefaultRecordMapper;
import org.observertc.webrtc.service.dto.InboundStreamMeasurementDTO;
import org.observertc.webrtc.service.dto.ObserverDTO;
import org.observertc.webrtc.service.model.CallPeerConnectionsEntry;
import org.observertc.webrtc.service.model.PeerConnectionSSRCsEntry;

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
		RecordMapper<R, E> result = this.mappers.get(type);
		if (Objects.isNull(result)) {
			result = new DefaultRecordMapper<>(recordType, type);
			this.mappers.put(type, result);
		}
		return result;
	}
}
