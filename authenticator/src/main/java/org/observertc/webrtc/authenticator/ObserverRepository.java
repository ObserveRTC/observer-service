//package org.observertc.webrtc.authenticator;
//
//import edu.umd.cs.findbugs.annotations.NonNull;
//import io.micronaut.data.repository.CrudRepository;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.stream.Collectors;
//import java.util.stream.StreamSupport;
//import javax.inject.Singleton;
//import javax.validation.Valid;
//import javax.validation.constraints.NotNull;
//import org.jooq.InsertValuesStep3;
//import org.observertc.webrtc.common.UUIDAdapter;
//import org.observertc.webrtc.observer.dto.ObserverDTO;
//import org.observertc.webrtc.observer.jooq.Tables;
//import org.observertc.webrtc.observer.jooq.tables.Observers;
//import org.observertc.webrtc.observer.jooq.tables.records.ObserversRecord;
//
//@Singleton
//public class ObserverRepository implements CrudRepository<ObserverDTO, UUID> {
//	/**
//	 * Due to the fact that bulk operations increase the size of the query,
//	 * this limit enforces to send the query when it reaches a certain
//	 * number of entry.
//	 */
//	private static int DEFAULT_BULK_SIZE = 5000;
//	private static final Observers TABLE = Tables.OBSERVERS;
//
//	//	private final JooqBulkWrapper jooqExecuteWrapper;
//	private final IDSLContextProvider contextProvider;
//
//	public ObserverRepository(IDSLContextProvider contextProvider) {
//		this.contextProvider = contextProvider;
//	}
//
//	@NonNull
//	@Override
//	public <S extends ObserverDTO> S save(@NonNull @Valid @NotNull S entity) {
//		// TODO: eiher throw exception, or return with something else
//		this.contextProvider.get()
//				.insertInto(TABLE)
//				.columns(TABLE.UUID, TABLE.NAME, TABLE.DESCRIPTION)
//				.values(UUIDAdapter.toBytes(entity.uuid), entity.name, entity.description)
//				.onDuplicateKeyUpdate()
//				.set(TABLE.UUID, UUIDAdapter.toBytes(entity.uuid))
//				.set(TABLE.NAME, entity.name)
//				.set(TABLE.DESCRIPTION, entity.description)
//				.execute();
//		return entity;
//	}
//
//	@NonNull
//	@Override
//	public <S extends ObserverDTO> S update(@NonNull @Valid @NotNull S entity) {
//		this.contextProvider.get().update(Tables.OBSERVERS)
//				.set(Tables.OBSERVERS.NAME, entity.name)
//				.set(Tables.OBSERVERS.DESCRIPTION, entity.description)
//				.where(Tables.OBSERVERS.UUID.eq(UUIDAdapter.toBytes(entity.uuid)))
//				.execute();
//		return entity;
//	}
//
//	@NonNull
//	@Override
//	public <S extends ObserverDTO> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
//		InsertValuesStep3<ObserversRecord, byte[], String, String> sql =
//				this.contextProvider.get().insertInto(Tables.OBSERVERS,
//						Tables.OBSERVERS.UUID,
//						Tables.OBSERVERS.NAME,
//						Tables.OBSERVERS.DESCRIPTION);
//		for (Iterator<S> it = entities.iterator(); it.hasNext(); ) {
//			ObserverDTO DTO = it.next();
//			byte[] uuid = UUIDAdapter.toBytes(DTO.uuid);
//			sql.values(uuid, DTO.name, DTO.description);
//		}
//		sql.execute();
//		return entities;
//	}
//
//	@NonNull
//	@Override
//	public Optional<ObserverDTO> findById(@NonNull @NotNull UUID uuid) {
//		return this.contextProvider.get()
//				.select(Tables.OBSERVERS.UUID, Tables.OBSERVERS.NAME, Tables.OBSERVERS.DESCRIPTION)
//				.from(Tables.OBSERVERS)
//				.where(Tables.OBSERVERS.UUID.eq(UUIDAdapter.toBytes(uuid)))
//				.fetchOptionalInto(ObserverDTO.class);
//	}
//
//	@Override
//	public boolean existsById(@NonNull @NotNull UUID uuid) {
//		return this.contextProvider.get().fetchExists(
//				this.contextProvider.get()
//						.selectOne()
//						.from(Tables.OBSERVERS)
//						.where(Tables.OBSERVERS.UUID.eq(UUIDAdapter.toBytes(uuid)))
//		);
//	}
//
//	@NonNull
//	@Override
//	public Iterable<ObserverDTO> findAll() {
//		return () -> this.contextProvider.get().selectFrom(Tables.OBSERVERS).fetchInto(ObserverDTO.class).iterator();
//	}
//
//	@Override
//	public long count() {
//		return this.contextProvider
//				.get()
//				.selectCount()
//				.from(Tables.OBSERVERS)
//				.fetchOne(0, long.class);
//	}
//
//	@Override
//	public void deleteById(@NonNull @NotNull UUID uuid) {
//		byte[] byteUUID = UUIDAdapter.toBytes(uuid);
//		this.contextProvider.get().deleteFrom(Tables.OBSERVERS)
//				.where(Tables.OBSERVERS.UUID.in(byteUUID))
//				.execute();
//	}
//
//	@Override
//	public void delete(@NonNull @NotNull ObserverDTO entity) {
//		byte[] byteUUID = UUIDAdapter.toBytes(entity.uuid);
//		this.contextProvider.get().deleteFrom(Tables.OBSERVERS)
//				.where(Tables.OBSERVERS.UUID.in(byteUUID))
//				.execute();
//	}
//
//	@Override
//	public void deleteAll(@NonNull @NotNull Iterable<? extends ObserverDTO> entities) {
//		List<byte[]> keys = StreamSupport.stream(entities
//				.spliterator(), false).map(e -> UUIDAdapter.toBytes(e.uuid)).collect(Collectors.toList());
//
//		this.contextProvider.get().deleteFrom(Tables.OBSERVERS).where(
//				Tables.OBSERVERS.UUID.in(keys)
//		).execute();
//	}
//
//	@Override
//	public void deleteAll() {
//		this.contextProvider.get().deleteFrom(Tables.OBSERVERS);
//	}
//
//}