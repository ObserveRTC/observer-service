package org.observertc.webrtc.service.repositories;

import static org.jooq.impl.DSL.row;
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
import org.jooq.InsertValuesStep4;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.RecordHandler;
import org.jooq.Row2;
import org.jooq.Row3;
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
				.columns(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, Tables.PEERCONNECTIONSSRCS.OBSERVER, Tables.PEERCONNECTIONSSRCS.UPDATED)
				.values(entity.SSRC, UUIDAdapter.toBytes(entity.peerConnectionUUID), UUIDAdapter.toBytes(entity.observerUUID), LocalDateTime.now())
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends PeerConnectionSSRCsEntry> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get()
				.insertInto(Tables.PEERCONNECTIONSSRCS)
				.columns(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVER, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, Tables.PEERCONNECTIONSSRCS.UPDATED)
				.values(entity.SSRC, UUIDAdapter.toBytes(entity.observerUUID), UUIDAdapter.toBytes(entity.peerConnectionUUID), LocalDateTime.now())
				.onDuplicateKeyUpdate()
				.set(Tables.PEERCONNECTIONSSRCS.OBSERVER, UUIDAdapter.toBytes(entity.observerUUID))
				.set(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, UUIDAdapter.toBytes(entity.peerConnectionUUID))
				.set(Tables.PEERCONNECTIONSSRCS.SSRC, entity.SSRC)
				.set(Tables.PEERCONNECTIONSSRCS.UPDATED, LocalDateTime.now())
				.execute();
		return entity;
	}

	public Iterable<UUID> getPeerConnections(Long SSRC, UUID observerUUID) {
		List<UUID> result = new ArrayList<>();
		DSLContext context = this.contextProvider.get();
		context.select(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION)
				.from(Tables.PEERCONNECTIONSSRCS)
				.where(Tables.PEERCONNECTIONSSRCS.SSRC.eq(SSRC)
						.and(Tables.PEERCONNECTIONSSRCS.OBSERVER.eq(UUIDAdapter.toBytes(observerUUID))))
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
				Tables.PEERCONNECTIONSSRCS.OBSERVER,
				Tables.PEERCONNECTIONSSRCS.PEERCONNECTION,
				Tables.CALLPEERCONNECTIONS.CALLID)
				.from(Tables.PEERCONNECTIONSSRCS)
				.leftJoin(Tables.PEERCONNECTIONSSRCS).on(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION.eq(Tables.CALLPEERCONNECTIONS.PEERCONNECTION))
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVER).in(whereConditions))
				.orderBy(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVER, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION).iterator();

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
		this.contextProvider.get().select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVER, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION,
				Tables.CALLPEERCONNECTIONS.CALLID)
				.from(Tables.PEERCONNECTIONSSRCS)
				.leftJoin(Tables.CALLPEERCONNECTIONS).on(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION.eq(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION))
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVER).in(whereCondition))
				.orderBy(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVER, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION)
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

	@NonNull
	@Override
	public <S extends PeerConnectionSSRCsEntry> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		InsertValuesStep4<PeerconnectionssrcsRecord, Long, byte[], byte[], LocalDateTime> sql =
				this.contextProvider.get().insertInto(Tables.PEERCONNECTIONSSRCS,
						Tables.PEERCONNECTIONSSRCS.SSRC,
						Tables.PEERCONNECTIONSSRCS.PEERCONNECTION,
						Tables.PEERCONNECTIONSSRCS.OBSERVER,
						Tables.PEERCONNECTIONSSRCS.UPDATED);
		for (Iterator<S> it = entities.iterator(); it.hasNext(); ) {
			PeerConnectionSSRCsEntry peerConnectionSSRCsEntry = it.next();
			byte[] peerConnection = UUIDAdapter.toBytes(peerConnectionSSRCsEntry.peerConnectionUUID);
			byte[] observer = UUIDAdapter.toBytes(peerConnectionSSRCsEntry.observerUUID);
			sql.values(peerConnectionSSRCsEntry.SSRC, peerConnection, observer, LocalDateTime.now());
		}
		sql.onDuplicateKeyUpdate()
				.set(Tables.PEERCONNECTIONSSRCS.SSRC, values(Tables.PEERCONNECTIONSSRCS.SSRC))
				.set(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, values(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION))
				.set(Tables.PEERCONNECTIONSSRCS.OBSERVER, values(Tables.PEERCONNECTIONSSRCS.OBSERVER))
				.set(Tables.PEERCONNECTIONSSRCS.UPDATED, values(Tables.PEERCONNECTIONSSRCS.UPDATED))
				.execute();
		return entities;
	}

	@NonNull
	@Override
	public Optional<PeerConnectionSSRCsEntry> findById(@NonNull @NotNull Triplet<Long, UUID, UUID> ssrcPCObserver) {
		return this.contextProvider.get()
				.select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, Tables.PEERCONNECTIONSSRCS.OBSERVER)
				.from(Tables.PEERCONNECTIONSSRCS)
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, Tables.PEERCONNECTIONSSRCS.OBSERVER)
						.eq(ssrcPCObserver.getValue0(),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue1()),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue2())))
				.fetchOptionalInto(PeerConnectionSSRCsEntry.class);
	}

	@Override
	public boolean existsById(@NonNull @NotNull Triplet<Long, UUID, UUID> ssrcPCObserver) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(Tables.PEERCONNECTIONSSRCS)
						.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, Tables.PEERCONNECTIONSSRCS.OBSERVER)
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
					.select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, Tables.PEERCONNECTIONSSRCS.OBSERVER, Tables.PEERCONNECTIONSSRCS.UPDATED)
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
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, Tables.PEERCONNECTIONSSRCS.OBSERVER)
						.eq(ssrcPCObserver.getValue0(),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue1()),
								UUIDAdapter.toBytes(ssrcPCObserver.getValue2())))
				.execute();

	}

	@Override
	public void delete(@NonNull @NotNull PeerConnectionSSRCsEntry entity) {
		this.contextProvider.get().deleteFrom(Tables.PEERCONNECTIONSSRCS)
				.where(row(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, Tables.PEERCONNECTIONSSRCS.OBSERVER)
						.eq(entity.SSRC,
								UUIDAdapter.toBytes(entity.peerConnectionUUID),
								UUIDAdapter.toBytes(entity.observerUUID)))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends PeerConnectionSSRCsEntry> entities) {
		Iterator<? extends PeerConnectionSSRCsEntry> iterator = entities.iterator();
		this.jooqExecuteWrapper.execute((context, items) -> {

			List<Row3<Long, byte[], byte[]>> keys = StreamSupport.stream(entities
					.spliterator(), false)
					.map(entity -> row(entity.SSRC,
							UUIDAdapter.toBytes(entity.peerConnectionUUID),
							UUIDAdapter.toBytes(entity.observerUUID))
					).collect(Collectors.toList());

			context
					.deleteFrom(Tables.PEERCONNECTIONSSRCS)
					.where(row(Tables.PEERCONNECTIONSSRCS.SSRC,
							Tables.PEERCONNECTIONSSRCS.PEERCONNECTION,
							Tables.PEERCONNECTIONSSRCS.OBSERVER).in(keys))
					.execute();
		}, entities);
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(Tables.PEERCONNECTIONSSRCS);
	}

	public void retrieveSSRCObserverPeerConnectionCallIDsOlderThan(LocalDateTime borderTime,
																   Consumer<Quartet<Long, UUID, UUID, UUID>> recordHandler) {
		this.contextProvider.get()
				.select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVER, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION,
						Tables.CALLPEERCONNECTIONS.CALLID)
				.from(Tables.PEERCONNECTIONSSRCS)
				.leftJoin(Tables.CALLPEERCONNECTIONS)
				.on(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION.eq(Tables.CALLPEERCONNECTIONS.PEERCONNECTION))
				.where(Tables.PEERCONNECTIONSSRCS.UPDATED.lt(borderTime))
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

	public void getSSRCMapEntriesOlderThan(LocalDateTime borderTime, Consumer<PeerConnectionSSRCsEntry> ssrcMapEntryConsumer) {
		this.contextProvider.get()
				.select(Tables.PEERCONNECTIONSSRCS.SSRC, Tables.PEERCONNECTIONSSRCS.OBSERVER, Tables.PEERCONNECTIONSSRCS.PEERCONNECTION, Tables.PEERCONNECTIONSSRCS.UPDATED)
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
				.where(Tables.PEERCONNECTIONSSRCS.PEERCONNECTION.in(
						peerConnectionUUIDs.stream().map(UUIDAdapter::toBytes).collect(Collectors.toList())
				)).execute();
	}

}