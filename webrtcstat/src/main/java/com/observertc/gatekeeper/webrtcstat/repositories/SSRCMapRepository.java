package com.observertc.gatekeeper.webrtcstat.repositories;

import static org.jooq.impl.DSL.row;
import com.observertc.gatekeeper.webrtcstat.UUIDAdapter;
import com.observertc.gatekeeper.webrtcstat.jooq.Tables;
import com.observertc.gatekeeper.webrtcstat.model.CallMapEntry;
import com.observertc.gatekeeper.webrtcstat.model.SSRCMapEntry;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.repository.CrudRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.RecordHandler;
import org.jooq.Row2;
import org.jooq.Row3;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SSRCMapRepository implements CrudRepository<SSRCMapEntry, Triplet<Long, UUID, UUID>> {
	private static final Logger logger = LoggerFactory.getLogger(SSRCMapRepository.class);

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

	public SSRCMapRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
		this.jooqExecuteWrapper = new JooqBulkWrapper(contextProvider, DEFAULT_BULK_SIZE);
	}

	@NonNull
	@Override
	public <S extends SSRCMapEntry> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(Tables.SSRCMAP)
				.columns(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.PEERCONNECTION, Tables.SSRCMAP.OBSERVER, Tables.SSRCMAP.UPDATED)
				.values(entity.SSRC, UUIDAdapter.toBytes(entity.peerConnectionUUID), UUIDAdapter.toBytes(entity.observerUUID), LocalDateTime.now())
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends SSRCMapEntry> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(Tables.SSRCMAP)
				.columns(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.OBSERVER, Tables.SSRCMAP.PEERCONNECTION, Tables.SSRCMAP.UPDATED)
				.values(entity.SSRC, UUIDAdapter.toBytes(entity.observerUUID), UUIDAdapter.toBytes(entity.peerConnectionUUID), LocalDateTime.now())
				.onDuplicateKeyUpdate()
				.set(Tables.SSRCMAP.OBSERVER, UUIDAdapter.toBytes(entity.observerUUID))
				.set(Tables.SSRCMAP.PEERCONNECTION, UUIDAdapter.toBytes(entity.peerConnectionUUID))
				.set(Tables.SSRCMAP.SSRC, entity.SSRC)
				.set(Tables.SSRCMAP.UPDATED, LocalDateTime.now())
				.execute();
		return entity;
	}

	public Iterable<UUID> getPeerConnections(Long SSRC, UUID observerUUID) {
		List<UUID> result = new ArrayList<>();
		DSLContext context = this.contextProvider.get();
		context.select(Tables.SSRCMAP.PEERCONNECTION)
				.from(Tables.SSRCMAP)
				.where(Tables.SSRCMAP.SSRC.eq(SSRC)
						.and(Tables.SSRCMAP.OBSERVER.eq(UUIDAdapter.toBytes(observerUUID))))
				.fetchInto(new RecordHandler<Record1<byte[]>>() {
					@Override
					public void next(Record1<byte[]> record) {
						result.add(UUIDAdapter.toUUID(record.component1()));
					}
				});
		return result;
	}

	public Iterable<Triplet<Long, UUID, Set<CallMapEntry>>> getPeerConnections(
			@NotNull Iterable<Pair<Long, UUID>> ssrcObserverTuples) {
		Set<Row2<Long, byte[]>> whereCondition = new HashSet<>();
		Iterator<Pair<Long, UUID>> ssrcObserverTuplesIt = ssrcObserverTuples.iterator();
		for (; ssrcObserverTuplesIt.hasNext(); ) {
			Pair<Long, UUID> ssrcObserverTuple = ssrcObserverTuplesIt.next();
			Long ssrc = ssrcObserverTuple.getValue0();
			byte[] observerUUID = UUIDAdapter.toBytes(ssrcObserverTuple.getValue1());
			whereCondition.add(row(ssrc, observerUUID));
		}
		Map<Pair<Long, UUID>, Set<CallMapEntry>> collectedResult = new HashMap<>();
		this.contextProvider.get().select(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.OBSERVER, Tables.SSRCMAP.PEERCONNECTION,
				Tables.CALLMAP.CALLID)
				.from(Tables.SSRCMAP)
				.leftJoin(Tables.CALLMAP).on(Tables.SSRCMAP.PEERCONNECTION.eq(Tables.CALLMAP.PEERCONNECTION))
				.where(row(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.OBSERVER).in(whereCondition))
				.orderBy(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.OBSERVER, Tables.SSRCMAP.PEERCONNECTION)
				.fetchInto(new RecordHandler<Record4<Long, byte[], byte[], byte[]>>() {
					@Override
					public void next(Record4<Long, byte[], byte[], byte[]> record) {
						Pair<Long, UUID> ssrcObserverTuple = Pair.with(
								record.value1(),
								UUIDAdapter.toUUID(record.value2())
						);
						Set<CallMapEntry> callMapEntries = collectedResult.getOrDefault(ssrcObserverTuple, new HashSet<>());
						CallMapEntry callMapEntry = new CallMapEntry();
						callMapEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value3());
						if (record.value4() != null) {
							callMapEntry.callUUID = UUIDAdapter.toUUID(record.value4());
						} else {
							callMapEntry.callUUID = null;
						}
						callMapEntries.add(callMapEntry);
						collectedResult.put(ssrcObserverTuple, callMapEntries);
					}
				});
		Iterator<Map.Entry<Pair<Long, UUID>, Set<CallMapEntry>>> resultIterator = collectedResult.entrySet().iterator();
		return () -> new Iterator<Triplet<Long, UUID, Set<CallMapEntry>>>() {
			@Override
			public boolean hasNext() {
				return resultIterator.hasNext();
			}

			@Override
			public Triplet<Long, UUID, Set<CallMapEntry>> next() {
				Map.Entry<Pair<Long, UUID>, Set<CallMapEntry>> entry = resultIterator.next();
				return Triplet.with(entry.getKey().getValue0(), entry.getKey().getValue1(), entry.getValue());
			}
		};
	}

	@NonNull
	@Override
	public <S extends SSRCMapEntry> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		this.jooqExecuteWrapper.execute((context, items) -> {
			var sql =
					context.insertInto(Tables.SSRCMAP,
							Tables.SSRCMAP.SSRC,
							Tables.SSRCMAP.PEERCONNECTION,
							Tables.SSRCMAP.OBSERVER,
							Tables.SSRCMAP.UPDATED);
			for (Iterator<S> it = items.iterator(); it.hasNext(); ) {
				SSRCMapEntry ssrcMapEntry = it.next();
				byte[] peerConnection = UUIDAdapter.toBytes(ssrcMapEntry.peerConnectionUUID);
				byte[] observer = UUIDAdapter.toBytes(ssrcMapEntry.observerUUID);
				sql.values(ssrcMapEntry.SSRC, peerConnection, observer, LocalDateTime.now());
			}
			sql.onDuplicateKeyUpdate()
					.set(Tables.SSRCMAP.SSRC, values(Tables.SSRCMAP.SSRC))
					.set(Tables.SSRCMAP.PEERCONNECTION, values(Tables.SSRCMAP.PEERCONNECTION))
					.set(Tables.SSRCMAP.OBSERVER, values(Tables.SSRCMAP.OBSERVER))
					.set(Tables.SSRCMAP.UPDATED, values(Tables.SSRCMAP.UPDATED))
					.execute();
		}, entities);
		return entities;
	}

	@NonNull
	@Override
	public Optional<SSRCMapEntry> findById(@NonNull @NotNull Triplet<Long, UUID, UUID> ssrcPCObserver) {
		return this.contextProvider.get()
				.select(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.PEERCONNECTION, Tables.SSRCMAP.OBSERVER)
				.from(Tables.SSRCMAP)
				.where(row(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.PEERCONNECTION, Tables.SSRCMAP.OBSERVER)
						.eq(ssrcPCObserver.getValue0(),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue1()),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue2())))
				.fetchOptionalInto(SSRCMapEntry.class);
	}

	@Override
	public boolean existsById(@NonNull @NotNull Triplet<Long, UUID, UUID> ssrcPCObserver) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(Tables.SSRCMAP)
						.where(row(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.PEERCONNECTION, Tables.SSRCMAP.OBSERVER)
								.eq(ssrcPCObserver.getValue0(),
										UUIDAdapter.toBytes(ssrcPCObserver.getValue1()),
										UUIDAdapter.toBytes(ssrcPCObserver.getValue2())))
		);
	}

	@NonNull
	@Override
	public Iterable<SSRCMapEntry> findAll() {
		Long tableSize = this.count();
		int bulkSize = DEFAULT_BULK_SIZE;
		return () -> this.jooqExecuteWrapper.retrieve((context, offset) -> {
			var result = context
					.select(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.PEERCONNECTION, Tables.SSRCMAP.OBSERVER, Tables.SSRCMAP.UPDATED)
					.from(Tables.SSRCMAP)
					.orderBy(Tables.SSRCMAP.UPDATED.desc())
					.offset(offset)
					.limit(bulkSize)
					.fetch().into(SSRCMapEntry.class);
			return result.iterator();
		}, tableSize.intValue());
	}

	@Override
	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(Tables.SSRCMAP)
				.fetchOne(0, long.class);
	}

	@Override
	public void deleteById(@NonNull @NotNull Triplet<Long, UUID, UUID> ssrcPCObserver) {
		this.contextProvider.get().deleteFrom(Tables.SSRCMAP)
				.where(row(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.PEERCONNECTION, Tables.SSRCMAP.OBSERVER)
						.eq(ssrcPCObserver.getValue0(),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue1()),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue2())))
				.execute();

	}

	@Override
	public void delete(@NonNull @NotNull SSRCMapEntry entity) {
		this.contextProvider.get().deleteFrom(Tables.SSRCMAP)
				.where(row(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.PEERCONNECTION, Tables.SSRCMAP.OBSERVER)
						.eq(entity.SSRC,
								UUIDAdapter.toBytes(entity.peerConnectionUUID),
								UUIDAdapter.toBytes(entity.observerUUID)))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends SSRCMapEntry> entities) {
		Iterator<? extends SSRCMapEntry> iterator = entities.iterator();
		this.jooqExecuteWrapper.execute((context, items) -> {

			List<Row3<Long, byte[], byte[]>> keys = StreamSupport.stream(entities
					.spliterator(), false)
					.map(entity -> row(entity.SSRC,
							UUIDAdapter.toBytes(entity.peerConnectionUUID),
							UUIDAdapter.toBytes(entity.observerUUID))
					).collect(Collectors.toList());

			context
					.deleteFrom(Tables.SSRCMAP)
					.where(row(Tables.SSRCMAP.SSRC,
							Tables.SSRCMAP.PEERCONNECTION,
							Tables.SSRCMAP.OBSERVER).in(keys))
					.execute();
		}, entities);
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(Tables.SSRCMAP);
	}

	public void retrieveSSRCObserverPeerConnectionCallIDsOlderThan(LocalDateTime borderTime,
																   Consumer<Quartet<Long, UUID, UUID, UUID>> recordHandler) {
		this.contextProvider.get()
				.select(Tables.SSRCMAP.SSRC, Tables.SSRCMAP.OBSERVER, Tables.SSRCMAP.PEERCONNECTION, Tables.CALLMAP.CALLID)
				.from(Tables.SSRCMAP)
				.leftJoin(Tables.CALLMAP)
				.on(Tables.SSRCMAP.PEERCONNECTION.eq(Tables.CALLMAP.PEERCONNECTION))
				.where(Tables.SSRCMAP.UPDATED.lt(borderTime))
				.fetchInto(new RecordHandler<Record4<Long, byte[], byte[], byte[]>>() {
					@Override
					public void next(Record4<Long, byte[], byte[], byte[]> record) {
						Long SSRC = record.value1();
						boolean error = false;
						UUID observer = null;
						if (record.value2() != null) {
							observer = UUIDAdapter.toUUID(record.value2());
						} else {
							error = true;
						}

						UUID peerConnectionUUID = null;
						if (record.value3() != null) {
							peerConnectionUUID = UUIDAdapter.toUUID(record.value3());
						} else {
							error = true;
						}

						UUID callUUID = null;
						if (record.value4() != null) {
							callUUID = UUIDAdapter.toUUID(record.value4());
						} else {
							error = true;
						}

						if (error) {
							logger.error("A value for < SSRC: {}, observer: {}, peerConnection: {}, call: {} >tuple is null", SSRC,
									observer,
									peerConnectionUUID, callUUID);
						}

						recordHandler.accept(Quartet.with(
								SSRC,
								observer,
								peerConnectionUUID,
								callUUID
						));
					}
				});
	}

	public void removePeerConnections(Collection<UUID> peerConnectionUUIDs) {
		this.contextProvider.get()
				.deleteFrom(Tables.SSRCMAP)
				.where(Tables.SSRCMAP.PEERCONNECTION.in(
						peerConnectionUUIDs.stream().map(UUIDAdapter::toBytes).collect(Collectors.toList())
				)).execute();
	}
}