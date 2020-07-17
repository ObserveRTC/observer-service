package org.observertc.webrtc.service.repositories;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.observertc.webrtc.common.BatchCollector;
import org.observertc.webrtc.service.jooq.Tables;
import org.observertc.webrtc.service.jooq.tables.Sentreports;
import org.observertc.webrtc.service.jooq.tables.records.SentreportsRecord;

@Singleton
public class SentReportsRepository implements CrudRepository<SentreportsRecord, byte[]> {
	private static Sentreports TABLE = Tables.SENTREPORTS;
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

	public SentReportsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

	@NonNull
	@Override
	public <S extends SentreportsRecord> S save(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends SentreportsRecord> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.onDuplicateKeyUpdate()
				.set(values(TABLE.SIGNED), entity.getSigned())
				.set(values(TABLE.TYPE), entity.getType())
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends SentreportsRecord> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(BULK_MAX_QUERY_SIZE, batchedEntities -> {
			contextProvider.get().insertInto(TABLE).values(batchedEntities).execute();
		}));
		return entities;
	}

	@NonNull
	@Override
	public Optional<SentreportsRecord> findById(@NonNull @NotNull byte[] signature) {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(TABLE.SIGNATURE.eq(signature))
				.fetchOptionalInto(SentreportsRecord.class);
	}

	@Override
	public boolean existsById(@NonNull @NotNull byte[] signature) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(TABLE)
						.where(TABLE.SIGNATURE.eq(signature))
		);
	}

	@NonNull
	@Override
	public Iterable<SentreportsRecord> findAll() {
		return () -> contextProvider.get().selectFrom(Tables.SENTREPORTS).stream().iterator();
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
	public void deleteById(@NonNull @NotNull byte[] signature) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.SIGNATURE.in(signature))
				.execute();
	}

	@Override
	public void delete(@NonNull @NotNull SentreportsRecord entity) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.SIGNATURE.in(entity.getSignature()))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends SentreportsRecord> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(BULK_MAX_QUERY_SIZE, batchedEntities -> {
			contextProvider.get().deleteFrom(TABLE)
					.where(TABLE.SIGNATURE.in(batchedEntities.stream().map(SentreportsRecord::getSignature).collect(Collectors.toList())))
					.execute();
		}));
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(TABLE);
	}

}