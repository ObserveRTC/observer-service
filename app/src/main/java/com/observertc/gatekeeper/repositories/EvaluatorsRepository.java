package com.observertc.gatekeeper.repositories;

import static com.observertc.gatekeeper.jooq.Tables.EVALUATORS;
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
public class EvaluatorsRepository implements CrudRepository<EvaluatorDTO, UUID> {
	/**
	 * Due to the fact that batch operations increase the size of the query,
	 * this limit enforces to send the query when it reaches a certain
	 * number of entry.
	 */
	private static int DEFAULT_BACH_SIZE = 5000;
	private final JooqBulkWrapper jooqExecuteWrapper;
	private final IDSLContextProvider contextProvider;

	public EvaluatorsRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
		this.jooqExecuteWrapper = new JooqBulkWrapper(contextProvider, DEFAULT_BACH_SIZE);
	}

	@NonNull
	@Override
	public <S extends EvaluatorDTO> S save(@NonNull @Valid @NotNull S entity) {
		// TODO: eiher throw exception, or return with something else
		this.contextProvider.get()
				.insertInto(EVALUATORS)
				.columns(EVALUATORS.UUID, EVALUATORS.NAME, EVALUATORS.DESCRIPTION)
				.values(UUIDAdapter.toBytes(entity.uuid), entity.name, entity.description)
				.onDuplicateKeyUpdate()
				.set(EVALUATORS.UUID, UUIDAdapter.toBytes(entity.uuid))
				.set(EVALUATORS.NAME, entity.name)
				.set(EVALUATORS.DESCRIPTION, entity.description)
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends EvaluatorDTO> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().update(EVALUATORS)
				.set(EVALUATORS.NAME, entity.name)
				.set(EVALUATORS.DESCRIPTION, entity.description)
				.where(EVALUATORS.UUID.eq(UUIDAdapter.toBytes(entity.uuid)))
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends EvaluatorDTO> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		this.jooqExecuteWrapper.execute((context, items) -> {
			var sql =
					context.insertInto(EVALUATORS,
							EVALUATORS.UUID,
							EVALUATORS.NAME,
							EVALUATORS.DESCRIPTION);
			for (Iterator<S> it = items.iterator(); it.hasNext(); ) {
				EvaluatorDTO DTO = it.next();
				byte[] uuid = UUIDAdapter.toBytes(DTO.uuid);
				sql.values(uuid, DTO.name, DTO.description);
			}
			sql.execute();
		}, entities);
		return entities;
	}

	@NonNull
	@Override
	public Optional<EvaluatorDTO> findById(@NonNull @NotNull UUID uuid) {
		return this.contextProvider.get()
				.select(EVALUATORS.UUID, EVALUATORS.NAME, EVALUATORS.DESCRIPTION)
				.from(EVALUATORS)
				.where(EVALUATORS.UUID.eq(UUIDAdapter.toBytes(uuid)))
				.fetchOptionalInto(EvaluatorDTO.class);
	}

	@Override
	public boolean existsById(@NonNull @NotNull UUID uuid) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(EVALUATORS)
						.where(EVALUATORS.UUID.eq(UUIDAdapter.toBytes(uuid)))
		);
	}

	@NonNull
	@Override
	public Iterable<EvaluatorDTO> findAll() {
		Long tableSize = this.count();
		int bulkSize = DEFAULT_BACH_SIZE;
		return () -> this.jooqExecuteWrapper.retrieve((context, offset) -> {
			var result = context
					.select(EVALUATORS.UUID, EVALUATORS.NAME, EVALUATORS.DESCRIPTION)
					.from(EVALUATORS)
					.orderBy(EVALUATORS.ID)
					.offset(offset)
					.limit(bulkSize)
					.fetch().into(EvaluatorDTO.class);
			return result.iterator();
		}, tableSize.intValue());
	}

	@Override
	public long count() {
		return this.contextProvider
				.get()
				.selectCount()
				.from(EVALUATORS)
				.fetchOne(0, long.class);
	}

	@Override
	public void deleteById(@NonNull @NotNull UUID uuid) {
		byte[] byteUUID = UUIDAdapter.toBytes(uuid);
		this.contextProvider.get().deleteFrom(EVALUATORS)
				.where(EVALUATORS.UUID.in(byteUUID))
				.execute();
	}

	@Override
	public void delete(@NonNull @NotNull EvaluatorDTO entity) {
		byte[] byteUUID = UUIDAdapter.toBytes(entity.uuid);
		this.contextProvider.get().deleteFrom(EVALUATORS)
				.where(EVALUATORS.UUID.in(byteUUID))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends EvaluatorDTO> entities) {
		Iterator<? extends EvaluatorDTO> iterator = entities.iterator();
		this.jooqExecuteWrapper.execute((context, items) -> {

			List<byte[]> keys = StreamSupport.stream(entities
					.spliterator(), false).map(e -> UUIDAdapter.toBytes(e.uuid)).collect(Collectors.toList());

			context
					.deleteFrom(EVALUATORS)
					.where(EVALUATORS.UUID.in(keys))
					.execute();
		}, entities);
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(EVALUATORS);
	}

}