package com.observertc.gatekeeper.webrtcstat.repositories;

import static org.jooq.impl.DSL.row;
import com.observertc.gatekeeper.webrtcstat.UUIDAdapter;
import com.observertc.gatekeeper.webrtcstat.jooq.Tables;
import com.observertc.gatekeeper.webrtcstat.model.CallMapEntry;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.repository.CrudRepository;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.RecordHandler;
import org.jooq.Row2;
import org.jooq.impl.DSL;

@Singleton
public class CallMapRepository implements CrudRepository<CallMapEntry, UUID> {
	/**
	 * For insert into set
	 *
	 * @param field
	 * @param <T>
	 * @return
	 */
	private static <T> Field<T> values(Field<T> field) {
		return DSL.field("VALUES({0})", field.getDataType(), field);
	}

	/**
	 * Due to the fact that bulk operations increase the size of the query,
	 * this limit enforces to send the query when it reaches a certain
	 * number of entry.
	 */
	private static int DEFAULT_BULK_SIZE = 5000;
	private final JooqBulkWrapper jooqExecuteWrapper;
	private final IDSLContextProvider contextProvider;

	public CallMapRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
		this.jooqExecuteWrapper = new JooqBulkWrapper(contextProvider, DEFAULT_BULK_SIZE);
	}

	@NonNull
	@Override
	public <S extends CallMapEntry> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(Tables.CALLMAP)
				.columns(Tables.CALLMAP.PEERCONNECTION, Tables.CALLMAP.CALLID)
				.values(UUIDAdapter.toBytes(entity.peerConnectionUUID), UUIDAdapter.toBytes(entity.callUUID))
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends CallMapEntry> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(Tables.CALLMAP)
				.columns(Tables.CALLMAP.PEERCONNECTION, Tables.CALLMAP.CALLID)
				.values(UUIDAdapter.toBytes(entity.peerConnectionUUID), UUIDAdapter.toBytes(entity.callUUID))
				.onDuplicateKeyUpdate()
				.set(Tables.CALLMAP.PEERCONNECTION, UUIDAdapter.toBytes(entity.peerConnectionUUID))
				.set(Tables.CALLMAP.CALLID, UUIDAdapter.toBytes(entity.callUUID))
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends CallMapEntry> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		this.jooqExecuteWrapper.execute((context, items) -> {
			var sql =
					context.insertInto(Tables.CALLMAP,
							Tables.CALLMAP.PEERCONNECTION,
							Tables.CALLMAP.CALLID);
			for (Iterator<S> it = items.iterator(); it.hasNext(); ) {
				CallMapEntry callMapEntry = it.next();
				byte[] peerConnection = UUIDAdapter.toBytes(callMapEntry.peerConnectionUUID);
				byte[] callUUID = UUIDAdapter.toBytes(callMapEntry.callUUID);
				sql.values(peerConnection, callUUID);
			}
			sql.onDuplicateKeyUpdate()
					.set(Tables.CALLMAP.PEERCONNECTION, values(Tables.CALLMAP.PEERCONNECTION))
					.set(Tables.CALLMAP.CALLID, values(Tables.CALLMAP.CALLID))
					.execute();
		}, entities);
		return entities;
	}

	@NonNull
	@Override
	public Optional<CallMapEntry> findById(@NonNull @NotNull UUID peerConnection) {
		return this.contextProvider.get()
				.select(Tables.CALLMAP.PEERCONNECTION, Tables.CALLMAP.CALLID)
				.from(Tables.CALLMAP)
				.where(Tables.CALLMAP.PEERCONNECTION.eq(UUIDAdapter.toBytes(peerConnection)))
				.fetchOptionalInto(CallMapEntry.class);
	}

	public void fetchByIds(@NonNull @NotNull Iterable<UUID> peerConnections, Consumer<CallMapEntry> callMapEntryConsumer) {
		Stream<byte[]> peerConnectionUUIDs = StreamSupport.stream(peerConnections.spliterator(), false).map(UUIDAdapter::toBytes);
		this.contextProvider.get()
				.select(Tables.CALLMAP.PEERCONNECTION, Tables.CALLMAP.CALLID)
				.from(Tables.CALLMAP)
				.where(Tables.CALLMAP.PEERCONNECTION.in(peerConnectionUUIDs.collect(Collectors.toList())))
				.fetchInto(new RecordHandler<Record2<byte[], byte[]>>() {
					@Override
					public void next(Record2<byte[], byte[]> record) {
						CallMapEntry callMapEntry = new CallMapEntry();
						callMapEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value1());
						callMapEntry.callUUID = UUIDAdapter.toUUID(record.value2());
						callMapEntryConsumer.accept(callMapEntry);
					}
				});
	}

	public void getCallMapsForCallUUIDs(@NonNull @NotNull Iterable<UUID> callUUIDs, Consumer<CallMapEntry> callMapEntryConsumer) {
		Stream<byte[]> callUUIDsStream = StreamSupport.stream(callUUIDs.spliterator(), false).map(UUIDAdapter::toBytes);
		var list = callUUIDsStream.collect(Collectors.toList());
		this.contextProvider.get()
				.select(Tables.CALLMAP.PEERCONNECTION, Tables.CALLMAP.CALLID)
				.from(Tables.CALLMAP)
				.where(Tables.CALLMAP.CALLID.in(list))
//				.where(Tables.CALLMAP.CALLID.in(callUUIDsStream.collect(Collectors.toList())))
				.fetchInto(new RecordHandler<Record2<byte[], byte[]>>() {
					@Override
					public void next(Record2<byte[], byte[]> record) {
						CallMapEntry callMapEntry = new CallMapEntry();
						callMapEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value1());
						callMapEntry.callUUID = UUIDAdapter.toUUID(record.value2());
						callMapEntryConsumer.accept(callMapEntry);
					}
				});

	}

	public void getCallMapsForPeerConnectionUUIDs(@NonNull @NotNull Iterable<UUID> peerConnectionUUIDs,
												  Consumer<CallMapEntry> callMapEntryConsumer) {
		Stream<byte[]> peerConnectionUUIDsStream = StreamSupport.stream(peerConnectionUUIDs.spliterator(), false).map(UUIDAdapter::toBytes);
		this.contextProvider.get()
				.select(Tables.CALLMAP.PEERCONNECTION, Tables.CALLMAP.CALLID)
				.from(Tables.CALLMAP)
				.where(Tables.CALLMAP.PEERCONNECTION.in(peerConnectionUUIDsStream.collect(Collectors.toList())))
				.fetchInto(new RecordHandler<Record2<byte[], byte[]>>() {
					@Override
					public void next(Record2<byte[], byte[]> record) {
						CallMapEntry callMapEntry = new CallMapEntry();
						callMapEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value1());
						callMapEntry.callUUID = UUIDAdapter.toUUID(record.value2());
						callMapEntryConsumer.accept(callMapEntry);
					}
				});

	}

	@Override
	public boolean existsById(@NonNull @NotNull UUID peerConnection) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(Tables.SSRCMAP)
						.where(Tables.CALLMAP.PEERCONNECTION.eq(UUIDAdapter.toBytes(peerConnection)))
		);
	}

	@NonNull
	@Override
	public Iterable<CallMapEntry> findAll() {
		Long tableSize = this.count();
		int bulkSize = DEFAULT_BULK_SIZE;
		return () -> this.jooqExecuteWrapper.retrieve((context, offset) -> {
			var result = context
					.select(Tables.CALLMAP.PEERCONNECTION, Tables.CALLMAP.CALLID)
					.from(Tables.CALLMAP)
					.offset(offset)
					.limit(bulkSize)
					.fetch().into(CallMapEntry.class);
			return result.iterator();
		}, tableSize.intValue());
	}

	@Override
	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(Tables.CALLMAP)
				.fetchOne(0, long.class);
	}

	@Override
	public void deleteById(@NonNull @NotNull UUID peerConnection) {
		this.contextProvider.get().deleteFrom(Tables.CALLMAP)
				.where(Tables.CALLMAP.PEERCONNECTION.eq(UUIDAdapter.toBytes(peerConnection)))
				.execute();

	}

	@Override
	public void delete(@NonNull @NotNull CallMapEntry entity) {
		this.contextProvider.get().deleteFrom(Tables.SSRCMAP)
				.where(row(Tables.CALLMAP.PEERCONNECTION, Tables.CALLMAP.CALLID).eq(UUIDAdapter.toBytes(entity.peerConnectionUUID), UUIDAdapter.toBytes(entity.callUUID)))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends CallMapEntry> entities) {
		Iterator<? extends CallMapEntry> iterator = entities.iterator();
		this.jooqExecuteWrapper.execute((context, items) -> {

			List<Row2<byte[], byte[]>> keys = StreamSupport.stream(entities
					.spliterator(), false)
					.map(entity -> row(
							UUIDAdapter.toBytes(entity.peerConnectionUUID),
							UUIDAdapter.toBytes(entity.callUUID))
					).collect(Collectors.toList());

			context
					.deleteFrom(Tables.CALLMAP)
					.where(row(Tables.CALLMAP.PEERCONNECTION,
							Tables.CALLMAP.CALLID).in(keys))
					.execute();
		}, entities);
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(Tables.CALLMAP);
	}

	public void deleteByIds(@NonNull @NotNull Iterable<UUID> peerConnections) {
		List<byte[]> peerConnectionsInBytes = new LinkedList<>();
		for (Iterator<UUID> it = peerConnections.iterator(); it.hasNext(); ) {
			peerConnectionsInBytes.add(UUIDAdapter.toBytes(it.next()));
		}

		this.contextProvider.get().deleteFrom(Tables.CALLMAP)
				.where(Tables.CALLMAP.PEERCONNECTION
						.in(peerConnectionsInBytes))
				.execute();
	}

	public Map<UUID, Integer> retrieveParticipantsPerCalls(Set<UUID> peerConnections) {
		Map<UUID, Integer> result = new HashMap<>();
		this.contextProvider.get()
				.select(Tables.CALLMAP.CALLID, DSL.count(Tables.CALLMAP.PEERCONNECTION))
				.from(Tables.CALLMAP)
				.where(Tables.CALLMAP.PEERCONNECTION.in(
						peerConnections.stream().map(UUIDAdapter::toBytes).collect(Collectors.toList())
				)).groupBy(Tables.CALLMAP.CALLID).fetchInto(new RecordHandler<Record2<byte[], Integer>>() {
			@Override
			public void next(Record2<byte[], Integer> record) {
				result.put(UUIDAdapter.toUUID(record.value1()), record.value2());
			}
		});
		return result;
	}

	public void removePeerConnections(Set<UUID> peerConnections) {
		this.contextProvider.get()
				.deleteFrom(Tables.CALLMAP)
				.where(Tables.CALLMAP.PEERCONNECTION.in(
						peerConnections.stream().map(UUIDAdapter::toBytes).collect(Collectors.toList())
				)).execute();
	}
}