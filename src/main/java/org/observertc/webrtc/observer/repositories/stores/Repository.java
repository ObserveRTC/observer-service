/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.observer.repositories.stores;

import io.reactivex.Single;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Repository<K, V> {

	/**
	 * Saves the entity to the map
	 *
	 * @param key    the key the entity will be associated to
	 * @param entity the entity, which is going to be saved
	 * @param <R>    The type of the key, extending the type of the key of the map
	 * @param <U>    the type of the value extending the type of the value of the map
	 */
	<R extends K, U extends V> void save(@NotNull R key, @NotNull U entity);

	/**
	 * The ReactiveX version of the {@link this#save(Object, Object)}.
	 *
	 * @param key    the key the entity will be associated to
	 * @param entity the entity, which is going to be saved
	 * @param <R>    The type of the key, extending the type of the key of the map
	 * @param <U>    the type of the value extending the type of the value of the map
	 */
	default <R extends K, U extends V> Completable rxSave(@NotNull R key, @NotNull U entity) {
		return Completable.fromRunnable(() -> this.save(key, entity));
	}


	/**
	 * Saves all the entities given as a parameter
	 *
	 * @param entities the entities designated to be saved
	 * @param <R>      The type of the key, extending the type of the key of the map
	 * @param <U>      the type of the value extending the type of the value of the map
	 */
	<R extends K, U extends V> void saveAll(@NotNull Map<R, U> entities);

	/**
	 * The reactiveX version of the {@link this#saveAll(Map)}
	 *
	 * @param entities the entities designated to be saved
	 * @param <R>      The type of the key, extending the type of the key of the repository
	 * @param <U>      the type of the value extending the type of the value of the repository
	 * @return
	 */
	default <R extends K, U extends V> Completable rxSaveAll(@NotNull Map<R, U> entities) {
		return Completable.fromRunnable(() -> this.saveAll(entities));
	}


	/**
	 * Saves the entity to the map if it has not been saved before, and returns with the value associated with the key
	 *
	 * @param key   the key we save the entity for
	 * @param value
	 * @param <R>   The type of the key, extending the type of the key of the repository
	 * @param <U>   the type of the value extending the type of the value of the repository
	 * @return An {@link Optional} holding the value or an empty optional
	 */
	<R extends K, U extends V> Optional<V> saveIfAbsent(@NotNull R key, @NotNull U value);

	/**
	 * The reactiveX version of the {@link this#saveIfAbsent(Object, Object)}
	 *
	 * @param key   the key we save the entity for if it is absent
	 * @param value
	 * @param <R>   The type of the key, extending the type of the key of the repository
	 * @param <U>   the type of the value extending the type of the value of the repository
	 * @return
	 */
	default <R extends K, U extends V> Maybe<V> rxSaveIfAbsent(@NotNull R key, @NotNull U value) {
		return Maybe.fromOptional(this.saveIfAbsent(key, value));
	}

	/**
	 * Saves the entity to the map, and returns with its previous value
	 * if it had
	 *
	 * @param key   the key we save the entity for
	 * @param value
	 * @param <R>   The type of the key, extending the type of the key of the repository
	 * @param <U>   the type of the value extending the type of the value of the repository
	 * @return An {@link Optional} holding the value or an empty optional
	 */
	<R extends K, U extends V> Optional<V> update(@NotNull R key, @NotNull U value);

	/**
	 * The reactiveX version of the {@link this#update(Object, Object)}
	 *
	 * @param key   the key we save the entity for
	 * @param value
	 * @param <R>   The type of the key, extending the type of the key of the repository
	 * @param <U>   the type of the value extending the type of the value of the repository
	 * @return
	 */
	default <R extends K, U extends V> Maybe<V> rxUpdate(@NotNull R key, @NotNull U value) {
		return Maybe.fromOptional(this.update(key, value));
	}

	/**
	 * Updates all the value and returns a map with the previous values
	 * If a value was not presented in the map the return map will not conntains it.
	 * <p>
	 * Note: if no value was presented an empty map will be returned
	 *
	 * @param entities the entities we want to update
	 * @return the map holding the previous value associated to the given values
	 */
	Map<K, V> updateAll(@NotNull Map<K, V> entities);

	/**
	 * The reactiveX version of the {@link this#findAll(Set)}
	 *
	 * @param entities the entities we want to update
	 * @return an {@link Observable} streams the entries
	 */
	default Observable<Map.Entry<K, V>> rxUpdateAll(@NotNull Map<K, V> entities) {
		return Observable.fromIterable(this.updateAll(entities).entrySet());
	}

	/**
	 * Find the entity in the repository if it is presented there
	 *
	 * @param key the key we are looking for
	 * @param <S>
	 * @return An {@link Optional} holding the value or an empty optional
	 */
	<S extends K> Optional<V> find(@NotNull S key);

	/**
	 * The reactiveX version of the {@link this#find(Object)} method.
	 *
	 * @param key The key we want to find
	 * @return Returns an {@link Maybe} indicating the result of the operation
	 */
	default <S extends K> Maybe<V> rxFind(@NotNull S key) {
		return Maybe.fromOptional(this.find(key));
	}

	/**
	 * Find all key value pair in the repositoiry
	 *
	 * @param keys A set of keys we are loiking for
	 * @return
	 */
	Map<K, V> findAll(@NotNull Set<K> keys);

	/**
	 * The reactiveX version of the {@link this#findAll(Set)} method.
	 *
	 * @param keys The keys set we want to remove
	 * @return Returns an {@link Single} indicating the result of the operation
	 */
	default Observable<Map.Entry<K, V>> rxFindAll(@NotNull Set<K> keys) {
		return Observable.fromIterable(this.findAll(keys).entrySet());
	}

	/**
	 * Gets all entries stored in all endpoints
	 *
	 * @return
	 */
	Map<K, V> getAllEntries();

	/**
	 * The RXJava version to get all entries
	 * @return
	 */
	default Observable<Map.Entry<K, V>> rxGetAllEntries() {
		return Observable.fromIterable(this.getAllEntries().entrySet());
	}

	/**
	 * Gets all entries stored in the local endpoint
	 *
	 * @return
	 */
	Map<K, V> getLocalEntries();

	/**
	 * The RXJava version to get all local entries
	 * @return
	 */
	default Observable<Map.Entry<K, V>> rxGetLocalEntries() {
		return Observable.fromIterable(this.getLocalEntries().entrySet());
	}

	/**
	 * Gets all keys stored in the local endpoint
	 *
	 * @return
	 */
	Set<K> getTotalKeySet();

	/**
	 * The RXJava version to get all local keyset
	 * @return
	 */
	default Observable<K> rxGetTotalKeySet() {
		return Observable.fromIterable(this.getTotalKeySet());
	}

	/**
	 * Gets all keys stored in the local endpoint
	 *
	 * @return
	 */
	Set<K> getLocalKeySet();

	/**
	 * The RXJava version to get all local keyset
	 * @return
	 */
	default Observable<K> rxGetLocalKeySet() {
		return Observable.fromIterable(this.getLocalKeySet());
	}

	/**
	 * Check if the key is belong to a value
	 *
	 * @param key the key we are looking for
	 * @param <S> The type of the key, extending the type of the key of the repository
	 * @return {@code true} if the key is associated to a value, {@code false} otherwise
	 */
	<S extends K> boolean exists(@NotNull S key);

	/**
	 * The reactiveX version of the {@link this#exists(Object)} method.
	 *
	 * @param key The key we want to remove
	 * @return Returns an {@link Single} indicating the result of the operation
	 */
	default <S extends K> Single<Boolean> rxExists(@NotNull S key) {
		return Single.fromCallable(() -> this.exists(key));
	}

	/**
	 * Check if all keys exists int the repository
	 *
	 * @param keys A set of keys we are loiking for
	 * @return
	 */
	boolean existsAll(@NotNull Set<K> keys);

	/**
	 * The reactiveX version of the {@link this#existsAll(Set)} method.
	 *
	 * @param keys The set of keys we want to remove
	 * @return Returns an {@link Single} indicating the result of the operation
	 */
	default Single<Boolean> rxExists(@NotNull Set<K> keys) {
		return Single.fromCallable(() -> this.existsAll(keys));
	}

	/**
	 * Delete a value from the map
	 *
	 * @param key The type of the key, extending the type of the key of the repository
	 * @param <S> The type of the key, extending the type of the key of the repository
	 */
	<S extends K> void delete(@NotNull S key);

	/**
	 * The reactiveX version of the {@link this#delete(Object)} method.
	 *
	 * @param key The key we want to remove
	 * @return Returns an {@link Completable} identifying the operation has
	 * completed normally, or has an error
	 */
	default <S extends K> Completable rxDelete(@NotNull S key) {
		return Completable.fromRunnable(() -> this.delete(key));
	}

	/**
	 * Delete all keys given as a parameter
	 *
	 * @param keys the set of keys we wish to delete
	 * @param <S>  The type of the key, extending the type of the key of the repository
	 */
	<S extends K> void deleteAll(@NotNull Set<S> keys);

	/**
	 * The reactiveX version of the {@link this#delete(Object)} method.
	 *
	 * @param keys The set of keys we want to remove
	 * @return Returns an {@link Completable} identifying the operation has
	 * completed normally, or has an error
	 */
	default <S extends K> Completable rxDeleteAll(@NotNull Set<S> keys) {
		return Completable.fromRunnable(() -> this.deleteAll(keys));
	}

	/**
	 * Removes the entity in the map, and returns its previous value if it was presented
	 *
	 * @param key the key we are looking for
	 * @param <S> The type of the key, extending the type of the key of the repository
	 * @return Returns with the value if the key has associated to any or an optional.empty
	 */
	<S extends K> Optional<V> remove(@NotNull S key);

	/**
	 * The reactiveX version of the {@link this#remove(Object)} method.
	 *
	 * @param key The key we want to remove
	 * @return Returns an {@link Observable} streaming the map entries of the removed items, or
	 * an {@link Observable#empty()} if it has not found any.
	 */
	default <S extends K> Maybe<V> rxRemove(@NotNull S key) {
		return Maybe.fromOptional(this.remove(key));
	}

	/**
	 * Removes all values associated with the given keys and returns with the removed values
	 *
	 * @param keys A set of keys we are looking for
	 * @return Returns the previous values of the values we removed, or an empty map
	 */
	Map<K, V> removeAll(@NotNull Set<K> keys);

	/**
	 * The reactiveX version of the {@link this#removeAll(Set)} method.
	 *
	 * @param keys A set of keys we are looking for
	 * @return Returns an {@link Observable} streaming the map entries of the removed items, or
	 * an {@link Observable#empty()} if it has not found any.
	 */
	default Observable<Map.Entry<K, V>> rxRemoveAll(@NotNull Set<K> keys) {
		return Observable.fromIterable(this.removeAll(keys).entrySet());
	}

    void purge();
}
