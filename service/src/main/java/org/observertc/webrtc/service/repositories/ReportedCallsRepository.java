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
import org.jooq.InsertValuesStep4;
import org.jooq.impl.DSL;
import org.observertc.webrtc.common.BatchCollector;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.jooq.enums.ReportedcallsStatus;
import org.observertc.webrtc.service.jooq.tables.Reportedcalls;
import org.observertc.webrtc.service.jooq.tables.records.ReportedcallsRecord;

@Singleton
public class ReportedCallsRepository implements CrudRepository<ReportedcallsRecord, byte[]> {
	private static Reportedcalls TABLE = Tables.REPORTEDCALLS;
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

	public ReportedCallsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

	@NonNull
	@Override
	public <S extends ReportedcallsRecord> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends ReportedcallsRecord> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.onDuplicateKeyUpdate()
				.set(values(TABLE.STATUS), entity.getStatus())
				.set(values(TABLE.UPDATED), entity.getUpdated())
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends ReportedcallsRecord> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(BULK_MAX_QUERY_SIZE, batchedEntities -> {
			contextProvider.get().insertInto(TABLE).values(batchedEntities).execute();
		}));
		return entities;
	}

	@NonNull
	public <S extends ReportedcallsRecord> Iterable<S> updateAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		this.consumeBatches(entities, batchedEntities -> {
			InsertValuesStep4<ReportedcallsRecord, byte[], byte[], ReportedcallsStatus, LocalDateTime> sql =
					contextProvider.get().insertInto(
							TABLE,
							TABLE.CALLUUID,
							TABLE.OBSERVERUUID,
							TABLE.STATUS,
							TABLE.UPDATED);
			Iterator<S> it = batchedEntities.iterator();
			for (; it.hasNext(); ) {
				ReportedcallsRecord record = it.next();
				sql.values(record.getCalluuid(),
						record.getObserveruuid(),
						record.getStatus(),
						record.getUpdated());
			}
			sql
					.onDuplicateKeyUpdate()
					.set(TABLE.CALLUUID, values(TABLE.CALLUUID))
					.set(TABLE.OBSERVERUUID, values(TABLE.OBSERVERUUID))
					.set(TABLE.STATUS, values(TABLE.STATUS))
					.set(TABLE.UPDATED, values(TABLE.UPDATED))
					.execute();
		});
		return entities;
	}

	private <S extends ReportedcallsRecord> void consumeBatches(Iterable<S> entities, Consumer<Iterable<S>> consumer) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(BULK_MAX_QUERY_SIZE, batchedEntities -> {
			if (batchedEntities.size() < 1) {
				return;
			}
			consumer.accept(batchedEntities);
		}));
	}

	@NonNull
	@Override
	public Optional<ReportedcallsRecord> findById(@NonNull @NotNull byte[] callUUID) {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(TABLE.CALLUUID.eq(callUUID))
				.fetchOptionalInto(ReportedcallsRecord.class);
	}

	@Override
	public boolean existsById(@NonNull @NotNull byte[] callUUID) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(TABLE)
						.where(TABLE.CALLUUID.eq(callUUID))
		);
	}

	@NonNull
	@Override
	public Iterable<ReportedcallsRecord> findAll() {
		return () -> contextProvider.get().selectFrom(TABLE).stream().iterator();
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
	public void deleteById(@NonNull @NotNull byte[] callUUID) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.CALLUUID.in(callUUID))
				.execute();
	}

	@Override
	public void delete(@NonNull @NotNull ReportedcallsRecord entity) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.CALLUUID.in(entity.getCalluuid()))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends ReportedcallsRecord> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(BULK_MAX_QUERY_SIZE, batchedEntities -> {
			contextProvider.get().deleteFrom(TABLE)
					.where(TABLE.CALLUUID.in(batchedEntities.stream().map(ReportedcallsRecord::getCalluuid).collect(Collectors.toList())))
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