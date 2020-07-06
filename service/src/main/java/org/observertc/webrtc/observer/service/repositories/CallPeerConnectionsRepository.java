package org.observertc.webrtc.observer.service.repositories;

import static org.jooq.impl.DSL.row;
import org.observertc.webrtc.observer.service.UUIDAdapter;
import org.observertc.webrtc.observer.service.jooq.Tables;
import org.observertc.webrtc.observer.service.jooq.tables.records.CallpeerconnectionsRecord;
import org.observertc.webrtc.observer.service.model.CallPeerConnectionsEntry;
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
import org.jooq.InsertValuesStep2;
import org.jooq.Record2;
import org.jooq.RecordHandler;
import org.jooq.Row2;
import org.jooq.impl.DSL;

@Singleton
public class CallPeerConnectionsRepository implements CrudRepository<CallPeerConnectionsEntry, UUID> {
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

	public CallPeerConnectionsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
		this.jooqExecuteWrapper = new JooqBulkWrapper(contextProvider, DEFAULT_BULK_SIZE);
	}

	@NonNull
	@Override
	public <S extends CallPeerConnectionsEntry> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(Tables.CALLPEERCONNECTIONS)
				.columns(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, Tables.CALLPEERCONNECTIONS.CALLID)
				.values(UUIDAdapter.toBytes(entity.peerConnectionUUID), UUIDAdapter.toBytes(entity.callUUID))
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends CallPeerConnectionsEntry> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(Tables.CALLPEERCONNECTIONS)
				.columns(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, Tables.CALLPEERCONNECTIONS.CALLID)
				.values(UUIDAdapter.toBytes(entity.peerConnectionUUID), UUIDAdapter.toBytes(entity.callUUID))
				.onDuplicateKeyUpdate()
				.set(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, UUIDAdapter.toBytes(entity.peerConnectionUUID))
				.set(Tables.CALLPEERCONNECTIONS.CALLID, UUIDAdapter.toBytes(entity.callUUID))
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends CallPeerConnectionsEntry> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		this.jooqExecuteWrapper.execute((context, items) -> {
			InsertValuesStep2<CallpeerconnectionsRecord, byte[], byte[]> sql =
					context.insertInto(Tables.CALLPEERCONNECTIONS,
							Tables.CALLPEERCONNECTIONS.PEERCONNECTION,
							Tables.CALLPEERCONNECTIONS.CALLID);
			for (Iterator<S> it = items.iterator(); it.hasNext(); ) {
				CallPeerConnectionsEntry callPeerConnectionsEntry = it.next();
				byte[] peerConnection = UUIDAdapter.toBytes(callPeerConnectionsEntry.peerConnectionUUID);
				byte[] callUUID = UUIDAdapter.toBytes(callPeerConnectionsEntry.callUUID);
				sql.values(peerConnection, callUUID);
			}
			sql.onDuplicateKeyUpdate()
					.set(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, values(Tables.CALLPEERCONNECTIONS.PEERCONNECTION))
					.set(Tables.CALLPEERCONNECTIONS.CALLID, values(Tables.CALLPEERCONNECTIONS.CALLID))
					.execute();
		}, entities);
		return entities;
	}

	@NonNull
	@Override
	public Optional<CallPeerConnectionsEntry> findById(@NonNull @NotNull UUID peerConnection) {
		return this.contextProvider.get()
				.select(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, Tables.CALLPEERCONNECTIONS.CALLID)
				.from(Tables.CALLPEERCONNECTIONS)
				.where(Tables.CALLPEERCONNECTIONS.PEERCONNECTION.eq(UUIDAdapter.toBytes(peerConnection)))
				.fetchOptionalInto(CallPeerConnectionsEntry.class);
	}

	public void fetchByIds(@NonNull @NotNull Iterable<UUID> peerConnections, Consumer<CallPeerConnectionsEntry> callMapEntryConsumer) {
		Stream<byte[]> peerConnectionUUIDs = StreamSupport.stream(peerConnections.spliterator(), false).map(UUIDAdapter::toBytes);
		this.contextProvider.get()
				.select(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, Tables.CALLPEERCONNECTIONS.CALLID)
				.from(Tables.CALLPEERCONNECTIONS)
				.where(Tables.CALLPEERCONNECTIONS.PEERCONNECTION.in(peerConnectionUUIDs.collect(Collectors.toList())))
				.fetchInto(new RecordHandler<Record2<byte[], byte[]>>() {
					@Override
					public void next(Record2<byte[], byte[]> record) {
						CallPeerConnectionsEntry callPeerConnectionsEntry = new CallPeerConnectionsEntry();
						callPeerConnectionsEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value1());
						callPeerConnectionsEntry.callUUID = UUIDAdapter.toUUID(record.value2());
						callMapEntryConsumer.accept(callPeerConnectionsEntry);
					}
				});
	}

	public void getCallMapsForCallUUIDs(@NonNull @NotNull Iterable<UUID> callUUIDs, Consumer<CallPeerConnectionsEntry> callMapEntryConsumer) {
		Stream<byte[]> callUUIDsStream = StreamSupport.stream(callUUIDs.spliterator(), false).map(UUIDAdapter::toBytes);
		List<byte[]> list = callUUIDsStream.collect(Collectors.toList());
		this.contextProvider.get()
				.select(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, Tables.CALLPEERCONNECTIONS.CALLID)
				.from(Tables.CALLPEERCONNECTIONS)
				.where(Tables.CALLPEERCONNECTIONS.CALLID.in(list))
//				.where(Tables.CALLMAP.CALLID.in(callUUIDsStream.collect(Collectors.toList())))
				.fetchInto(new RecordHandler<Record2<byte[], byte[]>>() {
					@Override
					public void next(Record2<byte[], byte[]> record) {
						CallPeerConnectionsEntry callPeerConnectionsEntry = new CallPeerConnectionsEntry();
						callPeerConnectionsEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value1());
						callPeerConnectionsEntry.callUUID = UUIDAdapter.toUUID(record.value2());
						callMapEntryConsumer.accept(callPeerConnectionsEntry);
					}
				});

	}

	public void getCallMapsForPeerConnectionUUIDs(@NonNull @NotNull Iterable<UUID> peerConnectionUUIDs,
												  Consumer<CallPeerConnectionsEntry> callMapEntryConsumer) {
		Stream<byte[]> peerConnectionUUIDsStream = StreamSupport.stream(peerConnectionUUIDs.spliterator(), false).map(UUIDAdapter::toBytes);
		this.contextProvider.get()
				.select(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, Tables.CALLPEERCONNECTIONS.CALLID)
				.from(Tables.CALLPEERCONNECTIONS)
				.where(Tables.CALLPEERCONNECTIONS.PEERCONNECTION.in(peerConnectionUUIDsStream.collect(Collectors.toList())))
				.fetchInto(new RecordHandler<Record2<byte[], byte[]>>() {
					@Override
					public void next(Record2<byte[], byte[]> record) {
						CallPeerConnectionsEntry callPeerConnectionsEntry = new CallPeerConnectionsEntry();
						callPeerConnectionsEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value1());
						callPeerConnectionsEntry.callUUID = UUIDAdapter.toUUID(record.value2());
						callMapEntryConsumer.accept(callPeerConnectionsEntry);
					}
				});

	}

	@Override
	public boolean existsById(@NonNull @NotNull UUID peerConnection) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(Tables.PEERCONNECTIONSSRCS)
						.where(Tables.CALLPEERCONNECTIONS.PEERCONNECTION.eq(UUIDAdapter.toBytes(peerConnection)))
		);
	}

	@NonNull
	@Override
	public Iterable<CallPeerConnectionsEntry> findAll() {
		Long tableSize = this.count();
		int bulkSize = DEFAULT_BULK_SIZE;
		return () -> this.jooqExecuteWrapper.retrieve((context, offset) -> {
			List<CallPeerConnectionsEntry> result = context
					.select(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, Tables.CALLPEERCONNECTIONS.CALLID)
					.from(Tables.CALLPEERCONNECTIONS)
					.offset(offset)
					.limit(bulkSize)
					.fetch().into(CallPeerConnectionsEntry.class);
			return result.iterator();
		}, tableSize.intValue());
	}

	@Override
	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(Tables.CALLPEERCONNECTIONS)
				.fetchOne(0, long.class);
	}

	@Override
	public void deleteById(@NonNull @NotNull UUID peerConnection) {
		this.contextProvider.get().deleteFrom(Tables.CALLPEERCONNECTIONS)
				.where(Tables.CALLPEERCONNECTIONS.PEERCONNECTION.eq(UUIDAdapter.toBytes(peerConnection)))
				.execute();

	}

	@Override
	public void delete(@NonNull @NotNull CallPeerConnectionsEntry entity) {
		this.contextProvider.get().deleteFrom(Tables.PEERCONNECTIONSSRCS)
				.where(row(Tables.CALLPEERCONNECTIONS.PEERCONNECTION, Tables.CALLPEERCONNECTIONS.CALLID).eq(UUIDAdapter.toBytes(entity.peerConnectionUUID), UUIDAdapter.toBytes(entity.callUUID)))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends CallPeerConnectionsEntry> entities) {
		Iterator<? extends CallPeerConnectionsEntry> iterator = entities.iterator();
		this.jooqExecuteWrapper.execute((context, items) -> {

			List<Row2<byte[], byte[]>> keys = StreamSupport.stream(entities
					.spliterator(), false)
					.map(entity -> row(
							UUIDAdapter.toBytes(entity.peerConnectionUUID),
							UUIDAdapter.toBytes(entity.callUUID))
					).collect(Collectors.toList());

			context
					.deleteFrom(Tables.CALLPEERCONNECTIONS)
					.where(row(Tables.CALLPEERCONNECTIONS.PEERCONNECTION,
							Tables.CALLPEERCONNECTIONS.CALLID).in(keys))
					.execute();
		}, entities);
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(Tables.CALLPEERCONNECTIONS);
	}

	public void deleteByIds(@NonNull @NotNull Iterable<UUID> peerConnections) {
		List<byte[]> peerConnectionsInBytes = new LinkedList<>();
		for (Iterator<UUID> it = peerConnections.iterator(); it.hasNext(); ) {
			peerConnectionsInBytes.add(UUIDAdapter.toBytes(it.next()));
		}

		this.contextProvider.get().deleteFrom(Tables.CALLPEERCONNECTIONS)
				.where(Tables.CALLPEERCONNECTIONS.PEERCONNECTION
						.in(peerConnectionsInBytes))
				.execute();
	}

	public Map<UUID, Integer> retrieveParticipantsPerCalls(Set<UUID> peerConnections) {
		Map<UUID, Integer> result = new HashMap<>();
		this.contextProvider.get()
				.select(Tables.CALLPEERCONNECTIONS.CALLID, DSL.count(Tables.CALLPEERCONNECTIONS.PEERCONNECTION))
				.from(Tables.CALLPEERCONNECTIONS)
				.where(Tables.CALLPEERCONNECTIONS.PEERCONNECTION.in(
						peerConnections.stream().map(UUIDAdapter::toBytes).collect(Collectors.toList())
				)).groupBy(Tables.CALLPEERCONNECTIONS.CALLID).fetchInto(new RecordHandler<Record2<byte[], Integer>>() {
			@Override
			public void next(Record2<byte[], Integer> record) {
				result.put(UUIDAdapter.toUUID(record.value1()), record.value2());
			}
		});
		return result;
	}

	public void removePeerConnections(Set<UUID> peerConnections) {
		this.contextProvider.get()
				.deleteFrom(Tables.CALLPEERCONNECTIONS)
				.where(Tables.CALLPEERCONNECTIONS.PEERCONNECTION.in(
						peerConnections.stream().map(UUIDAdapter::toBytes).collect(Collectors.toList())
				)).execute();
	}
}