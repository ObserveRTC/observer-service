package com.observertc.gatekeeper.repositories;

import static com.observertc.gatekeeper.jooq.Tables.EVALUATORS;
import static com.observertc.gatekeeper.jooq.Tables.OBSERVEREVALUATORS;
import static com.observertc.gatekeeper.jooq.Tables.OBSERVERS;
import com.observertc.gatekeeper.dto.ObserverDTO;
import com.observertc.gatekeeper.dto.EvaluatorDTO;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.repository.CrudRepository;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Singleton
public class ObserverRepository implements CrudRepository<ObserverDTO, UUID> {
	/**
	 * Due to the fact that batch operations increase the size of the query,
	 * this limit enforces to send the query when it reaches a certain
	 * number of entry.
	 */
	private static int DEFAULT_BACH_SIZE = 5000;
	private final JooqBulkWrapper jooqExecuteWrapper;
	private final IDSLContextProvider contextProvider;

	public ObserverRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
		this.jooqExecuteWrapper = new JooqBulkWrapper(contextProvider, DEFAULT_BACH_SIZE);
	}

	@NonNull
	@Override
	public <S extends ObserverDTO> S save(@NonNull @Valid @NotNull S entity) {
		// TODO: eiher throw exception, or return with something else
		this.contextProvider.get()
				.insertInto(OBSERVERS)
				.columns(OBSERVERS.UUID, OBSERVERS.NAME, OBSERVERS.DESCRIPTION)
				.values(UUIDAdapter.toBytes(entity.uuid), entity.name, entity.description)
				.onDuplicateKeyUpdate()
				.set(OBSERVERS.UUID, UUIDAdapter.toBytes(entity.uuid))
				.set(OBSERVERS.NAME, entity.name)
				.set(OBSERVERS.DESCRIPTION, entity.description)
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends ObserverDTO> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().update(OBSERVERS)
				.set(OBSERVERS.NAME, entity.name)
				.set(OBSERVERS.DESCRIPTION, entity.description)
				.where(OBSERVERS.UUID.eq(UUIDAdapter.toBytes(entity.uuid)))
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends ObserverDTO> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		this.jooqExecuteWrapper.execute((context, items) -> {
			var sql =
					context.insertInto(OBSERVERS,
							OBSERVERS.UUID,
							OBSERVERS.NAME,
							OBSERVERS.DESCRIPTION);
			for (Iterator<S> it = items.iterator(); it.hasNext(); ) {
				ObserverDTO DTO = it.next();
				byte[] uuid = UUIDAdapter.toBytes(DTO.uuid);
				sql.values(uuid, DTO.name, DTO.description);
			}
			sql.execute();
		}, entities);
		return entities;
	}

	@NonNull
	@Override
	public Optional<ObserverDTO> findById(@NonNull @NotNull UUID uuid) {
		return this.contextProvider.get()
				.select(OBSERVERS.UUID, OBSERVERS.NAME, OBSERVERS.DESCRIPTION)
				.from(OBSERVERS)
				.where(OBSERVERS.UUID.eq(UUIDAdapter.toBytes(uuid)))
				.fetchOptionalInto(ObserverDTO.class);
	}

	public Iterable<EvaluatorDTO> findEvaluators(@NonNull @NotNull UUID uuid) {
		return this.contextProvider.get()
				.select(EVALUATORS.UUID, EVALUATORS.NAME, EVALUATORS.DESCRIPTION)
				.from(EVALUATORS
						.join(OBSERVEREVALUATORS).on(OBSERVEREVALUATORS.EVALUATOR_ID.eq(EVALUATORS.ID))
						.join(OBSERVERS).on(OBSERVEREVALUATORS.OBSERVER_ID.eq(OBSERVERS.ID))
						.where(OBSERVERS.UUID.eq(UUIDAdapter.toBytes(uuid))))
				.fetch().into(EvaluatorDTO.class);
	}

	@Override
	public boolean existsById(@NonNull @NotNull UUID uuid) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(OBSERVERS)
						.where(OBSERVERS.UUID.eq(UUIDAdapter.toBytes(uuid)))
		);
	}

	@NonNull
	@Override
	public Iterable<ObserverDTO> findAll() {
		Long tableSize = this.count();
		int bulkSize = DEFAULT_BACH_SIZE;
		return () -> this.jooqExecuteWrapper.retrieve((context, offset) -> {
			var result = context
					.select(OBSERVERS.UUID, OBSERVERS.NAME, OBSERVERS.DESCRIPTION)
					.from(OBSERVERS)
					.orderBy(OBSERVERS.ID)
					.offset(offset)
					.limit(bulkSize)
					.fetch().into(ObserverDTO.class);
			return result.iterator();
		}, tableSize.intValue());
	}

	@Override
	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(OBSERVERS)
				.fetchOne(0, long.class);
	}

	@Override
	public void deleteById(@NonNull @NotNull UUID uuid) {
		byte[] byteUUID = UUIDAdapter.toBytes(uuid);
		this.contextProvider.get().deleteFrom(OBSERVERS)
				.where(OBSERVERS.UUID.in(byteUUID))
				.execute();
	}

	@Override
	public void delete(@NonNull @NotNull ObserverDTO entity) {
		byte[] byteUUID = UUIDAdapter.toBytes(entity.uuid);
		this.contextProvider.get().deleteFrom(OBSERVERS)
				.where(OBSERVERS.UUID.in(byteUUID))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends ObserverDTO> entities) {
		Iterator<? extends ObserverDTO> iterator = entities.iterator();
		this.jooqExecuteWrapper.execute((context, items) -> {

			List<byte[]> keys = StreamSupport.stream(entities
					.spliterator(), false).map(e -> UUIDAdapter.toBytes(e.uuid)).collect(Collectors.toList());

			context
					.deleteFrom(OBSERVERS)
					.where(OBSERVERS.UUID.in(keys))
					.execute();
		}, entities);
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(OBSERVERS);
	}

}