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

package org.observertc.webrtc.observer.repositories.hazelcast;


import com.hazelcast.map.IMap;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.repositories.Repository;

import javax.validation.constraints.NotNull;
import java.util.*;

public class MapRepositoryAbstract<K, V> implements Repository<K, V> {
	private final ObserverHazelcast observerHazelcast;
	private final IMap<K, V> entities;

	public MapRepositoryAbstract(ObserverHazelcast observerHazelcast, String name) {
		this.observerHazelcast = observerHazelcast;
		this.entities = observerHazelcast
				.getInstance()
				.getMap(name);

	}

	protected IMap<K, V> getEntities() {
		return this.entities;
	}

	@Override
	public <R extends K, U extends V> void save(@NotNull R key, @NotNull U entity) {
		this.entities.put(key, entity);
	}

	@Override
	public <R extends K, U extends V> void saveAll(@NotNull Map<R, U> entities) {
		this.entities.putAll(entities);
	}

	@Override
	public <R extends K, U extends V> Optional<V> update(@NotNull R key, @NotNull U value) {
		V result = this.entities.put(key, value);
		if (Objects.isNull(result)) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	@Override
	public Map<K, V> updateAll(@NotNull Map<K, V> entities) {
		Map<K, V> result = this.entities.getAll(entities.keySet());
		if (Objects.isNull(result)) {
			return Collections.unmodifiableMap(new HashMap<>());
		}
		return result;
	}

	@Override
	public <S extends K> Optional<V> find(@NotNull S key) {
		V result = this.entities.get(key);
		if (Objects.isNull(result)) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	@Override
	public Map<K, V> findAll(@NotNull Set<K> keys) {
		Map<K, V> result = this.entities.getAll(keys);
		if (Objects.isNull(result)) {
			return Collections.unmodifiableMap(new HashMap<>());
		}
		return result;
	}

	@Override
	public Map<K, V> getLocalEntries() {
		Set<K> keys = this.entities.localKeySet();
		return this.findAll(keys);
	}

	@Override
	public <S extends K> boolean exists(@NotNull S key) {
		return this.entities.containsKey(key);
	}

	@Override
	public boolean existsAll(@NotNull Set<K> keys) {
		Map<K, V> result = this.entities.getAll(keys);
		if (Objects.isNull(result)) {
			return false;
		}
		if (result.size() != keys.size()) {
			return false;
		}
		return keys.stream().map(result::containsKey).allMatch(result::containsKey);
	}

	@Override
	public <S extends K> void delete(@NotNull S key) {
		this.entities.delete(key);
	}

	@Override
	public <S extends K> void deleteAll(@NotNull Set<S> keys) {
		keys.stream().forEach(this.entities::delete);
	}

	@Override
	public <S extends K> Optional<V> remove(@NotNull S key) {
		V result = this.entities.remove(key);
		if (Objects.isNull(result)) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	@Override
	public Map<K, V> removeAll(@NotNull Set<K> keys) {
		Map<K, V> result = this.entities.getAll(keys);
		keys.stream().forEach(this.entities::delete);
		return Collections.unmodifiableMap(result);
	}
}
