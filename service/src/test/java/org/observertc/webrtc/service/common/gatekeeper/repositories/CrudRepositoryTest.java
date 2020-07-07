//package com.observertc.gatekeeper.repositories;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertIterableEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import io.micronaut.data.repository.CrudRepository;
//import java.util.Optional;
//import java.util.stream.StreamSupport;
//import org.jooq.exception.DataAccessException;
//import org.junit.jupiter.api.Test;
//
///**
// * Performs a so called "smoke" test on a repository extended from JooqControlledRepository
// *
// * @param <T>
// * @param <E>
// */
//
//public interface CrudRepositoryTest<T, E, R extends CrudRepository<E, T>> {
//
//	R getRepository();
//
//	Iterable<E> getExistingEntities();
//
//	Iterable<E> getNotExistingEntities();
//
//	E getNotExistingEntity();
//
//	E getExistingEntity();
//
//	T getExistingId();
//
//	T getNotExistingID();
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to save,
//	 * and the ID is NOT presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#save(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed, and returns with the saved entity
//	 */
//	@Test
//	default void shouldSaveANotExistingEntity() {
//		// Given 
//		E entity = this.getNotExistingEntity();
//
//		// When
//		E actual = this.getRepository().save(entity);
//
//		// Then
//		assertEquals(entity, actual);
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to save,
//	 * and the ID is presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#save(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed, but it
//	 * overrides the entity and returns with the new value
//	 */
//	@Test
//	default void shouldSaveAnExistingEntity() {
//		// Given 
//		E entity = this.getExistingEntity();
//
//		// When
//		E actual = this.getRepository().save(entity);
//
//		// Then
//		assertEquals(entity, actual);
//	}
//
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to update,
//	 * and the ID is NOT presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#update(Object)} is called
//	 *
//	 * <b>Then</b> {@link org.jooq.exception.DataAccessException} is thrown
//	 */
//	@Test
//	default void shouldNotUpdateANotExistingEntity() {
//		// Given 
//		E entity = this.getNotExistingEntity();
//
//		// Then
//		assertThrows(DataAccessException.class, () -> {
//			// When
//			E actual = this.getRepository().update(entity);
//		});
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to save,
//	 * and the ID is presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#update(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed, and it
//	 * overrides the entity and returns with the new value
//	 */
//	@Test
//	default void shouldUpdateAnExistingEntity() {
//		// Given
//		E entity = this.getExistingEntity();
//
//		// When
//		E actual = this.getRepository().update(entity);
//
//		// Then
//		assertEquals(entity, actual);
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITIES to save,
//	 * and the IDs are NOT presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#saveAll(Iterable)} is called
//	 *
//	 * <b>Then</b> the query is executed, and it
//	 * overrides the entities and returns with the new values
//	 */
//	@Test
//	default void shouldSaveAllExistingEntities() {
//		// Given
//		Iterable<E> entities = this.getExistingEntities();
//
//		// When
//		Iterable<E> actual = this.getRepository().saveAll(entities);
//
//		// Then
//		assertIterableEquals(entities, actual);
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITIES to save,
//	 * and the IDs are presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#saveAll(Iterable)} is called
//	 *
//	 * <b>Then</b> the query is executed, and it
//	 * overrides the entities and returns with the new values
//	 */
//	@Test
//	default void shouldSaveAllNotExistingEntities() {
//		// Given
//		Iterable<E> entities = this.getNotExistingEntities();
//
//		// When
//		Iterable<E> actual = this.getRepository().saveAll(entities);
//
//		// Then
//		assertIterableEquals(entities, actual);
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to find,
//	 * and the ID is presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#findById(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed, and it returns with the found entity
//	 */
//	@Test
//	default void shouldFindByIDAnExistingEntity() {
//		// Given
//		T id = this.getExistingId();
//
//		// When
//		Optional<E> actual = this.getRepository().findById(id);
//
//		// Then
//		assertTrue(actual.isPresent());
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to find,
//	 * and the ID is presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#findById(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed, and it returns true
//	 */
//	@Test
//	default void shouldExistsByIDForExistingEntity() {
//		// Given
//		T id = this.getExistingId();
//
//		// When
//		boolean actual = this.getRepository().existsById(id);
//
//		// Then
//		assertTrue(actual);
//	}
//
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITIES to find,
//	 * and the IDs are presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#findAll()} is called
//	 *
//	 * <b>Then</b> the query is executed, and it returns with the entities
//	 */
//	@Test
//	default void shouldFindAllI() {
//		// When
//		Iterable<E> actual = this.getRepository().findAll();
//
//		// Then
//		long count = StreamSupport.stream(actual.spliterator(), false).count();
//		assertTrue(0L < count);
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITIES to find,
//	 * and the IDs are NOT presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#count()}  is called
//	 *
//	 * <b>Then</b> the query is executed, and it returns with an integer greater than 0
//	 */
//	@Test
//	default void shouldCount() {
//		// When
//		long count = this.getRepository().count();
//
//		// Then
//		assertTrue(0L < count);
//	}
//
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to delete,
//	 * and the ID is NOT presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#deleteById(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed, and throws a {@link org.jooq.exception.DataAccessException}
//	 */
//	@Test
//	default void shouldNotDeleteByIdANotExistingEntity() {
//		// Given 
//		T id = this.getNotExistingID();
//
//		// Then
//		assertThrows(DataAccessException.class, () -> {
//			// When
//			this.getRepository().deleteById(id);
//		});
//	}
//
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to delete,
//	 * and the ID is presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#deleteById(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed
//	 */
//	@Test
//	default void shouldDeleteByIdAnExistingEntity() {
//		// Given
//		T id = this.getExistingId();
//
//		// When
//		this.getRepository().deleteById(id);
//
//		// Then
//		// an exception should be thrown if the id does not exists
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to delete,
//	 * and the ID is NOT presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#delete(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed, and throws a {@link org.jooq.exception.DataAccessException}
//	 */
//	@Test
//	default void shouldNotDeleteANotExistingEntity() {
//		// Given 
//		E entity = this.getNotExistingEntity();
//
//		// Then
//		assertThrows(DataAccessException.class, () -> {
//			// When
//			this.getRepository().delete(entity);
//		});
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITY to delete,
//	 * and the ID is presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#deleteById(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed
//	 */
//	@Test
//	default void shouldDeleteAnExistingEntity() {
//		// Given
//		E entity = this.getExistingEntity();
//
//		// When
//		this.getRepository().delete(entity);
//
//		// Then
//		// An exception should be thrown if the query has not been executed
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and an ENTITIES to delete,
//	 * and the IDs are presented in the DB
//	 *
//	 * <b>When</b> the {@link CrudRepository#deleteById(Object)} is called
//	 *
//	 * <b>Then</b> the query is executed
//	 */
//	@Test
//	default void shouldDeleteAllExistingEntities() {
//		// Given
//		Iterable<E> entity = this.getExistingEntities();
//
//		// When
//		this.getRepository().deleteAll(entity);
//
//		// Then
//		// An exception should be thrown if the query has not been executed
//	}
//
//	/**
//	 * <b>Given</b> a {@link CrudRepository} and ENTITIES to delete,
//	 *
//	 * <b>When</b> the {@link CrudRepository#deleteAll()}  is called
//	 *
//	 * <b>Then</b> the query is executed
//	 */
//	@Test
//	default void shouldPurgeAllExistingEntities() {
//		// When
//		this.getRepository().deleteAll();
//
//		// Then
//		// An exception should be thrown if the query has not been executed
//	}
//}