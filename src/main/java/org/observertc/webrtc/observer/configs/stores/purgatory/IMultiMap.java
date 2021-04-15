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

package org.observertc.webrtc.observer.configs.stores.purgatory;

import io.reactivex.Single;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Deprecated
public interface IMultiMap<K, V> {


	/**
	 * Saves the entity to the map
	 *
	 * @param key    the key the entity will be associated to
	 * @param entity the entities, which is going to be saved
	 * @param <R>    The type of the key, extending the type of the key of the map
	 * @param <U>    the type of the value extending the type of the value of the map
	 */
	<R extends K, U extends V> void add(@NotNull R key, @NotNull U entity);

	/**
	 * The ReactiveX version of the {@link this#add(Object, Object)}.
	 *
	 * @param key    the key the entity will be associated to
	 * @param entity the entities, which are going to be saved
	 * @param <R>    The type of the key, extending the type of the key of the map
	 * @param <U>    the type of the value extending the type of the value of the map
	 */
	default <R extends K, U extends V> Completable rxAdd(@NotNull R key, @NotNull U entity) {
		return Completable.fromRunnable(() -> this.add(key, entity));
	}

	/**
	 * Saves the entity to the map
	 *
	 * @param key      the key the entity will be associated to
	 * @param entities the entities, which are going to be saved
	 * @param <R>      The type of the key, extending the type of the key of the map
	 * @param <U>      the type of the value extending the type of the value of the map
	 */
	<R extends K, U extends V> void addAll(@NotNull R key, @NotNull Collection<U> entities);

	/**
	 * The ReactiveX version of the {@link this#addAll(Object, Collection)}.
	 *
	 * @param key      the key the entity will be associated to
	 * @param entities the entities, which are going to be saved
	 * @param <R>      The type of the key, extending the type of the key of the map
	 * @param <U>      the type of the value extending the type of the value of the map
	 */
	default <R extends K, U extends V> Completable rxAddAll(@NotNull R key, @NotNull Collection<U> entities) {
		return Completable.fromRunnable(() -> this.addAll(key, entities));
	}

	/**
	 * Find the entity in the repository if it is presented there
	 *
	 * @param key the key we are looking for
	 * @param <S>
	 * @return An {@link Collection} holding the values
	 */
	<S extends K> Collection<V> find(@NotNull S key);

	/**
	 * The reactiveX version of the {@link this#find(Object)} method.
	 *
	 * @param key The key we want to find
	 * @return Returns an {@link Maybe} indicating the result of the operation
	 */
	default <S extends K> Observable<V> rxFind(@NotNull S key) {
		return Observable.fromIterable(this.find(key));
	}

	/**
	 * Find all key value pair in the repositoiry
	 *
	 * @param keys A set of keys we are loiking for
	 * @return
	 */
	Map<K, Collection<V>> findAll(@NotNull Set<K> keys);

	/**
	 * Gets all entries stored in the local endpoint
	 *
	 * @return
	 */
	Map<K, Collection<V>> getLocalEntries();

	/**
	 * The reactiveX version of the {@link this#findAll(Set)} method.
	 *
	 * @param keys The keys set we want to remove
	 * @return Returns an {@link Single} indicating the result of the operation
	 */
	default Observable<Map.Entry<K, Collection<V>>> rxFindAll(@NotNull Set<K> keys) {
		return Observable.fromIterable(this.findAll(keys).entrySet());
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
	 * @param <R> The type of the key, extending the type of the key of the repository
	 * @param <U> the type of the value extending the type of the value of the repository
	 * @return Returns with the value if the key has associated to any or an optional.empty
	 */
	<R extends K, U extends V> boolean remove(@NotNull R key, @NotNull U entity);

	/**
	 * The reactiveX version of the {@link this#remove(Object, Object)} method.
	 *
	 * @param key The key we want to remove
	 * @return Returns an {@link Observable} streaming the map entries of the removed items, or
	 * an {@link Observable#empty()} if it has not found any.
	 */
	default <R extends K, U extends V> Single<Boolean> rxRemove(@NotNull R key, @NotNull U entity) {
		return Single.fromCallable(() -> this.remove(key, entity));
	}

	/**
	 * Removes all values associated with the given keys and returns with the removed values
	 *
	 * @param key The key we remoev the value for
	 * @return Returns the previous values of the values we removed, or an empty map
	 */
	<S extends K> Collection<V> removeAll(@NotNull K key);

	/**
	 * The reactiveX version of the {@link this#removeAll(Object)} method.
	 *
	 * @param key The key we remoev the value for
	 * @return Returns an {@link Observable} streaming the map entries of the removed items, or
	 * an {@link Observable#empty()} if it has not found any.
	 */
	default <S extends K> Observable<V> rxRemoveAll(@NotNull S key) {
		return Observable.fromIterable(this.removeAll(key));
	}

	void purge();
}
