package org.observertc.webrtc.service.repositories;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.jooq.Field;
import org.jooq.InsertValuesStep7;
import org.jooq.impl.DSL;
import org.observertc.webrtc.common.BatchCollector;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.jooq.enums.PeerconnectionsState;
import org.observertc.webrtc.service.jooq.tables.Peerconnections;
import org.observertc.webrtc.service.jooq.tables.records.PeerconnectionsRecord;

@Singleton
public class PeerConnectionsRepository {
	private static Peerconnections TABLE = Tables.PEERCONNECTIONS;
	private static int DEFAULT_BULK_SIZE = 5000;

	private static <T> Field<T> values(Field<T> field) {
		return DSL.field("VALUES({0})", field.getDataType(), field);
	}

	private final IDSLContextProvider contextProvider;

	public PeerConnectionsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

	@NonNull
	public <S extends PeerconnectionsRecord> S save(@NonNull @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.execute();
		return entity;
	}

	@NonNull
	public <S extends PeerconnectionsRecord> S update(@NonNull @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.onDuplicateKeyUpdate()
				.set(TABLE.PEERCONNECTIONUUID, values(TABLE.PEERCONNECTIONUUID))
				.set(TABLE.BROWSERID, values(TABLE.BROWSERID))
				.set(TABLE.STATE, values(TABLE.STATE))
				.set(TABLE.UPDATED, values(TABLE.UPDATED))
				.set(TABLE.TIMEZONE, values(TABLE.TIMEZONE))
				.set(TABLE.OBSERVERUUID, values(TABLE.OBSERVERUUID))
				.set(TABLE.CALLUUID, values(TABLE.CALLUUID))
				.execute();
		return entity;
	}

	@NonNull
	public <S extends PeerconnectionsRecord> Iterable<S> saveAll(@NonNull @NotNull Iterable<S> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(DEFAULT_BULK_SIZE, batchedEntities -> {
			contextProvider.get().batchInsert(batchedEntities).execute();
		}));
		return entities;
	}

	@NonNull
	public <S extends PeerconnectionsRecord> Iterable<S> updateAll(@NonNull @NotNull Iterable<S> entities) {
		this.consumeBatches(entities, batchedEntities -> {
			InsertValuesStep7<PeerconnectionsRecord, byte[], String, PeerconnectionsState, LocalDateTime, String, byte[], byte[]> sql =
					contextProvider.get().insertInto(
							TABLE,
							TABLE.PEERCONNECTIONUUID,
							TABLE.BROWSERID,
							TABLE.STATE,
							TABLE.UPDATED,
							TABLE.TIMEZONE,
							TABLE.OBSERVERUUID,
							TABLE.CALLUUID);
			Iterator<S> it = batchedEntities.iterator();
			for (; it.hasNext(); ) {
				PeerconnectionsRecord record = it.next();
				sql.values(record.getPeerconnectionuuid(),
						record.getBrowserid(),
						record.getState(),
						record.getUpdated(),
						record.getTimezone(),
						record.getObserveruuid(),
						record.getCalluuid()
				);
			}
			sql
					.onDuplicateKeyUpdate()
					.set(TABLE.PEERCONNECTIONUUID, values(TABLE.PEERCONNECTIONUUID))
					.set(TABLE.BROWSERID, values(TABLE.BROWSERID))
					.set(TABLE.STATE, values(TABLE.STATE))
					.set(TABLE.UPDATED, values(TABLE.UPDATED))
					.set(TABLE.TIMEZONE, values(TABLE.TIMEZONE))
					.set(TABLE.OBSERVERUUID, values(TABLE.OBSERVERUUID))
					.set(TABLE.CALLUUID, values(TABLE.CALLUUID))
					.execute();
		});
		return entities;
	}

	private <S extends PeerconnectionsRecord> void consumeBatches(Iterable<S> entities, Consumer<Iterable<S>> consumer) {
//		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(DEFAULT_BULK_SIZE, batchedEntities -> {
//			if (batchedEntities.size() < 1) {
//				return;
//			}
//			consumer.accept(batchedEntities);
//		}));
		// TODO: fix it, because itw not working
		consumer.accept(entities);
	}


	@NonNull
	public Stream<PeerconnectionsRecord> findByCallUUID(@NonNull @NotNull UUID callUUID) {
		byte[] callUUIDBytes = UUIDAdapter.toBytesOrDefault(callUUID, null);
		return this.findByCallUUIDBytes(callUUIDBytes);
	}

	@NonNull
	public Stream<PeerconnectionsRecord> findByCallUUIDBytes(@NonNull @NotNull byte[] callUUIDBytes) {
		return this.contextProvider.get().selectFrom(TABLE).where(TABLE.CALLUUID.eq(callUUIDBytes)).stream();
	}


	@NonNull
	public Stream<PeerconnectionsRecord> findJoinedPCsUpdatedLowerThan(@NonNull LocalDateTime threshold) {
		return this.contextProvider.get().selectFrom(TABLE).where(TABLE.STATE.eq(PeerconnectionsState.joined)).and(TABLE.UPDATED.lt(threshold)).stream();
	}

	@NonNull
	public Optional<PeerconnectionsRecord> findById(@NonNull @NotNull UUID peerConnectionUUID) {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(TABLE.PEERCONNECTIONUUID.eq(UUIDAdapter.toBytesOrDefault(peerConnectionUUID, null)))
				.fetchOptionalInto(PeerconnectionsRecord.class);
	}

	public boolean existsById(@NonNull @NotNull UUID peerConnectionUUID) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(TABLE)
						.where(TABLE.PEERCONNECTIONUUID.eq(UUIDAdapter.toBytesOrDefault(peerConnectionUUID, null))
						)
		);
	}

	@NonNull
	public Iterable<PeerconnectionsRecord> findAll() {
		return () -> contextProvider.get().selectFrom(TABLE).stream().iterator();
	}

	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(TABLE)
				.fetchOne(0, long.class);
	}

	public void deleteById(@NonNull @NotNull UUID peerConnectionUUID) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.PEERCONNECTIONUUID.eq(UUIDAdapter.toBytesOrDefault(peerConnectionUUID, null)))
				.execute();
	}

	public void delete(@NonNull @NotNull PeerconnectionsRecord entity) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.PEERCONNECTIONUUID.eq(entity.getPeerconnectionuuid()))
				.execute();
	}

	public void deleteAll(@NonNull @NotNull Iterable<? extends PeerconnectionsRecord> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(DEFAULT_BULK_SIZE, batchedEntities -> {
			contextProvider.get().deleteFrom(TABLE)
					.where(TABLE.PEERCONNECTIONUUID
							.in(batchedEntities.stream().map(
									PeerconnectionsRecord::getPeerconnectionuuid
							).collect(Collectors.toList())))
					.execute();
		}));
	}

	public void deleteAll() {
		this.contextProvider.get().deleteFrom(TABLE).execute();
	}

	public void deleteDetachedPCsUpdatedLessThan(LocalDateTime threshold) {
		this.contextProvider.get().deleteFrom(TABLE).where(TABLE.UPDATED.lt(threshold)).and(TABLE.STATE.eq(PeerconnectionsState.detached)).execute();
	}

	public Optional<PeerconnectionsRecord> getLastJoinedPC() {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(TABLE.STATE.eq(PeerconnectionsState.joined))
				.orderBy(TABLE.UPDATED.desc())
				.limit(1)
				.fetchOptional();
	}
}