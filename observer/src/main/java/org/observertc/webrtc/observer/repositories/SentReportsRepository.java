package org.observertc.webrtc.observer.repositories;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.jooq.Field;
import org.jooq.InsertValuesStep3;
import org.jooq.impl.DSL;
import org.observertc.webrtc.common.BatchCollector;
import org.observertc.webrtc.observer.jooq.Tables;
import org.observertc.webrtc.observer.jooq.tables.Sentreports;
import org.observertc.webrtc.observer.jooq.tables.records.SentreportsRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SentReportsRepository {

	private static final Logger logger = LoggerFactory.getLogger(SentReportsRepository.class);

	private static Sentreports TABLE = Tables.SENTREPORTS;
	private static int DEFAULT_BULK_SIZE = 5000;


	private static <T> Field<T> values(Field<T> field) {
		return DSL.field("VALUES({0})", field.getDataType(), field);
	}

	private final IDSLContextProvider contextProvider;

	public SentReportsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

	@NonNull
	public <S extends SentreportsRecord> S save(@NonNull @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.execute();
		return entity;
	}

	@NonNull
	public <S extends SentreportsRecord> S update(@NonNull @NotNull S entity) {
		this.contextProvider.get().insertInto(TABLE).set(entity)
				.onDuplicateKeyUpdate()
				.set(TABLE.PEERCONNECTIONUUID, values(TABLE.PEERCONNECTIONUUID))
				.set(TABLE.SIGNATURE, values(TABLE.SIGNATURE))
				.set(TABLE.REPORTED, values(TABLE.REPORTED))
				.execute();
		return entity;
	}

	@NonNull
	public <S extends SentreportsRecord> Iterable<S> saveAll(@NonNull @NotNull Iterable<S> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(DEFAULT_BULK_SIZE, batchedEntities -> {
			contextProvider.get().batchInsert(batchedEntities).execute();
		}));
		return entities;
	}

	@NonNull
	public <S extends SentreportsRecord> Iterable<S> updateAll(@NonNull @NotNull Iterable<S> entities) {
		this.consumeBatches(entities, batchedEntities -> {
			InsertValuesStep3<SentreportsRecord, byte[], byte[], Long> sql =
					contextProvider.get().insertInto(
							TABLE,
							TABLE.PEERCONNECTIONUUID,
							TABLE.SIGNATURE,
							TABLE.REPORTED);
			Iterator<S> it = batchedEntities.iterator();
			for (; it.hasNext(); ) {
				SentreportsRecord record = it.next();
				sql.values(record.getPeerconnectionuuid(),
						record.getSignature(),
						record.getReported());
			}
			sql
					.onDuplicateKeyUpdate()
					.set(TABLE.PEERCONNECTIONUUID, values(TABLE.PEERCONNECTIONUUID))
					.set(TABLE.SIGNATURE, values(TABLE.SIGNATURE))
					.set(TABLE.REPORTED, values(TABLE.REPORTED))
					.execute();
		});
		return entities;
	}

	private <S extends SentreportsRecord> void consumeBatches(Iterable<S> entities, Consumer<Iterable<S>> consumer) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(DEFAULT_BULK_SIZE, batchedEntities -> {
			if (batchedEntities.size() < 1) {
				return;
			}
			consumer.accept(batchedEntities);
		}));
	}


	@NonNull
	public Optional<SentreportsRecord> findById(@NonNull @NotNull byte[] signature) {
		return this.contextProvider.get()
				.selectFrom(TABLE)
				.where(TABLE.SIGNATURE.eq(signature))
				.fetchOptionalInto(SentreportsRecord.class);
	}

	public boolean existsBySignature(@NonNull @NotNull byte[] signature) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(TABLE)
						.where(TABLE.SIGNATURE.in(signature))
		);


	}

	@NonNull
	public Iterable<SentreportsRecord> findAll() {
		return () -> contextProvider.get().selectFrom(TABLE).stream().iterator();
	}

	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(TABLE)
				.fetchOne(0, long.class);
	}

	public void deleteById(@NonNull @NotNull byte[] signature) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.SIGNATURE.eq(signature))
				.execute();
	}

	public void delete(@NonNull @NotNull SentreportsRecord entity) {
		this.contextProvider.get().deleteFrom(TABLE)
				.where(TABLE.SIGNATURE.eq(entity.getSignature()))
				.execute();
	}


	public void deleteAll(@NonNull @NotNull Iterable<? extends SentreportsRecord> entities) {
		StreamSupport.stream(entities.spliterator(), false).collect(BatchCollector.makeCollector(DEFAULT_BULK_SIZE, batchedEntities -> {
			contextProvider.get().deleteFrom(TABLE)
					.where(TABLE.SIGNATURE
							.in(batchedEntities.stream().map(SentreportsRecord::getSignature).collect(Collectors.toList())
							))
					.execute();
		}));
	}

	public void deleteAll() {
		this.contextProvider.get().deleteFrom(TABLE).execute();
	}

	public void deleteReportedOlderThan(Long threshold) {
		this.contextProvider.get().deleteFrom(TABLE).where(TABLE.REPORTED.lt(threshold)).execute();
	}
}