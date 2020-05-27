//package com.observertc.gatekeeper.repositories;
//
//import com.observertc.gatekeeper.dto.UserDTO;
//import edu.umd.cs.findbugs.annotations.NonNull;
//import io.micronaut.data.repository.CrudRepository;
//import java.util.Optional;
//import java.util.UUID;
//import javax.inject.Singleton;
//import javax.validation.Valid;
//import javax.validation.constraints.NotNull;
//
//@Singleton
//public class UsersRepository implements CrudRepository<UUID, UserDTO> {
//
//	private static final int DEFAULT_BULK_SIZE = 5000;
//
//	private final JooqBulkWrapper jooqBulkWrapper;
//
//	public UsersRepository(IDSLContextProvider dslContextProvider) {
//		this.jooqBulkWrapper = new JooqBulkWrapper(dslContextProvider, DEFAULT_BULK_SIZE);
//	}
//
//	@NonNull
//	@Override
//	public <S extends UUID> S save(@NonNull @Valid @NotNull S entity) {
//		return null;
//	}
//
//	@NonNull
//	@Override
//	public <S extends UUID> S update(@NonNull @Valid @NotNull S entity) {
//		return null;
//	}
//
//	@NonNull
//	@Override
//	public <S extends UUID> Iterable<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities) {
//		return null;
//	}
//
//	@NonNull
//	@Override
//	public Optional<UUID> findById(@NonNull @NotNull UserDTO userDTO) {
//		return Optional.empty();
//	}
//
//	@Override
//	public boolean existsById(@NonNull @NotNull UserDTO userDTO) {
//		return false;
//	}
//
//	@NonNull
//	@Override
//	public Iterable<UUID> findAll() {
//		return null;
//	}
//
//	@Override
//	public long count() {
//		return 0;
//	}
//
//	@Override
//	public void deleteById(@NonNull @NotNull UserDTO userDTO) {
//
//	}
//
//	@Override
//	public void delete(@NonNull @NotNull UUID entity) {
//
//	}
//
//	@Override
//	public void deleteAll(@NonNull @NotNull Iterable<? extends UUID> entities) {
//
//	}
//
//	@Override
//	public void deleteAll() {
//
//	}
//}