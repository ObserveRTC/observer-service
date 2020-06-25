package com.observertc.gatekeeper.webrtcstat.repositories;

import com.observertc.gatekeeper.webrtcstat.UUIDAdapter;
import com.observertc.gatekeeper.webrtcstat.dto.EvaluatorDTO;
import com.observertc.gatekeeper.webrtcstat.dto.ObserverDTO;
import com.observertc.gatekeeper.webrtcstat.jooq.Tables;
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
import org.jooq.Record3;
import org.jooq.RecordHandler;

@Singleton
public class ObserverRepository implements CrudRepository<ObserverDTO, UUID> {
	/**
	 * Due to the fact that bulk operations increase the size of the query,
	 * this limit enforces to send the query when it reaches a certain
	 * number of entry.
	 */
	private static int DEFAULT_BULK_SIZE = 5000;
	private final JooqBulkWrapper jooqExecuteWrapper;
	private final IDSLContextProvider contextProvider;

	public ObserverRepository(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
		this.jooqExecuteWrapper = new JooqBulkWrapper(contextProvider, DEFAULT_BULK_SIZE);
	}

	@NonNull
	@Override
	public <S extends ObserverDTO> S save(@NonNull @Valid @NotNull S entity) {
		// TODO: eiher throw exception, or return with something else
		this.contextProvider.get()
				.insertInto(Tables.OBSERVERS)
				.columns(Tables.OBSERVERS.UUID, Tables.OBSERVERS.NAME, Tables.OBSERVERS.DESCRIPTION)
				.values(UUIDAdapter.toBytes(entity.uuid), entity.name, entity.description)
				.onDuplicateKeyUpdate()
				.set(Tables.OBSERVERS.UUID, UUIDAdapter.toBytes(entity.uuid))
				.set(Tables.OBSERVERS.NAME, entity.name)
				.set(Tables.OBSERVERS.DESCRIPTION, entity.description)
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends ObserverDTO> S update(@NonNull @Valid @NotNull S entity) {
		this.contextProvider.get().update(Tables.OBSERVERS)
				.set(Tables.OBSERVERS.NAME, entity.name)
				.set(Tables.OBSERVERS.DESCRIPTION, entity.description)
				.where(Tables.OBSERVERS.UUID.eq(UUIDAdapter.toBytes(entity.uuid)))
				.execute();
		return entity;
	}

	@NonNull
	@Override
	public <S extends ObserverDTO> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
		this.jooqExecuteWrapper.execute((context, items) -> {
			var sql =
					context.insertInto(Tables.OBSERVERS,
							Tables.OBSERVERS.UUID,
							Tables.OBSERVERS.NAME,
							Tables.OBSERVERS.DESCRIPTION);
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
				.select(Tables.OBSERVERS.UUID, Tables.OBSERVERS.NAME, Tables.OBSERVERS.DESCRIPTION)
				.from(Tables.OBSERVERS)
				.where(Tables.OBSERVERS.UUID.eq(UUIDAdapter.toBytes(uuid)))
				.fetchOptionalInto(ObserverDTO.class);
	}

	public Iterable<EvaluatorDTO> findEvaluators(@NonNull @NotNull UUID uuid) {
		return this.contextProvider.get()
				.select(Tables.ORGANISATIONS.UUID, Tables.ORGANISATIONS.NAME, Tables.ORGANISATIONS.DESCRIPTION)
				.from(Tables.ORGANISATIONS
						.join(Tables.OBSERVERORGANISATIONS).on(Tables.OBSERVERORGANISATIONS.ORGANISATION_ID.eq(Tables.ORGANISATIONS.ID))
						.join(Tables.OBSERVERS).on(Tables.OBSERVERORGANISATIONS.OBSERVER_ID.eq(Tables.OBSERVERS.ID))
						.where(Tables.OBSERVERS.UUID.eq(UUIDAdapter.toBytes(uuid))))
				.fetch().into(EvaluatorDTO.class);
	}

	@Override
	public boolean existsById(@NonNull @NotNull UUID uuid) {
		return this.contextProvider.get().fetchExists(
				this.contextProvider.get()
						.selectOne()
						.from(Tables.OBSERVERS)
						.where(Tables.OBSERVERS.UUID.eq(UUIDAdapter.toBytes(uuid)))
		);
	}

	@NonNull
	@Override
	public Iterable<ObserverDTO> findAll() {
		this.contextProvider.get().select(Tables.OBSERVERS.UUID, Tables.OBSERVERS.NAME, Tables.OBSERVERS.DESCRIPTION)
				.from(Tables.OBSERVERS)
				.orderBy(Tables.OBSERVERS.ID)
				.fetch().into(new RecordHandler<Record3<byte[], String, String>>() {
			@Override
			public void next(Record3<byte[], String, String> record) {

			}
		});
		Long tableSize = this.count();
		int bulkSize = DEFAULT_BULK_SIZE;
		return () -> this.jooqExecuteWrapper.retrieve((context, offset) -> {
			var result = context
					.select(Tables.OBSERVERS.UUID, Tables.OBSERVERS.NAME, Tables.OBSERVERS.DESCRIPTION)
					.from(Tables.OBSERVERS)
					.orderBy(Tables.OBSERVERS.ID)
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
				.from(Tables.OBSERVERS)
				.fetchOne(0, long.class);
	}

	@Override
	public void deleteById(@NonNull @NotNull UUID uuid) {
		byte[] byteUUID = UUIDAdapter.toBytes(uuid);
		this.contextProvider.get().deleteFrom(Tables.OBSERVERS)
				.where(Tables.OBSERVERS.UUID.in(byteUUID))
				.execute();
	}

	@Override
	public void delete(@NonNull @NotNull ObserverDTO entity) {
		byte[] byteUUID = UUIDAdapter.toBytes(entity.uuid);
		this.contextProvider.get().deleteFrom(Tables.OBSERVERS)
				.where(Tables.OBSERVERS.UUID.in(byteUUID))
				.execute();
	}

	@Override
	public void deleteAll(@NonNull @NotNull Iterable<? extends ObserverDTO> entities) {
		Iterator<? extends ObserverDTO> iterator = entities.iterator();
		this.jooqExecuteWrapper.execute((context, items) -> {

			List<byte[]> keys = StreamSupport.stream(entities
					.spliterator(), false).map(e -> UUIDAdapter.toBytes(e.uuid)).collect(Collectors.toList());

			context
					.deleteFrom(Tables.OBSERVERS)
					.where(Tables.OBSERVERS.UUID.in(keys))
					.execute();
		}, entities);
	}

	@Override
	public void deleteAll() {
		this.contextProvider.get().deleteFrom(Tables.OBSERVERS);
	}

}