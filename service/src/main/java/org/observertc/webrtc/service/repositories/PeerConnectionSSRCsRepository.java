package org.observertc.webrtc.service.repositories;

import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.select;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertValuesStep4;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.RecordHandler;
import org.jooq.Row2;
import org.jooq.impl.DSL;
import org.observertc.webrtc.service.UUIDAdapter;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.jooq.tables.records.PeerconnectionssrcsRecord;
import org.observertc.webrtc.service.model.CallPeerConnectionsEntry;
import org.observertc.webrtc.service.model.PeerConnectionSSRCsEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PeerConnectionSSRCsRepository implements CrudRepository<PeerConnectionSSRCsEntry, Triplet<Long, UUID, UUID>> {
	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionSSRCsRepository.class);

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

	public PeerConnectionSSRCsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
		this.jooqExecuteWrapper = new JooqBulkWrapper(contextProvider, DEFAULT_BULK_SIZE);
	}

	@NonNull
	@Override
	public <S extends PeerConnectionSSRCsEntry> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(Tables.PEERCONNECTIONSSRCS)
				.columns(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.UPDATED)
				.values(entity.SSRC, UUIDAdapter.toBytes(entity.peerConnectionUUID), UUIDAdapter.toBytes(entity.observerUUID), LocalDateTime.now())
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends PeerConnectionSSRCsEntry> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(Tables.PEERCONNECTIONSSRCS)
				.columns(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID, Tables.PEERCONNECTIONSSRCS.UPDATED)
				.values(entity.SSRC, UUIDAdapter.toBytes(entity.observerUUID), UUIDAdapter.toBytes(entity.peerConnectionUUID), LocalDateTime.now())
				.onDuplicateKeyUpdate()
				.set(Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, UUIDAdapter.toBytes(entity.observerUUID))
				.set(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID, UUIDAdapter.toBytes(entity.peerConnectionUUID))
				.set(Tables.PEERCONNECTIONSSRCS.SSRC, entity.SSRC)
				.set(Tables.PEERCONNECTIONSSRCS.UPDATED, LocalDateTime.now())
				.execute();
		return entity;
	}

	public Iterable<UUID> getPeerConnections(Long SSRC, UUID observerUUID) {
		List<UUID> result = new ArrayList<>();
		DSLContext context = this.contextProvider.get();
		context.select(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID)
				.from(Tables.PEERCONNECTIONSSRCS)
				.where(Tables.PEERCONNECTIONSSRCS.SSRC.eq(SSRC)
						.and(Tables.PEERCONNECTIONSSRCS.OBSERVERUUID.eq(UUIDAdapter.toBytes(observerUUID))))
				.fetchInto(new RecordHandler<Record1<byte[]>>() {
					@Override
					public void next(Record1<byte[]> record) {
						result.add(UUIDAdapter.toUUID(record.component1()));
					}
				});
		return result;
	}

	/**
	 * Returns with an iterable collection of tuple of SSRC, Observer, PeerConnection, Call
	 *
	 * @param ssrcObserverTuples
	 * @return
	 */
	public Iterable<Quartet<Long, UUID, UUID, UUID>> getCallsForSSRCs(
			@NotNull Iterable<Pair<Long, UUID>> ssrcObserverTuples) {

		List<Row2<Long, byte[]>> whereConditions = StreamSupport.stream(ssrcObserverTuples.spliterator(), false).map(
				ssrcObserverTuple -> {
//					return Pair.with(ssrcObserverTuple.getValue0(), UUIDAdapter.toBytes(ssrcObserverTuple.getValue1()))
					return row(ssrcObserverTuple.getValue0(), UUIDAdapter.toBytes(ssrcObserverTuple.getValue1()));
				}).collect(Collectors.toList());

		Iterator<Record4<Long, byte[], byte[], byte[]>> resultIterator = this.contextProvider.get().select(Tables.PEERCONNECTIONSSRCS.SSRC,
				Tables.PEERCONNECTIONSSRCS.OBSERVERUUID,
				Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
				Tables.CALLPEERCONNECTIONS.CALLUUID)
				.from(Tables.PEERCONNECTIONSSRCS)
				.leftJoin(Tables.PEERCONNECTIONSSRCS).on(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID.eq(Tables.CALLPEERCONNECTIONS.PEERCONNECTIONUUID))
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID).in(whereConditions))
				.orderBy(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID).iterator();

		return () -> new Iterator<Quartet<Long, UUID, UUID, UUID>>() {
			@Override
			public boolean hasNext() {
				return resultIterator.hasNext();
			}

			@Override
			public Quartet<Long, UUID, UUID, UUID> next() {
				Record4<Long, byte[], byte[], byte[]> record = resultIterator.next();
				Long SSRC = record.value1();
				UUID observer = UUIDAdapter.toUUID(record.value2());
				UUID peerConnectionUUID = UUIDAdapter.toUUID(record.value3());
				UUID callUUID = null;
				if (record.value4() != null) {
					callUUID = UUIDAdapter.toUUID(record.value4());
				}
				return Quartet.with(SSRC, observer, peerConnectionUUID, callUUID);
			}
		};
	}

	public Iterable<Triplet<Long, UUID, Set<CallPeerConnectionsEntry>>> getPeerConnections(
			@NotNull Iterable<Pair<Long, UUID>> ssrcObserverTuples) {
		Set<Row2<Long, byte[]>> whereCondition = new HashSet<>();
		Iterator<Pair<Long, UUID>> ssrcObserverTuplesIt = ssrcObserverTuples.iterator();
		for (; ssrcObserverTuplesIt.hasNext(); ) {
			Pair<Long, UUID> ssrcObserverTuple = ssrcObserverTuplesIt.next();
			Long ssrc = ssrcObserverTuple.getValue0();
			byte[] observerUUID = UUIDAdapter.toBytes(ssrcObserverTuple.getValue1());
			whereCondition.add(row(ssrc, observerUUID));
		}
		Map<Pair<Long, UUID>, Set<CallPeerConnectionsEntry>> collectedResult = new HashMap<>();
		this.contextProvider.get().select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
				Tables.CALLPEERCONNECTIONS.CALLUUID)
				.from(Tables.PEERCONNECTIONSSRCS)
				.leftJoin(Tables.CALLPEERCONNECTIONS).on(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID.eq(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID))
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID).in(whereCondition))
				.orderBy(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID,
						Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID)
				.fetchInto(new RecordHandler<Record4<Long, byte[], byte[], byte[]>>() {
					@Override
					public void next(Record4<Long, byte[], byte[], byte[]> record) {
						Pair<Long, UUID> ssrcObserverTuple = Pair.with(
								record.value1(),
								UUIDAdapter.toUUID(record.value2())
						);
						Set<CallPeerConnectionsEntry> callMapEntries = collectedResult.getOrDefault(ssrcObserverTuple, new HashSet<>());
						CallPeerConnectionsEntry callPeerConnectionsEntry = new CallPeerConnectionsEntry();
						callPeerConnectionsEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value3());
						if (record.value4() != null) {
							callPeerConnectionsEntry.callUUID = UUIDAdapter.toUUID(record.value4());
						} else {
							callPeerConnectionsEntry.callUUID = null;
						}
						callMapEntries.add(callPeerConnectionsEntry);
						collectedResult.put(ssrcObserverTuple, callMapEntries);
					}
				});
		Iterator<Map.Entry<Pair<Long, UUID>, Set<CallPeerConnectionsEntry>>> resultIterator = collectedResult.entrySet().iterator();
		return () -> new Iterator<Triplet<Long, UUID, Set<CallPeerConnectionsEntry>>>() {
			@Override
			public boolean hasNext() {
				return resultIterator.hasNext();
			}

			@Override
			public Triplet<Long, UUID, Set<CallPeerConnectionsEntry>> next() {
				Map.Entry<Pair<Long, UUID>, Set<CallPeerConnectionsEntry>> entry = resultIterator.next();
				return Triplet.with(entry.getKey().getValue0(), entry.getKey().getValue1(), entry.getValue());
			}
		};
	}

	private static final int BULK_QUERY_MAX_ITEMS = 5000;

	@NonNull
	@Override
	public <S extends PeerConnectionSSRCsEntry> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		Supplier<InsertValuesStep4<PeerconnectionssrcsRecord, Long, byte[], byte[], LocalDateTime>> sqlSupplier = () -> {
			return this.contextProvider.get().insertInto(Tables.PEERCONNECTIONSSRCS,
					Tables.PEERCONNECTIONSSRCS.SSRC,
					Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
					Tables.PEERCONNECTIONSSRCS.OBSERVERUUID,
					Tables.PEERCONNECTIONSSRCS.UPDATED);
		};
		Consumer<InsertValuesStep4<PeerconnectionssrcsRecord, Long, byte[], byte[], LocalDateTime>> executor = (sql) -> {
			sql.onDuplicateKeyUpdate()
					.set(Tables.PEERCONNECTIONSSRCS.SSRC, values(Tables.PEERCONNECTIONSSRCS.SSRC))
					.set(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID, values(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID))
					.set(Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, values(Tables.PEERCONNECTIONSSRCS.OBSERVERUUID))
					.set(Tables.PEERCONNECTIONSSRCS.UPDATED, values(Tables.PEERCONNECTIONSSRCS.UPDATED))
					.execute();
		};
		InsertValuesStep4<PeerconnectionssrcsRecord, Long, byte[], byte[], LocalDateTime> sql = sqlSupplier.get();
		int count = 0;
		for (Iterator<S> it = entities.iterator(); it.hasNext(); ++count) {
			PeerConnectionSSRCsEntry peerConnectionSSRCsEntry = it.next();
			byte[] peerConnection = UUIDAdapter.toBytes(peerConnectionSSRCsEntry.peerConnectionUUID);
			byte[] observer = UUIDAdapter.toBytes(peerConnectionSSRCsEntry.observerUUID);
			sql.values(peerConnectionSSRCsEntry.SSRC, peerConnection, observer, peerConnectionSSRCsEntry.updated);
			if (BULK_QUERY_MAX_ITEMS < count) {
				executor.accept(sql);
				sql = sqlSupplier.get();
				count = 0;
			}
		}

		if (0 < count) {
			executor.accept(sql);
		}
		return entities;
	}

	@NonNull
	@Override
	public Optional<PeerConnectionSSRCsEntry> findById(@NonNull @NotNull Triplet<Long, UUID, UUID> ssrcPCObserver) {
		return this.contextProvider.get()
				.select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
						Tables.PEERCONNECTIONSSRCS.OBSERVERUUID)
				.from(Tables.PEERCONNECTIONSSRCS)
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
						Tables.PEERCONNECTIONSSRCS.OBSERVERUUID)
						.eq(ssrcPCObserver.getValue0(),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue1()),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue2())))
				.fetchOptionalInto(PeerConnectionSSRCsEntry.class);
	}


	public void findCallUUIDs(@NonNull @NotNull Iterable<UUID> peerConnectionUUIDs,
							  Consumer<CallPeerConnectionsEntry> callMapEntryConsumer) {
		Stream<byte[]> peerConnectionUUIDsStream = StreamSupport.stream(peerConnectionUUIDs.spliterator(), false).map(UUIDAdapter::toBytes);
		this.contextProvider.get()
				.select(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID, Tables.CALLPEERCONNECTIONS.CALLUUID,
						Tables.PEERCONNECTIONSSRCS.UPDATED)
				.from(Tables.PEERCONNECTIONSSRCS)
				.leftJoin(Tables.CALLPEERCONNECTIONS).on(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID.eq(Tables.CALLPEERCONNECTIONS.PEERCONNECTIONUUID))
				.where(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID.in(peerConnectionUUIDsStream.collect(Collectors.toList())))
				.fetchInto(new RecordHandler<Record3<byte[], byte[], LocalDateTime>>() {
					@Override
					public void next(Record3<byte[], byte[], LocalDateTime> record) {
						CallPeerConnectionsEntry callPeerConnectionsEntry = new CallPeerConnectionsEntry();
						callPeerConnectionsEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value1());
						if (record.value2() != null) {
							callPeerConnectionsEntry.callUUID = UUIDAdapter.toUUID(record.value2());
						} else {
							callPeerConnectionsEntry.callUUID = null;
						}
						callPeerConnectionsEntry.updated = record.value3();
						callMapEntryConsumer.accept(callPeerConnectionsEntry);
					}
				});

	}

	/**
	 * Find peers for the provided peer connection based on observe ssrc a peer connection has.
	 * NOTE: The peer connection the search is based on will not appear in the result list
	 *
	 * @param peerConnectionUUID
	 * @param peerConnectionUUIDConsumer
	 */
	public void findPeers(@NonNull @NotNull UUID peerConnectionUUID,
						  Consumer<UUID> peerConnectionUUIDConsumer) {
		this.contextProvider.get()
				.select(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID)
				.from(Tables.PEERCONNECTIONSSRCS)
				.where(row(Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.SSRC)
						.in(select(Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.SSRC).from(Tables.PEERCONNECTIONSSRCS)
								.where(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID.eq(UUIDAdapter.toBytes(peerConnectionUUID)))))
				.fetchInto(new RecordHandler<Record1<byte[]>>() {
					@Override
					public void next(Record1<byte[]> record) {
						if (record.value1() == null) {
							return;
						}
						UUID peer = UUIDAdapter.toUUID(record.value1());
						if (peer.equals(peerConnectionUUID)) {
							return;
						}
						peerConnectionUUIDConsumer.accept(peer);
					}
				});
	}


	@Override
	public boolean existsById(@NonNull @NotNull Triplet<Long, UUID, UUID> ssrcPCObserver) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(Tables.PEERCONNECTIONSSRCS)
						.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
								Tables.PEERCONNECTIONSSRCS.OBSERVERUUID)
								.eq(ssrcPCObserver.getValue0(),
										UUIDAdapter.toBytes(ssrcPCObserver.getValue1()),
										UUIDAdapter.toBytes(ssrcPCObserver.getValue2())))
		);
	}

	@NonNull
	@Override
	public Iterable<PeerConnectionSSRCsEntry> findAll() {
		Long tableSize = this.count();
		int bulkSize = DEFAULT_BULK_SIZE;
		return () -> this.jooqExecuteWrapper.retrieve((context, offset) -> {
			List<PeerConnectionSSRCsEntry> result = context
					.select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.UPDATED)
					.from(Tables.PEERCONNECTIONSSRCS)
					.orderBy(Tables.PEERCONNECTIONSSRCS.UPDATED.desc())
					.offset(offset)
					.limit(bulkSize)
					.fetch().into(PeerConnectionSSRCsEntry.class);
			return result.iterator();
		}, tableSize.intValue());
	}

	@Override
	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(Tables.PEERCONNECTIONSSRCS)
				.fetchOne(0, long.class);
	}

	@Override
	public void deleteById(@NonNull @NotNull Triplet<Long, UUID, UUID> ssrcPCObserver) {
		this.contextProvider.get().deleteFrom(Tables.PEERCONNECTIONSSRCS)
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
						Tables.PEERCONNECTIONSSRCS.OBSERVERUUID)
						.eq(ssrcPCObserver.getValue0(),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue1()),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue2())))
				.execute();

	}

	@Override
	public void delete(@NonNull @NotNull PeerConnectionSSRCsEntry entity) {
		this.contextProvider.get().deleteFrom(Tables.PEERCONNECTIONSSRCS)
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
						Tables.PEERCONNECTIONSSRCS.OBSERVERUUID)
						.eq(entity.SSRC,
								UUIDAdapter.toBytes(entity.peerConnectionUUID),
								UUIDAdapter.toBytes(entity.observerUUID)))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends PeerConnectionSSRCsEntry> entities) {
		List<Row2<byte[], Long>> keys = StreamSupport.stream(entities
				.spliterator(), false)
				.map(entity -> row(
						UUIDAdapter.toBytes(entity.peerConnectionUUID),
						entity.SSRC
				)).collect(Collectors.toList());

		this.contextProvider.get()
				.deleteFrom(Tables.PEERCONNECTIONSSRCS)
				.where(row(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
						Tables.PEERCONNECTIONSSRCS.SSRC).in(keys))
				.execute();
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(Tables.PEERCONNECTIONSSRCS);
	}

	public List<PeerConnectionSSRCsEntry> findExpiredPeerConnections(LocalDateTime threshold) {
		return this.contextProvider.get()
				.select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
						Tables.PEERCONNECTIONSSRCS.UPDATED)
				.from(Tables.PEERCONNECTIONSSRCS)
				.where(Tables.PEERCONNECTIONSSRCS.UPDATED.lt(threshold))
				.orderBy(Tables.PEERCONNECTIONSSRCS.UPDATED.asc())
				.fetchInto(PeerConnectionSSRCsEntry.class);
	}

	public Iterable<PeerConnectionSSRCsEntry> findEntries(UUID peerConnectionUUID) {
		return this.contextProvider.get()
				.select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID,
						Tables.PEERCONNECTIONSSRCS.UPDATED)
				.from(Tables.PEERCONNECTIONSSRCS)
				.where(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID.eq(UUIDAdapter.toBytes(peerConnectionUUID)))
				.fetchInto(PeerConnectionSSRCsEntry.class);
	}

	public void getSSRCMapEntriesOlderThan(LocalDateTime borderTime, Consumer<PeerConnectionSSRCsEntry> ssrcMapEntryConsumer) {
		this.contextProvider.get()
				.select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVERUUID, Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID, Tables.PEERCONNECTIONSSRCS.UPDATED)
				.from(Tables.PEERCONNECTIONSSRCS)
				.where(Tables.PEERCONNECTIONSSRCS.UPDATED.lt(borderTime))
				.fetchInto(new RecordHandler<Record4<Long, byte[], byte[], LocalDateTime>>() {
					@Override
					public void next(Record4<Long, byte[], byte[], LocalDateTime> record) {
						PeerConnectionSSRCsEntry peerConnectionSSRCsEntry = new PeerConnectionSSRCsEntry();
						peerConnectionSSRCsEntry.SSRC = record.value1();
						peerConnectionSSRCsEntry.observerUUID = UUIDAdapter.toUUID(record.value2());
						peerConnectionSSRCsEntry.peerConnectionUUID = UUIDAdapter.toUUID(record.value3());
						peerConnectionSSRCsEntry.updated = record.value4();
						ssrcMapEntryConsumer.accept(peerConnectionSSRCsEntry);
					}
				});
	}

	public void removePeerConnections(Collection<UUID> peerConnectionUUIDs) {
		this.contextProvider.get()
				.deleteFrom(Tables.PEERCONNECTIONSSRCS)
				.where(Tables.PEERCONNECTIONSSRCS.PEERCONNECTIONUUID.in(
						peerConnectionUUIDs.stream().map(UUIDAdapter::toBytes).collect(Collectors.toList())
				)).execute();
	}

}