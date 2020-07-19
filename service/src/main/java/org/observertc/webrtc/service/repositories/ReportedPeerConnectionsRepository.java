package org.observertc.webrtc.service.repositories;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.repository.CrudRepository;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.jooq.Field;
import org.jooq.InsertValuesStep5;
import org.jooq.impl.DSL;
import org.observertc.webrtc.common.BatchCollector;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.jooq.enums.ReportedpeerconnectionsStatus;
import org.observertc.webrtc.service.jooq.tables.Reportedpeerconnections;
import org.observertc.webrtc.service.jooq.tables.records.ReportedpeerconnectionsRecord;

@Singleton
public class ReportedPeerConnectionsRepository implements CrudRepository<ReportedpeerconnectionsRecord, byte[]> {
	private static Reportedpeerconnections TABLE = Tables.REPORTEDPEERCONNECTIONS;
	private static int BULK_MAX_QUERY_SIZE = 5000;

	private static <T> Field<T> values(Field<T> field) {
		return DSL.field("VALUES({0})", field.getDataType(), field);
	}

	/**
	 * Due to the fact that bulk operations increase the size of the query,
	 * this limit enforces to send the query when it reaches a certain
	 * number of entry.
	 */
	private final IDSLContextProvider contextProvider;

	public ReportedPeerConnectionsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

	@NonNull
	@Override
	public <S extends ReportedpeerconnectionsRecord> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends ReportedpeerconnectionsRecord> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.onDuplicateKeyUpdate()
				.set(values(TABLE.STATUS), entity.getStatus())
				.set(values(TABLE.UPDATED), entity.getUpdated())
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends ReportedpeerconnectionsRecord> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(BULK_MAX_QUERY_SIZE, batchedEntities -> {
			contextProvider.get().insertInto(TABLE).values(batchedEntities).execute();
		}));
		return entities;
	}

	private <S extends ReportedpeerconnectionsRecord> void consumeBatches(Iterable<S> entities, Consumer<Iterable<S>> consumer) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(BULK_MAX_QUERY_SIZE, batchedEntities -> {
			if (batchedEntities.size() < 1) {
				return;
			}
			consumer.accept(batchedEntities);
		}));
	}

	@NonNull
	public <S extends ReportedpeerconnectionsRecord> Iterable<S> updateAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		this.consumeBatches(entities, batchedEntities -> {
			InsertValuesStep5<ReportedpeerconnectionsRecord, byte[], byte[], byte[], ReportedpeerconnectionsStatus, LocalDateTime> sql =
					contextProvider.get().insertInto(
							TABLE,
							TABLE.CALLUUID,
							TABLE.OBSERVERUUID,
							TABLE.PEERCONNECTIONUUID,
							TABLE.STATUS,
							TABLE.UPDATED);
			Iterator<S> it = batchedEntities.iterator();
			for (; it.hasNext(); ) {
				ReportedpeerconnectionsRecord record = it.next();
				sql.values(record.getCalluuid(),
						record.getObserveruuid(),
						record.getPeerconnectionuuid(),
						record.getStatus(),
						record.getUpdated());
			}
			sql
					.onDuplicateKeyUpdate()
					.set(TABLE.CALLUUID, values(TABLE.CALLUUID))
					.set(TABLE.PEERCONNECTIONUUID, values(TABLE.PEERCONNECTIONUUID))
					.set(TABLE.OBSERVERUUID, values(TABLE.OBSERVERUUID))
					.set(TABLE.STATUS, values(TABLE.STATUS))
					.set(TABLE.UPDATED, values(TABLE.UPDATED))
					.execute();
		});
		return entities;
	}

	@NonNull
	@Override
	public Optional<ReportedpeerconnectionsRecord> findById(@NonNull @NotNull byte[] peerConnectionUUID) {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(TABLE.PEERCONNECTIONUUID.eq(peerConnectionUUID))
				.fetchOptionalInto(ReportedpeerconnectionsRecord.class);
	}

	@Override
	public boolean existsById(@NonNull @NotNull byte[] peerConnectionUUID) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(TABLE)
						.where(TABLE.PEERCONNECTIONUUID.eq(peerConnectionUUID))
		);
	}

	@NonNull
	@Override
	public Iterable<ReportedpeerconnectionsRecord> findAll() {
		return () -> contextProvider.get().selectFrom(Tables.REPORTEDPEERCONNECTIONS).stream().iterator();
	}

	@Override
	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(TABLE)
				.fetchOne(0, long.class);
	}

	@Override
	public void deleteById(@NonNull @NotNull byte[] peerConnectionUUID) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.PEERCONNECTIONUUID.in(peerConnectionUUID))
				.execute();
	}

	@Override
	public void delete(@NonNull @NotNull ReportedpeerconnectionsRecord entity) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.PEERCONNECTIONUUID.in(entity.getPeerconnectionuuid()))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends ReportedpeerconnectionsRecord> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(BULK_MAX_QUERY_SIZE, batchedEntities -> {
			contextProvider.get().deleteFrom(TABLE)
					.where(TABLE.PEERCONNECTIONUUID.in(batchedEntities.stream().map(ReportedpeerconnectionsRecord::getPeerconnectionuuid).collect(Collectors.toList())))
					.execute();
		}));
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(TABLE).execute();
	}

	public void deleteOlderThan(LocalDateTime threshold) {
		this.contextProvider.get().deleteFrom(TABLE).where(TABLE.UPDATED.lt(threshold)).execute();
	}
}